package towersim.control;

import towersim.aircraft.Aircraft;
import towersim.aircraft.AircraftCharacteristics;
import towersim.aircraft.FreightAircraft;
import towersim.aircraft.PassengerAircraft;
import towersim.ground.AirplaneTerminal;
import towersim.ground.Gate;
import towersim.ground.HelicopterTerminal;
import towersim.ground.Terminal;
import towersim.tasks.Task;
import towersim.tasks.TaskList;

import towersim.tasks.TaskType;
import towersim.util.MalformedSaveException;
import towersim.util.NoSpaceException;

import java.io.*;
import java.util.*;

/**
 * Utility class that contains static methods for loading a
 * control tower and associated entities from files.
 */
public class ControlTowerInitialiser {

    /**
     * Checks that the expected number of colons in the string are correct,
     * otherwise throws a MalformedSavedException
     *
     * @param line     the string line to be checked
     * @param colonVal the number of colons expected
     * @throws MalformedSaveException if the number of expected colons does not equal colons found
     */
    private static void validateColons(String line, int colonVal) throws MalformedSaveException {
        int count = 0;
        for (int i = 0; i < line.length(); i++) {
            if (line.charAt(i) == ':') {
                count++;
            }
        }
        if (count != colonVal) {
            throw new MalformedSaveException();
        }
    }

    /**
     * Returns the number of at (@) symbols found using iteration.
     *
     * @param line task to be validated
     * @return the number of (@) within the string
     */
    private static int checkSymbolQuantity(String line) {
        int count = 0;
        for (int i = 0; i < line.length(); i++) {
            if (line.charAt(i) == '@') {
                count++;
            }
        }
        return count;
    }

    /**
     * Checks that the given input is a valid Enum constant of AircraftCharacteristics.
     *
     * @param line  the string to be validated
     * @return      the Aircraft Characteristics Enum
     * @throws MalformedSaveException if the input is invalid
     */
    private static AircraftCharacteristics validateCharacteristics(String line)
            throws MalformedSaveException {

        AircraftCharacteristics aircraftCharacteristics;
        try {
            aircraftCharacteristics = AircraftCharacteristics.valueOf(line);
        } catch (IllegalArgumentException iae) {
            throw new MalformedSaveException();
        }

        for (AircraftCharacteristics characteristics : AircraftCharacteristics.values()) {
            if (characteristics == aircraftCharacteristics) {
                return characteristics;
            }
        }
        throw new MalformedSaveException();
    }

    /**
     * Checks that the given input is a valid Enum constant of TaskType.
     *
     * @param line  the string to be validated
     * @return      the TaskType Enum
     * @throws MalformedSaveException if the input is invalid
     */
    private static TaskType validateTaskType(String line) throws MalformedSaveException {
        TaskType taskType;
        try {
            taskType = TaskType.valueOf(line);
        } catch (IllegalArgumentException iae) {
            throw new MalformedSaveException();
        }

        for (TaskType type : TaskType.values()) {
            if (type == taskType) {
                return type;
            }
        }
        throw new MalformedSaveException();
    }

    /**
     * Validates that an input can be converted to double and returns that double
     * @param line the line to be converted
     * @return the converted value from String to double
     * @throws MalformedSaveException if it cannot be converted to double
     */
    private static double validateDouble(String line) throws MalformedSaveException {
        try {
            return Double.parseDouble(line);
        } catch (NumberFormatException nfe) {
            throw new MalformedSaveException();
        }
    }

    /**
     * Validates that an input can be converted to integer and returns that integer
     * @param line the line to be converted
     * @return the converted value from String to integer
     * @throws MalformedSaveException if it cannot be converted to integer
     */
    private static int validateInteger(String line) throws MalformedSaveException {
        try {
            return Integer.parseInt(line);
        } catch (NumberFormatException nfe) {
            throw new MalformedSaveException();
        }
    }

