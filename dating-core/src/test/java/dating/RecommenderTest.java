package dating;

import dating.exception.IllegalActionException;
import dating.matching.Recommendation;
import dating.matching.Recommender;
import dating.sim.Network;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static dating.Fixtures.profile;
import static org.junit.jupiter.api.Assertions.*;

class RecommenderTest {
    private Recommender recommender;

    @BeforeEach
    void setUp() {
        Network.Builder b = Network.builder();
        for (int id = 1; id <= 7; id++) {
            b.addProfile(profile(id, 25, "Moscow", "chess"));
        }
        // 1 knows 2 and 3; 4 is known by both of them; 5 by 2 only; 6 by 3 only; 7 is alone
        b.addFriendship(1, 2).addFriendship(1, 3).addFriendship(2, 4).addFriendship(3, 4)
                .addFriendship(2, 5).addFriendship(3, 6);
        recommender = new Recommender(b.build());
    }

    private static List<Integer> ids(List<Recommendation> list) {
        return list.stream().map(Recommendation::userId).toList();
    }

    @Test
    void moreCommonFriendsComeFirstAndTiesGoById() {
        List<Recommendation> result = recommender.mightKnow(1, 10);
        assertEquals(List.of(4, 5, 6), ids(result));
        assertEquals(2, result.get(0).commonFriends());
        assertEquals(List.of(2, 3), result.get(0).mutualFriends());
    }

    @Test
    void limitIsApplied() {
        assertEquals(List.of(4), ids(recommender.mightKnow(1, 1)));
    }

    @Test
    void userWithoutFriendsGetsNothing() {
        assertTrue(recommender.mightKnow(7, 10).isEmpty());
    }

    @Test
    void existingFriendsAreNeverRecommended() {
        assertFalse(ids(recommender.mightKnow(2, 10)).contains(1));
        assertFalse(ids(recommender.mightKnow(2, 10)).contains(2));
    }

    @Test
    void commonFriendsOfTwoUsers() {
        assertEquals(Set.of(2, 3), recommender.commonFriends(1, 4));
        assertThrows(IllegalActionException.class, () -> recommender.commonFriends(1, 99));
    }
}
