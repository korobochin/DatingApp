package dating.matching;

import dating.model.NetworkView;
import dating.model.Profile;

import java.util.Comparator;
import java.util.List;
import java.util.function.Predicate;

/**
 * Finds and ranks candidates for a user. The scoring strategy and the ordering
 * are injected, so the same class serves dating search, friend search and bots.
 */
public final class Matcher {
    private final NetworkView network;
    private final MatchScorer scorer;
    private final Comparator<MatchResult> order;

    /**
     * @param network state to search in
     * @param scorer  scoring strategy
     * @param order   ordering of results
     */
    public Matcher(NetworkView network, MatchScorer scorer, Comparator<MatchResult> order) {
        this.network = network;
        this.scorer = scorer;
        this.order = order;
    }

    /**
     * Matcher with {@link MatchScorers#standard()} and {@link MatchComparators#DEFAULT}.
     *
     * @param network state to search in
     */
    public Matcher(NetworkView network) {
        this(network, MatchScorers.standard(), MatchComparators.DEFAULT);
    }

    /**
     * Ranks all other profiles that pass the filter.
     *
     * @param userId searching user
     * @param filter additional condition on candidates (the user is always excluded)
     * @param limit  maximal number of results, non-negative
     * @return best candidates first
     * @throws dating.exception.IllegalActionException if the user is unknown
     */
    public List<MatchResult> rank(int userId, Predicate<Profile> filter, int limit) {
        Profile me = network.profile(userId);
        return network.profiles().stream()
                .filter(p -> p.id() != userId)
                .filter(filter)
                .map(p -> evaluate(me, p))
                .sorted(order)
                .limit(limit)
                .toList();
    }

    /**
     * Scores a single pair.
     *
     * @param me        the searching profile
     * @param candidate the candidate
     * @return result with score and shared counts
     */
    public MatchResult evaluate(Profile me, Profile candidate) {
        int commonInterests = MatchScorers.commonCount(me, candidate);
        int commonFriends = network.friends().contains(me.id()) && network.friends().contains(candidate.id())
                ? network.friends().commonNeighbors(me.id(), candidate.id()).size() : 0;
        return new MatchResult(candidate, scorer.score(me, candidate), commonInterests, commonFriends);
    }

    /**
     * @param userId a user
     * @return predicate that rejects people who are already friends with the user
     */
    public Predicate<Profile> notFriendsWith(int userId) {
        return p -> !network.areFriends(userId, p.id());
    }
}
