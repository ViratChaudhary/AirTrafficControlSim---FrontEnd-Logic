package towersim.control;

import org.junit.Before;
import org.junit.Test;
import towersim.aircraft.Aircraft;
import towersim.aircraft.AircraftCharacteristics;
import towersim.aircraft.FreightAircraft;
import towersim.aircraft.PassengerAircraft;
import towersim.tasks.Task;
import towersim.tasks.TaskList;
import towersim.tasks.TaskType;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

public class LandingQueueTest {

    private LandingQueue landingQueue;

    private Aircraft emergencyAircraft1;
    private Aircraft emergencyAircraft2;
    private Aircraft lowFuelAircraft1;
    private Aircraft lowFuelAircraft2;
    private Aircraft passengerAircraft1;
    private Aircraft passengerAircraft2;
    private Aircraft freightAircraft1;
    private Aircraft freightAircraft2;

    @Before
    public void setup() {
        landingQueue = new LandingQueue();

        TaskList taskList = new TaskList(List.of(
                new Task(TaskType.AWAY),
                new Task(TaskType.LAND),
                new Task(TaskType.LOAD),
                new Task(TaskType.TAKEOFF)));

        // Generate Emergency Aircraft
        emergencyAircraft1 = new PassengerAircraft("ABC101",
                                AircraftCharacteristics.AIRBUS_A320,
                taskList, AircraftCharacteristics.AIRBUS_A320.fuelCapacity / 2,
                                AircraftCharacteristics.AIRBUS_A320.passengerCapacity / 2);
        emergencyAircraft1.declareEmergency();

        emergencyAircraft2 = new FreightAircraft("CBA101",
                AircraftCharacteristics.SIKORSKY_SKYCRANE,
                taskList, AircraftCharacteristics.SIKORSKY_SKYCRANE.fuelCapacity / 2,
                AircraftCharacteristics.SIKORSKY_SKYCRANE.freightCapacity / 2);
        emergencyAircraft2.declareEmergency();

        // Generates Low Fuel Aircraft
        lowFuelAircraft1 = new PassengerAircraft("DEF102",
                                AircraftCharacteristics.BOEING_787,
                taskList, AircraftCharacteristics.BOEING_787.fuelCapacity / 5,
                                AircraftCharacteristics.BOEING_787.passengerCapacity / 2);

        lowFuelAircraft2 = new FreightAircraft("FED201",
                AircraftCharacteristics.BOEING_747_8F,
                taskList, AircraftCharacteristics.BOEING_747_8F.fuelCapacity / 6,
                AircraftCharacteristics.BOEING_747_8F.freightCapacity / 2);

        // Generates Passenger Aircrafts
        passengerAircraft1 = new PassengerAircraft("GHI103",
                                AircraftCharacteristics.ROBINSON_R44,
                taskList, AircraftCharacteristics.ROBINSON_R44.fuelCapacity / 2,
                                AircraftCharacteristics.ROBINSON_R44.passengerCapacity / 2);

        passengerAircraft2 = new PassengerAircraft("JKL104",
                                AircraftCharacteristics.FOKKER_100,
                taskList, AircraftCharacteristics.FOKKER_100.fuelCapacity / 2,
                                AircraftCharacteristics.FOKKER_100.passengerCapacity / 2);

        // Generates Any Other Aircrafts needed for testing
        freightAircraft1 = new FreightAircraft("MNO105", AircraftCharacteristics.BOEING_747_8F,
                taskList, AircraftCharacteristics.BOEING_747_8F.fuelCapacity / 2,
                                AircraftCharacteristics.BOEING_747_8F.freightCapacity / 2);

        freightAircraft2 = new FreightAircraft("PKR106", AircraftCharacteristics.SIKORSKY_SKYCRANE,
                taskList, AircraftCharacteristics.SIKORSKY_SKYCRANE.fuelCapacity / 2,
                                AircraftCharacteristics.SIKORSKY_SKYCRANE.freightCapacity / 2);
    }

    @Test
    public void peekAircraftEmptyQueueTest() {
        assertNull("This queue is empty", landingQueue.peekAircraft());
    }

    @Test
    public void peekAircraftSingleEmergencyAircraftTest() {
        landingQueue.addAircraft(emergencyAircraft1);
        landingQueue.addAircraft(passengerAircraft1);
        landingQueue.addAircraft(passengerAircraft2);
        landingQueue.addAircraft(freightAircraft1);
        landingQueue.addAircraft(freightAircraft2);
        assertEquals("An Emergency Aircraft should have priority", emergencyAircraft1, landingQueue.peekAircraft());
    }

