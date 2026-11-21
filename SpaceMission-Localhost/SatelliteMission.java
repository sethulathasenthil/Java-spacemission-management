
 // Inheritance: SatelliteMission inherits properties and methods from SpaceMission.
class SatelliteMission extends SpaceMission {

    // Constructor: Initializes a mission with Earth Orbit as its destination.
    SatelliteMission(String id, double fuel) {
        super(id, "Earth Orbit", fuel);
    }

    // Method overriding: Specifies the fuel required for a satellite mission.
    @Override
    double calculateFuelRequired() {
        return 2000;
    }

    // Method overriding: Defines the launch procedure for a satellite mission.
    @Override
    public void launch() throws InsufficientFuelException {
        checkFuel();
        System.out.println("Satellite launched successfully!");
    }
}
