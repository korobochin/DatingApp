package dating;

import dating.random.RandomSource;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class RandomSourceTest {

    private static List<Integer> sample(long seed) {
        RandomSource r = new RandomSource(seed);
        List<Integer> values = new ArrayList<>();
        for (int i = 0; i < 20; i++) {
            values.add(r.nextInt(1000));
        }
        return values;
    }

    @Test
    void sameSeedGivesSameSequence() {
        assertEquals(sample(7), sample(7));
    }

    @Test
    void differentSeedsGiveDifferentSequences() {
        assertNotEquals(sample(7), sample(8));
    }

    @Test
    void rangeBoundsAreInclusive() {
        RandomSource r = new RandomSource(1);
        boolean sawFrom = false;
        boolean sawTo = false;
        for (int i = 0; i < 500; i++) {
            int v = r.nextInt(3, 5);
            assertTrue(v >= 3 && v <= 5);
            sawFrom |= v == 3;
            sawTo |= v == 5;
        }
        assertTrue(sawFrom && sawTo);
    }

    @Test
    void pickFromEmptyListIsRejected() {
        assertThrows(IllegalArgumentException.class, () -> new RandomSource(1).pick(List.of()));
    }
}
