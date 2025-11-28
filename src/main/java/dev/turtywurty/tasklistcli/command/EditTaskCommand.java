package dev.turtywurty.tasklistcli.command;

import dev.turtywurty.tasklistcli.storage.AcceptsTaskStorage;
import dev.turtywurty.tasklistcli.storage.TaskStorage;
import dev.turtywurty.tasklistcli.storage.data.Task;
import dev.turtywurty.tasklistcli.util.TimeUtils;
import picocli.CommandLine;

import java.util.Arrays;
import java.util.UUID;
import java.util.concurrent.Callable;

import static picocli.CommandLine.*;

@Command(
        name = "edit",
        description = "Edits a task's attributes (e.g. name, description, due date, etc)",
        mixinStandardHelpOptions = true
)
public class EditTaskCommand implements Callable<Integer>, AcceptsTaskStorage {
    @Option(
            names = {"-i", "--id"},
            description = "The id of the task which you wish to edit",
            required = true
    )
    private String uuid;

    @Option(
            names = {"-n", "--name", "-t", "--title"},
            description = "The new name/title of the task"
    )
    private String name;

    @Option(
            names = {"-d", "--description"},
            description = "The new description of the task"
    )
    private String description;

    @Option(
            names = {"-p", "--priority"},
            description = "The new priority of the task"
    )
    private String priority;

    @Option(
            names = {"-dd", "--due-date"},
            description = "The new due date of the task"
    )
    private String dueDate;

    @Option(
            names = {"-at", "--add-tags"},
            description = "A set of tags to add to the task"
    )
    private String[] addTags;

    @Option(
            names = {"-rt", "--remove-tags"},
            description = "A set of tags to remove from the task"
    )
    private String[] removeTags;

    @Option(
            names = {"-ct", "--clear-tags"},
            description = "Clear the current tags from the task"
    )
    private boolean clearCurrentTags;

    private final TaskStorage storage;

    public EditTaskCommand(TaskStorage storage) {
        this.storage = storage;
    }

    @Override
    public Integer call() {
        UUID uuid;
        try {
            uuid = UUID.fromString(this.uuid);
        } catch (IllegalArgumentException ignored) {
            System.err.printf("Invalid task UUID provided: %s%n", this.uuid);
            return 1;
        }

        Task task = this.storage.findTaskByUUID(uuid);
        if (task == null) {
            System.err.printf("No task found with the ID '%s', use 'list -e' to view all tasks", uuid);
            return 1;
        }

        boolean worked = false;

        if (this.name != null) {
            task.setName(name);
            worked = true;
        }

        if (this.description != null) {
            task.setDescription(description);
            worked = true;
        }

        if (this.priority != null) {
            try {
                task.setPriority(Task.Priority.valueOf(this.priority.toUpperCase()));
                worked = true;
            } catch (IllegalArgumentException ignored) {
                System.err.printf("Invalid priority found '%s'. Keeping priority as '%s'", this.priority, task.getPriority().getDisplayName());
            }
        }

        if (this.dueDate != null) {
            if (TimeUtils.parseOrNull(this.dueDate) != null) {
                task.setDueDate(this.dueDate);
                worked = true;
            } else {
                System.err.println("Invalid due date format, should be YYYY-MM-DD.");
            }
        }

        if (this.clearCurrentTags) {
            task.getTags().clear();
            worked = true;
        }

        if (this.addTags != null && this.addTags.length != 0) {
            task.getTags().addAll(Arrays.asList(this.addTags));
            worked = true;
        }

        if (this.removeTags != null && this.removeTags.length != 0) {
            Arrays.asList(this.removeTags).forEach(task.getTags()::remove);
            worked = true;
        }

        System.err.printf(worked ? "Successfully updated task ID: '%s'" : "Failed to update task ID: '%s'", this.uuid);
        return worked ? 0 : 1;
    }
}
