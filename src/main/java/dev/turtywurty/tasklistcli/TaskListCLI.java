package dev.turtywurty.tasklistcli;

import dev.turtywurty.tasklistcli.command.TaskCommand;
import dev.turtywurty.tasklistcli.util.StringUtils;
import dev.turtywurty.tasklistcli.util.TaskFactory;
import picocli.CommandLine;

import java.util.Scanner;

public class TaskListCLI {
    public static final String APPLICATION_NAME = "TaskListCLI";

    public static void main(String[] args) {
        System.out.println("Task List CLI v1.0");
        var taskFactory = new TaskFactory();
        var commandLine = new CommandLine(new TaskCommand(), taskFactory);
        var scanner = new Scanner(System.in);
        while (!Thread.interrupted()) {
            System.out.print("> ");
            String input = scanner.nextLine().trim();
            if (shouldExit(input)) {
                taskFactory.getStorage().saveChanges();
                break;
            }

            String[] arguments = StringUtils.readCommand(input);
            commandLine.execute(arguments);
            System.out.println();
        }
    }

    private static boolean shouldExit(String input) {
        return input.equalsIgnoreCase("exit") || input.equalsIgnoreCase("quit");
    }
}
