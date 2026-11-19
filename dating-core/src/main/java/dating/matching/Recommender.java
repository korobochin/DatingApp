package dating.matching;

import dating.model.NetworkView;

import java.util.Comparator;
import java.util.List;
import java.util.Set;

/**
 * "People you may know": candidates at distance two in the friendship graph,
 * ranked by the number of common friends.
 */
public final class Recommender {
    private static final Comparator<Recommendation> ORDER =
            Comparator.comparingInt(Recommendation::commonFriends).reversed()
                    .thenComparingInt(Recommendation::userId);

    private final NetworkView network;

    /**
     * @param network state to read
     */
    public Recommender(NetworkView network) {
        this.network = network;
    }

    /**
     * @param userId user to recommend for
     * @param limit  maximal number of suggestions
     * @return friends of friends that are not yet friends, most common friends first, ties by id
     * @throws dating.exception.IllegalActionException if the user is unknown
     */
    public List<Recommendation> mightKnow(int userId, int limit) {
        network.profile(userId);
        return network.friends().friendsOfFriends(userId).entrySet().stream()
                .map(e -> new Recommendation(e.getKey(), e.getValue().size(), List.copyOf(e.getValue())))
                .sorted(ORDER)
                .limit(limit)
                .toList();
    }

    /**
     * @param a first user
     * @param b second user
     * @return ids of their common friends, ascending
     * @throws dating.exception.IllegalActionException if a user is unknown
     */
    public Set<Integer> commonFriends(int a, int b) {
        network.profile(a);
        network.profile(b);
        return network.friends().commonNeighbors(a, b);
    }
}
