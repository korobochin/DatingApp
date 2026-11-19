package dating.sim;

import dating.exception.IllegalActionException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * Applies commands to the {@link Network}, keeps the journal of accepted commands
 * and notifies observers. The engine never prints anything.
 */
public final class SimulationEngine {

    /**
     * One accepted command with its consequences.
     *
     * @param step    1-based number of the accepted command
     * @param command the command
     * @param events  events it produced
     */
    public record JournalEntry(int step, Command command, List<Event> events) {
    }

    private final Network network;
    private final List<JournalEntry> journal = new ArrayList<>();
    private final List<EventObserver> observers = new ArrayList<>();
    private int step;
    private int rejected;
    private EndReason endReason;

    /**
     * @param network initial state, owned by the engine from now on
     */
    public SimulationEngine(Network network) {
        this.network = network;
    }

    /** @return the current state (read it through {@link dating.model.NetworkView}) */
    public Network network() {
        return network;
    }

    /**
     * Registers an observer; it receives all later events.
     *
     * @param observer observer to add
     */
    public void subscribe(EventObserver observer) {
        observers.add(observer);
    }

    /**
     * Validates and applies a command.
     *
     * @param command the move
     * @return events produced by the move
     * @throws IllegalActionException if the move breaks a rule (the state is unchanged)
     * @throws IllegalStateException  if the simulation is already finished
     */
    public List<Event> apply(Command command) {
        if (endReason != null) {
            throw new IllegalStateException("Simulation already ended: " + endReason);
        }
        command.validate(network);
        List<Event> events = List.copyOf(command.execute(network));
        step++;
        journal.add(new JournalEntry(step, command, events));
        events.forEach(this::publish);
        return events;
    }

    /**
     * Like {@link #apply(Command)} but an illegal move does not throw: it is reported as
     * an {@link Event.ActionRejected} event and is not written to the journal.
     *
     * @param command the move
     * @return events produced, or a single rejection event
     */
    public List<Event> submit(Command command) {
        try {
            return apply(command);
        } catch (IllegalActionException ex) {
            rejected++;
            Event event = new Event.ActionRejected(command, ex.getMessage());
            publish(event);
            return List.of(event);
        }
    }

    /**
     * Ends the simulation; later calls have no effect.
     *
     * @param reason why it ended
     */
    public void finish(EndReason reason) {
        if (endReason == null) {
            endReason = reason;
            publish(new Event.SimulationEnded(reason, step));
        }
    }

    /** @return number of accepted commands */
    public int step() {
        return step;
    }

    /** @return number of commands rejected by {@link #submit(Command)} */
    public int rejectedCount() {
        return rejected;
    }

    /** @return {@code true} once {@link #finish(EndReason)} was called */
    public boolean isFinished() {
        return endReason != null;
    }

    /** @return the end reason, if the simulation is finished */
    public Optional<EndReason> endReason() {
        return Optional.ofNullable(endReason);
    }

    /** @return unmodifiable journal of accepted commands */
    public List<JournalEntry> journal() {
        return Collections.unmodifiableList(journal);
    }

    private void publish(Event event) {
        for (EventObserver observer : observers) {
            observer.onEvent(event);
        }
    }
}
