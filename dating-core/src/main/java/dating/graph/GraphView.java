package dating.graph;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.OptionalInt;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

/**
 * Read-only view of an undirected graph with comparable vertices.
 * Algorithms are provided as default methods, so any implementation gets them.
 * All returned collections are iterated in a deterministic (sorted) order.
 *
 * @param <V> vertex type
 */
public interface GraphView<V extends Comparable<V>> {

    /**
     * An undirected edge with {@code a < b}.
     *
     * @param a   smaller endpoint
     * @param b   larger endpoint
     * @param <T> vertex type
     */
    record Edge<T>(T a, T b) {
    }

    /**
     * @param vertex a vertex
     * @return {@code true} if the vertex exists
     */
    boolean contains(V vertex);

    /** @return all vertices in ascending order */
    Set<V> vertices();

    /**
     * @param vertex a vertex
     * @return its neighbours in ascending order
     * @throws IllegalArgumentException if the vertex is unknown
     */
    Set<V> neighbors(V vertex);

    /** @return number of edges */
    int edgeCount();

    /**
     * @param a first vertex
     * @param b second vertex
     * @return {@code true} if they are adjacent
     */
    default boolean hasEdge(V a, V b) {
        return contains(a) && contains(b) && neighbors(a).contains(b);
    }

    /**
     * @param vertex a vertex
     * @return number of neighbours
     */
    default int degree(V vertex) {
        return neighbors(vertex).size();
    }

    /**
     * @param a first vertex
     * @param b second vertex
     * @return vertices adjacent to both, sorted
     */
    default Set<V> commonNeighbors(V a, V b) {
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
    default Map<V, Set<V>> friendsOfFriends(V v) {
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
    default OptionalInt distance(V from, V to) {
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
    default List<Edge<V>> edges() {
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
