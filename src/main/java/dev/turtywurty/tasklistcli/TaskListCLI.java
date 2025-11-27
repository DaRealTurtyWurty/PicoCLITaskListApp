package dev.turtywurty.tasklistcli;

import dev.turtywurty.tasklistcli.command.TaskCommand;
import dev.turtywurty.tasklistcli.util.TaskFactory;
import picocli.CommandLine;

public class TaskListCLI {
    public static final String APPLICATION_NAME = "TaskListCLI";

    public static void main(String[] args) {
        System.out.println("Task List CLI v1.0");
        System.exit(new CommandLine(new TaskCommand(), new TaskFactory()).execute(args));
    }
}
