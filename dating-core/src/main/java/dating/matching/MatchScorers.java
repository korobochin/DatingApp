package dating.matching;

import dating.model.Profile;

/** Factory of {@link MatchScorer} strategies. */
public final class MatchScorers {
    private MatchScorers() {
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
     * The default strategy.
     *
     * @return {@link #jaccard()}
     */
    public static MatchScorer standard() {
        return jaccard();
    }

    static int commonCount(Profile a, Profile b) {
        return (int) a.interests().stream().filter(b.interests()::contains).count();
    }
}
