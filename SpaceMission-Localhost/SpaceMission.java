
 // Abstraction: Abstract class represents a general space mission.
abstract class SpaceMission implements Launchable {

    // Encapsulation: Private variables protect mission data.
    private String missionId;
    private String destination;
    private double fuel;

    // Static variable: Counts the number of missions created.
    static int missionCount = 0;

    // Final variable: Defines the maximum permitted fuel.
    final double MAX_FUEL = 10000;

    // Constructor: Initializes mission details and validates fuel.
    SpaceMission(String missionId, String destination, double fuel) {
        this.missionId = missionId;
        this.destination = destination;
        setFuel(fuel);
        missionCount++;
    }

    // Constructor overloading: Provides a default fuel value.
    SpaceMission(String missionId, String destination) {
        this(missionId, destination, 5000);
    }

    // Getters: Provide controlled access to private variables.
    public String getMissionId() {
        return missionId;
    }

    public String getDestination() {
        return destination;
    }

    public double getFuel() {
        return fuel;
    }

    // Setter: Validates fuel before updating the private variable.
    public void setFuel(double fuel) {
        if (fuel < 0 || fuel > MAX_FUEL) {
            throw new IllegalArgumentException(
                "Fuel must be between 0 and " + MAX_FUEL);
        }
        this.fuel = fuel;
    }

    // Abstraction: Forces subclasses to calculate their fuel requirements.
    abstract double calculateFuelRequired();

    // Method overloading: Displays mission details with an optional fuel value.
    void display(boolean showFuel) {
        System.out.println("Mission ID: " + missionId);
        System.out.println("Destination: " + destination);

        if (showFuel) {
            System.out.println("Available fuel: " + fuel);
        }
    }

    // Method overloading: Displays mission details with fuel by default.
    void display() {
        display(true);
    }

    // Exception handling: Checks whether sufficient fuel is available.
    protected void checkFuel() throws InsufficientFuelException {
        if (fuel < calculateFuelRequired()) {
            throw new InsufficientFuelException(
                "Insufficient fuel for " + missionId);
        }
    }
}
