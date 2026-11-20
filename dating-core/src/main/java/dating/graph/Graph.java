package dating.graph;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.OptionalInt;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

/**
 * Undirected graph without loops or parallel edges. Vertices and neighbour sets are kept sorted,
 * so every traversal and every result is deterministic.
 *
 * @param <V> vertex type
 */
public final class Graph<V extends Comparable<V>> {

    /**
     * An undirected edge with {@code a < b}.
     *
     * @param a   smaller endpoint
     * @param b   larger endpoint
     * @param <T> vertex type
     */
    public record Edge<T>(T a, T b) {
    }

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

    /**
     * @param vertex a vertex
     * @return {@code true} if the vertex exists
     */
    public boolean contains(V vertex) {
        return adjacency.containsKey(vertex);
    }

    /** @return all vertices in ascending order */
    public Set<V> vertices() {
        return Collections.unmodifiableSet(adjacency.keySet());
    }

    /**
     * @param vertex a vertex
     * @return its neighbours in ascending order
     * @throws IllegalArgumentException if the vertex is unknown
     */
    public Set<V> neighbors(V vertex) {
        Set<V> result = adjacency.get(vertex);
        if (result == null) {
            throw new IllegalArgumentException("Unknown vertex: " + vertex);
        }
        return Collections.unmodifiableSet(result);
    }

    /** @return number of edges */
    public int edgeCount() {
        return adjacency.values().stream().mapToInt(Set::size).sum() / 2;
    }

    /**
     * @param a first vertex
     * @param b second vertex
     * @return {@code true} if they are adjacent
     */
    public boolean hasEdge(V a, V b) {
        return contains(a) && contains(b) && neighbors(a).contains(b);
    }

    /**
     * @param vertex a vertex
     * @return number of neighbours
     */
    public int degree(V vertex) {
        return neighbors(vertex).size();
    }

    /**
     * @param a first vertex
     * @param b second vertex
     * @return vertices adjacent to both, sorted
     */
    public Set<V> commonNeighbors(V a, V b) {
        Set<V> result = new TreeSet<>(neighbors(a));
        result.retainAll(neighbors(b));
        return result;
    }

    /**
     * Vertices at distance exactly two from {@code v}, each mapped to the set of
     * intermediate vertices (their common neighbours with {@code v}).
     *
     * @param v start vertex
     * @return candidate to mutual neighbours, sorted by candidate
     */
    public Map<V, Set<V>> friendsOfFriends(V v) {
        Set<V> direct = neighbors(v);
        Map<V, Set<V>> result = new TreeMap<>();
        for (V n : direct) {
            for (V w : neighbors(n)) {
                if (!w.equals(v) && !direct.contains(w)) {
                    result.computeIfAbsent(w, k -> new TreeSet<>()).add(n);
                }
            }
        }
        return result;
    }

    /**
     * Shortest path length (in edges) found with breadth-first search.
     *
     * @param from start vertex
     * @param to   target vertex
     * @return distance, or empty if unreachable or a vertex is unknown
     */
    public OptionalInt distance(V from, V to) {
        if (!contains(from) || !contains(to)) {
            return OptionalInt.empty();
        }
        if (from.equals(to)) {
            return OptionalInt.of(0);
        }
        Map<V, Integer> dist = new HashMap<>();
        Deque<V> queue = new ArrayDeque<>();
        dist.put(from, 0);
        queue.add(from);
        while (!queue.isEmpty()) {
            V current = queue.poll();
            for (V next : neighbors(current)) {
                if (!dist.containsKey(next)) {
                    dist.put(next, dist.get(current) + 1);
                    if (next.equals(to)) {
                        return OptionalInt.of(dist.get(next));
                    }
                    queue.add(next);
                }
            }
        }
        return OptionalInt.empty();
    }

    /** @return all edges, sorted by first then second endpoint */
    public List<Edge<V>> edges() {
        List<Edge<V>> result = new ArrayList<>();
        for (V a : vertices()) {
            for (V b : neighbors(a)) {
                if (a.compareTo(b) < 0) {
                    result.add(new Edge<>(a, b));
                }
            }
        }
        return result;
    }
}
