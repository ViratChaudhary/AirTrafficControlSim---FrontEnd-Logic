package towersim.control;

import towersim.aircraft.Aircraft;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a first-in-first-out (FIFO) queue of aircraft waiting to take off.
 * FIFO ensures that the order in which aircraft are allowed to take off is based
 * on long they have been waiting in the queue. An aircraft that has been waiting
 * for longer than another aircraft will always be allowed to take off before the other aircraft.
 */
public class TakeoffQueue extends AircraftQueue {

    /**
     * The takeoff queue which contains aircrafts preparing to takeoff
     */
    private List<Aircraft> takeoffQueue;

    /**
     * Constructs a new TakeoffQueue with an initially empty queue of aircraft.
     */
    public TakeoffQueue() {
        takeoffQueue = new ArrayList<Aircraft>();
    }

    /**
     * Adds the given aircraft to the queue.
     *
     * @param aircraft aircraft to add to queue
     */
    public void addAircraft(Aircraft aircraft) {
        this.takeoffQueue.add(aircraft);
    }

    /**
     * Returns the aircraft at the front of the queue without removing it from the queue,
     * or null if the queue is empty.
     *
     * @return aircraft at front of queue
     */
    public Aircraft peekAircraft() {
        if (this.takeoffQueue.size() == 0) {
            return null;
        }
        return this.takeoffQueue.get(0);
    }

    /**
     * Removes and returns the aircraft at the front of the queue.
     * Returns null if the queue is empty.
     *
     * @return aircraft at front of queue
     */
    public Aircraft removeAircraft() {
        if (this.takeoffQueue.size() == 0) {
            return null;
        }
        return this.takeoffQueue.remove(0);
    }

    /**
     * Returns a list containing all aircraft in the queue, in order.
     *
     * @return list of all aircraft in queue, in queue order
     */
    public List<Aircraft> getAircraftInOrder() {
        return new ArrayList<>(this.takeoffQueue);
    }

    /**
     * Returns true if the given aircraft is in the queue.
     *
     * @param aircraft aircraft to find in queue
     * @return true if aircraft is in queue; false otherwise
     */
    public boolean containsAircraft(Aircraft aircraft) {
        for (Aircraft ac : this.takeoffQueue) {
            if (ac.equals(aircraft)) {
                return true;
            }
        }
        return false;
    }
}
