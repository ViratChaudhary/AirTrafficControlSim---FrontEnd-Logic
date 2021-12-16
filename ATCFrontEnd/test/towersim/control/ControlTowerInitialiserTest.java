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
import towersim.util.MalformedSaveException;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.StringJoiner;

import static org.junit.Assert.*;

public class ControlTowerInitialiserTest {

    private TaskList taskList1;
    private TaskList taskList2;
    private TaskList taskList3;
    private TaskList taskList4;

    private Aircraft aircraft1;
    private Aircraft aircraft2;
    private Aircraft aircraft3;
    private Aircraft aircraft4;

    @Before
    public void setup() {

        taskList1 = new TaskList(List.of(
                new Task(TaskType.AWAY),
                new Task(TaskType.AWAY),
                new Task(TaskType.LAND),
                new Task(TaskType.WAIT),
                new Task(TaskType.WAIT),
                new Task(TaskType.LOAD, 60),
                new Task(TaskType.TAKEOFF),
                new Task(TaskType.AWAY)));

        taskList2 = new TaskList(List.of(
                new Task(TaskType.WAIT),
                new Task(TaskType.LOAD, 100),
                new Task(TaskType.TAKEOFF),
                new Task(TaskType.AWAY),
                new Task(TaskType.AWAY),
                new Task(TaskType.AWAY),
                new Task(TaskType.LAND)));

        taskList3 = new TaskList(List.of(
                new Task(TaskType.WAIT),
                new Task(TaskType.LOAD, 50),
                new Task(TaskType.TAKEOFF),
                new Task(TaskType.AWAY),
                new Task(TaskType.AWAY),
                new Task(TaskType.AWAY),
                new Task(TaskType.LAND)));

        taskList4 = new TaskList(List.of(
                new Task(TaskType.LAND),
                new Task(TaskType.WAIT),
                new Task(TaskType.LOAD, 75),
                new Task(TaskType.TAKEOFF),
                new Task(TaskType.AWAY),
                new Task(TaskType.AWAY)));

        aircraft1 = new PassengerAircraft("QFA481", AircraftCharacteristics.AIRBUS_A320,
                taskList1, 10000.00, 132);

        aircraft2 = new PassengerAircraft("UTD302", AircraftCharacteristics.BOEING_787,
                taskList2, 10000.00, 0);

        aircraft3 = new FreightAircraft("UPS119", AircraftCharacteristics.BOEING_747_8F,
                taskList3, 4000.00, 0);

        aircraft4 = new PassengerAircraft("VH-BFK", AircraftCharacteristics.ROBINSON_R44,
                taskList4, 40.00, 4);

        /*
        QFA481:AIRBUS_A320:AWAY,AWAY,LAND,WAIT,WAIT,LOAD@60,TAKEOFF,AWAY:10000.00:false:132
        UTD302:BOEING_787:WAIT,LOAD@100,TAKEOFF,AWAY,AWAY,AWAY,LAND:10000.00:false:0
        UPS119:BOEING_747_8F:WAIT,LOAD@50,TAKEOFF,AWAY,AWAY,AWAY,LAND:4000.00:false:0
        VH-BFK:ROBINSON_R44:LAND,WAIT,LOAD@75,TAKEOFF,AWAY,AWAY:40.00:false:4
         */
    }

    @Test
    public void loadAircraftValidInputsTest() {
        boolean expected = false;
        List<Aircraft> ac = null;

        List<Aircraft> expectedList = new ArrayList<>();
        expectedList.add(aircraft1);
        expectedList.add(aircraft2);
        expectedList.add(aircraft3);
        expectedList.add(aircraft4);

        StringJoiner joiner = new StringJoiner(System.lineSeparator());
        joiner.add("4");
        joiner.add("QFA481:AIRBUS_A320:AWAY,AWAY,LAND,WAIT,WAIT,LOAD@60,TAKEOFF,AWAY:10000.00:false:132");
        joiner.add("UTD302:BOEING_787:WAIT,LOAD@100,TAKEOFF,AWAY,AWAY,AWAY,LAND:10000.00:false:0");
        joiner.add("UPS119:BOEING_747_8F:WAIT,LOAD@50,TAKEOFF,AWAY,AWAY,AWAY,LAND:4000.00:false:0");
        joiner.add("VH-BFK:ROBINSON_R44:LAND,WAIT,LOAD@75,TAKEOFF,AWAY,AWAY:40.00:false:4");

        try {
            ac = ControlTowerInitialiser.loadAircraft(new StringReader(joiner.toString()));
        } catch (MalformedSaveException | IOException ex) {
            expected = true;
        }

        assertEquals("The aircraft list generated does not match the given input", expectedList, ac);
    }