    /**
     * Validates that an input can be converted to long and returns that long
     * @param line the line to be converted
     * @return the converted value from String to long
     * @throws MalformedSaveException if it cannot be converted to long
     */
    private static long validateLong(String line) throws MalformedSaveException {
        try {
            return Long.parseLong(line);
        } catch (NumberFormatException nfe) {
            throw new MalformedSaveException();
        }
    }

    /**
     * Loads the number of ticks elapsed from the given reader instance.
     * The contents read from the reader are invalid if any of the following conditions are true:
     *
     *      The number of ticks elapsed is not an integer
     *      (i.e. cannot be parsed by Long.parseLong(String)).
     *
     *      The number of ticks elapsed is less than zero.
     *
     * @param reader reader from which to load the number of ticks elapsed
     * @return number of ticks elapsed
     * @throws MalformedSaveException if the format of the text read from the
     *                                reader is invalid according to the rules above
     * @throws IOException            if an IOException is encountered when reading from the reader
     */
    public static long loadTick(Reader reader) throws MalformedSaveException, IOException {
        try (BufferedReader bufferedReader = new BufferedReader(reader)) {
            String line = bufferedReader.readLine();

            long ticks = validateLong(line);

            if (ticks < 0) {
                throw new MalformedSaveException();
            }

            return ticks;
        } catch (IOException ioe) {
            throw new IOException();
        } catch (MalformedSaveException mse) {
            throw new MalformedSaveException();
        }
    }

    /**
     * Loads the list of all aircraft managed by the control tower from the given reader instance.
     *
     * The contents read from the reader are invalid if any of the following conditions are true:
     *
     *      The number of aircraft specified on the first line of the reader is not an integer
     *      (i.e. cannot be parsed by Integer.parseInt(String)).
     *
     *      The number of aircraft specified on the first line is not equal to the number
     *      of aircraft actually read from the reader.
     *
     *      Any of the conditions listed in the Javadoc for readAircraft(String) are true.
     *
     * @param reader reader from which to load the list of aircraft
     * @return list of aircraft read from the reader
     * @throws IOException            if an IOException is encountered when reading from the reader
     * @throws MalformedSaveException if the format of the text read from the
     *                                reader is invalid according to the rules above
     */
    public static List<Aircraft> loadAircraft(Reader reader)
            throws IOException, MalformedSaveException {
        List<Aircraft> aircrafts = new ArrayList<Aircraft>();
        try (BufferedReader bufferedReader = new BufferedReader(reader)) {

            String line = bufferedReader.readLine();
            int numAircraft = validateInteger(line);

            if (numAircraft == 0) {
                return aircrafts;
            }

            // this block determines inconsistencies between the integer given on the
            // first line to outline the number of aircrafts to read and how many are present
            String aircraftEncoded;
            int bufferElapsed = 1;
            while ((aircraftEncoded = bufferedReader.readLine()) != null) {
                // if there are more lines present than the number of aircrafts
                // declared in the first line, then error thrown
                if (bufferElapsed > numAircraft) {
                    throw new MalformedSaveException();
                }
                aircrafts.add(readAircraft(aircraftEncoded));
                bufferElapsed++;
            }

            // if at the end of the iteration, if there were less lines read
            // then what was declared on the first line, then error thrown
            if (bufferElapsed <= numAircraft) {
                throw new MalformedSaveException();
            }

            return aircrafts;
        } catch (IOException ioe) {
            throw new IOException();
        } catch (MalformedSaveException mse) {
            throw new MalformedSaveException();
        }
    }

