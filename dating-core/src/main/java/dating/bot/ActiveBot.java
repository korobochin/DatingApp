package dating.bot;

import dating.matching.MatchResult;
import dating.matching.Recommendation;
import dating.model.FriendRequest;
import dating.model.NetworkView;
import dating.model.Profile;
import dating.sim.Command;

import java.util.List;
import java.util.Optional;

/**
 * Sociable bot: accepts every request, often likes photos of the best-ranked candidates
 * and sends requests to "people you may know".
 *
 * @param userId id of the controlled profile
 */
public record ActiveBot(int userId) implements Bot {

    @Override
    public Optional<Command> decide(BotContext ctx) {
        NetworkView n = ctx.network();
        List<FriendRequest> incoming = n.incoming(userId);
        if (!incoming.isEmpty()) {
            return Optional.of(new Command.AcceptFriendRequest(userId, incoming.get(0).from()));
        }
        Profile me = n.profile(userId);
        if (ctx.random().chance(0.6)) {
            List<Profile> top = ctx.matcher().rank(userId, BotSupport.eligible(ctx, me), 5)
                    .stream().map(MatchResult::profile).toList();
            List<Command> likes = BotSupport.likeOptions(n, userId, top);
            if (!likes.isEmpty()) {
                return Optional.of(ctx.random().pick(likes));
            }
        }
        List<Recommendation> recs = ctx.recommender().mightKnow(userId, 3).stream()
                .filter(r -> n.canRequest(userId, r.userId())).toList();
        if (!recs.isEmpty()) {
            return Optional.of(new Command.SendFriendRequest(userId, ctx.random().pick(recs).userId()));
        }
        List<MatchResult> best = ctx.matcher()
                .rank(userId, BotSupport.eligible(ctx, me).and(p -> n.canRequest(userId, p.id())), 3);
        if (!best.isEmpty()) {
            return Optional.of(new Command.SendFriendRequest(userId, ctx.random().pick(best).profile().id()));
        }
        return Optional.empty();
    }
}
