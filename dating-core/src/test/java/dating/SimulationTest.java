package dating;

import dating.bot.Bot;
import dating.bot.BotFactory;
import dating.gen.ProfileGenerator;
import dating.model.NetworkView;
import dating.model.Profile;
import dating.random.RandomSource;
import dating.runner.Simulation;
import dating.sim.EndReason;
import dating.sim.Network;
import dating.sim.SimulationConfig;
import dating.sim.SimulationEngine;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class SimulationTest {

    /** Everything needed to inspect a finished game. */
    private record Game(SimulationEngine engine, Simulation simulation, EndReason reason) {
    }

    private static Game play(long seed, int users, SimulationConfig config) {
        RandomSource random = new RandomSource(seed);
        Network network = ProfileGenerator.generate(users, random);
        SimulationEngine engine = new SimulationEngine(network);
        List<Bot> bots = BotFactory.createFor(network, random);
        Simulation simulation = new Simulation(engine, bots, random, config);
        EndReason reason = simulation.run();
        return new Game(engine, simulation, reason);
    }

    @Test
    void sameSeedGivesIdenticalGame() {
        Game a = play(2024, 25, SimulationConfig.limitedTo(400));
        Game b = play(2024, 25, SimulationConfig.limitedTo(400));
        assertEquals(a.engine().journal(), b.engine().journal());
        assertEquals(a.engine().network().profiles().stream().toList(),
                b.engine().network().profiles().stream().toList());
        assertEquals(a.engine().network().matches(), b.engine().network().matches());
    }

    @Test
    void differentSeedsGiveDifferentGames() {
        Game a = play(1, 25, SimulationConfig.limitedTo(400));
        Game b = play(2, 25, SimulationConfig.limitedTo(400));
        assertNotEquals(a.engine().journal(), b.engine().journal());
    }

    @Test
    void fullGameFromFixedSeedHasExpectedOutcome() {
        Game game = play(2024, 25, SimulationConfig.limitedTo(400));
        NetworkView n = game.engine().network();
        assertEquals(EndReason.STEP_LIMIT, game.reason());
        assertEquals(400, game.engine().step());
        assertEquals(0, game.engine().rejectedCount());
        assertEquals(160, n.friends().edgeCount());
        assertEquals(18, n.matches().size());
    }

    @Test
    void gameRunsWithoutAnyConsoleOutput() {
        PrintStream oldOut = System.out;
        PrintStream oldErr = System.err;
        ByteArrayOutputStream captured = new ByteArrayOutputStream();
        PrintStream sink = new PrintStream(captured);
        System.setOut(sink);
        System.setErr(sink);
        try {
            play(7, 20, SimulationConfig.limitedTo(300));
        } finally {
            sink.flush();
            System.setOut(oldOut);
            System.setErr(oldErr);
        }
        assertEquals(0, captured.size());
    }

    @Test
    void attemptLimitStopsTheGame() {
        Game game = play(5, 20, SimulationConfig.limitedTo(5));
        assertEquals(EndReason.STEP_LIMIT, game.reason());
        assertEquals(5, game.simulation().attempts());
        assertTrue(game.engine().step() <= 5);
    }

    @Test
    void friendshipTargetStopsTheGame() {
        Game game = play(11, 25, new SimulationConfig(10_000, 3, 3));
        assertEquals(EndReason.TARGET_REACHED, game.reason());
        assertEquals(3, game.simulation().friendshipsCreated());
    }

    @Test
    void gameWithoutBotsEndsImmediatelyWithNoMoves() {
        RandomSource random = new RandomSource(1);
        SimulationEngine engine = new SimulationEngine(ProfileGenerator.generate(5, random));
        Simulation simulation = new Simulation(engine, List.of(), random, SimulationConfig.limitedTo(100));
        assertEquals(EndReason.NO_MOVES, simulation.run());
        assertEquals(0, engine.step());
    }

    @Test
    void gameEventuallyRunsOutOfMovesWhenLimitIsHuge() {
        Game game = play(3, 8, SimulationConfig.limitedTo(1_000_000));
        assertEquals(EndReason.NO_MOVES, game.reason());
    }

    @Test
    void invalidConfigIsRejected() {
        assertThrows(IllegalArgumentException.class, () -> new SimulationConfig(0, 0, 3));
        assertThrows(IllegalArgumentException.class, () -> new SimulationConfig(10, -1, 3));
        assertThrows(IllegalArgumentException.class, () -> new SimulationConfig(10, 0, 0));
    }

    @Test
    void finishedGameKeepsRuleInvariants() {
        Game game = play(99, 30, SimulationConfig.limitedTo(600));
        NetworkView n = game.engine().network();
        for (var request : n.requests()) {
            assertFalse(n.areFriends(request.from(), request.to()));
            assertNotEquals(request.from(), request.to());
        }
        for (var pair : n.matches()) {
            assertTrue(n.hasLikedAnyPhotoOf(pair.a(), pair.b()));
            assertTrue(n.hasLikedAnyPhotoOf(pair.b(), pair.a()));
        }
        for (Profile p : n.profiles()) {
            for (var photo : n.photosOf(p.id())) {
                assertFalse(n.likers(photo.id()).contains(p.id()));
            }
        }
    }

    @Test
    void generatorIsReproducible() {
        List<Profile> a = ProfileGenerator.generate(15, new RandomSource(5)).profiles().stream().toList();
        List<Profile> b = ProfileGenerator.generate(15, new RandomSource(5)).profiles().stream().toList();
        assertEquals(a, b);
        assertThrows(IllegalArgumentException.class, () -> ProfileGenerator.generate(2, new RandomSource(5)));
    }
}
