package towersim.control;

import towersim.aircraft.Aircraft;
import towersim.aircraft.AircraftType;
import towersim.ground.AirplaneTerminal;
import towersim.ground.Gate;
import towersim.ground.HelicopterTerminal;
import towersim.ground.Terminal;
import towersim.tasks.TaskType;
import towersim.util.NoSpaceException;
import towersim.util.NoSuitableGateException;
import towersim.util.Tickable;

import java.util.*;

/**
 * Represents a the control tower of an airport.
 * <p>
 * The control tower is responsible for managing the operations of the airport, including arrivals
 * and departures in/out of the airport, as well as aircraft that need to be loaded with cargo
 * at gates in terminals.
 *
 * @ass1
 */
public class ControlTower implements Tickable {
    /**
     * List of all aircraft managed by the control tower.
     */
    private final List<Aircraft> aircraft;

    /**
     * List of all terminals in the airport.
     */
    private final List<Terminal> terminals;

    /**
     * number of ticks that have elapsed since the tower was first created
     */
    private long ticksElapsed;

    /**
     * number of ticks that have occurred when control tower was created
     */
    private long ticksAtCommencement;

    /**
     * queue of aircraft waiting to land
     */
    private LandingQueue landingQueue;

    /**
     * queue of aircraft waiting to take off
     */
    private TakeoffQueue takeoffQueue;

    /**
     * mapping of aircraft that are loading cargo to the number of ticks remaining for loading
     */
    private Map<Aircraft, Integer> loadingAircraft;

    /**
     * Creates a new ControlTower.
     * The number of ticks elapsed, list of aircraft, landing queue,
     * takeoff queue and map of loading aircraft to loading times should
     * all be set to the values passed as parameters.
     * The list of terminals should be initialised as an empty list.
     *
     * @param ticksElapsed    number of ticks that have elapsed since the tower was first create
     * @param aircraft        aircraft managed by control tower
     * @param landingQueue    queue of aircraft waiting to land
     * @param takeoffQueue    queue of aircraft waiting to take off
     * @param loadingAircraft mapping of aircraft that are loading cargo to loading ticks remaining
     */
    public ControlTower(long ticksElapsed, List<Aircraft> aircraft, LandingQueue landingQueue,
                        TakeoffQueue takeoffQueue, Map<Aircraft, Integer> loadingAircraft) {

        this.ticksElapsed = ticksElapsed;
        this.ticksAtCommencement = ticksElapsed;
        this.aircraft = aircraft;
        this.landingQueue = landingQueue;
        this.takeoffQueue = takeoffQueue;
        this.loadingAircraft = loadingAircraft;
        this.terminals = new ArrayList<>();
    }

    /**
     * Adds the given terminal to the jurisdiction of this control tower.
     *
     * @param terminal terminal to add
     * @ass1
     */
    public void addTerminal(Terminal terminal) {
        this.terminals.add(terminal);
    }

    /**
     * Returns a list of all terminals currently managed by this control tower.
     * <p>
     * The order in which terminals appear in this list should be the same as the order in which
     * they were added by calling {@link #addTerminal(Terminal)}.
     * <p>
     * Adding or removing elements from the returned list should not affect the original list.
     *
     * @return all terminals
     * @ass1
     */
    public List<Terminal> getTerminals() {
        return new ArrayList<>(this.terminals);
    }

