package dating.sim;

import dating.exception.IllegalActionException;
import dating.graph.Graph;
import dating.graph.GraphView;
import dating.model.FriendRequest;
import dating.model.NetworkView;
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
 * The full state of the platform. Outside this package it can only be read
 * (through {@link NetworkView}); it is changed exclusively by commands applied by the
 * {@link SimulationEngine}. Initial state is assembled with {@link #builder()}.
 * All collections are sorted, so iteration order never depends on hashing.
 */
public final class Network implements NetworkView {
    private final Map<Integer, Profile> profiles = new TreeMap<>();
    private final Map<Integer, Photo> photos = new TreeMap<>();
    private final Map<Integer, Set<Integer>> likes = new TreeMap<>();
    private final Graph<Integer> friends = new Graph<>();
    private final Set<FriendRequest> requests = new TreeSet<>();
    private final Set<Pair> matches = new TreeSet<>();

    private Network() {
    }

    /** @return a builder for the initial state */
    public static Builder builder() {
        return new Builder();
    }

    // ---------------------------------------------------------------- queries

    @Override
    public Collection<Profile> profiles() {
        return Collections.unmodifiableCollection(profiles.values());
    }

    @Override
    public Profile profile(int id) {
        Profile profile = profiles.get(id);
        if (profile == null) {
            throw new IllegalActionException("Unknown user " + id);
        }
        return profile;
    }

    @Override
    public boolean hasProfile(int id) {
        return profiles.containsKey(id);
    }

    @Override
    public GraphView<Integer> friends() {
        return friends;
    }

    @Override
    public Optional<Photo> photo(int photoId) {
        return Optional.ofNullable(photos.get(photoId));
    }

    @Override
    public List<Photo> photosOf(int userId) {
        return photos.values().stream().filter(p -> p.ownerId() == userId).toList();
    }

    @Override
    public Set<Integer> likers(int photoId) {
        Set<Integer> result = likes.get(photoId);
        return result == null ? Set.of() : Collections.unmodifiableSet(result);
    }

    @Override
    public Set<FriendRequest> requests() {
        return Collections.unmodifiableSet(requests);
    }

    @Override
    public Set<Pair> matches() {
        return Collections.unmodifiableSet(matches);
    }

    /**
     * @param userId user id
     * @return the profile
     * @throws IllegalActionException if the user does not exist
     */
    public Profile requireProfile(int userId) {
        return profile(userId);
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
