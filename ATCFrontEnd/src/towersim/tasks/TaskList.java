package towersim.tasks;

import towersim.util.Encodable;
import towersim.util.MalformedSaveException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.StringJoiner;

/**
 * Represents a circular list of tasks for an aircraft to cycle through.
 * @ass1
 */
public class TaskList implements Encodable {
    /** List of tasks to cycle through. */
    private final List<Task> tasks;
    /** Index of current task in tasks list. */
    private int currentTaskIndex;

    /**
     * Creates a new TaskList with the given list of tasks.
     * <p>
     * Initially, the current task (as returned by {@link #getCurrentTask()}) should be the first
     * task in the given list.
     *
     * @param tasks list of tasks
     * @ass1
     */
    public TaskList(List<Task> tasks) {

        // validates the task list by returning true or false
        if (!validateTaskList(tasks)) {
            throw new IllegalArgumentException();
        }

        this.tasks = tasks;
        this.currentTaskIndex = 0;
    }

    /**
     * Returns true or false depending on whether the tasklist is of valid ordering
     *
     * @param tasks the task list to be considered for validation
     * @return true if it is a valid task list, and false otherwise
     */
    private static boolean validateTaskList(List<Task> tasks) {
        if (tasks.isEmpty()) {
            return false;
        }

        if (tasks.size() == 1) {
            // since only 1 element in the list, get(0) returns the only element available
            return tasks.get(0).getType() == TaskType.AWAY
                    || tasks.get(0).getType() == TaskType.WAIT;
        }

        // creates a copy of the input task list for validation
        List<Task> testingTasks = new ArrayList<>(tasks);

        /*
        Appends the first element of the list (0th index) to the end of the copied list.
        This allows for the last element of the task list to be compared to with
        the first element of the task list during the iteration by comparing to the next element.
        Since this is a cyclic list, this means that each element would have been compared to
        with the element in front of it.
         */
        testingTasks.add(testingTasks.get(0));

        // compares each task with the next task in the list for all tasks
        // distinctively checks that only certain tasks can come after a specific task,
        // returning false if the criteria is violated
        for (int i = 0; i < testingTasks.size() - 1; i++) {
            if (testingTasks.get(i).getType() == TaskType.AWAY) {
                if (testingTasks.get(i + 1).getType() != TaskType.AWAY
                        && testingTasks.get(i + 1).getType() != TaskType.LAND) {
                    return false;
                }
            } else if (testingTasks.get(i).getType() == TaskType.LAND) {
                if (testingTasks.get(i + 1).getType() != TaskType.WAIT
                        && testingTasks.get(i + 1).getType() != TaskType.LOAD) {
                    return false;
                }
            } else if (testingTasks.get(i).getType() == TaskType.WAIT) {
                if (testingTasks.get(i + 1).getType() != TaskType.WAIT
                        && testingTasks.get(i + 1).getType() != TaskType.LOAD) {
                    return false;
                }
            } else if (testingTasks.get(i).getType() == TaskType.LOAD) {
                if (testingTasks.get(i + 1).getType() != TaskType.TAKEOFF) {
                    return false;
                }
            } else if (testingTasks.get(i).getType() == TaskType.TAKEOFF) {
                if (testingTasks.get(i + 1).getType() != TaskType.AWAY) {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * Returns the current task in the list.
     *
     * @return current task
     * @ass1
     */
    public Task getCurrentTask() {
        return this.tasks.get(this.currentTaskIndex);
    }

    /**
     * Returns the task in the list that comes after the current task.
     * <p>
     * After calling this method, the current task should still be the same as it was before calling
     * the method.
     * <p>
     * Note that the list is treated as circular, so if the current task is the last in the list,
     * this method should return the first element of the list.
     *
     * @return next task
     * @ass1
     */
    public Task getNextTask() {
        int nextTaskIndex = (this.currentTaskIndex + 1) % this.tasks.size();
        return this.tasks.get(nextTaskIndex);
    }

    /**
     * Moves the reference to the current task forward by one in the circular task list.
     * <p>
     * After calling this method, the current task should be the next task in the circular list
     * after the "old" current task.
     * <p>
     * Note that the list is treated as circular, so if the current task is the last in the list,
     * the new current task should be the first element of the list.
     * @ass1
     */
    public void moveToNextTask() {
        this.currentTaskIndex = (this.currentTaskIndex + 1) % this.tasks.size();
    }

    /**
     * Returns the human-readable string representation of this task list.
     * <p>
     * The format of the string to return is
     * <pre>TaskList currently on currentTask [taskNum/totalNumTasks]</pre>
     * where {@code currentTask} is the {@code toString()} representation of the current task as
     * returned by {@link Task#toString()},
     * {@code taskNum} is the place the current task occurs in the task list, and
     * {@code totalNumTasks} is the number of tasks in the task list.
     * <p>
     * For example, a task list with the list of tasks {@code [AWAY, LAND, WAIT, LOAD, TAKEOFF]}
     * which is currently on the {@code WAIT} task would have a string representation of
     * {@code "TaskList currently on WAIT [3/5]"}.
     *
     * @return string representation of this task list
     * @ass1
     */
    @Override
    public String toString() {
        return String.format("TaskList currently on %s [%d/%d]",
                this.getCurrentTask(),
                this.currentTaskIndex + 1,
                this.tasks.size());
    }

    /**
     * Returns the machine-readable string representation of this task list.
     * The format of the string to return is
     * encodedTask1,encodedTask2,...,encodedTaskN
     *
     * @return encoded string representation of this task list
     */
    public String encode() {
        StringJoiner joiner = new StringJoiner(",");

        for (int i = 0; i < this.tasks.size(); i++) {
            joiner.add(getCurrentTask().encode());
            moveToNextTask();
        }
        return joiner.toString();
    }
}
