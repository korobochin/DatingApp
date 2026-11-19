package dating.cli;

import dating.matching.MatchResult;
import dating.matching.Recommendation;
import dating.model.Profile;
import dating.sim.Event;

/** Turns model objects into text. This is the only place that knows how things look on screen. */
public final class ConsoleRenderer {

    /**
     * @param event an event from the engine
     * @return one-line description
     */
    public String render(Event event) {
        return switch (event) {
            case Event.PhotoLiked e -> "user %d liked photo %d of user %d".formatted(e.userId(), e.photoId(), e.ownerId());
            case Event.MatchFormed e -> "MATCH: users %d and %d liked each other".formatted(e.firstId(), e.secondId());
            case Event.FriendRequestSent e -> "user %d sent a friend request to user %d".formatted(e.from(), e.to());
            case Event.FriendRequestDeclined e -> "user %d declined the request of user %d".formatted(e.to(), e.from());
            case Event.FriendshipCreated e -> "FRIENDS: users %d and %d".formatted(e.requesterId(), e.accepterId());
            case Event.FriendshipEnded e -> "user %d is no longer friends with user %d".formatted(e.userId(), e.otherId());
            case Event.InterestAdded e -> "user %d added interest '%s'".formatted(e.userId(), e.interest());
            case Event.CityChanged e -> "user %d moved from %s to %s".formatted(e.userId(), e.oldCity(), e.newCity());
            case Event.ActionRejected e -> "rejected %s: %s".formatted(e.command(), e.reason());
            case Event.SimulationEnded e -> "END (%s) after %d accepted actions".formatted(e.reason(), e.steps());
        };
    }

    /**
     * @param p a profile
     * @return one-line description
     */
    public String render(Profile p) {
        return "#%d %s, %d, %s, %s, looking for %s (%s); interests: %s"
                .formatted(p.id(), p.name(), p.age(), p.city(), p.gender(), p.lookingFor(), p.intent(),
                        String.join(", ", p.interests()));
    }

    /**
     * @param r ranked candidate
     * @return one-line description
     */
    public String render(MatchResult r) {
        return "%s | score %.3f | common interests %d | common friends %d"
                .formatted(render(r.profile()), r.score(), r.commonInterests(), r.commonFriends());
    }

    /**
     * @param r suggestion
     * @return one-line description
     */
    public String render(Recommendation r) {
        return "user %d | %d common friends %s".formatted(r.userId(), r.commonFriends(), r.mutualFriends());
    }
}