    @Test
    public void loadAircraftInvalidIntegerTest() {
        boolean expected = false;

        StringJoiner joiner = new StringJoiner(System.lineSeparator());
        joiner.add("ABC");
        joiner.add("QFA481:AIRBUS_A320:AWAY,AWAY,LAND,WAIT,WAIT,LOAD@60,TAKEOFF,AWAY:10000.00:false:132");
        joiner.add("UTD302:BOEING_787:WAIT,LOAD@100,TAKEOFF,AWAY,AWAY,AWAY,LAND:10000.00:false:0");
        joiner.add("UPS119:BOEING_747_8F:WAIT,LOAD@50,TAKEOFF,AWAY,AWAY,AWAY,LAND:4000.00:false:0");
        joiner.add("VH-BFK:ROBINSON_R44:LAND,WAIT,LOAD@75,TAKEOFF,AWAY,AWAY:40.00:false:4");

        try {
            ControlTowerInitialiser.loadAircraft(new StringReader(joiner.toString()));
        } catch (MalformedSaveException | IOException ex) {
            expected = true;
        }

        assertTrue("Invalid Aircraft Count Integer", expected);
    }

    @Test
    public void loadAircraftBelowRequiredReadCount() {
        boolean expected = false;

        StringJoiner joiner = new StringJoiner(System.lineSeparator());
        joiner.add("3");
        joiner.add("QFA481:AIRBUS_A320:AWAY,AWAY,LAND,WAIT,WAIT,LOAD@60,TAKEOFF,AWAY:10000.00:false:132");
        joiner.add("UTD302:BOEING_787:WAIT,LOAD@100,TAKEOFF,AWAY,AWAY,AWAY,LAND:10000.00:false:0");
        joiner.add("UPS119:BOEING_747_8F:WAIT,LOAD@50,TAKEOFF,AWAY,AWAY,AWAY,LAND:4000.00:false:0");
        joiner.add("VH-BFK:ROBINSON_R44:LAND,WAIT,LOAD@75,TAKEOFF,AWAY,AWAY:40.00:false:4");

        try {
            ControlTowerInitialiser.loadAircraft(new StringReader(joiner.toString()));
        } catch (MalformedSaveException | IOException ex) {
            expected = true;
        }

        assertTrue("Invalid number of aircraft declaration to aircraft lines contained", expected);
    }

    @Test
    public void loadAircraftAboveRequiredReadCount() {
        boolean expected = false;

        StringJoiner joiner = new StringJoiner(System.lineSeparator());
        joiner.add("10");
        joiner.add("QFA481:AIRBUS_A320:AWAY,AWAY,LAND,WAIT,WAIT,LOAD@60,TAKEOFF,AWAY:10000.00:false:132");
        joiner.add("UTD302:BOEING_787:WAIT,LOAD@100,TAKEOFF,AWAY,AWAY,AWAY,LAND:10000.00:false:0");
        joiner.add("UPS119:BOEING_747_8F:WAIT,LOAD@50,TAKEOFF,AWAY,AWAY,AWAY,LAND:4000.00:false:0");
        joiner.add("VH-BFK:ROBINSON_R44:LAND,WAIT,LOAD@75,TAKEOFF,AWAY,AWAY:40.00:false:4");

        try {
            ControlTowerInitialiser.loadAircraft(new StringReader(joiner.toString()));
        } catch (MalformedSaveException | IOException ex) {
            expected = true;
        }

        assertTrue("Invalid number of aircraft declaration to aircraft lines contained", expected);
    }

    @Test
    public void readAircraftValidInputOneTest() {
        String test = "QFA481:AIRBUS_A320:AWAY,AWAY,LAND,WAIT,WAIT,LOAD@60,TAKEOFF,AWAY:10000.00:false:132";
        Aircraft ac = null;
        boolean expected = false;

        try {
            ac = ControlTowerInitialiser.readAircraft(test);
        } catch (MalformedSaveException e) {
            expected = true;
        }

        assertEquals("The aircraft read does not match the given input", aircraft1, ac);
    }

