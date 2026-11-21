
 // Inheritance: MarsMission inherits properties and methods from SpaceMission.
class MarsMission extends SpaceMission {

    // Constructor: Initializes a mission with Mars as its destination.
    MarsMission(String id, double fuel) {
        super(id, "Mars", fuel);
    }

    // Method overriding: Specifies the fuel required for a Mars mission.
    @Override
    double calculateFuelRequired() {
        return 6000;
    }

    // Method overriding: Defines the launch procedure for a Mars mission.
    @Override
    public void launch() throws InsufficientFuelException {
        checkFuel();
        System.out.println("Mars mission launched successfully!");
    }
}
