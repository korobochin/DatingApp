package dating.bot;

import dating.model.FriendRequest;
import dating.model.NetworkView;
import dating.model.Profile;
import dating.sim.Command;

import java.util.List;
import java.util.Optional;

/**
 * Passive bot: accepts every request and only rarely likes a random compatible person's photo.
 *
 * @param userId id of the controlled profile
 */
public record PassiveBot(int userId) implements Bot {

    @Override
    public Optional<Command> decide(BotContext ctx) {
        NetworkView n = ctx.network();
        List<FriendRequest> incoming = n.incoming(userId);
        if (!incoming.isEmpty()) {
            return Optional.of(new Command.AcceptFriendRequest(userId, incoming.get(0).from()));
        }
        if (!ctx.random().chance(0.2)) {
            return Optional.empty();
        }
        Profile me = n.profile(userId);
        List<Profile> pool = n.profiles().stream()
                .filter(p -> p.id() != userId)
                .filter(BotSupport.eligible(ctx, me)).toList();
        if (pool.isEmpty()) {
            return Optional.empty();
        }
        List<Command> likes = BotSupport.likeOptions(n, userId, List.of(ctx.random().pick(pool)));
        return likes.isEmpty() ? Optional.empty() : Optional.of(ctx.random().pick(likes));
    }
}
