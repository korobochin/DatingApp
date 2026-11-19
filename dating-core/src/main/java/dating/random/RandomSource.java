package dating.random;

import java.util.List;
import java.util.Random;

/**
 * The single source of randomness for a whole simulation.
 * Two instances created with the same seed produce identical sequences,
 * which makes games reproducible and testable.
 */
public final class RandomSource {
    private final long seed;
    private final Random random;

    /**
     * Creates a generator.
     *
     * @param seed initial seed
     */
    public RandomSource(long seed) {
        this.seed = seed;
        this.random = new Random(seed);
    }

    /** @return the seed this generator was created with */
    public long seed() {
        return seed;
    }

    /**
     * @param bound exclusive upper bound, must be positive
     * @return an int in {@code [0, bound)}
     */
    public int nextInt(int bound) {
        return random.nextInt(bound);
    }

    /**
     * @param from        lower bound, inclusive
     * @param toInclusive upper bound, inclusive
     * @return an int in {@code [from, toInclusive]}
     */
    public int nextInt(int from, int toInclusive) {
        if (toInclusive < from) {
            throw new IllegalArgumentException("toInclusive < from");
        }
        return from + random.nextInt(toInclusive - from + 1);
    }

    /** @return a double in {@code [0, 1)} */
    public double nextDouble() {
        return random.nextDouble();
    }

    /**
     * @param probability probability of {@code true}, in {@code [0, 1]}
     * @return {@code true} with the given probability
     */
    public boolean chance(double probability) {
        return random.nextDouble() < probability;
    }

    /**
     * Picks a uniformly random element.
     *
     * @param items non-empty list
     * @param <T>   element type
     * @return the chosen element
     * @throws IllegalArgumentException if the list is empty
     */
    public <T> T pick(List<T> items) {
        if (items.isEmpty()) {
            throw new IllegalArgumentException("Cannot pick from an empty list");
        }
        return items.get(random.nextInt(items.size()));
    }
}