    @Test
    public void peekAircraftSingleEmergencyAircraftUnorderedTest() {
        landingQueue.addAircraft(freightAircraft1);
        landingQueue.addAircraft(passengerAircraft1);
        landingQueue.addAircraft(emergencyAircraft1);
        landingQueue.addAircraft(freightAircraft2);
        landingQueue.addAircraft(passengerAircraft2);
        assertEquals("An Emergency Aircraft should have priority", emergencyAircraft1, landingQueue.peekAircraft());
    }

    @Test
    public void peekAircraftMultipleEmergencyAircraftTest() {
        landingQueue.addAircraft(emergencyAircraft1);
        landingQueue.addAircraft(emergencyAircraft2);
        landingQueue.addAircraft(passengerAircraft1);
        landingQueue.addAircraft(passengerAircraft2);
        landingQueue.addAircraft(freightAircraft1);
        landingQueue.addAircraft(freightAircraft2);
        assertEquals("An Emergency Aircraft should have priority", emergencyAircraft1, landingQueue.peekAircraft());
    }

    @Test
    public void peekAircraftMultipleEmergencyAircraftUnorderedTest() {
        landingQueue.addAircraft(passengerAircraft1);
        landingQueue.addAircraft(emergencyAircraft2);
        landingQueue.addAircraft(freightAircraft1);
        landingQueue.addAircraft(passengerAircraft2);
        landingQueue.addAircraft(emergencyAircraft1);
        landingQueue.addAircraft(freightAircraft2);
        assertEquals("An Emergency Aircraft should have priority", emergencyAircraft2, landingQueue.peekAircraft());
    }

    @Test
    public void peekAircraftSingleLowFuelAircraftTest() {
        landingQueue.addAircraft(lowFuelAircraft1);
        landingQueue.addAircraft(passengerAircraft1);
        landingQueue.addAircraft(passengerAircraft2);
        landingQueue.addAircraft(freightAircraft1);
        landingQueue.addAircraft(freightAircraft2);
        assertEquals("An Emergency Aircraft should have priority", lowFuelAircraft1, landingQueue.peekAircraft());
    }

    @Test
    public void peekAircraftSingleLowFuelAircraftUnorderedTest() {
        landingQueue.addAircraft(freightAircraft1);
        landingQueue.addAircraft(passengerAircraft1);
        landingQueue.addAircraft(lowFuelAircraft1);
        landingQueue.addAircraft(freightAircraft2);
        landingQueue.addAircraft(passengerAircraft2);
        assertEquals("An Emergency Aircraft should have priority", lowFuelAircraft1, landingQueue.peekAircraft());
    }

    @Test
    public void peekAircraftMultipleLowFuelAircraftTest() {
        landingQueue.addAircraft(lowFuelAircraft1);
        landingQueue.addAircraft(lowFuelAircraft2);
        landingQueue.addAircraft(passengerAircraft1);
        landingQueue.addAircraft(passengerAircraft2);
        landingQueue.addAircraft(freightAircraft1);
        landingQueue.addAircraft(freightAircraft2);
        assertEquals("An Emergency Aircraft should have priority", lowFuelAircraft1, landingQueue.peekAircraft());
    }

    @Test
    public void peekAircraftMultipleLowFuelAircraftUnorderedTest() {
        landingQueue.addAircraft(passengerAircraft1);
        landingQueue.addAircraft(lowFuelAircraft2);
        landingQueue.addAircraft(freightAircraft1);
        landingQueue.addAircraft(passengerAircraft2);
        landingQueue.addAircraft(lowFuelAircraft1);
        landingQueue.addAircraft(freightAircraft2);
        assertEquals("An Emergency Aircraft should have priority", lowFuelAircraft2, landingQueue.peekAircraft());
    }

    @Test
    public void peekAircraftSinglePassengerAircraftTest() {
        landingQueue.addAircraft(passengerAircraft1);
        landingQueue.addAircraft(freightAircraft1);
        landingQueue.addAircraft(freightAircraft2);
        assertEquals("An Emergency Aircraft should have priority", passengerAircraft1, landingQueue.peekAircraft());
    }

