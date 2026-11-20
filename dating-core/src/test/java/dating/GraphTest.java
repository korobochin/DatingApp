package dating;

import dating.graph.Graph;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class GraphTest {
    private Graph<Integer> g;

    @BeforeEach
    void setUp() {
        g = new Graph<>();
    }

    @Test
    void edgeIsSymmetricAndNotDuplicated() {
        assertTrue(g.addEdge(1, 2));
        assertFalse(g.addEdge(2, 1));
        assertTrue(g.hasEdge(2, 1));
        assertEquals(1, g.edgeCount());
    }

    @Test
    void loopIsRejected() {
        assertThrows(IllegalArgumentException.class, () -> g.addEdge(1, 1));
    }

    @Test
    void unknownVertexIsRejected() {
        assertThrows(IllegalArgumentException.class, () -> g.neighbors(42));
    }

    @Test
    void commonNeighborsAreIntersectionOfNeighbourSets() {
        g.addEdge(1, 3);
        g.addEdge(2, 3);
        g.addEdge(1, 4);
        g.addEdge(2, 4);
        g.addEdge(1, 5);
        assertEquals(Set.of(3, 4), g.commonNeighbors(1, 2));
    }

    @Test
    void friendsOfFriendsSkipsSelfAndDirectFriends() {
        g.addEdge(1, 2);
        g.addEdge(1, 3);
        g.addEdge(2, 3);
        g.addEdge(2, 4);
        g.addEdge(3, 4);
        g.addEdge(3, 5);
        Map<Integer, Set<Integer>> result = g.friendsOfFriends(1);
        assertEquals(Map.of(4, Set.of(2, 3), 5, Set.of(3)), result);
    }

    @Test
    void isolatedVertexHasNoCandidatesAndNoPaths() {
        g.addVertex(9);
        g.addEdge(1, 2);
        assertTrue(g.friendsOfFriends(9).isEmpty());
        assertFalse(g.distance(9, 1).isPresent());
    }

    @Test
    void distanceIsShortestPathLength() {
        g.addEdge(1, 2);
        g.addEdge(2, 3);
        g.addEdge(3, 4);
        g.addEdge(1, 4);
        assertEquals(1, g.distance(1, 4).getAsInt());
        assertEquals(2, g.distance(2, 4).getAsInt());
        assertEquals(0, g.distance(3, 3).getAsInt());
    }

    @Test
    void removeEdgeWorksInBothDirections() {
        g.addEdge(1, 2);
        assertTrue(g.removeEdge(2, 1));
        assertFalse(g.hasEdge(1, 2));
        assertFalse(g.removeEdge(1, 2));
        assertTrue(g.contains(1));
    }

    @Test
    void edgesAreListedOnceInSortedOrder() {
        g.addEdge(3, 1);
        g.addEdge(2, 1);
        assertEquals(List.of(new Graph.Edge<>(1, 2), new Graph.Edge<>(1, 3)), g.edges());
    }

    @Test
    void worksWithAnyComparableVertexType() {
        Graph<String> names = new Graph<>();
        names.addEdge("bob", "alice");
        assertEquals(Set.of("alice"), names.neighbors("bob"));
    }
}