    /**
     * Reads an aircraft from its encoded representation in the given string.
     *
     * The encoded string is invalid if any of the following conditions are true:
     *
     *      More/fewer colons (:) are detected in the string than expected.
     *
     *      The aircraft's AircraftCharacteristics is not valid,
     *      i.e. it is not one of those listed in AircraftCharacteristics.values().
     *
     *      The aircraft's fuel amount is not a double
     *      (i.e. cannot be parsed by Double.parseDouble(String)).
     *
     *      The aircraft's fuel amount is less than zero or
     *      greater than the aircraft's maximum fuel capacity.
     *
     *      The amount of cargo (freight/passengers) onboard the aircraft is not an integer
     *      (i.e. cannot be parsed by Integer.parseInt(String)).
     *
     *      The amount of cargo (freight/passengers) onboard the aircraft is
     *      less than zero or greater than the aircraft's maximum freight/passenger capacity.
     *
     *      Any of the conditions listed in the Javadoc for readTaskList(String) are true.
     *
     * @param line line of text containing the encoded aircraft
     * @return decoded aircraft instance
     * @throws MalformedSaveException if the format of the given string is
     *                                invalid according to the rules above
     */
    public static Aircraft readAircraft(String line) throws MalformedSaveException {
        try {
            validateColons(line, 5);
        } catch (MalformedSaveException mse) {
            throw new MalformedSaveException();
        }

        String[] lineContents = line.split(":");

        AircraftCharacteristics aircraftCharacteristics;
        double aircraftFuelAmount;
        int cargoAmount;
        int aircraftPassengerCapacity;
        int aircraftFreightCapacity;
        boolean emergency = false;
        TaskList aircraftTaskList;
        try {
            // validates the aircraft characteristics (1st index of split array)
            aircraftCharacteristics = validateCharacteristics(lineContents[1]);

            // validates the fuel amount (3rd index of split array) of the aircraft
            // to ensure it remains between 0 and the fuel capacity
            double aircraftFuelCapacity = aircraftCharacteristics.fuelCapacity;
            aircraftFuelAmount = validateDouble(lineContents[3]);
            if (aircraftFuelAmount < 0 || aircraftFuelAmount > aircraftFuelCapacity) {
                throw new MalformedSaveException();
            }

            // validates the numerical value of cargo and determines which type of cargo it is
            cargoAmount = validateInteger(lineContents[5]);
            aircraftPassengerCapacity = aircraftCharacteristics.passengerCapacity;
            aircraftFreightCapacity = aircraftCharacteristics.freightCapacity;
            if (cargoAmount < 0
                    || ((cargoAmount > aircraftFreightCapacity)
                    && (cargoAmount > aircraftPassengerCapacity))) {
                throw new MalformedSaveException();
            }

            // determines the emergency state (4th index in split array)
            if (lineContents[4].equals("true")) {
                emergency = true;
            }

            // creates the aircraft task list using the comma-connected string in 2nd index
            aircraftTaskList = readTaskList(lineContents[2]);
        } catch (MalformedSaveException mfe) {
            throw new MalformedSaveException();
        }

        // generates the aircraft as either passenger or freight based on the cargo given
        if (aircraftPassengerCapacity > aircraftFreightCapacity) {
            PassengerAircraft passengerAircraft = new PassengerAircraft(
                    lineContents[0],
                    aircraftCharacteristics, aircraftTaskList,
                    aircraftFuelAmount, cargoAmount);
            if (emergency) {
                passengerAircraft.declareEmergency();
            }
            return passengerAircraft;
        } else if (aircraftPassengerCapacity < aircraftFreightCapacity) {
            FreightAircraft freightAircraft = new FreightAircraft(
                    lineContents[0], aircraftCharacteristics,
                    aircraftTaskList, aircraftFuelAmount, cargoAmount);
            if (emergency) {
                freightAircraft.declareEmergency();
            }
            return freightAircraft;
        } else {
            throw new MalformedSaveException();
        }
    }

