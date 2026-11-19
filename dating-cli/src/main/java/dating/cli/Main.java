package dating.cli;

import dating.bot.Bot;
import dating.bot.BotFactory;
import dating.gen.ProfileGenerator;
import dating.random.RandomSource;
import dating.runner.Simulation;
import dating.sim.EndReason;
import dating.sim.Network;
import dating.sim.SimulationConfig;
import dating.sim.SimulationEngine;
import dating.stats.EventCounter;
import dating.stats.Statistics;

import java.io.PrintStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

/**
 * Console entry point.
 * <pre>
 * sim  [--seed N] [--users N] [--attempts N] [--target N] [--quiet]   bots play, the log is printed
 * play [--seed N] [--users N] [--me ID]                               you play, bots answer
 * </pre>
 */
public final class Main {
    private Main() {
    }

    /**
     * @param args command line arguments
     */
    public static void main(String[] args) {
        String mode = args.length > 0 && !args[0].startsWith("--") ? args[0] : "sim";
        Map<String, String> options = parseOptions(args);
        long seed = Long.parseLong(options.getOrDefault("seed", "42"));
        int users = Integer.parseInt(options.getOrDefault("users", "30"));
        PrintStream out = System.out;
        ConsoleRenderer renderer = new ConsoleRenderer();

        RandomSource random = new RandomSource(seed);
        Network network = ProfileGenerator.generate(users, random);
        SimulationEngine engine = new SimulationEngine(network);

        if (mode.equals("play")) {
            int me = Integer.parseInt(options.getOrDefault("me", "1"));
            List<Bot> bots = BotFactory.createFor(network, random, me);
            Simulation simulation = new Simulation(engine, bots, random, SimulationConfig.limitedTo(1_000_000));
            engine.subscribe(e -> out.println("  * " + renderer.render(e)));
            new PlaySession(engine, simulation, me, new Scanner(System.in), out).run();
            return;
        }

        int attempts = Integer.parseInt(options.getOrDefault("attempts", "300"));
        int target = Integer.parseInt(options.getOrDefault("target", "0"));
        boolean quiet = options.containsKey("quiet");
        List<Bot> bots = BotFactory.createFor(network, random);
        EventCounter counter = new EventCounter();
        engine.subscribe(counter);
        if (!quiet) {
            engine.subscribe(e -> out.printf("[%03d] %s%n", engine.step(), renderer.render(e)));
        }
        out.printf("Simulation: seed=%d, users=%d, bots=%d%n", seed, users, bots.size());
        Simulation simulation = new Simulation(engine, bots, random, new SimulationConfig(attempts, target, 3));
        EndReason reason = simulation.run();

        out.println();
        out.println("Ended: " + reason + ", accepted actions: " + engine.step()
                + ", rejected: " + engine.rejectedCount());
        out.println("Events: " + counter.counts());
        out.println("Friendships: " + network.friends().edgeCount() + ", matches: " + network.matches().size());
        out.println("Most liked users: " + Statistics.mostLikedProfiles(network, 5));
        out.println("Popular interests: " + Statistics.interestPopularity(network).stream().limit(5).toList());
        out.println("Profiles by city: " + Statistics.profilesByCity(network));
    }

    private static Map<String, String> parseOptions(String[] args) {
        Map<String, String> result = new HashMap<>();
        for (int i = 0; i < args.length; i++) {
            if (args[i].startsWith("--")) {
                String key = args[i].substring(2);
                boolean hasValue = i + 1 < args.length && !args[i + 1].startsWith("--");
                result.put(key, hasValue ? args[++i] : "true");
            }
        }
        return result;
    }
}
