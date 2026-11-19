package dating.sim;

/**
 * Parameters of a simulation run.
 *
 * @param maxAttempts       maximal number of attempted actions (accepted or rejected)
 * @param targetFriendships stop when this many new friendships appeared; {@code 0} disables the goal
 * @param maxIdleRounds     stop after this many consecutive rounds without an accepted action
 */
public record SimulationConfig(int maxAttempts, int targetFriendships, int maxIdleRounds) {

    /** Validates the parameters. */
    public SimulationConfig {
        if (maxAttempts < 1) {
            throw new IllegalArgumentException("maxAttempts must be positive");
        }
        if (targetFriendships < 0) {
            throw new IllegalArgumentException("targetFriendships must not be negative");
        }
        if (maxIdleRounds < 1) {
            throw new IllegalArgumentException("maxIdleRounds must be positive");
        }
    }

    /**
     * @param maxAttempts attempt limit
     * @return config without a friendship target and with 3 idle rounds allowed
     */
    public static SimulationConfig limitedTo(int maxAttempts) {
        return new SimulationConfig(maxAttempts, 0, 3);
    }
}