    @Test
    public void peekAircraftSinglePassengerAircraftUnorderedTest() {
        landingQueue.addAircraft(freightAircraft1);
        landingQueue.addAircraft(passengerAircraft2);
        landingQueue.addAircraft(freightAircraft2);
        assertEquals("An Emergency Aircraft should have priority", passengerAircraft2, landingQueue.peekAircraft());
    }

    @Test
    public void peekAircraftMultiplePassengerAircraftTest() {
        landingQueue.addAircraft(passengerAircraft1);
        landingQueue.addAircraft(passengerAircraft2);
        landingQueue.addAircraft(freightAircraft1);
        landingQueue.addAircraft(freightAircraft2);
        assertEquals("An Emergency Aircraft should have priority", passengerAircraft1, landingQueue.peekAircraft());
    }

    @Test
    public void peekAircraftMultiplePassengerAircraftUnorderedTest() {
        landingQueue.addAircraft(freightAircraft1);
        landingQueue.addAircraft(passengerAircraft2);
        landingQueue.addAircraft(freightAircraft2);
        landingQueue.addAircraft(passengerAircraft1);
        assertEquals("An Emergency Aircraft should have priority", passengerAircraft2, landingQueue.peekAircraft());
    }

    @Test
    public void peekAircraftEndPointReachedTest() {
        landingQueue.addAircraft(freightAircraft1);
        landingQueue.addAircraft(freightAircraft2);
        assertEquals("An Emergency Aircraft should have priority", freightAircraft1, landingQueue.peekAircraft());
    }

    @Test
    public void peekAircraftEmergencyPriorityLastPlaceTest() {
        landingQueue.addAircraft(lowFuelAircraft1);
        landingQueue.addAircraft(passengerAircraft2);
        landingQueue.addAircraft(lowFuelAircraft2);
        landingQueue.addAircraft(passengerAircraft1);
        landingQueue.addAircraft(emergencyAircraft1);
        assertEquals("", emergencyAircraft1, landingQueue.peekAircraft());
    }

    @Test
    public void peekAircraftEmergencyPriorityUnorderedTest() {
        landingQueue.addAircraft(lowFuelAircraft1);
        landingQueue.addAircraft(passengerAircraft1);
        landingQueue.addAircraft(freightAircraft1);
        landingQueue.addAircraft(emergencyAircraft2);
        landingQueue.addAircraft(passengerAircraft1);
        assertEquals("", emergencyAircraft2, landingQueue.peekAircraft());
    }

    @Test
    public void peekAircraftLowFuelPriorityLastPlaceTest() {
        landingQueue.addAircraft(passengerAircraft1);
        landingQueue.addAircraft(passengerAircraft2);
        landingQueue.addAircraft(freightAircraft1);
        landingQueue.addAircraft(lowFuelAircraft1);
        assertEquals("", lowFuelAircraft1, landingQueue.peekAircraft());
    }

    @Test
    public void peekAircraftLowFuelPriorityUnorderedTest() {
        landingQueue.addAircraft(passengerAircraft1);
        landingQueue.addAircraft(passengerAircraft2);
        landingQueue.addAircraft(lowFuelAircraft2);
        landingQueue.addAircraft(freightAircraft1);
        assertEquals("", lowFuelAircraft2, landingQueue.peekAircraft());
    }

    @Test
    public void peekAircraftPassengerPriorityLastPlaceTest() {
        landingQueue.addAircraft(freightAircraft1);
        landingQueue.addAircraft(freightAircraft2);
        landingQueue.addAircraft(passengerAircraft1);
        assertEquals("", passengerAircraft1, landingQueue.peekAircraft());
    }

    @Test
    public void peekAircraftAllTypesTest() {
        landingQueue.addAircraft(passengerAircraft1);
        landingQueue.addAircraft(freightAircraft2);
        landingQueue.addAircraft(lowFuelAircraft1);
        landingQueue.addAircraft(freightAircraft1);
        landingQueue.addAircraft(emergencyAircraft1);
        landingQueue.addAircraft(emergencyAircraft2);
        landingQueue.addAircraft(lowFuelAircraft2);
        landingQueue.addAircraft(passengerAircraft2);
        assertEquals("", emergencyAircraft1, landingQueue.peekAircraft());
    }

