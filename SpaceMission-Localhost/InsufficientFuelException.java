
 // User-defined exception: Handles insufficient fuel during launch.
class InsufficientFuelException extends Exception {

    // Constructor: Passes the error message to the parent exception class.
    InsufficientFuelException(String message) {
        super(message);
    }
}
