package dating;

import dating.sim.IllegalActionException;
import dating.matching.ProfileFilters;
import dating.matching.MatchComparators;
import dating.matching.MatchResult;
import dating.matching.MatchScorer;
import dating.matching.MatchScorers;
import dating.matching.Matcher;
import dating.model.Gender;
import dating.model.Intent;
import dating.model.Profile;
import dating.sim.Network;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Comparator;
import java.util.List;
import java.util.function.Predicate;

import static dating.Fixtures.profile;
import static org.junit.jupiter.api.Assertions.*;

class MatcherTest {
    private Network network;
    private Matcher matcher;

    private static List<Integer> ids(List<MatchResult> results) {
        return results.stream().map(r -> r.profile().id()).toList();
    }

    @BeforeEach
    void setUp() {
        network = Network.builder()
                .addProfile(profile(1, 30, "Moscow", "chess", "jazz", "hiking"))
                .addProfile(profile(2, 31, "Moscow", "chess", "jazz", "hiking"))
                .addProfile(profile(3, 30, "Kazan", "chess"))
                .addProfile(profile(4, 45, "Moscow", "chess", "jazz"))
                .addProfile(profile(5, 30, "Moscow", "cooking"))
                .build();
        matcher = new Matcher(network);
    }

    @Test
    void candidatesAreRankedByInterestSimilarity() {
        assertEquals(List.of(2, 4, 3, 5), ids(matcher.rank(1, p -> true, 10)));
    }

    @Test
    void resultCarriesCommonInterestCount() {
        MatchResult best = matcher.rank(1, p -> true, 1).get(0);
        assertEquals(2, best.profile().id());
        assertEquals(3, best.commonInterests());
    }

    @Test
    void searchingUserIsNeverInTheResult() {
        assertFalse(ids(matcher.rank(1, p -> true, 10)).contains(1));
    }

    @Test
    void limitCutsTheList() {
        assertEquals(List.of(2, 4), ids(matcher.rank(1, p -> true, 2)));
        assertTrue(matcher.rank(1, p -> true, 0).isEmpty());
    }

    @Test
    void ageFilterBoundariesAreInclusive() {
        Predicate<Profile> thirtyToThirtyOne = ProfileFilters.ageBetween(30, 31);
        assertEquals(List.of(2, 3, 5), ids(matcher.rank(1, thirtyToThirtyOne, 10)));
        assertEquals(List.of(3, 5), ids(matcher.rank(1, ProfileFilters.ageBetween(30, 30), 10)));
    }

    @Test
    void filterThatRejectsEveryoneGivesEmptyResult() {
        assertTrue(matcher.rank(1, ProfileFilters.inCity("Berlin"), 10).isEmpty());
    }

    @Test
    void filtersComposeWithAndOrNegate() {
        Predicate<Profile> moscowOrKazan = ProfileFilters.inCity("moscow").or(ProfileFilters.inCity("Kazan"));
        Predicate<Profile> notKazan = moscowOrKazan.and(ProfileFilters.inCity("Kazan").negate());
        assertEquals(List.of(2, 4, 5), ids(matcher.rank(1, notKazan, 10)));
        assertEquals(List.of(3), ids(matcher.rank(1, ProfileFilters.inAnyCity(List.of("Kazan")), 10)));
    }

    @Test
    void equalScoresAreOrderedByCommonFriendsThenById() {
        Network n = Network.builder()
                .addProfile(profile(1, 30, "Moscow", "chess"))
                .addProfile(profile(2, 30, "Moscow", "chess"))
                .addProfile(profile(3, 30, "Moscow", "chess"))
                .addProfile(profile(4, 30, "Moscow", "chess"))
                .addProfile(profile(5, 30, "Moscow", "chess"))
                .addFriendship(1, 5).addFriendship(3, 5)
                .build();
        MatchScorer constant = (a, b) -> 1.0;
        Matcher m = new Matcher(n, constant, MatchComparators.DEFAULT);
        assertEquals(List.of(3, 2, 4), ids(m.rank(1, m.notFriendsWith(1), 10)));
    }

    @Test
    void comparatorsCanBeChainedInCustomOrder() {
        Comparator<MatchResult> byInterestsThenId = MatchComparators.chain(
                List.of(MatchComparators.BY_COMMON_INTERESTS, MatchComparators.BY_ID));
        Matcher m = new Matcher(network, MatchScorers.commonInterests(), byInterestsThenId);
        assertEquals(List.of(2, 4, 3, 5), ids(m.rank(1, p -> true, 10)));
    }

    @Test
    void unknownUserIsRejected() {
        assertThrows(IllegalActionException.class, () -> matcher.rank(99, p -> true, 5));
    }

    @Test
    void jaccardCoversIdenticalDisjointAndEmptySets() {
        MatchScorer jaccard = MatchScorers.jaccard();
        assertEquals(1.0, jaccard.score(profile(1, 20, "A", "x", "y"), profile(2, 20, "A", "x", "y")), 1e-9);
        assertEquals(0.0, jaccard.score(profile(1, 20, "A", "x"), profile(2, 20, "A", "y")), 1e-9);
        assertEquals(0.0, jaccard.score(profile(1, 20, "A"), profile(2, 20, "A")), 1e-9);
        assertEquals(1.0 / 3, jaccard.score(profile(1, 20, "A", "x", "y"), profile(2, 20, "A", "y", "z")), 1e-9);
    }

    @Test
    void compatibilityRequiresMutualOrientationAndIntent() {
        Profile man = Profile.builder().id(1).name("M").age(30).city("A").gender(Gender.MALE)
                .lookingFor(Gender.FEMALE).intent(Intent.DATING).build();
        Profile woman = Profile.builder().id(2).name("W").age(30).city("A").gender(Gender.FEMALE)
                .lookingFor(Gender.MALE).intent(Intent.DATING).build();
        Profile womanForWomen = Profile.builder().id(3).name("W2").age(30).city("A").gender(Gender.FEMALE)
                .lookingFor(Gender.FEMALE).intent(Intent.DATING).build();
        Profile friendlyWoman = Profile.builder().id(4).name("W3").age(30).city("A").gender(Gender.FEMALE)
                .lookingFor(Gender.MALE).intent(Intent.FRIENDSHIP).build();
        Predicate<Profile> forMan = ProfileFilters.compatibleWith(man);
        assertTrue(forMan.test(woman));
        assertFalse(forMan.test(womanForWomen));
        assertFalse(forMan.test(friendlyWoman));
    }
}
