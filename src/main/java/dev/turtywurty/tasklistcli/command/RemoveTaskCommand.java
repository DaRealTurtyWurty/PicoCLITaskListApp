package dev.turtywurty.tasklistcli.command;

import dev.turtywurty.tasklistcli.storage.AcceptsTaskStorage;
import dev.turtywurty.tasklistcli.storage.TaskStorage;
import dev.turtywurty.tasklistcli.storage.data.TaskList;

import java.util.concurrent.Callable;

import static picocli.CommandLine.Command;
import static picocli.CommandLine.Option;

@Command(
        name = "remove",
        description = "Remove a task from your task list",
        mixinStandardHelpOptions = true
)
public class RemoveTaskCommand implements Callable<Integer>, AcceptsTaskStorage {
    private final TaskStorage storage;
    @Option(
            names = {"-L", "--list"},
            description = "The task list to remove the task from",
            defaultValue = "default"
    )
    private String listName;
    @Option(
            names = {"-n", "--name"},
            description = "The name of the task to remove",
            required = true
    )
    private String taskName;

    public RemoveTaskCommand(TaskStorage storage) {
        this.storage = storage;
    }

    @Override
    public Integer call() {
        TaskList taskList = storage.getTaskList(listName, false);
        if (taskList == null) {
            System.out.println("Task list '" + listName + "' does not exist.");
            return 1;
        }

        boolean removed = taskList.removeTaskByName(taskName);
        if (removed) {
            storage.saveChanges();
            System.out.println("Task '" + taskName + "' removed from list '" + listName + "'.");
            return 0;
        } else {
            System.out.println("Task '" + taskName + "' not found in list '" + listName + "'.");
            return 1;
        }
    }
}
