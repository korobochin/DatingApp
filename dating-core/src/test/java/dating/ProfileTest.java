package dating;

import dating.model.Gender;
import dating.model.Profile;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static dating.Fixtures.profile;
import static org.junit.jupiter.api.Assertions.*;

class ProfileTest {

    @Test
    void ageBoundaryIsEighteen() {
        assertThrows(IllegalArgumentException.class, () -> profile(1, 17, "Moscow"));
        assertEquals(18, profile(1, 18, "Moscow").age());
    }

    @Test
    void blankNameIsRejected() {
        assertThrows(IllegalArgumentException.class, () -> Profile.builder().id(1).name(" ").age(20)
                .city("Moscow").gender(Gender.MALE).build());
    }

    @Test
    void interestsAreNormalizedAndSorted() {
        Profile p = profile(1, 20, "Moscow", " Chess ", "JAZZ", "chess", " ");
        assertEquals(Set.of("chess", "jazz"), p.interests());
        assertEquals("chess", p.interests().iterator().next());
    }

    @Test
    void interestsCannotBeModifiedFromOutside() {
        Profile p = profile(1, 20, "Moscow", "chess");
        assertThrows(UnsupportedOperationException.class, () -> p.interests().add("jazz"));
    }
}
