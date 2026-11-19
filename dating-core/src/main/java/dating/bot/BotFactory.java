package dating.bot;

import dating.model.NetworkView;
import dating.model.Profile;
import dating.random.RandomSource;

import java.util.List;

/** Factory of bots. */
public final class BotFactory {
    private static final List<BotType> MIX =
            List.of(BotType.ACTIVE, BotType.ACTIVE, BotType.SELECTIVE, BotType.PASSIVE);

    private BotFactory() {
    }

    /**
     * @param type   behaviour
     * @param userId profile to control
     * @return a new bot
     */
    public static Bot create(BotType type, int userId) {
        return switch (type) {
            case ACTIVE -> new ActiveBot(userId);
            case SELECTIVE -> new SelectiveBot(userId);
            case PASSIVE -> new PassiveBot(userId);
        };
    }

    /**
     * Creates one bot per profile (in ascending id order, except the excluded ones);
     * behaviours are drawn from the shared random generator.
     *
     * @param network  state with the profiles
     * @param random   random generator
     * @param excluded user ids that must not get a bot (for example a human player)
     * @return bots ordered by user id
     */
    public static List<Bot> createFor(NetworkView network, RandomSource random, int... excluded) {
        return network.profiles().stream()
                .map(Profile::id)
                .filter(id -> java.util.Arrays.stream(excluded).noneMatch(x -> x == id))
                .map(id -> create(random.pick(MIX), id))
                .toList();
    }
}
