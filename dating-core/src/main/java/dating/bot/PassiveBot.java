package dating.bot;

import dating.model.FriendRequest;
import dating.model.Profile;
import dating.sim.Command;
import dating.sim.Network;

import java.util.List;
import java.util.Optional;

/**
 * Passive bot: declines requests from people with no common interests, accepts the rest,
 * and only rarely likes a random compatible person's photo.
 *
 * @param userId id of the controlled profile
 */
public record PassiveBot(int userId) implements Bot {

    @Override
    public Optional<Command> decide(BotContext ctx) {
        Network n = ctx.network();
        Profile me = n.profile(userId);
        List<FriendRequest> incoming = n.incoming(userId);
        if (!incoming.isEmpty()) {
            int from = incoming.get(0).from();
            boolean common = me.interests().stream().anyMatch(n.profile(from).interests()::contains);
            return Optional.of(common
                    ? new Command.AcceptFriendRequest(userId, from)
                    : new Command.DeclineFriendRequest(userId, from));
        }
        if (!ctx.random().chance(0.2)) {
            return Optional.empty();
        }
        List<Profile> pool = n.profiles().stream().filter(p -> p.id() != userId).filter(ctx.eligible(me)).toList();
        if (pool.isEmpty()) {
            return Optional.empty();
        }
        List<Command> likes = ctx.likeOptions(userId, List.of(ctx.random().pick(pool)));
        return likes.isEmpty() ? Optional.empty() : Optional.of(ctx.random().pick(likes));
    }
}
