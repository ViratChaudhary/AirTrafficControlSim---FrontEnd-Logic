package towersim.control;

import towersim.aircraft.Aircraft;
import towersim.aircraft.PassengerAircraft;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a rule-based queue of aircraft waiting in the air to land.
 * The rules in the landing queue are designed to ensure that aircraft are prioritised for landing
 * based on "urgency" factors such as remaining fuel onboard, emergency status and cargo type.
 */
public class LandingQueue extends AircraftQueue {

    /**
     * The landing queue which contains aircrafts preparing to land
     */
    private List<Aircraft> landingQueue;

    /**
     * Constructs a new LandingQueue with an initially empty queue of aircraft.
     */
    public LandingQueue() {
        landingQueue = new ArrayList<Aircraft>();
    }

    /**
     * Adds the given aircraft to the queue.
     *
     * @param aircraft aircraft to add to queue
     */
    public void addAircraft(Aircraft aircraft) {
        this.landingQueue.add(aircraft);
    }

    /**
     * Returns the aircraft at the front of the queue without removing it from the queue,
     * or null if the queue is empty.
     *
     * @return aircraft at front of queue
     */
    public Aircraft peekAircraft() {
        if (this.landingQueue.size() == 0) {
            return null;
        }

        /*
        checks based on priority, emergency comes first, then low fuel aircrafts,
        then passenger aircrafts and finally if nothing is returned then the first
        aircraft in the queue.
         */

        for (Aircraft aircraft : this.landingQueue) {
            if (aircraft.hasEmergency()) {
                return aircraft;
            }
        }

        for (Aircraft aircraft : this.landingQueue) {
            if (aircraft.getFuelPercentRemaining() <= 20) {
                return aircraft;
            }
        }

        for (Aircraft aircraft : this.landingQueue) {
            if (aircraft instanceof PassengerAircraft) {
                return aircraft;
            }
        }
        return this.landingQueue.get(0);
    }

    /**
     * Removes and returns the aircraft at the front of the queue.
     * Returns null if the queue is empty.
     *
     * @return aircraft at front of queue
     */
    public Aircraft removeAircraft() {
        if (this.landingQueue.size() == 0) {
            return null;
        }

        int positionOfAircraft = this.landingQueue.indexOf(this.peekAircraft());
        return this.landingQueue.remove(positionOfAircraft);
    }

    /**
     * Returns a list containing all aircraft in the queue, in order.
     *
     * @return list of all aircraft in queue, in queue order
     */
    public List<Aircraft> getAircraftInOrder() {
        if (this.landingQueue.size() == 0) {
            return this.landingQueue;
        }

        // creates a copy of the landing queue and an empty ordered queue
        List<Aircraft> landingQueue2 = new ArrayList<Aircraft>(this.landingQueue);
        List<Aircraft> orderedAircraft = new ArrayList<Aircraft>();

        // completes the ordered queue by removing from the original queue
        for (int i = 0; i < landingQueue2.size(); i++) {
            orderedAircraft.add(this.removeAircraft());
        }

        // replaces the original queue back to its normal state using the copy made
        this.landingQueue = landingQueue2;
        return orderedAircraft;
    }

    /**
     * Returns true if the given aircraft is in the queue.
     *
     * @param aircraft aircraft to find in queue
     * @return true if aircraft is in queue; false otherwise
     */
    public boolean containsAircraft(Aircraft aircraft) {
        for (Aircraft ac : this.landingQueue) {
            if (ac.equals(aircraft)) {
                return true;
            }
        }
        return false;
    }
}