    /**
     * Adds the given aircraft to the jurisdiction of this control tower.
     * <p>
     * If the aircraft's current task type is {@code WAIT} or {@code LOAD}, it should be parked at a
     * suitable gate as found by the {@link #findUnoccupiedGate(Aircraft)} method.
     * If there is no suitable gate for the aircraft, the {@code NoSuitableGateException} thrown by
     * {@code findUnoccupiedGate()} should be propagated out of this method.
     * After the aircraft has been added, it should be placed in the appropriate queues
     * by calling placeAircraftInQueues(Aircraft).
     *
     * @param aircraft aircraft to add
     * @throws NoSuitableGateException if there is no suitable gate for an aircraft with a current
     *                                 task type of {@code WAIT} or {@code LOAD}
     * @ass1
     */
    public void addAircraft(Aircraft aircraft) throws NoSuitableGateException {
        TaskType currentTaskType = aircraft.getTaskList().getCurrentTask().getType();
        if (currentTaskType == TaskType.WAIT || currentTaskType == TaskType.LOAD) {
            Gate gate = findUnoccupiedGate(aircraft);
            try {
                gate.parkAircraft(aircraft);
            } catch (NoSpaceException ignored) {
                // not possible, gate unoccupied
            }
        }
        this.aircraft.add(aircraft);
        placeAircraftInQueues(aircraft);
    }

    /**
     * Returns a list of all aircraft currently managed by this control tower.
     * <p>
     * The order in which aircraft appear in this list should be the same as the order in which
     * they were added by calling {@link #addAircraft(Aircraft)}.
     * <p>
     * Adding or removing elements from the returned list should not affect the original list.
     *
     * @return all aircraft
     * @ass1
     */
    public List<Aircraft> getAircraft() {
        return new ArrayList<>(this.aircraft);
    }

    /**
     * Attempts to find an unoccupied gate in a compatible terminal for the given aircraft.
     * <p>
     * Only terminals of the same type as the aircraft's AircraftType (see
     * {@link towersim.aircraft.AircraftCharacteristics#type}) should be considered. For example,
     * for an aircraft with an AircraftType of {@code AIRPLANE}, only AirplaneTerminals may be
     * considered.
     * <p>
     * For each compatible terminal, the {@link Terminal#findUnoccupiedGate()} method should be
     * called to attempt to find an unoccupied gate in that terminal. If
     * {@code findUnoccupiedGate()} does not find a suitable gate, the next compatible terminal
     * in the order they were added should be checked instead, and so on.
     * <p>
     * If no unoccupied gates could be found across all compatible terminals, a
     * {@code NoSuitableGateException} should be thrown.
     *
     * @param aircraft aircraft for which to find gate
     * @return gate for given aircraft if one exists
     * @throws NoSuitableGateException if no suitable gate could be found
     * @ass1
     */
    public Gate findUnoccupiedGate(Aircraft aircraft) throws NoSuitableGateException {
        AircraftType aircraftType = aircraft.getCharacteristics().type;
        for (Terminal terminal : terminals) {
            /*
             * Only check for available gates at terminals that are of the same aircraft type as
             * the aircraft and ensure the terminal does not have a current emergency
             */
            if ((terminal instanceof AirplaneTerminal && aircraftType == AircraftType.AIRPLANE
                    && !terminal.hasEmergency())
                    || (terminal instanceof HelicopterTerminal
                    && aircraftType == AircraftType.HELICOPTER && !terminal.hasEmergency())) {
                try {
                    // This terminal found a gate, return it
                    return terminal.findUnoccupiedGate();
                } catch (NoSuitableGateException e) {
                    // If this terminal has no unoccupied gates, try the next one
                }
            }
        }
        throw new NoSuitableGateException("No gate available for aircraft");
    }

    /**
     * Finds the gate where the given aircraft is parked, and returns null if the aircraft is
     * not parked at any gate in any terminal.
     *
     * @param aircraft aircraft whose gate to find
     * @return gate occupied by the given aircraft; or null if none exists
     * @ass1
     */
    public Gate findGateOfAircraft(Aircraft aircraft) {
        for (Terminal terminal : this.terminals) {
            for (Gate gate : terminal.getGates()) {
                if (Objects.equals(gate.getAircraftAtGate(), aircraft)) {
                    return gate;
                }
            }
        }
        return null;
    }