    @Test
    public void readAircraftValidInputTwoTest() {
        String test = "UTD302:BOEING_787:WAIT,LOAD@100,TAKEOFF,AWAY,AWAY,AWAY,LAND:10000.00:false:0";
        Aircraft ac = null;
        boolean expected = false;

        try {
            ac = ControlTowerInitialiser.readAircraft(test);
        } catch (MalformedSaveException e) {
            expected = true;
        }

        assertEquals("The aircraft read does not match the given input", aircraft2, ac);
    }

    @Test
    public void readAircraftValidInputThreeTest() {
        String test = "UPS119:BOEING_747_8F:WAIT,LOAD@50,TAKEOFF,AWAY,AWAY,AWAY,LAND:4000.00:false:0";
        Aircraft ac = null;
        boolean expected = false;

        try {
            ac = ControlTowerInitialiser.readAircraft(test);
        } catch (MalformedSaveException e) {
            expected = true;
        }

        assertEquals("The aircraft read does not match the given input", aircraft3, ac);
    }

    @Test
    public void readAircraftValidInputFourTest() {
        String test = "VH-BFK:ROBINSON_R44:LAND,WAIT,LOAD@75,TAKEOFF,AWAY,AWAY:40.00:false:4";
        Aircraft ac = null;
        boolean expected = false;

        try {
            ac = ControlTowerInitialiser.readAircraft(test);
        } catch (MalformedSaveException e) {
            expected = true;
        }

        assertEquals("The aircraft read does not match the given input", aircraft4, ac);
    }

    @Test
    public void readAircraftMoreColonsTest() {
        String test = ":UTD302:BOEING_787:WAIT,LOAD@100,TAKEOFF,AWAY,AWAY,AWAY,LAND:10000.00:false:0:";
        boolean expected = false;
        try {
            ControlTowerInitialiser.readAircraft(test);
        } catch (MalformedSaveException e) {
            expected = true;
        }
        assertTrue("This aircraft input contains more colons than expected", expected);
    }

    @Test
    public void readAircraftLessColonsTest() {
        String test = "UTD302BOEING_787:WAIT,LOAD@100,TAKEOFF,AWAY,AWAY,AWAY,LAND:10000.00:false:0";
        boolean expected = false;
        try {
            ControlTowerInitialiser.readAircraft(test);
        } catch (MalformedSaveException e) {
            expected = true;
        }
        assertTrue("This aircraft input contains less colons than expected", expected);
    }


    @Test
    public void readAircraftInvalidAircraftCharacteristicsTest() {
        String test = "UTD302:BOEING_78:WAIT,LOAD@100,TAKEOFF,AWAY,AWAY,AWAY,LAND:10000.00:false:0";
        boolean expected = false;
        try {
            ControlTowerInitialiser.readAircraft(test);
        } catch (MalformedSaveException e) {
            expected = true;
        }
        assertTrue("This aircraft input contains an invalid AircraftCharacteristic", expected);
    }

    @Test
    public void readAircraftInvalidAircraftFuelAmountTest() {
        String test = "UTD302:BOEING_787:WAIT,LOAD@100,TAKEOFF,AWAY,AWAY,AWAY,LAND:10xx.00:false:0";
        boolean expected = false;
        try {
            ControlTowerInitialiser.readAircraft(test);
        } catch (MalformedSaveException e) {
            expected = true;
        }
        assertTrue("This aircraft input contains an invalid fuel amount", expected);
    }

    @Test
    public void readAircraftInvalidAircraftFuelAmountLessThanZeroTest() {
        String test = "UTD302:BOEING_787:WAIT,LOAD@100,TAKEOFF,AWAY,AWAY,AWAY,LAND:-10:false:0";
        boolean expected = false;
        try {
            ControlTowerInitialiser.readAircraft(test);
        } catch (MalformedSaveException e) {
            expected = true;
        }
        assertTrue("This aircraft input contains a negative fuel amount", expected);
    }

