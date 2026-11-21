import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.regex.Pattern;

// MAIN CLASS OF THE WEB VERSION.
// Starts a small web server on http://localhost:8080 that
//   1. serves the web page (web/index.html)            -> FRONTEND
//   2. answers REST requests under /api/missions       -> BACKEND
//   3. stores everything through MissionDatabase (JDBC) -> DATABASE
//
// The original classes (SpaceMission, MarsMission, SatelliteMission, MissionRegistry,
// MissionFileManager, MissionLogger, InsufficientFuelException) are reused unchanged.
public class MissionWebApp {

    // Shared registry (Collections): same class the original GUI used.
    private static final MissionRegistry registry = new MissionRegistry();

    // Synchronization: only one request at a time touches registry + database.
    private static final Object LOCK = new Object();

    private static final Pattern VALID_ID = Pattern.compile("[A-Z0-9_-]{1,30}");

    public static void main(String[] args) throws Exception {

        MissionDatabase.initialize();
        loadMissionsFromDatabase();

        int port = Integer.parseInt(
                System.getenv().getOrDefault("PORT", "8080"));

        // Binds to 127.0.0.1 so only this computer can open the page.
        HttpServer server = HttpServer.create(
                new InetSocketAddress("127.0.0.1", port), 0);

        server.createContext("/api/missions", MissionWebApp::handleApi);
        server.createContext("/", MissionWebApp::handleStatic);
        server.setExecutor(Executors.newFixedThreadPool(4));
        server.start();

        MissionLogger.logInfo("Web server started on port " + port);
        System.out.println();
        System.out.println("=======================================================");
        System.out.println(" Space Mission Management System is running!");
        System.out.println(" Open in your browser:  http://localhost:" + port);
        System.out.println(" Database: " + MissionDatabase.describe());
        System.out.println(" Press Ctrl+C here to stop.");
        System.out.println("=======================================================");

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            server.stop(0);
            if (MissionDatabase.isDerby()) {
                try {
                    // Cleanly shuts Derby down (it always reports this as an exception).
                    java.sql.DriverManager.getConnection("jdbc:derby:;shutdown=true");
                } catch (SQLException ignored) {
                }
            }
        }));
    }

    // On start-up: rebuild the in-memory registry from the rows saved in the database.
    private static void loadMissionsFromDatabase() throws Exception {
        for (Map<String, Object> row : MissionDatabase.getAllMissions()) {
            String id = (String) row.get("id");
            double fuel = (Double) row.get("fuel");
            SpaceMission mission = createMission(
                    (String) row.get("destination"), id, fuel);
            registry.addMission(mission);

            // A launch that was interrupted by closing the program is reset.
            if ("LAUNCHING".equals(row.get("status"))) {
                MissionDatabase.updateStatus(id, statusFor(mission));
            }
        }
        System.out.println("Loaded " + registry.getMissionCount()
                + " mission(s) from the database.");
    }

    // Polymorphism: a SpaceMission reference holds either subclass.
    private static SpaceMission createMission(
            String destination, String id, double fuel) {

        if ("Mars".equals(destination)) {
            return new MarsMission(id, fuel);
        } else if ("Earth Orbit".equals(destination)) {
            return new SatelliteMission(id, fuel);
        }
        throw new IllegalArgumentException(
                "Destination must be 'Mars' or 'Earth Orbit'.");
    }

    private static String statusFor(SpaceMission mission) {
        return mission.getFuel() >= mission.calculateFuelRequired()
                ? "READY" : "INSUFFICIENT FUEL";
    }

    // ------------------------------------------------------------------
    //  REST API:   GET    /api/missions              list all missions   (SELECT)
    //              POST   /api/missions              register a mission  (INSERT)
    //              PUT    /api/missions/{id}         change fuel         (UPDATE)
    //              DELETE /api/missions/{id}         remove a mission    (DELETE)
    //              POST   /api/missions/{id}/launch  start the launch thread
    // ------------------------------------------------------------------
    private static void handleApi(HttpExchange ex) throws IOException {
        try {
            String method = ex.getRequestMethod();
            String rest = ex.getRequestURI().getPath()
                    .substring("/api/missions".length());

            String[] parts = rest.isEmpty() || rest.equals("/")
                    ? new String[0]
                    : rest.substring(1).split("/");

            synchronized (LOCK) {
                if (parts.length == 0) {
                    if (method.equals("GET")) {
                        send(ex, 200, MissionDatabase.getAllMissions());
                    } else if (method.equals("POST")) {
                        registerMission(ex);
                    } else {
                        sendError(ex, 405, "Method not allowed");
                    }
                } else if (parts.length == 1) {
                    String id = parts[0].trim().toUpperCase();
                    if (method.equals("PUT")) {
                        updateFuel(ex, id);
                    } else if (method.equals("DELETE")) {
                        deleteMission(ex, id);
                    } else {
                        sendError(ex, 405, "Method not allowed");
                    }
                } else if (parts.length == 2 && parts[1].equals("launch")
                        && method.equals("POST")) {
                    launchMission(ex, parts[0].trim().toUpperCase());
                } else {
                    sendError(ex, 404, "Not found");
                }
            }
        } catch (IllegalArgumentException e) {
            // Bad input (invalid JSON, fuel out of range, missing field...).
            sendError(ex, 400, e.getMessage());
        } catch (Exception e) {
            MissionLogger.logError("Server error: " + e);
            sendError(ex, 500, "Server error: " + e.getMessage());
        }
    }

    // INSERT: validates input, then saves the mission in memory, file, log and database.
    private static void registerMission(HttpExchange ex) throws Exception {
        Map<String, Object> body = readBody(ex);

        String id = text(body, "id").trim().toUpperCase();
        if (!VALID_ID.matcher(id).matches()) {
            throw new IllegalArgumentException(
                    "Mission ID may only contain letters, digits, - and _ (max 30).");
        }
        double fuel = number(body, "fuel");
        String destination = text(body, "destination");

        // Constructor validation: throws IllegalArgumentException if fuel is invalid.
        SpaceMission mission = createMission(destination, id, fuel);

        // Collections: the HashSet inside the registry rejects duplicate IDs.
        if (!registry.addMission(mission)) {
            sendError(ex, 409, "Mission ID " + id + " already exists.");
            return;
        }

        try {
            MissionDatabase.insertMission(mission, statusFor(mission));
        } catch (SQLException e) {
            registry.removeMission(id);   // keep memory and database consistent
            if (e.getSQLState() != null && e.getSQLState().startsWith("23")) {
                sendError(ex, 409, "Mission ID " + id + " already exists.");
                return;
            }
            throw e;
        }

        MissionLogger.logInfo("Mission " + id + " registered.");
        try {
            MissionFileManager.saveMissionReport(mission);
        } catch (IOException e) {
            MissionLogger.logWarning("Could not write report file: " + e.getMessage());
        }

        send(ex, 201, MissionDatabase.findMission(id));
    }

    // UPDATE: changes the available fuel and recalculates the status.
    private static void updateFuel(HttpExchange ex, String id) throws Exception {
        Map<String, Object> body = readBody(ex);
        double newFuel = number(body, "fuel");

        SpaceMission mission = registry.getMission(id);
        Map<String, Object> row = MissionDatabase.findMission(id);
        if (mission == null || row == null) {
            sendError(ex, 404, "Mission " + id + " not found.");
            return;
        }
        String current = (String) row.get("status");
        if ("LAUNCHED".equals(current) || "LAUNCHING".equals(current)) {
            sendError(ex, 409, "A launched mission cannot be edited.");
            return;
        }

        double oldFuel = mission.getFuel();
        mission.setFuel(newFuel);          // setter validation (0 .. MAX_FUEL)
        try {
            MissionDatabase.updateMission(id, newFuel, statusFor(mission));
        } catch (SQLException e) {
            mission.setFuel(oldFuel);      // undo the in-memory change
            throw e;
        }

        MissionLogger.logInfo("Mission " + id + " fuel updated to " + newFuel + ".");
        send(ex, 200, MissionDatabase.findMission(id));
    }

    // DELETE: removes the mission from memory and from the database.
    private static void deleteMission(HttpExchange ex, String id) throws Exception {
        boolean inDatabase = MissionDatabase.deleteMission(id);
        boolean inMemory = registry.removeMission(id);
        if (!inDatabase && !inMemory) {
            sendError(ex, 404, "Mission " + id + " not found.");
            return;
        }
        MissionLogger.logInfo("Mission " + id + " deleted.");
        Map<String, Object> ok = new LinkedHashMap<>();
        ok.put("deleted", id);
        send(ex, 200, ok);
    }

    // THREADING: runs the 3-second countdown and launch in a background thread,
    // so the web server keeps answering requests while the mission "flies".
    private static void launchMission(HttpExchange ex, String id) throws Exception {
        SpaceMission mission = registry.getMission(id);
        Map<String, Object> row = MissionDatabase.findMission(id);
        if (mission == null || row == null) {
            sendError(ex, 404, "Mission " + id + " not found.");
            return;
        }
        String current = (String) row.get("status");
        if ("LAUNCHED".equals(current) || "LAUNCHING".equals(current)) {
            sendError(ex, 409, "Mission " + id + " was already launched.");
            return;
        }

        MissionDatabase.updateStatus(id, "LAUNCHING");

        Thread launchThread = new Thread(() -> {
            try {
                MissionLogger.logInfo("Mission " + id + " launch started.");
                for (int i = 3; i >= 1; i--) {
                    System.out.println("Mission " + id + ": launch in " + i + "...");
                    Thread.sleep(1000);
                }

                String finalStatus;
                try {
                    mission.launch();                 // may throw InsufficientFuelException
                    finalStatus = "LAUNCHED";
                    MissionLogger.logInfo("Mission " + id + " launched successfully.");
                } catch (InsufficientFuelException e) {
                    finalStatus = "INSUFFICIENT FUEL";
                    MissionLogger.logWarning(e.getMessage());
                }

                synchronized (LOCK) {
                    if (registry.getMission(id) != null) {   // not deleted meanwhile
                        MissionDatabase.updateStatus(id, finalStatus);
                    }
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } catch (Exception e) {
                MissionLogger.logError("Launch thread failed: " + e);
            }
        }, "launch-" + id);
        launchThread.start();

        Map<String, Object> reply = new LinkedHashMap<>();
        reply.put("message", "Launch sequence started for " + id);
        send(ex, 202, reply);
    }

    // ------------------------------------------------------------------
    //  Serves the single web page.
    // ------------------------------------------------------------------
    private static void handleStatic(HttpExchange ex) throws IOException {
        String path = ex.getRequestURI().getPath();
        if (!path.equals("/") && !path.equals("/index.html")) {
            sendError(ex, 404, "Not found");
            return;
        }
        Path page = Path.of("web", "index.html");
        if (!Files.exists(page)) {
            sendError(ex, 500, "web/index.html is missing. Start the program from the project folder.");
            return;
        }
        byte[] bytes = Files.readAllBytes(page);
        ex.getResponseHeaders().set("Content-Type", "text/html; charset=utf-8");
        ex.sendResponseHeaders(200, bytes.length);
        ex.getResponseBody().write(bytes);
        ex.close();
    }

    // ------------------------------------------------------------------
    //  Small helpers.
    // ------------------------------------------------------------------
    private static Map<String, Object> readBody(HttpExchange ex) throws IOException {
        String text = new String(ex.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
        try {
            return Json.parseObject(text);
        } catch (RuntimeException e) {
            throw new IllegalArgumentException("Invalid request data.");
        }
    }

    private static String text(Map<String, Object> body, String key) {
        Object value = body.get(key);
        if (value == null || value.toString().trim().isEmpty()) {
            throw new IllegalArgumentException("Please enter " + key + ".");
        }
        return value.toString();
    }

    private static double number(Map<String, Object> body, String key) {
        Object value = body.get(key);
        if (value == null || value.toString().trim().isEmpty()) {
            throw new IllegalArgumentException("Please enter valid " + key + ".");
        }
        try {
            double d = value instanceof Number n
                    ? n.doubleValue()
                    : Double.parseDouble(value.toString().trim());
            if (Double.isNaN(d) || Double.isInfinite(d)) {
                throw new NumberFormatException();
            }
            return d;
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Please enter valid " + key + ".");
        }
    }

    private static void sendError(HttpExchange ex, int status, String message)
            throws IOException {
        Map<String, Object> error = new LinkedHashMap<>();
        error.put("error", message);
        send(ex, status, error);
    }

    private static void send(HttpExchange ex, int status, Object body) throws IOException {
        byte[] bytes = Json.stringify(body).getBytes(StandardCharsets.UTF_8);
        ex.getResponseHeaders().set("Content-Type", "application/json; charset=utf-8");
        ex.getResponseHeaders().set("Cache-Control", "no-store");
        ex.sendResponseHeaders(status, bytes.length);
        ex.getResponseBody().write(bytes);
        ex.close();
    }
}
