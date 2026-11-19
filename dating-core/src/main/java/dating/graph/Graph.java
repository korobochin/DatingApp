package dating.graph;

import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

/**
 * Mutable undirected graph without loops or parallel edges.
 * Vertices and neighbour sets are kept sorted so every traversal is deterministic.
 *
 * @param <V> vertex type
 */
public final class Graph<V extends Comparable<V>> implements GraphView<V> {
    private final Map<V, Set<V>> adjacency = new TreeMap<>();

    /**
     * @param vertex vertex to add
     * @return {@code true} if it was not present
     */
    public boolean addVertex(V vertex) {
        return adjacency.putIfAbsent(vertex, new TreeSet<>()) == null;
    }

    /**
     * Adds an undirected edge, creating missing vertices.
     *
     * @param a first endpoint
     * @param b second endpoint
     * @return {@code true} if the edge was new
     * @throws IllegalArgumentException for a loop
     */
    public boolean addEdge(V a, V b) {
        if (a.equals(b)) {
            throw new IllegalArgumentException("Loops are not allowed: " + a);
        }
        addVertex(a);
        addVertex(b);
        boolean added = adjacency.get(a).add(b);
        adjacency.get(b).add(a);
        return added;
    }

    /**
     * @param a first endpoint
     * @param b second endpoint
     * @return {@code true} if the edge existed and was removed
     */
    public boolean removeEdge(V a, V b) {
        if (!hasEdge(a, b)) {
            return false;
        }
        adjacency.get(a).remove(b);
        adjacency.get(b).remove(a);
        return true;
    }

    @Override
    public boolean contains(V vertex) {
        return adjacency.containsKey(vertex);
    }

    @Override
    public Set<V> vertices() {
        return Collections.unmodifiableSet(adjacency.keySet());
    }

    @Override
    public Set<V> neighbors(V vertex) {
        Set<V> result = adjacency.get(vertex);
        if (result == null) {
            throw new IllegalArgumentException("Unknown vertex: " + vertex);
        }
        return Collections.unmodifiableSet(result);
    }

    @Override
    public int edgeCount() {
        return adjacency.values().stream().mapToInt(Set::size).sum() / 2;
    }
}
