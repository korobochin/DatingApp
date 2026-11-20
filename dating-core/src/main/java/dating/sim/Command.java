package dating.sim;

import dating.model.FriendRequest;
import dating.model.Photo;

import java.util.ArrayList;
import java.util.List;

/**
 * A move on the platform (Command pattern). {@link #validate(Network)} checks the rules
 * without touching the state; {@link #execute(Network)} changes the state and
 * returns the resulting events. The engine always validates before executing.
 */
public sealed interface Command {

    /**
     * Checks that the move is legal in the current state.
     *
     * @param network current state (not modified)
     * @throws IllegalActionException if the move breaks a rule
     */
    void validate(Network network);

    /**
     * Applies an already validated move.
     *
     * @param network state to modify
     * @return events caused by the move, in order
     */
    List<Event> execute(Network network);

    /**
     * Like a photo of another user. Mutual likes form a match.
     *
     * @param userId  who likes
     * @param photoId photo to like
     */
    record LikePhoto(int userId, int photoId) implements Command {
        @Override
        public void validate(Network n) {
            n.profile(userId);
            Photo photo = n.photo(photoId)
                    .orElseThrow(() -> new IllegalActionException("Unknown photo " + photoId));
            if (photo.ownerId() == userId) {
                throw new IllegalActionException("User " + userId + " cannot like own photo " + photoId);
            }
            if (n.hasLiked(userId, photoId)) {
                throw new IllegalActionException("User " + userId + " already liked photo " + photoId);
            }
        }

        @Override
        public List<Event> execute(Network n) {
            int owner = n.photo(photoId).orElseThrow().ownerId();
            n.addLike(userId, photoId);
            List<Event> events = new ArrayList<>();
            events.add(new Event.PhotoLiked(userId, owner, photoId));
            if (!n.isMatched(userId, owner) && n.hasLikedAnyPhotoOf(owner, userId)) {
                n.addMatch(userId, owner);
                events.add(new Event.MatchFormed(userId, owner));
            }
            return events;
        }
    }

    /**
     * Send a friend request.
     *
     * @param from sender
     * @param to   recipient
     */
    record SendFriendRequest(int from, int to) implements Command {
        @Override
        public void validate(Network n) {
            n.profile(from);
            n.profile(to);
            if (from == to) {
                throw new IllegalActionException("User " + from + " cannot send a request to self");
            }
            if (n.areFriends(from, to)) {
                throw new IllegalActionException("Users " + from + " and " + to + " are already friends");
            }
            if (n.hasRequest(from, to)) {
                throw new IllegalActionException("Request " + from + "->" + to + " is already pending");
            }
            if (n.hasRequest(to, from)) {
                throw new IllegalActionException("User " + to + " already sent a request to " + from + "; accept it");
            }
            if (n.wasDeclined(from, to)) {
                throw new IllegalActionException("User " + to + " already declined a request from " + from);
            }
        }

        @Override
        public List<Event> execute(Network n) {
            n.addRequest(new FriendRequest(from, to));
            return List.of(new Event.FriendRequestSent(from, to));
        }
    }

    /**
     * Accept a pending request addressed to the user.
     *
     * @param userId recipient who accepts
     * @param fromId sender of the request
     */
    record AcceptFriendRequest(int userId, int fromId) implements Command {
        @Override
        public void validate(Network n) {
            n.profile(userId);
            n.profile(fromId);
            if (!n.hasRequest(fromId, userId)) {
                throw new IllegalActionException("No pending request " + fromId + "->" + userId);
            }
        }

        @Override
        public List<Event> execute(Network n) {
            n.removeRequest(new FriendRequest(fromId, userId));
            n.addFriendship(fromId, userId);
            return List.of(new Event.FriendshipCreated(fromId, userId));
        }
    }

    /**
     * Decline a pending request addressed to the user.
     *
     * @param userId recipient who declines
     * @param fromId sender of the request
     */
    record DeclineFriendRequest(int userId, int fromId) implements Command {
        @Override
        public void validate(Network n) {
            n.profile(userId);
            n.profile(fromId);
            if (!n.hasRequest(fromId, userId)) {
                throw new IllegalActionException("No pending request " + fromId + "->" + userId);
            }
        }

        @Override
        public List<Event> execute(Network n) {
            FriendRequest request = new FriendRequest(fromId, userId);
            n.removeRequest(request);
            n.addDeclined(request);
            return List.of(new Event.FriendRequestDeclined(fromId, userId));
        }
    }

    /**
     * End a friendship.
     *
     * @param userId  who ends it
     * @param otherId the friend
     */
    record RemoveFriend(int userId, int otherId) implements Command {
        @Override
        public void validate(Network n) {
            n.profile(userId);
            n.profile(otherId);
            if (!n.areFriends(userId, otherId)) {
                throw new IllegalActionException("Users " + userId + " and " + otherId + " are not friends");
            }
        }

        @Override
        public List<Event> execute(Network n) {
            n.removeFriendship(userId, otherId);
            return List.of(new Event.FriendshipEnded(userId, otherId));
        }
    }
}
