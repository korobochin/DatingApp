package dating.bot;

import dating.matching.Matcher;
import dating.matching.ProfileFilters;
import dating.matching.Recommender;
import dating.model.Photo;
import dating.model.Profile;
import dating.random.RandomSource;
import dating.sim.Command;
import dating.sim.Network;

import java.util.List;
import java.util.function.Predicate;

/**
 * Everything a bot may use to make a decision: the state (read only), search services
 * and the shared random generator.
 *
 * @param network     the state
 * @param matcher     candidate ranking
 * @param recommender "people you may know"
 * @param random      the simulation's only source of randomness
 */
public record BotContext(Network network, Matcher matcher, Recommender recommender, RandomSource random) {

    /**
     * Creates a context with default services.
     *
     * @param network the state
     * @param random  random generator
     * @return the context
     */
    public static BotContext of(Network network, RandomSource random) {
        return new BotContext(network, new Matcher(network), new Recommender(network), random);
    }

    /**
     * @param me the user looking for somebody
     * @return candidates compatible with the user who are not yet friends
     */
    public Predicate<Profile> eligible(Profile me) {
        return ProfileFilters.compatibleWith(me).and(matcher.notFriendsWith(me.id()));
    }

    /**
     * @param userId  who would like
     * @param targets whose photos may be liked
     * @return a like for every photo of the targets that the user has not liked yet
     */
    public List<Command> likeOptions(int userId, List<Profile> targets) {
        return targets.stream()
                .flatMap(p -> network.photosOf(p.id()).stream())
                .filter(photo -> !network.hasLiked(userId, photo.id()))
                .map(Photo::id)
                .<Command>map(photoId -> new Command.LikePhoto(userId, photoId))
                .toList();
    }
}
