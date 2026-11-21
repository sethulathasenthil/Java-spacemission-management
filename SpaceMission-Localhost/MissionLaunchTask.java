 // Threading: Runnable defines a separate mission launch task.
public class MissionLaunchTask implements Runnable {
    private SpaceMission mission;
    // Constructor: Receives the mission object.
    MissionLaunchTask(SpaceMission mission) {
        this.mission = mission;
    }
    // Thread execution: Performs the mission launch operation.
    @Override
    public void run() {
        System.out.println("\nPreparing mission launch...");
        // Threading: Simulates a three-second countdown.
        for (int i = 3; i >= 1; i--) {
            System.out.println("Launch in " + i + "...");
            try {
                // Pauses the current thread for one second.
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                System.out.println("Launch countdown interrupted!");
                Thread.currentThread().interrupt();
                return;
            }
        }
        // Exception handling: Checks fuel before launching.
        try {
            mission.launch();
        } catch (InsufficientFuelException e) {
            System.out.println("Launch Error: " + e.getMessage());
        }
    }
}
