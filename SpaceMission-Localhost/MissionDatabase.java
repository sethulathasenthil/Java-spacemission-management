
import java.sql.*;
import java.util.*;

// JDBC: Manages database connectivity and mission records (all SQL lives here).
//
// DEFAULT DATABASE: Apache Derby (embedded). It is a real SQL database that runs
// inside the Java program itself, so nothing has to be installed. Its files are
// stored in the "spacemissiondb" folder next to the project.
//
// MYSQL STILL WORKS: set these environment variables before starting and the same
// code talks to MySQL instead (the MySQL driver jar must then be added to lib/):
//   DB_URL=jdbc:mysql://localhost:3306/space_mission_db  DB_USER=root  DB_PASSWORD=...
public class MissionDatabase {

    // Database connection details (can be overridden with environment variables).
    private static final String URL =
            env("DB_URL", "jdbc:derby:spacemissiondb;create=true");

    private static final String USER = env("DB_USER", "root");

    // Change this (or set DB_PASSWORD) if your MySQL account has a password.
    private static final String PASSWORD = env("DB_PASSWORD", "");

    private static String env(String name, String fallback) {
        String value = System.getenv(name);
        return (value == null || value.isEmpty()) ? fallback : value;
    }

    public static boolean isDerby() {
        return URL.startsWith("jdbc:derby");
    }

    public static String describe() {
        return isDerby() ? "Apache Derby (embedded, folder 'spacemissiondb')" : URL;
    }

    // JDBC: Establishes the connection.
    public static Connection getConnection()
            throws SQLException, ClassNotFoundException {

        if (isDerby()) {
            // Loads the embedded Derby JDBC driver.
            Class.forName("org.apache.derby.jdbc.EmbeddedDriver");
            return DriverManager.getConnection(URL);
        }

        // Loads the MySQL JDBC driver.
        Class.forName("com.mysql.cj.jdbc.Driver");
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }

    // SQL DDL: Creates the missions table the first time the program runs.
    public static void initialize()
            throws SQLException, ClassNotFoundException {

        String sql =
            "CREATE TABLE missions (" +
            "mission_id VARCHAR(50) PRIMARY KEY, " +
            "destination VARCHAR(50) NOT NULL, " +
            "fuel DOUBLE NOT NULL, " +
            "required_fuel DOUBLE NOT NULL, " +
            "status VARCHAR(30) NOT NULL, " +
            "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP)";

        try (Connection con = getConnection();
             Statement st = con.createStatement()) {

            st.executeUpdate(sql);
            System.out.println("Table 'missions' created.");

        } catch (SQLException e) {
            // X0Y32 = Derby "table already exists", 42S01 = MySQL "table exists".
            if ("X0Y32".equals(e.getSQLState())
                    || "42S01".equals(e.getSQLState())) {
                System.out.println("Table 'missions' already exists.");
            } else {
                throw e;
            }
        }
    }

    // SQL INSERT: Inserts mission details into the database.
    public static void insertMission(
            SpaceMission mission, String status)
            throws SQLException, ClassNotFoundException {

        String sql =
            "INSERT INTO missions " +
            "(mission_id, destination, fuel, required_fuel, status) " +
            "VALUES (?, ?, ?, ?, ?)";

        // PreparedStatement: Executes a parameterized SQL query.
        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, mission.getMissionId());
            ps.setString(2, mission.getDestination());
            ps.setDouble(3, mission.getFuel());

            ps.setDouble(4,
                    mission.calculateFuelRequired());

            ps.setString(5, status);

            ps.executeUpdate();

            System.out.println(
                    "Mission inserted into database successfully!");
        }
    }

    // SQL UPDATE: Changes the fuel and status of one mission.
    public static boolean updateMission(
            String missionId, double fuel, String status)
            throws SQLException, ClassNotFoundException {

        String sql =
            "UPDATE missions SET fuel = ?, status = ? WHERE mission_id = ?";

        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setDouble(1, fuel);
            ps.setString(2, status);
            ps.setString(3, missionId);

            return ps.executeUpdate() > 0;
        }
    }

    // SQL UPDATE: Changes only the status (used by the launch thread).
    public static boolean updateStatus(String missionId, String status)
            throws SQLException, ClassNotFoundException {

        String sql = "UPDATE missions SET status = ? WHERE mission_id = ?";

        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, status);
            ps.setString(2, missionId);

            return ps.executeUpdate() > 0;
        }
    }

    // SQL DELETE: Removes one mission.
    public static boolean deleteMission(String missionId)
            throws SQLException, ClassNotFoundException {

        String sql = "DELETE FROM missions WHERE mission_id = ?";

        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, missionId);

            return ps.executeUpdate() > 0;
        }
    }

    // SQL SELECT: Returns every stored mission (used by the web page).
    public static List<Map<String, Object>> getAllMissions()
            throws SQLException, ClassNotFoundException {

        String sql =
            "SELECT mission_id, destination, fuel, required_fuel, " +
            "status, created_at FROM missions " +
            "ORDER BY created_at DESC, mission_id";

        List<Map<String, Object>> list = new ArrayList<>();

        try (Connection con = getConnection();
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery(sql)) {

            while (rs.next()) {
                list.add(toMap(rs));
            }
        }
        return list;
    }

    // SQL SELECT ... WHERE: Finds one mission, or returns null.
    public static Map<String, Object> findMission(String missionId)
            throws SQLException, ClassNotFoundException {

        String sql =
            "SELECT mission_id, destination, fuel, required_fuel, " +
            "status, created_at FROM missions WHERE mission_id = ?";

        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, missionId);

            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? toMap(rs) : null;
            }
        }
    }

    // Converts the current ResultSet row into a Map (becomes JSON for the browser).
    private static Map<String, Object> toMap(ResultSet rs)
            throws SQLException {

        Map<String, Object> row = new LinkedHashMap<>();
        row.put("id", rs.getString("mission_id"));
        row.put("destination", rs.getString("destination"));
        row.put("fuel", rs.getDouble("fuel"));
        row.put("requiredFuel", rs.getDouble("required_fuel"));
        row.put("status", rs.getString("status"));
        Timestamp created = rs.getTimestamp("created_at");
        row.put("createdAt", created == null ? "" : created.toString());
        return row;
    }

    // JDBC: Retrieves and displays all stored mission records (console version).
    public static void displayMissions()
            throws SQLException, ClassNotFoundException {

        String sql = "SELECT * FROM missions";

        try (Connection con = getConnection();
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery(sql)) {

            System.out.println("\n=== DATABASE MISSION RECORDS ===");

            while (rs.next()) {

                System.out.println(
                        "Mission ID: " +
                        rs.getString("mission_id"));

                System.out.println(
                        "Destination: " +
                        rs.getString("destination"));

                System.out.println(
                        "Fuel: " +
                        rs.getDouble("fuel"));

                System.out.println(
                        "Required Fuel: " +
                        rs.getDouble("required_fuel"));

                System.out.println(
                        "Status: " +
                        rs.getString("status"));

                System.out.println("----------------------");
            }
        }
    }
}
