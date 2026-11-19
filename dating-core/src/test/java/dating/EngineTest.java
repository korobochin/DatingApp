package dating;

import dating.exception.IllegalActionException;
import dating.model.FriendRequest;
import dating.model.Pair;
import dating.sim.Command;
import dating.sim.EndReason;
import dating.sim.Event;
import dating.sim.Network;
import dating.sim.SimulationEngine;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class EngineTest {
    private Network network;
    private SimulationEngine engine;

    @BeforeEach
    void setUp() {
        network = Fixtures.engineNetwork();
        engine = new SimulationEngine(network);
    }

    // ---------------------------------------------------------------- likes

    @Test
    void likingOwnPhotoIsIllegal() {
        assertThrows(IllegalActionException.class, () -> engine.apply(new Command.LikePhoto(1, 1)));
    }

    @Test
    void likingTheSamePhotoTwiceIsIllegal() {
        engine.apply(new Command.LikePhoto(1, 2));
        assertThrows(IllegalActionException.class, () -> engine.apply(new Command.LikePhoto(1, 2)));
        assertEquals(Set.of(1), network.likers(2));
    }

    @Test
    void unknownUserOrPhotoIsIllegal() {
        assertThrows(IllegalActionException.class, () -> engine.apply(new Command.LikePhoto(99, 2)));
        assertThrows(IllegalActionException.class, () -> engine.apply(new Command.LikePhoto(1, 99)));
    }

    @Test
    void rejectedCommandLeavesStateAndJournalUntouched() {
        assertThrows(IllegalActionException.class, () -> engine.apply(new Command.LikePhoto(1, 1)));
        assertTrue(network.likers(1).isEmpty());
        assertTrue(engine.journal().isEmpty());
        assertEquals(0, engine.step());
    }

    @Test
    void mutualLikesFormExactlyOneMatch() {
        assertEquals(List.of(new Event.PhotoLiked(1, 2, 2)), engine.apply(new Command.LikePhoto(1, 2)));
        assertTrue(network.matches().isEmpty());
        assertEquals(List.of(new Event.PhotoLiked(2, 1, 1), new Event.MatchFormed(2, 1)),
                engine.apply(new Command.LikePhoto(2, 1)));
        assertEquals(1, engine.apply(new Command.LikePhoto(1, 3)).size());
        assertEquals(Set.of(Pair.of(1, 2)), network.matches());
    }

    // ------------------------------------------------------------- requests

    @Test
    void friendRequestFlowCreatesFriendship() {
        assertEquals(List.of(new Event.FriendRequestSent(1, 2)), engine.apply(new Command.SendFriendRequest(1, 2)));
        assertTrue(network.hasRequest(1, 2));
        assertFalse(network.areFriends(1, 2));
        assertEquals(List.of(new Event.FriendshipCreated(1, 2)), engine.apply(new Command.AcceptFriendRequest(2, 1)));
        assertTrue(network.areFriends(1, 2));
        assertTrue(network.requests().isEmpty());
    }

    @Test
    void requestToExistingFriendIsIllegal() {
        assertThrows(IllegalActionException.class, () -> engine.apply(new Command.SendFriendRequest(3, 4)));
    }

    @Test
    void duplicateAndReverseRequestsAreIllegal() {
        engine.apply(new Command.SendFriendRequest(1, 2));
        assertThrows(IllegalActionException.class, () -> engine.apply(new Command.SendFriendRequest(1, 2)));
        assertThrows(IllegalActionException.class, () -> engine.apply(new Command.SendFriendRequest(2, 1)));
    }

    @Test
    void requestToSelfIsIllegal() {
        assertThrows(IllegalActionException.class, () -> engine.apply(new Command.SendFriendRequest(2, 2)));
    }

    @Test
    void acceptingWithoutRequestIsIllegal() {
        assertThrows(IllegalActionException.class, () -> engine.apply(new Command.AcceptFriendRequest(2, 1)));
        engine.apply(new Command.SendFriendRequest(1, 2));
        assertThrows(IllegalActionException.class, () -> engine.apply(new Command.AcceptFriendRequest(1, 2)));
    }

    @Test
    void decliningRemovesTheRequestWithoutFriendship() {
        engine.apply(new Command.SendFriendRequest(1, 2));
        assertEquals(List.of(new Event.FriendRequestDeclined(1, 2)), engine.apply(new Command.DeclineFriendRequest(2, 1)));
        assertTrue(network.requests().isEmpty());
        assertFalse(network.areFriends(1, 2));
        assertEquals(List.of(), network.incoming(2));
    }

    @Test
    void declinedRequestCannotBeSentAgain() {
        engine.apply(new Command.SendFriendRequest(1, 2));
        engine.apply(new Command.DeclineFriendRequest(2, 1));
        assertTrue(network.wasDeclined(1, 2));
        assertThrows(IllegalActionException.class, () -> engine.apply(new Command.SendFriendRequest(1, 2)));
        assertEquals(1, engine.apply(new Command.SendFriendRequest(2, 1)).size());
    }

    @Test
    void pendingRequestsAreVisibleToBothSides() {
        engine.apply(new Command.SendFriendRequest(1, 2));
        assertEquals(List.of(new FriendRequest(1, 2)), network.outgoing(1));
        assertEquals(List.of(new FriendRequest(1, 2)), network.incoming(2));
    }

    @Test
    void removingFriendWorksOnlyForFriends() {
        engine.apply(new Command.RemoveFriend(3, 4));
        assertFalse(network.areFriends(3, 4));
        assertThrows(IllegalActionException.class, () -> engine.apply(new Command.RemoveFriend(3, 4)));
    }

    // -------------------------------------------------------------- profile

    @Test
    void addingInterestNormalizesAndRejectsDuplicates() {
        assertThrows(IllegalActionException.class, () -> engine.apply(new Command.AddInterest(1, " CHESS ")));
        assertThrows(IllegalActionException.class, () -> engine.apply(new Command.AddInterest(1, "  ")));
        assertEquals(List.of(new Event.InterestAdded(1, "jazz")), engine.apply(new Command.AddInterest(1, " Jazz")));
        assertEquals(Set.of("chess", "jazz"), network.profile(1).interests());
    }

    @Test
    void changingCityToTheSameOneIsIllegal() {
        assertThrows(IllegalActionException.class, () -> engine.apply(new Command.ChangeCity(1, "moscow")));
        assertEquals(List.of(new Event.CityChanged(1, "Moscow", "Kazan")), engine.apply(new Command.ChangeCity(1, "Kazan")));
        assertEquals("Kazan", network.profile(1).city());
    }

    // ------------------------------------------------------ engine behaviour

    @Test
    void applyAfterFinishThrowsIllegalState() {
        engine.finish(EndReason.STEP_LIMIT);
        assertTrue(engine.isFinished());
        assertThrows(IllegalStateException.class, () -> engine.apply(new Command.LikePhoto(1, 2)));
    }

    @Test
    void finishIsReportedOnceAndKeepsFirstReason() {
        List<Event> seen = new ArrayList<>();
        engine.subscribe(seen::add);
        engine.finish(EndReason.NO_MOVES);
        engine.finish(EndReason.STEP_LIMIT);
        assertEquals(List.of(new Event.SimulationEnded(EndReason.NO_MOVES, 0)), seen);
        assertEquals(EndReason.NO_MOVES, engine.endReason().orElseThrow());
    }

    @Test
    void submitReportsRejectionInsteadOfThrowing() {
        List<Event> events = engine.submit(new Command.LikePhoto(1, 1));
        assertEquals(1, events.size());
        assertTrue(events.get(0) instanceof Event.ActionRejected);
        assertEquals(1, engine.rejectedCount());
        assertTrue(engine.journal().isEmpty());
    }

    @Test
    void observersReceiveEventsInOrder() {
        List<Event> seen = new ArrayList<>();
        engine.subscribe(seen::add);
        engine.apply(new Command.LikePhoto(1, 2));
        engine.apply(new Command.LikePhoto(2, 1));
        assertEquals(List.of(new Event.PhotoLiked(1, 2, 2), new Event.PhotoLiked(2, 1, 1),
                new Event.MatchFormed(2, 1)), seen);
    }

    @Test
    void journalNumbersAcceptedCommandsFromOne() {
        engine.apply(new Command.LikePhoto(1, 2));
        engine.submit(new Command.LikePhoto(1, 2));
        engine.apply(new Command.LikePhoto(1, 3));
        assertEquals(2, engine.journal().size());
        assertEquals(1, engine.journal().get(0).step());
        assertEquals(2, engine.journal().get(1).step());
        assertEquals(new Command.LikePhoto(1, 3), engine.journal().get(1).command());
    }

    @Test
    void journalCannotBeModifiedFromOutside() {
        assertThrows(UnsupportedOperationException.class, () -> engine.journal().clear());
    }

    @Test
    void networkBuilderChecksReferences() {
        assertThrows(IllegalArgumentException.class, () -> Network.builder().addPhoto(1, 5));
        assertThrows(IllegalArgumentException.class, () ->
                Network.builder().addProfile(Fixtures.profile(1, 20, "A")).addFriendship(1, 2));
        assertThrows(IllegalArgumentException.class, () ->
                Network.builder().addProfile(Fixtures.profile(1, 20, "A")).addProfile(Fixtures.profile(1, 21, "B")));
    }
}
