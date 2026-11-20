package dating.sim;

import dating.graph.Graph;
import dating.model.FriendRequest;
import dating.model.Pair;
import dating.model.Photo;
import dating.model.Profile;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

/**
 * The full state of the platform: profiles, friendship graph, photos with likes,
 * pending and declined friend requests, and mutual-like matches.
 * Everything outside this package only reads it; it is changed exclusively by commands
 * applied by the {@link SimulationEngine}. The initial state is assembled with {@link #builder()}.
 * All collections are sorted, so iteration order never depends on hashing.
 */
public final class Network {
    private final Map<Integer, Profile> profiles = new TreeMap<>();
    private final Map<Integer, Photo> photos = new TreeMap<>();
    private final Map<Integer, Set<Integer>> likes = new TreeMap<>();
    private final Graph<Integer> friends = new Graph<>();
    private final Set<FriendRequest> requests = new TreeSet<>();
    private final Set<FriendRequest> declined = new TreeSet<>();
    private final Set<Pair> matches = new TreeSet<>();

    private Network() {
    }

    /** @return a builder for the initial state */
    public static Builder builder() {
        return new Builder();
    }

    // ---------------------------------------------------------------- queries

    /** @return all profiles ordered by id */
    public Collection<Profile> profiles() {
        return Collections.unmodifiableCollection(profiles.values());
    }

    /**
     * @param id user id
     * @return the profile
     * @throws IllegalActionException if the user does not exist
     */
    public Profile profile(int id) {
        Profile profile = profiles.get(id);
        if (profile == null) {
            throw new IllegalActionException("Unknown user " + id);
        }
        return profile;
    }

    /**
     * @param id user id
     * @return {@code true} if such a user exists
     */
    public boolean hasProfile(int id) {
        return profiles.containsKey(id);
    }

    /**
     * The friendship graph keyed by user id. Read it freely, but change it only through commands.
     *
     * @return the friendship graph
     */
    public Graph<Integer> friends() {
        return friends;
    }

    /**
     * @param a first user
     * @param b second user
     * @return {@code true} if they are friends
     */
    public boolean areFriends(int a, int b) {
        return friends.hasEdge(a, b);
    }

    /**
     * @param photoId photo id
     * @return the photo, if it exists
     */
    public Optional<Photo> photo(int photoId) {
        return Optional.ofNullable(photos.get(photoId));
    }

    /**
     * @param userId owner id
     * @return photos of the user ordered by id
     */
    public List<Photo> photosOf(int userId) {
        return photos.values().stream().filter(p -> p.ownerId() == userId).toList();
    }

    /**
     * @param photoId photo id
     * @return ids of users who liked the photo, ascending
     */
    public Set<Integer> likers(int photoId) {
        Set<Integer> result = likes.get(photoId);
        return result == null ? Set.of() : Collections.unmodifiableSet(result);
    }

    /**
     * @param userId user id
     * @param photoId photo id
     * @return {@code true} if the user liked the photo
     */
    public boolean hasLiked(int userId, int photoId) {
        return likers(photoId).contains(userId);
    }

    /**
     * @param liker possible liker
     * @param owner photo owner
     * @return {@code true} if the liker liked at least one photo of the owner
     */
    public boolean hasLikedAnyPhotoOf(int liker, int owner) {
        return photosOf(owner).stream().anyMatch(p -> hasLiked(liker, p.id()));
    }

    /**
     * @param userId user id
     * @return total number of likes received on all photos of the user
     */
    public int likesReceived(int userId) {
        return photosOf(userId).stream().mapToInt(p -> likers(p.id()).size()).sum();
    }

    /** @return pending friend requests ordered by sender, then recipient */
    public Set<FriendRequest> requests() {
        return Collections.unmodifiableSet(requests);
    }

    /**
     * @param userId recipient id
     * @return requests addressed to the user
     */
    public List<FriendRequest> incoming(int userId) {
        return requests.stream().filter(r -> r.to() == userId).toList();
    }

