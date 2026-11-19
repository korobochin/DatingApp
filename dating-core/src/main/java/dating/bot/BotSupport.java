package dating.bot;

import dating.model.NetworkView;
import dating.model.Photo;
import dating.model.Profile;
import dating.sim.Command;

import java.util.List;
import java.util.function.Predicate;

/** Helpers shared by bot strategies. */
final class BotSupport {
    private BotSupport() {
    }

    /** Candidates compatible with the user who are not yet friends. */
    static Predicate<Profile> eligible(BotContext ctx, Profile me) {
        return dating.filter.ProfileFilters.compatibleWith(me).and(ctx.matcher().notFriendsWith(me.id()));
    }

    /** Likes the user could still give to photos of the given profiles. */
    static List<Command> likeOptions(NetworkView n, int userId, List<Profile> targets) {
        return targets.stream()
                .flatMap(p -> n.photosOf(p.id()).stream())
                .filter(photo -> !n.hasLiked(userId, photo.id()))
                .map(Photo::id)
                .<Command>map(photoId -> new Command.LikePhoto(userId, photoId))
                .toList();
    }
}
