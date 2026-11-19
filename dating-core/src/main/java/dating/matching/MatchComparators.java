package dating.matching;

import java.util.Arrays;
import java.util.Comparator;

/** Comparators for {@link MatchResult}; they are built by composition and always end with the id. */
public final class MatchComparators {
    private MatchComparators() {
    }

    /** Higher score first. */
    public static final Comparator<MatchResult> BY_SCORE =
            Comparator.comparingDouble(MatchResult::score).reversed();

    /** More shared interests first. */
    public static final Comparator<MatchResult> BY_COMMON_INTERESTS =
            Comparator.comparingInt(MatchResult::commonInterests).reversed();

    /** More shared friends first. */
    public static final Comparator<MatchResult> BY_COMMON_FRIENDS =
            Comparator.comparingInt(MatchResult::commonFriends).reversed();

    /** Smaller id first: the last tie-breaker that makes the order total and deterministic. */
    public static final Comparator<MatchResult> BY_ID =
            Comparator.comparingInt(r -> r.profile().id());

    /**
     * Default order: score, then shared friends, then shared interests, then id.
     */
    public static final Comparator<MatchResult> DEFAULT = chain(BY_SCORE, BY_COMMON_FRIENDS, BY_COMMON_INTERESTS, BY_ID);

    /**
     * Composes comparators: the next one is used only when the previous ones tie.
     *
     * @param comparators at least one comparator
     * @return the composition
     */
    @SafeVarargs
    public static Comparator<MatchResult> chain(Comparator<MatchResult>... comparators) {
        return Arrays.stream(comparators)
                .reduce((a, b) -> a.thenComparing(b))
                .orElseThrow(() -> new IllegalArgumentException("No comparators"));
    }
}
