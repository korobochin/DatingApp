package dating.stats;

import dating.sim.Event;
import dating.sim.EventObserver;

import java.util.Map;
import java.util.TreeMap;

/** Observer that counts events by type. */
public final class EventCounter implements EventObserver {
    private final Map<String, Long> counts = new TreeMap<>();

    @Override
    public void onEvent(Event event) {
        counts.merge(event.getClass().getSimpleName(), 1L, Long::sum);
    }

    /** @return event type name to number of occurrences, sorted by name */
    public Map<String, Long> counts() {
        return Map.copyOf(new TreeMap<>(counts));
    }

    /**
     * @param type simple class name of an event, e.g. {@code "MatchFormed"}
     * @return how many such events were seen
     */
    public long count(String type) {
        return counts.getOrDefault(type, 0L);
    }
}
