package dating.matching;

import java.util.List;

/**
 * A "people you may know" suggestion.
 *
 * @param userId        suggested user
 * @param commonFriends number of common friends
 * @param mutualFriends ids of those common friends, ascending
 */
public record Recommendation(int userId, int commonFriends, List<Integer> mutualFriends) {
}
