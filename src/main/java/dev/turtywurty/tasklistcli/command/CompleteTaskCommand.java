package dev.turtywurty.tasklistcli.command;

import dev.turtywurty.tasklistcli.storage.AcceptsTaskStorage;
import dev.turtywurty.tasklistcli.storage.TaskStorage;
import dev.turtywurty.tasklistcli.storage.data.Task;
import dev.turtywurty.tasklistcli.storage.data.TaskList;

import java.util.concurrent.Callable;

import static picocli.CommandLine.Command;
import static picocli.CommandLine.Option;

@Command(
        name = "complete",
        description = "Mark a task as completed",
        mixinStandardHelpOptions = true
)
public class CompleteTaskCommand implements Callable<Integer>, AcceptsTaskStorage {
    private final TaskStorage storage;
    @Option(
            names = {"-l", "--list"},
            description = "The list this task is in",
            required = true
    )
    private String listName;
    @Option(
            names = {"-t", "--task"},
            description = "The name of the task",
            required = true
    )
    private String taskName;
    @Option(
            names = {"-c", "--completed"},
            description = "Mark the task as completed",
            required = false,
            defaultValue = "true"
    )
    private boolean completed;

    public CompleteTaskCommand(TaskStorage storage) {
        this.storage = storage;
    }

    @Override
    public Integer call() {
        TaskList list = this.storage.getTaskList(this.listName, false);
        if (list == null) {
            System.err.printf("Task list '%s' does not exist.%n", this.listName);
            return 1;
        }

        Task task = list.getTask(this.taskName, false);
        if (task == null) {
            System.err.printf("Task '%s' does not exist in list '%s'.%n", this.taskName, this.listName);
            return 1;
        }

        task.setCompleted(this.completed);
        this.storage.updateFileChanged();
        System.out.printf("Task '%s' in list '%s' marked as %s completed.%n", this.taskName, this.listName, this.completed ? "" : "not");
        return 0;
    }
}
