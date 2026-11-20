package dating.sim;

/**
 * Thrown when a command breaks the rules of the platform
 * (liking one's own photo, duplicate request, unknown user, ...).
 * The state of the network is never changed by a rejected command.
 */
public class IllegalActionException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    /**
     * @param message human-readable reason
     */
    public IllegalActionException(String message) {
        super(message);
    }
}