    /**
     * Reads a task list from its encoded representation in the given string.
     *
     * The encoded string is invalid if any of the following conditions are true:
     *
     *      The task list's TaskType is not valid
     *      (i.e. it is not one of those listed in TaskType.values()).
     *
     *      A task's load percentage is not an integer
     *      (i.e. cannot be parsed by Integer.parseInt(String)).
     *
     *      A task's load percentage is less than zero.
     *
     *      More than one at-symbol (@) is detected for any task in the task list.
     *
     *      The task list is invalid according to the rules specified in TaskList(List).
     *
     * @param taskListPart string containing the encoded task list
     * @return decoded task list instance
     * @throws MalformedSaveException if the format of the given string is
     *                                invalid according to the rules above
     */
    public static TaskList readTaskList(String taskListPart) throws MalformedSaveException {
        List<Task> tasks = new ArrayList<Task>();

        // splits string into tasks
        String[] taskComponents = taskListPart.split(",");

        try {
            for (String task : taskComponents) {
                if (checkSymbolQuantity(task) == 1) {
                    // if there is an '@' then its of type LOAD, where the string is
                    // further decomposed to the task type and load percentage
                    String[] loadTypeComponents = task.split("@");

                    int loadPercent = validateInteger(loadTypeComponents[1]);
                    if (loadPercent < 0) {
                        throw new MalformedSaveException();
                    }

                    // use helper method to validate the task using the task type found on the
                    // 0th index and load percent on 1st index of the loadTypeComponents
                    Task taskToBeAdded = new Task(
                            validateTaskType(loadTypeComponents[0]), loadPercent);
                    tasks.add(taskToBeAdded);

                } else if (checkSymbolQuantity(task) == 0) {
                    // if it does not have '@', then it is any task but LOAD, and
                    // further decomposition is not necessary
                    Task taskToBeAdded = new Task(validateTaskType(task));
                    tasks.add(taskToBeAdded);
                } else {
                    throw new MalformedSaveException();
                }
            }
            return new TaskList(tasks);
        } catch (MalformedSaveException | IllegalArgumentException ex) {
            throw new MalformedSaveException();
        }
    }

    /**
     * Loads the list of terminals and their gates from the given reader instance.
     *
     * The contents read from the reader are invalid if any of the following conditions are true:
     *
     *      The number of terminals specified at the top of the file is not an integer
     *      (i.e. cannot be parsed by Integer.parseInt(String)).
     *
     *      The number of terminals specified is not equal to
     *      the number of terminals actually read from the reader.
     *
     *      Any of the conditions listed in the Javadoc for
     *      readTerminal(String, BufferedReader, List) and readGate(String, List) are true.
     *
     * @param reader   reader from which to load the list of terminals and their gates
     * @param aircraft list of all aircraft, used when validating that callsigns exist
     * @return list of terminals (with their gates) read from the reader
     * @throws MalformedSaveException if the format of the text read from the reader is
     *                                invalid according to the rules above
     * @throws IOException            if an IOException is encountered when reading from the reader
     */
    public static List<Terminal> loadTerminalsWithGates(Reader reader, List<Aircraft> aircraft)
            throws MalformedSaveException, IOException {

        List<Terminal> terminals = new ArrayList<Terminal>();

        try (BufferedReader bufferedReader = new BufferedReader(reader)) {
            String line = bufferedReader.readLine();
            int numTerminal = validateInteger(line);

            if (numTerminal == 0) {
                return terminals;
            }

            // similar to the loadAircraft() logic, this block ensures that no inconsistencies
            // between the terminal number given on the first line and lines present
            String terminalEncoded;
            int bufferElapsed = 1;
            while ((terminalEncoded = bufferedReader.readLine()) != null) {
                if (bufferElapsed > numTerminal) {
                    throw new MalformedSaveException();
                }
                terminals.add(readTerminal(terminalEncoded, bufferedReader, aircraft));
                bufferElapsed++;
            }
            if (bufferElapsed <= numTerminal) {
                throw new MalformedSaveException();
            }

            return terminals;

        } catch (IOException ioe) {
            throw new IOException();
        } catch (MalformedSaveException mse) {
            throw new MalformedSaveException();
        }
    }

