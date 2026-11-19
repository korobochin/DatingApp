package dating.model;

import dating.graph.GraphView;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * Read-only access to the state of the platform: profiles, friendship graph,
 * photos with likes, pending requests and mutual-like matches.
 * Matching, recommendations, bots and statistics depend only on this interface.
 */
public interface NetworkView {

    /** @return all profiles ordered by id */
    Collection<Profile> profiles();

    /**
     * @param id user id
     * @return the profile
     * @throws dating.exception.IllegalActionException if the user does not exist
     */
    Profile profile(int id);

    /**
     * @param id user id
     * @return {@code true} if such a user exists
     */
    boolean hasProfile(int id);

    /** @return read-only friendship graph keyed by user id */
    GraphView<Integer> friends();

    /**
     * @param photoId photo id
     * @return the photo, if it exists
     */
    Optional<Photo> photo(int photoId);

    /**
     * @param userId owner id
     * @return photos of the user ordered by id
     */
    List<Photo> photosOf(int userId);

    /**
     * @param photoId photo id
     * @return ids of users who liked the photo, ascending
     */
    Set<Integer> likers(int photoId);

    /** @return pending friend requests ordered by sender, then recipient */
    Set<FriendRequest> requests();

    /** @return pairs of users who liked each other's photos, ordered */
    Set<Pair> matches();

    /** @return requests that were declined; a declined request cannot be sent again */
    Set<FriendRequest> declinedRequests();

    /**
     * @param from sender
     * @param to   recipient
     * @return {@code true} if the recipient has already declined a request from the sender
     */
    default boolean wasDeclined(int from, int to) {
        return declinedRequests().contains(new FriendRequest(from, to));
    }

    /**
     * @param from sender
     * @param to   recipient
     * @return {@code true} if a new request {@code from -> to} would be legal
     */
    default boolean canRequest(int from, int to) {
        return from != to && !areFriends(from, to) && !hasPendingBetween(from, to) && !wasDeclined(from, to);
    }

    /**
     * @param a first user
     * @param b second user
     * @return {@code true} if they are friends
     */
    default boolean areFriends(int a, int b) {
        return friends().hasEdge(a, b);
    }

    /**
     * @param userId user id
     * @param photoId photo id
     * @return {@code true} if the user liked the photo
     */
    default boolean hasLiked(int userId, int photoId) {
        return likers(photoId).contains(userId);
    }

    /**
     * @param liker possible liker
     * @param owner photo owner
     * @return {@code true} if the liker liked at least one photo of the owner
     */
    default boolean hasLikedAnyPhotoOf(int liker, int owner) {
        return photosOf(owner).stream().anyMatch(p -> hasLiked(liker, p.id()));
    }

    /**
     * @param userId user id
     * @return total number of likes received on all photos of the user
     */
    default int likesReceived(int userId) {
        return photosOf(userId).stream().mapToInt(p -> likers(p.id()).size()).sum();
    }

    /**
     * @param userId recipient id
     * @return requests addressed to the user
     */
    default List<FriendRequest> incoming(int userId) {
        return requests().stream().filter(r -> r.to() == userId).toList();
    }

    /**
     * @param userId sender id
     * @return requests sent by the user
     */
    default List<FriendRequest> outgoing(int userId) {
        return requests().stream().filter(r -> r.from() == userId).toList();
    }

    /**
     * @param from sender
     * @param to   recipient
     * @return {@code true} if exactly this request is pending
     */
    default boolean hasRequest(int from, int to) {
        return requests().contains(new FriendRequest(from, to));
    }

    /**
     * @param a first user
     * @param b second user
     * @return {@code true} if a request is pending in either direction
     */
    default boolean hasPendingBetween(int a, int b) {
        return hasRequest(a, b) || hasRequest(b, a);
    }

    /**
     * @param a first user
     * @param b second user
     * @return {@code true} if the two users already formed a mutual-like match
     */
    default boolean isMatched(int a, int b) {
        return matches().contains(Pair.of(a, b));
    }
}
