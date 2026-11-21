import java.io.*;
// File Handling: Manages saving and reading mission reports.
public class MissionFileManager {
    // File writing: Saves mission details into a text file.
    public static void saveMissionReport(SpaceMission mission)
            throws IOException {
        // String handling: Removes spaces and converts ID to uppercase.
        String id = mission.getMissionId().trim().toUpperCase();
        // String handling: Converts destination to uppercase.
        String destination =
                mission.getDestination().trim().toUpperCase();
        // String handling: Checks the length of mission ID.
        System.out.println("Mission ID length: " + id.length());
        // StringBuilder: Creates the mission report efficiently.
        StringBuilder report = new StringBuilder();
        report.append("\n========== MISSION REPORT ==========\n");
        report.append("Mission ID: ").append(id).append("\n");
        report.append("Destination: ").append(destination).append("\n");
        report.append("Available Fuel: ")
              .append(mission.getFuel()).append("\n");
        report.append("Required Fuel: ")
              .append(mission.calculateFuelRequired()).append("\n");
        // String handling: Determines the mission launch status.
        if (mission.getFuel() >= mission.calculateFuelRequired()) {
            report.append("Status: LAUNCH SUCCESSFUL\n");
        } else {
            report.append("Status: INSUFFICIENT FUEL\n");
        }
        report.append("====================================\n");
        // File handling: Opens the file in append mode.
        try (BufferedWriter writer = new BufferedWriter(
                new FileWriter("mission_reports.txt", true))) {
            writer.write(report.toString());
            System.out.println("Mission report saved successfully!");
        }
    }
    // File reading: Reads and displays saved mission reports.
    public static void readMissionReports(String searchId)
            throws IOException {
        // String handling: Normalizes the search ID.
        searchId = searchId.trim().toUpperCase();
        // StringBuilder: Stores the matching report content.
        StringBuilder result = new StringBuilder();
        // File handling: Reads the existing report file.
        try (BufferedReader reader = new BufferedReader(
                new FileReader("mission_reports.txt"))) {
            String line;
            while ((line = reader.readLine()) != null) {
                // String handling: Searches for the given mission ID.
                if (line.toUpperCase().contains(searchId)) {
                    result.append(line).append("\n");
                }
            }
        }
        // Displays the matching report.
        if (result.length() > 0) {
            System.out.println("\nMatching Mission Reports:");
            System.out.println(result);
        } else {
            System.out.println("No matching mission report found.");
        }
    }
}