    /**
     * Reads a terminal from the given string and reads its gates from the given reader instance.
     *
     * The encoded terminal is invalid if any of the following conditions are true:
     *
     *      The number of colons (:) detected on the first line is more/fewer than expected.
     *
     *      The terminal type specified on the first line is neither
     *      AirplaneTerminal nor HelicopterTerminal.
     *
     *      The terminal number is not an integer
     *      (i.e. cannot be parsed by Integer.parseInt(String)).
     *
     *      The terminal number is less than one (1).
     *
     *      The number of gates in the terminal is not an integer.
     *
     *      The number of gates is less than zero or is greater than Terminal.MAX_NUM_GATES.
     *
     *      A line containing an encoded gate was expected, but EOF (end of file) was received
     *      (i.e. BufferedReader.readLine() returns null).
     *
     *      Any of the conditions listed in the Javadoc for readGate(String, List) are true.
     *
     * @param line     string containing the first line of the encoded terminal
     * @param reader   reader from which to load the gates of the terminal (subsequent lines)
     * @param aircraft list of all aircraft, used when validating that callsigns exist
     * @return decoded terminal with its gates added
     * @throws IOException            if an IOException is encountered when reading from the reader
     * @throws MalformedSaveException if the format of the given string or the text read from the
     *                                reader is invalid according to the rules above
     */
    public static Terminal readTerminal(String line, BufferedReader reader, List<Aircraft> aircraft)
            throws IOException, MalformedSaveException {

        int terminalNum;
        int gateNum;
        Terminal terminal = null;

        try {
            validateColons(line, 3);

            String[] terminalComponents = line.split(":");

            // validates the terminal number (1st index of split array)
            terminalNum = validateInteger(terminalComponents[1]);
            if (terminalNum < 1) {
                throw new MalformedSaveException();
            }

            // validates the distinct terminal type of AirplaneTerminal
            // or Helicopter Terminal (0th index of split array)
            String terminalType = terminalComponents[0];
            if (terminalType.equals("AirplaneTerminal")) {
                terminal = new AirplaneTerminal(terminalNum);
            } else if (terminalType.equals("HelicopterTerminal")) {
                terminal = new HelicopterTerminal(terminalNum);
            } else {
                throw new MalformedSaveException();
            }

            // validates the emergency status (2nd index of split array)
            if (terminalComponents[2].equals("true")) {
                terminal.declareEmergency();
            }

            // validates the number of gates is within the limits of 0 and max capacity
            int maxGates = Terminal.MAX_NUM_GATES;
            gateNum = validateInteger(terminalComponents[3]);
            if (gateNum < 0 || gateNum > maxGates) {
                throw new MalformedSaveException();
            }


            // checking for EOF (end of file) when reading each encoded gate
            for (int i = 0; i < gateNum; i++) {
                String gateEncoded = reader.readLine();

                if (gateEncoded == null) {
                    throw new MalformedSaveException();
                }

                Gate gate = readGate(gateEncoded, aircraft);
                terminal.addGate(gate);
            }
        } catch (MalformedSaveException mse) {
            throw new MalformedSaveException();
        } catch (IOException ioe) {
            throw new IOException();
        } catch (NoSpaceException e) {
            // from terminal.addGate - this should not occur since the limits are checked
        }
        return terminal;
    }

    /**
     * Reads a gate from its encoded representation in the given string.
     * <p>
     * The encoded string is invalid if any of the following conditions are true:
     * <p>
     * The number of colons (:) detected was more/fewer than expected.
     * <p>
     * The gate number is not an integer (i.e. cannot be parsed by Integer.parseInt(String)).
     * <p>
     * The gate number is less than one (1).
     * <p>
     * The callsign of the aircraft parked at the gate is not empty and
     * the callsign does not correspond to the callsign of any aircraft
     * contained in the list of aircraft given as a parameter.
     *
     * @param line     string containing the encoded gate
     * @param aircraft list of all aircraft, used when validating that callsigns exist
     * @return decoded gate instance
     * @throws MalformedSaveException if the format of the given string is
     *                                invalid according to the rules above
     */
    public static Gate readGate(String line, List<Aircraft> aircraft)
            throws MalformedSaveException {

        try {
            validateColons(line, 1);
        } catch (MalformedSaveException mse) {
            throw new MalformedSaveException();
        }

        String[] gateContents = line.split(":");

        int gateNum;
        String aircraftCallsign;
        boolean validCallsign = false;
        Gate gate;
        try {
            // validates the gate number to be at least 1
            gateNum = validateInteger(gateContents[0]);
            if (gateNum < 1) {
                throw new MalformedSaveException();
            }

            gate = new Gate(gateNum);

            // checks that the callsign extracted from the string and stored in gateContents
            // is either a valid aircraft string (belongs in the aircraft list) or empty
            aircraftCallsign = gateContents[1];
            for (Aircraft currentAircraft : aircraft) {
                if (currentAircraft.getCallsign().equals(aircraftCallsign)) {
                    validCallsign = true;
                    gate.parkAircraft(currentAircraft);
                    break;
                }
            }

            if (!aircraftCallsign.equals("empty") && !validCallsign) {
                throw new MalformedSaveException();
            }

        } catch (MalformedSaveException | NoSpaceException mse) {
            throw new MalformedSaveException();
        }

        return gate;
    }

