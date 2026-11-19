package dating.bot;

import dating.matching.Matcher;
import dating.matching.Recommender;
import dating.model.NetworkView;
import dating.random.RandomSource;

/**
 * Everything a bot may use to make a decision: read-only state, search services
 * and the shared random generator.
 *
 * @param network     read-only state
 * @param matcher     candidate ranking
 * @param recommender "people you may know"
 * @param random      the simulation's only source of randomness
 */
public record BotContext(NetworkView network, Matcher matcher, Recommender recommender, RandomSource random) {

    /**
     * Creates a context with default services.
     *
     * @param network read-only state
     * @param random  random generator
     * @return the context
     */
    public static BotContext of(NetworkView network, RandomSource random) {
        return new BotContext(network, new Matcher(network), new Recommender(network), random);
    }
}
