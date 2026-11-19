package dating.matching;

import dating.model.Profile;

/**
 * Strategy that estimates how well two profiles fit each other.
 * A higher score means a better match.
 */
@FunctionalInterface
public interface MatchScorer {

    /**
     * @param me        the searching profile
     * @param candidate the candidate
     * @return a non-negative score
     */
    double score(Profile me, Profile candidate);
}
