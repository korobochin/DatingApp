package dating.model;

/**
 * An unordered pair of user ids, normalised so that {@code a <= b}.
 *
 * @param a smaller id
 * @param b larger id
 */
public record Pair(int a, int b) implements Comparable<Pair> {

    /**
     * Creates a normalised pair.
     *
     * @param x first id
     * @param y second id
     * @return the pair with ids in ascending order
     */
    public static Pair of(int x, int y) {
        return x <= y ? new Pair(x, y) : new Pair(y, x);
    }

    /**
     * @param id user id
     * @return {@code true} if the id is one of the two members
     */
    public boolean contains(int id) {
        return a == id || b == id;
    }

    /**
     * @param id a member of the pair
     * @return the other member
     */
    public int other(int id) {
        if (id == a) {
            return b;
        }
        if (id == b) {
            return a;
        }
        throw new IllegalArgumentException(id + " is not in " + this);
    }

    @Override
    public int compareTo(Pair o) {
        int c = Integer.compare(a, o.a);
        return c != 0 ? c : Integer.compare(b, o.b);
    }
}