    /**
     * Loads the takeoff queue, landing queue and map of
     * loading aircraft from the given reader instance.
     *
     * @param reader          reader from which to load the queues and loading map
     * @param aircraft        list of all aircraft, used when validating that callsigns exist
     * @param takeoffQueue    empty takeoff queue that aircraft will be added to
     * @param landingQueue    empty landing queue that aircraft will be added to
     * @param loadingAircraft empty map that aircraft and loading times will be added to
     * @throws MalformedSaveException if the format of the text read from the reader is
     *                                invalid according to the rules above
     * @throws IOException            if an IOException is encountered when reading from the reader
     */
    public static void loadQueues(Reader reader, List<Aircraft> aircraft, TakeoffQueue takeoffQueue,
        LandingQueue landingQueue, Map<Aircraft, Integer> loadingAircraft)
            throws MalformedSaveException, IOException {

        try (BufferedReader bufferedReader = new BufferedReader(reader)) {
            // the contents of the takeoff and landing queue are read by the shared method readQueue
            readQueue(bufferedReader, aircraft, takeoffQueue);
            readQueue(bufferedReader, aircraft, landingQueue);

            // loading aircraft is unique compared to the other queues and hence handled separately
            readLoadingAircraft(bufferedReader, aircraft, loadingAircraft);
        } catch (MalformedSaveException mse) {
            throw new MalformedSaveException();
        } catch (IOException ioe) {
            throw new IOException();
        }
    }

    /**
     * Reads an aircraft queue from the given reader instance.
     *
     * The contents read from the reader are invalid if any of the following conditions are true:
     *
     *      The first line read from the reader is null.
     *
     *      The first line contains more/fewer colons (:) than expected.
     *
     *      The queue type specified in the first line is not equal
     *      to the simple class name of the queue provided as a parameter.
     *
     *      The number of aircraft specified on the first line is not an integer
     *      (i.e. cannot be parsed by Integer.parseInt(String)).
     *
     *      The number of aircraft specified is greater than zero and the second line read is null.
     *
     *      The number of callsigns listed on the second line is not equal
     *      to the number of aircraft specified on the first line.
     *
     *      A callsign listed on the second line does not correspond to
     *      the callsign of any aircraft contained in the list of aircraft given as a parameter.
     *
     * @param reader   reader from which to load the aircraft queue
     * @param aircraft list of all aircraft, used when validating that callsigns exist
     * @param queue    empty queue that aircraft will be added to
     * @throws IOException            if an IOException is encountered when reading from the reader
     * @throws MalformedSaveException if the format of the text read from the reader is
     *                                invalid according to the rules above
     */
    public static void readQueue(BufferedReader reader, List<Aircraft> aircraft,
            AircraftQueue queue) throws IOException, MalformedSaveException {

        String aircrafts = null;
        boolean invalidCallsign = false;

        try {
            String line = reader.readLine();

            if (line == null) {
                throw new MalformedSaveException();
            }

            validateColons(line, 1);

            String[] aircraftQueueContents = line.split(":");

            // checks that the queue type written in the first line and stored in
            // aircraftQueueContents (index 0) is equivalent to the queue type provided as
            // the parameter. This ensures that the operations are held in a specific queue.
            if (!aircraftQueueContents[0].equals(queue.getClass().getSimpleName())) {
                throw new MalformedSaveException();
            }

            // number of aircraft in the queue (stored at index 1 of split array)
            int numAircraftInQueue = validateInteger(aircraftQueueContents[1]);

            // ensures that if the number of aircraft is greater than 0, then the next
            // readable line is not null
            if (numAircraftInQueue > 0) {
                aircrafts = reader.readLine();
            } else if (numAircraftInQueue == 0) {
                return;
            }

            if (aircrafts == null) {
                throw new MalformedSaveException();
            }

            // splits array to separate each callsign from the second line of queue encoded
            String[] aircraftCallsigns = aircrafts.split(",");

            // validates that the number of callsigns present within the queue is equivalent
            // to the number of aircraft described on the previous line
            if (aircraftCallsigns.length != numAircraftInQueue) {
                throw new MalformedSaveException();
            }

            // validates that all callsigns read from the queue
            // belong to the list of aircraft given as parameter
            for (String callsign : aircraftCallsigns) {
                for (Aircraft currentAircraft : aircraft) {
                    if (currentAircraft.getCallsign().equals(callsign)) {
                        invalidCallsign = true;
                        queue.addAircraft(currentAircraft);
                        break;
                    } else {
                        invalidCallsign = false;
                    }
                }
                if (!invalidCallsign) {
                    throw new MalformedSaveException();
                }
            }
        } catch (MalformedSaveException mse) {
            throw new MalformedSaveException();
        } catch (IOException ioe) {
            throw new IOException();
        }
    }