    @Test
    public void peekAircraftAllTypesTest2() {
        landingQueue.addAircraft(passengerAircraft1);
        landingQueue.addAircraft(freightAircraft2);
        landingQueue.addAircraft(emergencyAircraft2);
        landingQueue.addAircraft(lowFuelAircraft1);
        landingQueue.addAircraft(freightAircraft1);
        landingQueue.addAircraft(emergencyAircraft1);
        landingQueue.addAircraft(lowFuelAircraft2);
        landingQueue.addAircraft(passengerAircraft2);
        assertEquals("", emergencyAircraft2, landingQueue.peekAircraft());
    }

    @Test
    public void removeAircraftEmptyQueue() {
        assertNull("This queue is empty", landingQueue.removeAircraft());
    }

    @Test
    public void removeEmergencyAircraftSingleTest() {
        landingQueue.addAircraft(freightAircraft1);
        landingQueue.addAircraft(passengerAircraft1);
        landingQueue.addAircraft(lowFuelAircraft2);
        landingQueue.addAircraft(emergencyAircraft1);
        assertEquals("", emergencyAircraft1, landingQueue.removeAircraft());
        assertEquals("", 3, landingQueue.getAircraftInOrder().size());
    }

    @Test
    public void removeEmergencyAircraftMultipleTest() {
        landingQueue.addAircraft(freightAircraft1);
        landingQueue.addAircraft(passengerAircraft1);
        landingQueue.addAircraft(emergencyAircraft2);
        landingQueue.addAircraft(lowFuelAircraft2);
        landingQueue.addAircraft(emergencyAircraft1);
        assertEquals("", emergencyAircraft2, landingQueue.removeAircraft());
        assertEquals("", 4, landingQueue.getAircraftInOrder().size());
    }

    @Test
    public void removeLowFuelAircraftSingleTest() {
        landingQueue.addAircraft(freightAircraft1);
        landingQueue.addAircraft(passengerAircraft1);
        landingQueue.addAircraft(lowFuelAircraft2);
        assertEquals("", lowFuelAircraft2, landingQueue.removeAircraft());
        assertEquals("", 2, landingQueue.getAircraftInOrder().size());
    }

    @Test
    public void removeLowFuelAircraftMultipleTest() {
        landingQueue.addAircraft(freightAircraft1);
        landingQueue.addAircraft(passengerAircraft1);
        landingQueue.addAircraft(lowFuelAircraft1);
        landingQueue.addAircraft(passengerAircraft2);
        landingQueue.addAircraft(lowFuelAircraft2);
        landingQueue.addAircraft(freightAircraft2);
        assertEquals("", lowFuelAircraft1, landingQueue.removeAircraft());
        assertEquals("", 5, landingQueue.getAircraftInOrder().size());
    }

    @Test
    public void removePassengerAircraftSingleTest() {
        landingQueue.addAircraft(freightAircraft2);
        landingQueue.addAircraft(freightAircraft1);
        landingQueue.addAircraft(passengerAircraft1);
        assertEquals("", passengerAircraft1, landingQueue.removeAircraft());
        assertEquals("", 2, landingQueue.getAircraftInOrder().size());
    }

    @Test
    public void removePassengerAircraftMultipleTest() {
        landingQueue.addAircraft(freightAircraft2);
        landingQueue.addAircraft(passengerAircraft2);
        landingQueue.addAircraft(freightAircraft1);
        landingQueue.addAircraft(passengerAircraft1);
        assertEquals("", passengerAircraft2, landingQueue.removeAircraft());
        assertEquals("", 3, landingQueue.getAircraftInOrder().size());
    }

    @Test
    public void removeFirstAircraftTest() {
        landingQueue.addAircraft(freightAircraft1);
        landingQueue.addAircraft(freightAircraft2);
        assertEquals("", freightAircraft1, landingQueue.removeAircraft());
        assertEquals("", 1, landingQueue.getAircraftInOrder().size());
    }

    @Test
    public void getAircraftInOrderEmptyQueue() {
        List<Aircraft> expected = new ArrayList<Aircraft>();
        assertEquals("", expected, landingQueue.getAircraftInOrder());
    }

