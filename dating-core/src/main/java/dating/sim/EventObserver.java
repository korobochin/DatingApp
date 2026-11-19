package dating.sim;

/** Observer of the event stream produced by the {@link SimulationEngine}. */
@FunctionalInterface
public interface EventObserver {

    /**
     * Called synchronously for every event, in order of occurrence.
     *
     * @param event the event
     */
    void onEvent(Event event);
}