    @Test
    public void readAircraftInvalidAircraftFuelAmountMoreThanCapacityTest() {

        String test = "UTD302:BOEING_787:WAIT,LOAD@100,TAKEOFF,AWAY,AWAY,AWAY,LAND:126207.00:false:0";
        boolean expected = false;
        try {
            ControlTowerInitialiser.readAircraft(test);
        } catch (MalformedSaveException e) {
            expected = true;
        }
        assertTrue("This aircraft input contains a fuel amount above the capacity", expected);
    }

    @Test
    public void readAircraftInvalidAircraftCargoTest() {
        String test = "UTD302:BOEING_787:WAIT,LOAD@100,TAKEOFF,AWAY,AWAY,AWAY,LAND:10000.00:false:X";
        boolean expected = false;
        try {
            ControlTowerInitialiser.readAircraft(test);
        } catch (MalformedSaveException e) {
            expected = true;
        }
        assertTrue("This aircraft input contains an invalid cargo amount", expected);
    }

    @Test
    public void readAircraftInvalidAircraftCargoLessThanZeroTest() {
        String test = "UTD302:BOEING_787:WAIT,LOAD@100,TAKEOFF,AWAY,AWAY,AWAY,LAND:10000.00:false:-1";
        boolean expected = false;
        try {
            ControlTowerInitialiser.readAircraft(test);
        } catch (MalformedSaveException e) {
            expected = true;
        }
        assertTrue("This aircraft input contains a cargo amount less than 0", expected);
    }

    @Test
    public void readAircraftInvalidAircraftCargoMoreThanCapacityTest() {
        String test = "UTD302:BOEING_787:WAIT,LOAD@100,TAKEOFF,AWAY,AWAY,AWAY,LAND:10000.00:false:243";
        boolean expected = false;
        try {
            ControlTowerInitialiser.readAircraft(test);
        } catch (MalformedSaveException e) {
            expected = true;
        }
        assertTrue("This aircraft input contains a cargo amount more than the capacity", expected);
    }

    @Test
    public void readTaskListValidInputOneTest() {
        String test = "AWAY,AWAY,LAND,WAIT,WAIT,LOAD@60,TAKEOFF,AWAY";
        boolean expected = false;

        try {
            ControlTowerInitialiser.readTaskList(test);
        } catch (MalformedSaveException e) {
            expected = true;
        }

        assertFalse("the task list read does not match the given input", expected);
    }

    @Test
    public void readTaskListValidInputTwoTest() {
        String test = "WAIT,LOAD@100,TAKEOFF,AWAY,AWAY,AWAY,LAND";
        boolean expected = false;

        try {
            ControlTowerInitialiser.readTaskList(test);
        } catch (MalformedSaveException e) {
            expected = true;
        }

        assertFalse("The task list read does not match the given input", expected);
    }

    @Test
    public void readTaskListValidInputThreeTest() {
        String test = "WAIT,LOAD@50,TAKEOFF,AWAY,AWAY,AWAY,LAND";
        boolean expected = false;

        try {
            ControlTowerInitialiser.readTaskList(test);
        } catch (MalformedSaveException e) {
            expected = true;
        }

        assertFalse("The task list read does not match the given input", expected);
    }

    @Test
    public void readTaskListValidInputFourTest() {
        String test = "LAND,WAIT,LOAD@75,TAKEOFF,AWAY,AWAY";
        boolean expected = false;

        try {
            ControlTowerInitialiser.readTaskList(test);
        } catch (MalformedSaveException e) {
            expected = true;
        }

        assertFalse("The task list read does not match the given input", expected);
    }

    @Test
    public void readTaskListValidOrderingTwoTest() {
        String test = "TAKEOFF,AWAY,AWAY,AWAY,AWAY,LAND,WAIT,LOAD@80";
        boolean expected = false;
        try {
            ControlTowerInitialiser.readTaskList(test);
        } catch (MalformedSaveException e) {
            expected = true;
        }
        assertFalse("The task list is correctly ordered", expected);
    }

    @Test
    public void readTaskListValidOrderingSingleTaskOneTest() {
        String test = "AWAY,AWAY,AWAY,AWAY,AWAY,AWAY";
        boolean expected = false;
        try {
            ControlTowerInitialiser.readTaskList(test);
        } catch (MalformedSaveException e) {
            expected = true;
        }
        assertFalse("The task list is correctly ordered", expected);
    }

