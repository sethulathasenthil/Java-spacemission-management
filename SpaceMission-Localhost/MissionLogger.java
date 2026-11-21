import java.util.logging.*;
import java.io.IOException;
// Logging: Records mission activities and errors.
public class MissionLogger {
    // Logger: Creates a logger for the mission management system.
    private static final Logger logger =
            Logger.getLogger("SpaceMissionLogger");
    // Static block: Configures logging when the class is loaded.
    static {
        logger.setUseParentHandlers(false);
        try {
            // FileHandler: Stores log messages in a file.
            FileHandler handler =
                    new FileHandler("mission.log", true);
            // SimpleFormatter: Formats log messages.
            handler.setFormatter(new SimpleFormatter());
            logger.addHandler(handler);
        } catch (IOException e) {
            System.out.println("Unable to create log file.");
        }
    }
    // INFO: Records normal mission activities.
    public static void logInfo(String message) {
        logger.info(message);
    }
    // WARNING: Records mission warnings.
    public static void logWarning(String message) {
        logger.warning(message);
    }
    // SEVERE: Records serious errors.
    public static void logError(String message) {
        logger.severe(message);
    }
}
