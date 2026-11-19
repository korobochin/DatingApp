package dating.bot;

import dating.sim.Command;

import java.util.Optional;

/**
 * A computer-controlled user. Different behaviours are different implementations (Strategy).
 * A bot only proposes a command; the engine decides whether it is legal.
 */
public interface Bot {

    /** @return id of the profile this bot controls */
    int userId();

    /**
     * @param context state and services
     * @return the next move, or empty if the bot has nothing to do now
     */
    Optional<Command> decide(BotContext context);
}
