package dating.bot;

import dating.model.Profile;
import dating.random.RandomSource;
import dating.sim.Network;

import java.util.Arrays;
import java.util.List;

/** Factory of bots. */
public final class BotFactory {
    private BotFactory() {
    }

    /**
     * Creates one bot per profile, in ascending id order; about two thirds are active, the rest passive.
     *
     * @param network  state with the profiles
     * @param random   random generator
     * @param excluded user ids that must not get a bot (for example a human player)
     * @return bots ordered by user id
     */
    public static List<Bot> createFor(Network network, RandomSource random, int... excluded) {
        return network.profiles().stream()
                .map(Profile::id)
                .filter(id -> Arrays.stream(excluded).noneMatch(x -> x == id))
                .map(id -> random.chance(2.0 / 3) ? (Bot) new ActiveBot(id) : new PassiveBot(id))
                .toList();
    }
}