    @Test
    public void getAircraftInOrderEmergencyPriorityTest() {
        landingQueue.addAircraft(passengerAircraft2);
        landingQueue.addAircraft(emergencyAircraft1);
        landingQueue.addAircraft(freightAircraft2);
        landingQueue.addAircraft(passengerAircraft1);
        landingQueue.addAircraft(emergencyAircraft2);
        landingQueue.addAircraft(lowFuelAircraft1);
        landingQueue.addAircraft(lowFuelAircraft2);

        List<Aircraft> expected = new ArrayList<Aircraft>();
        expected.add(emergencyAircraft1);
        expected.add(emergencyAircraft2);
        expected.add(lowFuelAircraft1);
        expected.add(lowFuelAircraft2);
        expected.add(passengerAircraft2);
        expected.add(passengerAircraft1);
        expected.add(freightAircraft2);

        assertEquals("Incorrect Order", expected, landingQueue.getAircraftInOrder());
    }

    @Test
    public void getAircraftInOrderLowFuelPriorityTest() {
        landingQueue.addAircraft(passengerAircraft2);
        landingQueue.addAircraft(lowFuelAircraft1);
        landingQueue.addAircraft(freightAircraft2);
        landingQueue.addAircraft(passengerAircraft1);
        landingQueue.addAircraft(freightAircraft1);
        landingQueue.addAircraft(lowFuelAircraft2);

        List<Aircraft> expected = new ArrayList<Aircraft>();
        expected.add(lowFuelAircraft1);
        expected.add(lowFuelAircraft2);
        expected.add(passengerAircraft2);
        expected.add(passengerAircraft1);
        expected.add(freightAircraft2);
        expected.add(freightAircraft1);

        assertEquals("Incorrect Order", expected, landingQueue.getAircraftInOrder());
    }

    @Test
    public void getAircraftInOrderPassengerPriorityTest() {
        landingQueue.addAircraft(passengerAircraft2);
        landingQueue.addAircraft(freightAircraft2);
        landingQueue.addAircraft(passengerAircraft1);
        landingQueue.addAircraft(freightAircraft1);

        List<Aircraft> expected = new ArrayList<Aircraft>();
        expected.add(passengerAircraft2);
        expected.add(passengerAircraft1);
        expected.add(freightAircraft2);
        expected.add(freightAircraft1);

        assertEquals("Incorrect Order", expected, landingQueue.getAircraftInOrder());
    }

    @Test
    public void getAircraftInOrderAllTypesTest() {
        landingQueue.addAircraft(passengerAircraft1);
        landingQueue.addAircraft(freightAircraft1);
        landingQueue.addAircraft(emergencyAircraft1);
        landingQueue.addAircraft(passengerAircraft2);
        landingQueue.addAircraft(lowFuelAircraft1);
        landingQueue.addAircraft(emergencyAircraft2);
        landingQueue.addAircraft(lowFuelAircraft2);

        List<Aircraft> expected = new ArrayList<Aircraft>();
        expected.add(emergencyAircraft1);
        expected.add(emergencyAircraft2);
        expected.add(lowFuelAircraft1);
        expected.add(lowFuelAircraft2);
        expected.add(passengerAircraft1);
        expected.add(passengerAircraft2);
        expected.add(freightAircraft1);

        assertEquals("Incorrect Order", expected, landingQueue.getAircraftInOrder());
    }

    @Test
    public void containsAircraftFirstValidAircraftInQueueTest() {
        landingQueue.addAircraft(emergencyAircraft1);
        landingQueue.addAircraft(lowFuelAircraft1);
        landingQueue.addAircraft(passengerAircraft1);
        landingQueue.addAircraft(freightAircraft1);
        assertTrue("This aircraft belongs to the queue", landingQueue.containsAircraft(emergencyAircraft1));
    }

    @Test
    public void containsAircraftRandomValidAircraftInQueueTest() {
        landingQueue.addAircraft(emergencyAircraft2);
        landingQueue.addAircraft(lowFuelAircraft2);
        landingQueue.addAircraft(passengerAircraft2);
        landingQueue.addAircraft(freightAircraft2);
        assertTrue("This aircraft belongs to the queue", landingQueue.containsAircraft(passengerAircraft2));
    }

    @Test
    public void containsAircraftInValidAircraftInQueueTest() {
        landingQueue.addAircraft(emergencyAircraft1);
        landingQueue.addAircraft(lowFuelAircraft1);
        landingQueue.addAircraft(passengerAircraft1);
        landingQueue.addAircraft(freightAircraft1);
        assertFalse("This aircraft does not belongs to the queue", landingQueue.containsAircraft(emergencyAircraft2));
    }
}
