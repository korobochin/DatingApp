package dating.bot;

import dating.filter.ProfileFilters;
import dating.matching.MatchResult;
import dating.model.FriendRequest;
import dating.model.NetworkView;
import dating.model.Pair;
import dating.model.Profile;
import dating.sim.Command;

import java.util.List;
import java.util.Optional;

/**
 * Picky bot: answers requests only after checking shared interests, likes only people of similar age
 * with at least two common interests, and sends a request only after a mutual like.
 *
 * @param userId id of the controlled profile
 */
public record SelectiveBot(int userId) implements Bot {

    @Override
    public Optional<Command> decide(BotContext ctx) {
        NetworkView n = ctx.network();
        Profile me = n.profile(userId);
        List<FriendRequest> incoming = n.incoming(userId);
        if (!incoming.isEmpty()) {
            int from = incoming.get(0).from();
            long common = me.interests().stream().filter(n.profile(from).interests()::contains).count();
            return Optional.of(common >= 2
                    ? new Command.AcceptFriendRequest(userId, from)
                    : new Command.DeclineFriendRequest(userId, from));
        }
        List<Profile> suitable = ctx.matcher()
                .rank(userId, BotSupport.eligible(ctx, me).and(ProfileFilters.ageBetween(me.age() - 7, me.age() + 7)), 3)
                .stream().filter(r -> r.commonInterests() >= 2).map(MatchResult::profile).toList();
        List<Command> likes = BotSupport.likeOptions(n, userId, suitable);
        if (!likes.isEmpty()) {
            return Optional.of(ctx.random().pick(likes));
        }
        return n.matches().stream()
                .filter(pair -> pair.contains(userId))
                .map(pair -> pair.other(userId))
                .filter(other -> n.canRequest(userId, other))
                .findFirst()
                .map(other -> new Command.SendFriendRequest(userId, other));
    }
}
