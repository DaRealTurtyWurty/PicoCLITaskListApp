package dev.turtywurty.tasklistcli.command;

import java.util.concurrent.Callable;

import static picocli.CommandLine.Command;

@Command(
        name = "task",
        description = "Manage your tasks",
        subcommands = {
                AddTaskCommand.class,
                ListTasksCommand.class,
                RemoveTaskCommand.class,
                CompleteTaskCommand.class,
                EditTaskCommand.class
        },
        mixinStandardHelpOptions = true,
        version = "Task CLI 1.0"
)
public class TaskCommand implements Callable<Integer> {
    @Override
    public Integer call() {
        System.out.println("Use one of the subcommands to manage your tasks. Use --help for more information.");
        return 0;
    }
}
