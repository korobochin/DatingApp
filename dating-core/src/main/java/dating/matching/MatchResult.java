package dating.matching;

import dating.model.Profile;

/**
 * One ranked candidate.
 *
 * @param profile         the candidate
 * @param score           score assigned by the {@link MatchScorer}
 * @param commonInterests number of shared interests
 * @param commonFriends   number of shared friends
 */
public record MatchResult(Profile profile, double score, int commonInterests, int commonFriends) {
}
