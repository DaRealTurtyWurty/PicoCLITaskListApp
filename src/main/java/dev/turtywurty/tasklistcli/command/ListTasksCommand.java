package dev.turtywurty.tasklistcli.command;

import dev.turtywurty.tasklistcli.storage.AcceptsTaskStorage;
import dev.turtywurty.tasklistcli.storage.TaskStorage;
import dev.turtywurty.tasklistcli.storage.data.Task;
import dev.turtywurty.tasklistcli.storage.data.TaskList;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

import static picocli.CommandLine.Command;
import static picocli.CommandLine.Option;

@Command(
        name = "list",
        description = "List all tasks in your task list",
        mixinStandardHelpOptions = true
)
public class ListTasksCommand implements Callable<Integer>, AcceptsTaskStorage {
    private final TaskStorage storage;
    @Option(
            names = {"-L", "--list"},
            description = "The task list to display",
            defaultValue = "default"
    )
    private String listName;
    @Option(
            names = {"-a", "--all"},
            description = "Show all tasks, including completed ones"
    )
    private boolean showAll;
    @Option(
            names = {"-p", "--priority"},
            description = "Filter tasks by priority (low, medium, high)"
    )
    private String priorityFilter;
    @Option(
            names = {"-d", "--due-before"},
            description = "Show tasks due before the specified date (YYYY-MM-DD)"
    )
    private String dueBefore;
    @Option(
            names = {"-c", "--completed"},
            description = "Show only completed tasks"
    )
    private boolean showCompleted;

    public ListTasksCommand(TaskStorage storage) {
        this.storage = storage;
    }

    @Override
    public Integer call() {
        TaskList taskList = storage.getTaskList(listName, false);
        if (taskList == null) {
            System.out.println("Task list '" + listName + "' does not exist.");
            return 1;
        }

        List<Task> tasks = new ArrayList<>(taskList.getTasks());
        if (priorityFilter != null) {
            tasks.removeIf(task -> !task.getPriority().name().equalsIgnoreCase(priorityFilter));
        }

        if (dueBefore != null) {
            tasks.removeIf(task -> task.getDueDate() == null || !LocalDate.parse(task.getDueDate()).isBefore(LocalDate.parse(dueBefore)));
        }

        if (showCompleted) {
            tasks.removeIf(task -> !task.isCompleted());
        } else if (!showAll) {
            tasks.removeIf(Task::isCompleted);
        }

        if (tasks.isEmpty()) {
            System.out.println("No tasks found in list '" + listName + "'.");
            return 0;
        }

        System.out.println("Tasks in list '" + listName + "':");
        for (var task : tasks) {
            System.out.println("- [" + (task.isCompleted() ? "x" : " ") + "] " + task.getName() +
                    (task.getDueDate() != null ? " (Due: " + task.getDueDate() + ")" : "") +
                    " [Priority: " + task.getPriority() + "]");
        }

        return 0;
    }
}