    /**
     * @param userId sender id
     * @return requests sent by the user
     */
    public List<FriendRequest> outgoing(int userId) {
        return requests.stream().filter(r -> r.from() == userId).toList();
    }

    /**
     * @param from sender
     * @param to   recipient
     * @return {@code true} if exactly this request is pending
     */
    public boolean hasRequest(int from, int to) {
        return requests.contains(new FriendRequest(from, to));
    }

    /**
     * @param from sender
     * @param to   recipient
     * @return {@code true} if the recipient has already declined a request from the sender
     */
    public boolean wasDeclined(int from, int to) {
        return declined.contains(new FriendRequest(from, to));
    }

    /**
     * @param from sender
     * @param to   recipient
     * @return {@code true} if a new request {@code from -> to} would be legal
     */
    public boolean canRequest(int from, int to) {
        return from != to && !areFriends(from, to) && !hasRequest(from, to) && !hasRequest(to, from)
                && !wasDeclined(from, to);
    }

    /** @return pairs of users who liked each other's photos, ordered */
    public Set<Pair> matches() {
        return Collections.unmodifiableSet(matches);
    }

    /**
     * @param a first user
     * @param b second user
     * @return {@code true} if the two users already formed a mutual-like match
     */
    public boolean isMatched(int a, int b) {
        return matches.contains(Pair.of(a, b));
    }

    // -------------------------------------------- mutators (package-private)

    void putProfile(Profile profile) {
        profiles.put(profile.id(), profile);
    }

    void addFriendship(int a, int b) {
        friends.addEdge(a, b);
    }

    void removeFriendship(int a, int b) {
        friends.removeEdge(a, b);
    }

    void addLike(int userId, int photoId) {
        likes.computeIfAbsent(photoId, k -> new TreeSet<>()).add(userId);
    }

    void addRequest(FriendRequest request) {
        requests.add(request);
    }

    void removeRequest(FriendRequest request) {
        requests.remove(request);
    }

    void addDeclined(FriendRequest request) {
        declined.add(request);
    }

    void addMatch(int a, int b) {
        matches.add(Pair.of(a, b));
    }

    // ---------------------------------------------------------------- builder

    /** Builder of the initial state; validates references between entities. */
    public static final class Builder {
        private final Network network = new Network();

        private Builder() {
        }

        /**
         * @param profile profile to add
         * @return this builder
         * @throws IllegalArgumentException if the id is already used
         */
        public Builder addProfile(Profile profile) {
            if (network.profiles.containsKey(profile.id())) {
                throw new IllegalArgumentException("Duplicate profile id " + profile.id());
            }
            network.putProfile(profile);
            network.friends.addVertex(profile.id());
            return this;
        }

        /**
         * @param photoId unique photo id
         * @param ownerId existing profile id
         * @return this builder
         * @throws IllegalArgumentException if the id is used or the owner is unknown
         */
        public Builder addPhoto(int photoId, int ownerId) {
            if (!network.profiles.containsKey(ownerId)) {
                throw new IllegalArgumentException("Photo " + photoId + " has unknown owner " + ownerId);
            }
            if (network.photos.containsKey(photoId)) {
                throw new IllegalArgumentException("Duplicate photo id " + photoId);
            }
            network.photos.put(photoId, new Photo(photoId, ownerId));
            return this;
        }

        /**
         * Adds an initial friendship; adding it twice has no effect.
         *
         * @param a first user
         * @param b second user
         * @return this builder
         * @throws IllegalArgumentException if a user is unknown or a == b
         */
        public Builder addFriendship(int a, int b) {
            if (!network.profiles.containsKey(a) || !network.profiles.containsKey(b)) {
                throw new IllegalArgumentException("Friendship " + a + "-" + b + " refers to an unknown user");
            }
            network.addFriendship(a, b);
            return this;
        }

        /** @return the assembled network (the builder must not be reused afterwards) */
        public Network build() {
            return network;
        }
    }
}