    @Test
    public void readTaskListValidOrderingSingleTaskTwoTest() {
        String test = "WAIT,WAIT,WAIT,WAIT,WAIT,WAIT";
        boolean expected = false;
        try {
            ControlTowerInitialiser.readTaskList(test);
        } catch (MalformedSaveException e) {
            expected = true;
        }
        assertFalse("The task list is correctly ordered", expected);
    }

    @Test
    public void readTaskListInvalidTaskTypeTest() {
        String test = "WAIT,LOAD@100,TAKOF,AWAY,AWAY,AWAY,LAND";
        boolean expected = false;
        try {
            ControlTowerInitialiser.readTaskList(test);
        } catch (MalformedSaveException e) {
            expected = true;
        }
        assertTrue("The task list contains an invalid task type", expected);
    }

    @Test
    public void readTaskListInvalidLoadPercentageTest() {
        String test = "WAIT,LOAD@X,TAKEOFF,AWAY,AWAY,AWAY,LAND";
        boolean expected = false;
        try {
            ControlTowerInitialiser.readTaskList(test);
        } catch (MalformedSaveException e) {
            expected = true;
        }
        assertTrue("The task list contains an invalid load percentage for a LOAD task", expected);
    }

    @Test
    public void readTaskListInvalidLoadPercentageLessThanZeroTest() {
        String test = "WAIT,LOAD@-5,TAKEOFF,AWAY,AWAY,AWAY,LAND";
        boolean expected = false;
        try {
            ControlTowerInitialiser.readTaskList(test);
        } catch (MalformedSaveException e) {
            expected = true;
        }
        assertTrue("The task list contains a load percentage less than 0 for" +
                " a LOAD task", expected);
    }

    @Test
    public void readTaskListInvalidSymbolTest() {
        String test = "WAIT,LOAD@@100,TAKEOFF,AWAY,AWAY,AWAY,LAND";
        boolean expected = false;
        try {
            ControlTowerInitialiser.readTaskList(test);
        } catch (MalformedSaveException e) {
            expected = true;
        }
        assertTrue("The task list contains tasks with more than one '@' symbol", expected);
    }

    @Test
    public void readTaskListInvalidOrderingOneTest() {
        String test = "WAIT,LOAD@100,AWAY,AWAY,AWAY,LAND,TAKEOFF";
        boolean expected = false;
        try {
            ControlTowerInitialiser.readTaskList(test);
        } catch (MalformedSaveException e) {
            expected = true;
        }
        assertTrue("The task list is not correctly ordered to match the " +
                "task list criteria", expected);
    }

    @Test
    public void readTaskListInvalidOrderingTwoTest() {
        String test = "WAIT,LOAD@100,TAKEOFF,AWAY,LOAD@50,AWAY,AWAY,LAND";
        boolean expected = false;
        try {
            ControlTowerInitialiser.readTaskList(test);
        } catch (MalformedSaveException e) {
            expected = true;
        }
        assertTrue("The task list is not correctly ordered to match the " +
                "task list criteria", expected);
    }

    @Test
    public void readTaskListInvalidOrderingThreeTest() {
        String test = "WAIT,LOAD@100,TAKEOFF,WAIT,AWAY,AWAY,LAND";
        boolean expected = false;
        try {
            ControlTowerInitialiser.readTaskList(test);
        } catch (MalformedSaveException e) {
            expected = true;
        }
        assertTrue("The task list is not correctly ordered to match the " +
                "task list criteria", expected);
    }

    @Test
    public void readTaskListInvalidOrderingFourTest() {
        String test = "WAIT,LOAD@100,TAKEOFF,AWAY,AWAY,AWAY,LAND,LAND";
        boolean expected = false;
        try {
            ControlTowerInitialiser.readTaskList(test);
        } catch (MalformedSaveException e) {
            expected = true;
        }
        assertTrue("The task list is not correctly ordered to match the " +
                "task list criteria", expected);
    }

    @Test
    public void readTaskListInvalidOrderingFiveTest() {
        String test = "TAKEOFF,TAKEOFF,TAKEOFF,TAKEOFF,TAKEOFF";
        boolean expected = false;
        try {
            ControlTowerInitialiser.readTaskList(test);
        } catch (MalformedSaveException e) {
            expected = true;
        }
        assertTrue("The task list is not correctly ordered to match the " +
                "task list criteria", expected);
    }
}












