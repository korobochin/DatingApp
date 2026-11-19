package dating;

import dating.sim.Command;
import dating.sim.Network;
import dating.sim.SimulationEngine;
import dating.stats.EventCounter;
import dating.stats.Statistics;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static dating.Fixtures.profile;
import static org.junit.jupiter.api.Assertions.*;

class StatisticsTest {
    private Network network;
    private SimulationEngine engine;
    private EventCounter counter;

    @BeforeEach
    void setUp() {
        network = Network.builder()
                .addProfile(profile(1, 20, "Moscow", "chess", "jazz"))
                .addProfile(profile(2, 30, "Moscow", "chess"))
                .addProfile(profile(3, 40, "Moscow", "jazz", "hiking"))
                .addProfile(profile(4, 50, "Kazan", "chess"))
                .addPhoto(1, 2).addPhoto(2, 3).addPhoto(3, 3)
                .addFriendship(1, 2).addFriendship(1, 3)
                .build();
        engine = new SimulationEngine(network);
        counter = new EventCounter();
        engine.subscribe(counter);
        engine.apply(new Command.LikePhoto(1, 1));
        engine.apply(new Command.LikePhoto(1, 2));
        engine.apply(new Command.LikePhoto(4, 2));
        engine.apply(new Command.LikePhoto(4, 3));
    }

    @Test
    void groupingByCityCountsProfiles() {
        assertEquals(Map.of("Moscow", 3L, "Kazan", 1L), Statistics.profilesByCity(network));
    }

    @Test
    void averageAgeIsComputedPerCity() {
        Map<String, Double> avg = Statistics.averageAgeByCity(network);
        assertEquals(30.0, avg.get("Moscow"), 1e-9);
        assertEquals(50.0, avg.get("Kazan"), 1e-9);
    }

    @Test
    void ageSummaryAggregatesAllProfiles() {
        var summary = Statistics.ageSummary(network);
        assertEquals(20, summary.getMin());
        assertEquals(50, summary.getMax());
        assertEquals(35.0, summary.getAverage(), 1e-9);
    }

    @Test
    void partitionSplitsByAgeBorder() {
        assertEquals(Map.of(true, 2L, false, 2L), Statistics.youngerThan(network, 35));
        assertEquals(Map.of(true, 0L, false, 4L), Statistics.youngerThan(network, 20));
    }

    @Test
    void interestsAreRankedByPopularityThenAlphabetically() {
        assertEquals(List.of("chess=3", "jazz=2", "hiking=1"),
                Statistics.interestPopularity(network).stream().map(e -> e.getKey() + "=" + e.getValue()).toList());
    }

    @Test
    void friendCountDistributionGroupsByDegree() {
        assertEquals(Map.of(0, 1L, 1, 2L, 2, 1L), Statistics.friendCountDistribution(network));
    }

    @Test
    void mostLikedProfilesAreOrderedByLikesThenId() {
        assertEquals(List.of(3, 2, 1), Statistics.mostLikedProfiles(network, 3));
        assertEquals(4L, Statistics.totalLikes(network));
    }

    @Test
    void eventCounterCountsByEventType() {
        assertEquals(4L, counter.count("PhotoLiked"));
        assertEquals(0L, counter.count("MatchFormed"));
        assertEquals(Map.of("PhotoLiked", 4L), counter.counts());
    }
}