    /**
     * Returns the number of ticks that have elapsed for this control tower.
     *
     * @return number of ticks elapsed
     */
    public long getTicksElapsed() {
        return this.ticksElapsed;
    }

    /**
     * Returns the queue of aircraft waiting to land.
     *
     * @return landing queue
     */
    public AircraftQueue getLandingQueue() {
        return this.landingQueue;
    }

    /**
     * Returns the queue of aircraft waiting to take off.
     *
     * @return takeoff queue
     */
    public AircraftQueue getTakeoffQueue() {
        return this.takeoffQueue;
    }

    /**
     * Returns the mapping of loading aircraft to their remaining load times.
     *
     * @return loading aircraft map
     */
    public Map<Aircraft, Integer> getLoadingAircraft() {
        return this.loadingAircraft;
    }

    /**
     * Attempts to land one aircraft waiting in the landing queue and park it at a suitable gate.
     * If there are no aircraft in the landing queue waiting to land,
     * then the method should return false and no further action should be taken.
     *
     * @return true if an aircraft was successfully landed and parked; false otherwise
     */
    public boolean tryLandAircraft() {
        if (this.landingQueue.peekAircraft() == null) {
            return false;
        }

        try {
            Gate aircraftGate = this.findUnoccupiedGate(this.landingQueue.peekAircraft());
            Aircraft aircraftToLand = this.landingQueue.removeAircraft();
            aircraftGate.parkAircraft(aircraftToLand);
            aircraftToLand.unload();
            aircraftToLand.getTaskList().moveToNextTask();
        } catch (NoSuitableGateException e) {
            return false;
        } catch (NoSpaceException e) {
            // should not occur as the gate found by findUnoccupiedGate() is empty
        }
        return true;
    }

    /**
     * Attempts to allow one aircraft waiting in the takeoff queue to take off.
     * If there are no aircraft waiting in the takeoff queue, then the method should return.
     * Otherwise, the aircraft at the front of the takeoff queue should be removed from
     * the queue and it should move to the next task in its task list.
     */
    public void tryTakeOffAircraft() {
        if (this.takeoffQueue.peekAircraft() == null) {
            return;
        }
        Aircraft aircraftToTakeoff = this.takeoffQueue.removeAircraft();
        aircraftToTakeoff.getTaskList().moveToNextTask();
    }

    /**
     * Updates the time remaining to load on all currently loading aircraft and
     * removes aircraft from their gate once finished loading.
     * Any aircraft in the loading map should have their time remaining decremented by one tick.
     * If any aircraft's time remaining is now zero, it has finished loading and
     * should be removed from the loading map.
     * Additionally, it should leave the gate it is parked at and should move on to its next task.
     */
    public void loadAircraft() {
        Map<Aircraft, Integer> secondaryLoadingAircraft
                = new TreeMap<Aircraft, Integer>(Comparator.comparing(Aircraft::getCallsign));

        secondaryLoadingAircraft.putAll(this.loadingAircraft);

        for (Map.Entry<Aircraft, Integer> entry : secondaryLoadingAircraft.entrySet()) {
            int loadTicks = entry.getValue();
            loadTicks = loadTicks - 1;

            // decrements load time by one for each aircraft
            loadingAircraft.put(entry.getKey(), loadTicks);
            secondaryLoadingAircraft.put(entry.getKey(), loadTicks);

            if (loadTicks == 0) {
                loadingAircraft.remove(entry.getKey());
                findGateOfAircraft(entry.getKey()).aircraftLeaves();
                entry.getKey().getTaskList().moveToNextTask();
            }
        }
    }

