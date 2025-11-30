package dev.turtywurty.tasklistcli.command;

import dev.turtywurty.tasklistcli.storage.AcceptsTaskStorage;
import dev.turtywurty.tasklistcli.storage.TaskStorage;
import dev.turtywurty.tasklistcli.storage.data.Task;
import dev.turtywurty.tasklistcli.storage.data.TaskList;

import java.util.concurrent.Callable;

import static picocli.CommandLine.Command;
import static picocli.CommandLine.Option;

@Command(
        name = "add",
        description = "Add a new task to your task list",
        mixinStandardHelpOptions = true
)
public class AddTaskCommand implements AcceptsTaskStorage, Callable<Integer> {
    private final TaskStorage storage;
    @Option(
            names = {"-L", "--list"},
            description = "The task list to add the task to",
            defaultValue = "default"
    )
    private String listName;
    @Option(
            names = {"-t", "--title", "-n", "--name"},
            description = "The title of the task",
            required = true
    )
    private String title;
    @Option(
            names = {"-d", "--description"},
            description = "The description of the task"
    )
    private String description;
    @Option(
            names = {"-p", "--priority"},
            description = "The priority of the task (low, medium, high)",
            defaultValue = "medium"
    )
    private String priority;
    @Option(
            names = {"-D", "--due-date"},
            description = "The due date of the task (YYYY-MM-DD)"
    )
    private String dueDate;

    @Option(
            names = {"--tags"},
            description = "A list of tags to add to this task"
    )
    private String[] tags;

    public AddTaskCommand(TaskStorage storage) {
        this.storage = storage;
    }

    @Override
    public Integer call() {
        String description = this.description != null ? this.description : "No description";
        Task.Priority priority;
        try {
            priority = Task.Priority.valueOf(this.priority.toUpperCase());
        } catch (IllegalArgumentException ignored) {
            System.out.println("Invalid priority level: " + this.priority + ". Using MEDIUM priority.");
            priority = Task.Priority.MEDIUM;
        }

        String dueDate = this.dueDate != null ? this.dueDate : "No due date";
        System.out.println("Adding task:");
        System.out.println("Title: " + title);
        System.out.println("List: " + listName);
        System.out.println("Description: " + description);
        System.out.println("Priority: " + priority);
        System.out.println("Due Date: " + dueDate);

        TaskList taskList = storage.getTaskList(listName, true);
        if (taskList.hasTask(title)) {
            System.out.println("A task with the title '" + title + "' already exists in the list '" + listName + "'.");
            return 1;
        }

        var task = new Task(title);
        task.setDescription(description);
        task.setPriority(priority);
        if (this.dueDate != null) {
            task.setDueDate(this.dueDate);
        }

        if (tags != null) {
            for (String tag : tags) {
                task.addTag(tag);
            }
        }

        taskList.addTask(task);
        storage.saveChanges();
        return 0;
    }
}
