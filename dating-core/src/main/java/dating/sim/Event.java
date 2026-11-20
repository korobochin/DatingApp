package dating.sim;

/**
 * Something that happened on the platform. The mechanics only returns events;
 * how they are shown (console, log file, GUI) is decided by an outer layer.
 */
public sealed interface Event {

    /**
     * A user liked a photo.
     *
     * @param userId  who liked
     * @param ownerId owner of the photo
     * @param photoId liked photo
     */
    record PhotoLiked(int userId, int ownerId, int photoId) implements Event {
    }

    /**
     * Two users liked each other's photos.
     *
     * @param firstId  the user whose like completed the match
     * @param secondId the other user
     */
    record MatchFormed(int firstId, int secondId) implements Event {
    }

    /**
     * A friend request was sent.
     *
     * @param from sender
     * @param to   recipient
     */
    record FriendRequestSent(int from, int to) implements Event {
    }

    /**
     * A friend request was declined.
     *
     * @param from sender of the request
     * @param to   user who declined
     */
    record FriendRequestDeclined(int from, int to) implements Event {
    }

    /**
     * A friendship was created by an accepted request.
     *
     * @param requesterId sender of the request
     * @param accepterId  user who accepted
     */
    record FriendshipCreated(int requesterId, int accepterId) implements Event {
    }

    /**
     * A friendship was ended.
     *
     * @param userId  who ended it
     * @param otherId former friend
     */
    record FriendshipEnded(int userId, int otherId) implements Event {
    }

    /**
     * A command broke the rules and was not applied.
     *
     * @param command rejected command
     * @param reason  explanation
     */
    record ActionRejected(Command command, String reason) implements Event {
    }

    /**
     * The simulation is over.
     *
     * @param reason why it ended
     * @param steps  number of accepted commands
     */
    record SimulationEnded(EndReason reason, int steps) implements Event {
    }
}
