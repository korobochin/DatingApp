package dating.bot;

import dating.random.RandomSource;
import dating.sim.Command;
import dating.sim.EndReason;
import dating.sim.Event;
import dating.sim.SimulationConfig;
import dating.sim.SimulationEngine;

import java.util.List;
import java.util.Optional;

/**
 * Runs bots against a {@link SimulationEngine} round by round: in every round each bot, in list order,
 * may propose one move. The run ends by the step limit, the friendship target, or when nobody moves
 * for several rounds. Nothing is printed; observers of the engine see everything.
 */
public final class Simulation {
    private final SimulationEngine engine;
    private final List<Bot> bots;
    private final BotContext context;
    private final SimulationConfig config;
    private int attempts;
    private int idleRounds;
    private int friendshipsCreated;

    /**
     * @param engine engine holding the state
     * @param bots   bots in the order they act
     * @param random the shared random generator
     * @param config run parameters
     */
    public Simulation(SimulationEngine engine, List<Bot> bots, RandomSource random, SimulationConfig config) {
        this.engine = engine;
        this.bots = List.copyOf(bots);
        this.context = BotContext.of(engine.network(), random);
        this.config = config;
        engine.subscribe(e -> {
            if (e instanceof Event.FriendshipCreated) {
                friendshipsCreated++;
            }
        });
    }

    /**
     * Gives every bot one chance to act.
     *
     * @return number of accepted commands in this round
     */
    public int playRound() {
        int accepted = 0;
        for (Bot bot : bots) {
            if (engine.isFinished() || attempts >= config.maxAttempts()) {
                break;
            }
            Optional<Command> move = bot.decide(context);
            if (move.isEmpty()) {
                continue;
            }
            attempts++;
            int before = engine.step();
            engine.submit(move.get());
            if (engine.step() > before) {
                accepted++;
            }
            if (config.targetFriendships() > 0 && friendshipsCreated >= config.targetFriendships()) {
                engine.finish(EndReason.TARGET_REACHED);
            }
        }
        return accepted;
    }

    /**
     * Plays rounds until a stop condition holds.
     *
     * @return why the run ended
     */
    public EndReason run() {
        while (!engine.isFinished()) {
            if (attempts >= config.maxAttempts()) {
                engine.finish(EndReason.STEP_LIMIT);
                break;
            }
            int accepted = playRound();
            if (engine.isFinished()) {
                break;
            }
            idleRounds = accepted == 0 ? idleRounds + 1 : 0;
            if (idleRounds >= config.maxIdleRounds()) {
                engine.finish(EndReason.NO_MOVES);
            }
        }
        return engine.endReason().orElseThrow();
    }

    /** @return number of attempted actions so far (accepted and rejected) */
    public int attempts() {
        return attempts;
    }

    /** @return number of friendships created during this run */
    public int friendshipsCreated() {
        return friendshipsCreated;
    }
}