    /**
     * Moves the given aircraft to the appropriate queue based on its current task.
     *
     *
     * @param aircraft aircraft to move to appropriate queue
     */
    public void placeAircraftInQueues(Aircraft aircraft) {
        TaskType currentAircraftTask = aircraft.getTaskList().getCurrentTask().getType();

        // if aircraft currently on LAND, then add to landing queue if not already in it
        if (currentAircraftTask == TaskType.LAND
                && !(this.landingQueue.containsAircraft(aircraft))) {
            this.landingQueue.addAircraft(aircraft);

        // if aircraft currently in TAKEOFF, then add to takeoff queue if not already in it
        } else if (currentAircraftTask == TaskType.TAKEOFF
                && !(this.takeoffQueue.containsAircraft(aircraft))) {
            this.takeoffQueue.addAircraft(aircraft);

        // if aircraft currently in LOAD, then add to loading Aircraft if not already in it
        } else if (currentAircraftTask == TaskType.LOAD
                && !(this.loadingAircraft.containsKey(aircraft))) {
            this.loadingAircraft.put(aircraft, aircraft.getLoadingTime());
        }
    }

    /**
     * Places all aircraft managed by the control tower to its appropriate location.
     */
    public void placeAllAircraftInQueues() {
        for (Aircraft aircraft : this.aircraft) {
            this.placeAircraftInQueues(aircraft);
        }
    }

    /**
     * Advances the simulation by one tick.
     * <p>
     * On each tick, the control tower should call {@link Aircraft#tick()} on all aircraft managed
     * by the control tower.
     *
     * On each tick, the control tower should perform the following actions in this order:
     *
     *      Call Aircraft.tick() on all aircraft.
     *      Move all aircraft with a current task type of AWAY or WAIT to their next task.
     *
     *      Process loading aircraft by calling loadAircraft().
     *
     *      On every second tick, attempt to land an aircraft by calling tryLandAircraft().
     *
     *      If an aircraft cannot be landed, attempt to allow an aircraft to take off instead
     *      by calling tryTakeOffAircraft().
     *
     *      Note that this begins from the second time tick() is called and
     *      every second tick thereafter.
     *
     *      If this is not a tick where the control tower is attempting to land an aircraft,
     *      an aircraft should be allowed to take off instead.
     *      This ensures that aircraft wishing to take off and
     *      land are given an equal share of the runway.
     *
     *      Place all aircraft in their appropriate queues by calling placeAllAircraftInQueues().
     *
     * @ass1
     */
    @Override
    public void tick() {
        // Call tick() on all other sub-entities
        for (Aircraft aircraft : this.aircraft) {

            aircraft.tick();

            // move all AWAY and WAIT aircraft to next task
            TaskType aircraftCurrentTask = aircraft.getTaskList().getCurrentTask().getType();
            if (aircraftCurrentTask == TaskType.AWAY || aircraftCurrentTask == TaskType.WAIT) {
                aircraft.getTaskList().moveToNextTask();
            }

            loadAircraft();

            // calculates how many ticks have occurred since creation of control tower and
            // determine whether landing or takeoff would occur based on if its every second day
            long ticksSinceCommencement = this.ticksElapsed - this.ticksAtCommencement;
            if (ticksSinceCommencement != 0 && ticksSinceCommencement % 2 == 0) {
                if (tryLandAircraft()) {
                    tryLandAircraft();
                } else {
                    tryTakeOffAircraft();
                }
            } else {
                tryTakeOffAircraft();
            }

            placeAllAircraftInQueues();

            this.ticksElapsed += 1;
        }
    }

    /**
     * Returns the human-readable string representation of this control tower.
     * The format of the string to return is
     * ControlTower: numTerminals terminals, numAircraft total aircraft
     * (numLanding LAND, numTakeoff TAKEOFF, numLoad LOAD)
     *
     * @return string representation of this control tower
     */
    @Override
    public String toString() {
        return String.format("ControlTower: %d terminals, %d total aircraft "
                             + "(%d LAND, %d TAKEOFF, %d LOAD)",
                this.getTerminals().size(),
                this.getAircraft().size(),
                this.landingQueue.getAircraftInOrder().size(),
                this.takeoffQueue.getAircraftInOrder().size(),
                this.loadingAircraft.size());
    }
}