    /**
     * Reads the map of currently loading aircraft from the given reader instance.
     *
     * The contents read from the reader are invalid if any of the following conditions are true:
     *
     *      The first line read from the reader is null.
     *
     *      The number of colons (:) detected on the first line is more/fewer than expected.
     *
     *      The number of aircraft specified on the first line is not an integer
     *      (i.e. cannot be parsed by Integer.parseInt(String)).
     *
     *      The number of aircraft is greater than zero and
     *      the second line read from the reader is null.
     *
     *      The number of aircraft specified on the first line is not equal
     *      to the number of callsigns read on the second line.
     *
     *      For any callsign/loading time pair on the second line, the number of colons
     *      detected is not equal to one. For example, ABC123:5:9 is invalid.
     *
     *      A callsign listed on the second line does not correspond to the callsign
     *      of any aircraft contained in the list of aircraft given as a parameter.
     *
     *      Any ticksRemaining value on the second line is not an integer
     *      (i.e. cannot be parsed by Integer.parseInt(String)).
     *
     *      Any ticksRemaining value on the second line is less than one (1).
     *
     * @param reader          reader from which to load the map of loading aircraft
     * @param aircraft        list of all aircraft, used when validating that callsigns exist
     * @param loadingAircraft empty map that aircraft and their loading times will be added to
     * @throws IOException            if an IOException is encountered when reading from the reader
     * @throws MalformedSaveException if the format of the text read from the reader is
     *                                invalid according to the rules above
     */
    public static void readLoadingAircraft(BufferedReader reader, List<Aircraft> aircraft,
        Map<Aircraft, Integer> loadingAircraft) throws IOException, MalformedSaveException {

        String loadingDetails = null;
        boolean invalidCallsign = false;
        try {
            String line = reader.readLine();

            if (line == null) {
                throw new MalformedSaveException();
            }

            validateColons(line, 1);

            // split array of the first line
            String[] loadingContents = line.split(":");

            // validates the number of aircrafts present in loading is an integer (at index 1)
            int numAircraftInLoading = validateInteger(loadingContents[1]);

            // ensures that if number of aircraft in loading is more than 0,
            // then the next line is not null
            if (numAircraftInLoading > 0) {
                loadingDetails = reader.readLine();
            } else if (numAircraftInLoading == 0) {
                return;
            }

            if (loadingDetails == null) {
                throw new MalformedSaveException();
            }

            // splits the line into colon (:) tuple strings which contain the callsign of
            // the aircraft and its respective ticks remaining in load
            String[] aircraftLoadingDetails = loadingDetails.split(",");

            // validate that the number of aircraft read is equivalent to the number given
            if (aircraftLoadingDetails.length != numAircraftInLoading) {
                throw new MalformedSaveException();
            }

            // checks through each colon (:) tuple in the array to satisfy properties
            for (String loadingInfo : aircraftLoadingDetails) {
                validateColons(loadingInfo, 1);

                String[] loadCallsignAndTicks = loadingInfo.split(":");

                // isolating the callsign of the aircraft and its ticks remaining at indexes 0 and 1
                String aircraftCallsign = loadCallsignAndTicks[0];

                int ticksRemaining = validateInteger(loadCallsignAndTicks[1]);
                if (ticksRemaining < 1) {
                    throw new MalformedSaveException();
                }

                // checks that all callsigns read belong to the given list of aircraft
                for (Aircraft currentAircraft : aircraft) {
                    if (currentAircraft.getCallsign().equals(aircraftCallsign)) {
                        invalidCallsign = true;
                        loadingAircraft.put(currentAircraft, ticksRemaining);
                        // do we add aircraft/loading time like this???
                        break;
                    } else {
                        invalidCallsign = false;
                    }
                }
                if (!invalidCallsign) {
                    throw new MalformedSaveException();
                }
            }
        } catch (MalformedSaveException mse) {
            throw new MalformedSaveException();
        } catch (IOException ioe) {
            throw new IOException();
        }
    }

