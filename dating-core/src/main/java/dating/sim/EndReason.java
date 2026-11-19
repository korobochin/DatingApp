package dating.sim;

/** Why a simulation ended. */
public enum EndReason {
    /** The maximal number of attempted actions was reached. */
    STEP_LIMIT,
    /** Nobody wanted or was able to act for several consecutive rounds. */
    NO_MOVES,
    /** The target number of new friendships was reached. */
    TARGET_REACHED
}
