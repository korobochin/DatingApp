package dating.matching;

import dating.model.Profile;

import java.util.Arrays;

/** Factory of {@link MatchScorer} strategies. */
public final class MatchScorers {
    private MatchScorers() {
    }

    /**
     * A scorer together with its weight, used by {@link #weighted(Weighted...)}.
     *
     * @param scorer the scorer
     * @param weight non-negative weight
     */
    public record Weighted(MatchScorer scorer, double weight) {
    }

    /**
     * Number of shared interests.
     *
     * @return scorer returning {@code |A ∩ B|}
     */
    public static MatchScorer commonInterests() {
        return (me, other) -> commonCount(me, other);
    }

    /**
     * Jaccard similarity of interest sets: {@code |A ∩ B| / |A ∪ B|}, {@code 0} if both are empty.
     *
     * @return scorer in {@code [0, 1]}
     */
    public static MatchScorer jaccard() {
        return (me, other) -> {
            int common = commonCount(me, other);
            int union = me.interests().size() + other.interests().size() - common;
            return union == 0 ? 0.0 : (double) common / union;
        };
    }

    /**
     * The smaller the age gap, the higher the score: {@code 1 / (1 + gap)}.
     *
     * @return scorer in {@code (0, 1]}
     */
    public static MatchScorer ageProximity() {
        return (me, other) -> 1.0 / (1 + Math.abs(me.age() - other.age()));
    }

    /**
     * Weighted sum of several scorers.
     *
     * @param parts scorers with weights
     * @return combined scorer
     */
    public static MatchScorer weighted(Weighted... parts) {
        return (me, other) -> Arrays.stream(parts)
                .mapToDouble(w -> w.weight() * w.scorer().score(me, other))
                .sum();
    }

    /**
     * The default strategy: Jaccard similarity of interests plus a small age bonus.
     *
     * @return scorer
     */
    public static MatchScorer standard() {
        return weighted(new Weighted(jaccard(), 1.0), new Weighted(ageProximity(), 0.1));
    }

    static int commonCount(Profile a, Profile b) {
        return (int) a.interests().stream().filter(b.interests()::contains).count();
    }
}