    /**
     * Creates a control tower instance by reading various airport entities from the given readers.
     * <p>
     * The following methods should be called in this order,
     * and their results stored temporarily, to load information from the readers:
     * <p>
     * loadTick(Reader) to load the number of elapsed ticks
     * <p>
     * loadAircraft(Reader) to load the list of all aircraft
     * <p>
     * loadTerminalsWithGates(Reader, List) to load the terminals and their gates
     * <p>
     * loadQueues(Reader, List, TakeoffQueue, LandingQueue, Map) to load the takeoff queue,
     * landing queue and map of loading aircraft to their loading time remaining
     *
     * @param tick               reader from which to load the number of ticks elapsed
     * @param aircraft           reader from which to load the list of aircraft
     * @param queues             reader from which to load the aircraft queues
     *                           and map of loading aircraft
     * @param terminalsWithGates reader from which to load the terminals and their gates
     * @return control tower created by reading from the given readers
     * @throws MalformedSaveException if reading from any of the given readers results
     *                                in a MalformedSaveException, indicating the contents
     *                                of that reader are invalid
     * @throws IOException            if an IOException is encountered when
     *                                reading from any of the readers
     */
    public static ControlTower createControlTower(Reader tick, Reader aircraft, Reader queues,
        Reader terminalsWithGates) throws MalformedSaveException, IOException {

        // instantiates empty queues and loading map
        TakeoffQueue takeoffQueue = new TakeoffQueue();
        LandingQueue landingQueue = new LandingQueue();
        TreeMap<Aircraft, Integer> loadingAircraft
                = new TreeMap<>(Comparator.comparing(Aircraft::getCallsign));

        try {
            // load all information to the collections with the given readers
            long ticks = loadTick(tick);
            List<Aircraft> aircrafts = loadAircraft(aircraft);
            List<Terminal> terminals = loadTerminalsWithGates(terminalsWithGates, aircrafts);
            loadQueues(queues, aircrafts, takeoffQueue, landingQueue, loadingAircraft);

            // create a control tower after acquiring all information from the load methods
            ControlTower controlTower = new ControlTower(ticks, aircrafts, landingQueue,
                    takeoffQueue, loadingAircraft);

            for (Terminal terminal : terminals) {
                controlTower.addTerminal(terminal);
            }

            return controlTower;

        } catch (MalformedSaveException mse) {
            throw new MalformedSaveException();
        } catch (IOException ioe) {
            throw new IOException();
        }
    }
}