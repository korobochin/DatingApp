package dating.model;

/**
 * A pending friendship request.
 *
 * @param from id of the sender
 * @param to   id of the recipient
 */
public record FriendRequest(int from, int to) implements Comparable<FriendRequest> {
    @Override
    public int compareTo(FriendRequest o) {
        int c = Integer.compare(from, o.from);
        return c != 0 ? c : Integer.compare(to, o.to);
    }
}
