import java.util.*;
// Collections: Maintains the registered space mission records.
public class MissionRegistry {
    // ArrayList: Stores all mission objects.
    private ArrayList<SpaceMission> missions = new ArrayList<>();
    // HashSet: Stores unique mission IDs.
    private HashSet<String> missionIds = new HashSet<>();
    // HashMap: Maps mission IDs to their destinations.
    private HashMap<String, String> missionMap = new HashMap<>();
    // Adds a mission to the collection.
    public boolean addMission(SpaceMission mission) {
        String id = mission.getMissionId().trim().toUpperCase();
        // HashSet: Prevents duplicate mission IDs.
        if (missionIds.contains(id)) {
            System.out.println("Duplicate mission ID!");
            return false;
        }
        missions.add(mission);
        missionIds.add(id);
        missionMap.put(id, mission.getDestination());
        System.out.println("Mission added to registry!");
        return true;
    }
    // Iterator: Displays all registered missions.
    public void displayMissions() {
        Iterator<SpaceMission> itr = missions.iterator();
        System.out.println("\n=== MISSION REGISTRY ===");
        while (itr.hasNext()) {
            SpaceMission mission = itr.next();
            System.out.println("Mission ID: "
                    + mission.getMissionId());
            System.out.println("Destination: "
                    + mission.getDestination());
            System.out.println("Fuel: "
                    + mission.getFuel());
            System.out.println("-----------------------");
        }
    }
    // HashMap: Searches for a mission using its ID.
    public void searchMission(String id) {
        id = id.trim().toUpperCase();
        if (missionMap.containsKey(id)) {
            System.out.println("Mission found!");
            System.out.println("Mission ID: " + id);
            System.out.println("Destination: "
                    + missionMap.get(id));
        } else {
            System.out.println("Mission not found!");
        }
    }
    // Returns the total number of registered missions.
    public int getMissionCount() {
        return missions.size();
    }
    // Finds the mission object for an ID (null if it is not registered).
    public SpaceMission getMission(String id) {
        id = id.trim().toUpperCase();
        for (SpaceMission mission : missions) {
            if (mission.getMissionId().trim().toUpperCase().equals(id)) {
                return mission;
            }
        }
        return null;
    }
    // Removes a mission from all three collections (used by the DELETE feature).
    public boolean removeMission(String id) {
        id = id.trim().toUpperCase();
        final String key = id;
        boolean removed = missions.removeIf(
                m -> m.getMissionId().trim().toUpperCase().equals(key));
        missionIds.remove(id);
        missionMap.remove(id);
        return removed;
    }
}
