package dev.turtywurty.tasklistcli.command;

import dev.turtywurty.tasklistcli.storage.AcceptsTaskStorage;
import dev.turtywurty.tasklistcli.storage.TaskStorage;
import dev.turtywurty.tasklistcli.storage.data.Task;
import dev.turtywurty.tasklistcli.storage.data.TaskList;
import dev.turtywurty.tasklistcli.util.TimeUtils;

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

    @Option(
            names = {"-i", "--incomplete"},
            description = "Show only incomplete tasks",
            defaultValue = "false"
    )
    private boolean showIncomplete;

    @Option(
            names = {"-e", "--everything"},
            description = "Show all tasks from all lists",
            defaultValue = "false"
    )
    private boolean showAllLists;

    public ListTasksCommand(TaskStorage storage) {
        this.storage = storage;
    }

    @Override
    public Integer call() {
        List<TaskList> taskLists = new ArrayList<>();
        if (showAllLists) {
            taskLists.addAll(storage.getTaskLists());
        } else {
            TaskList taskList = storage.getTaskList(listName, false);
            if (taskList == null) {
                System.out.println("Task list '" + listName + "' does not exist.");
                return 1;
            }

            taskLists.add(taskList);
        }

        for (TaskList taskList : taskLists) {
            System.out.println("Tasks in list: " + taskList.getName());
            List<Task> tasks = taskList.getTasks();

            for (Task task : tasks) {
                if (priorityFilter != null && !task.getPriority().name().equalsIgnoreCase(priorityFilter))
                    continue;

                if (dueBefore != null) {
                    LocalDate dueDate = TimeUtils.parseOrNull(dueBefore);
                    LocalDate taskDueDate = TimeUtils.parseOrNull(task.getDueDate());
                    if (dueDate == null || taskDueDate == null || !taskDueDate.isBefore(dueDate))
                        continue;
                }

                if (showCompleted && !task.isCompleted())
                    continue;

                if (showIncomplete && task.isCompleted())
                    continue;

                System.out.println(task);
            }
        }

        return 0;
    }
}
