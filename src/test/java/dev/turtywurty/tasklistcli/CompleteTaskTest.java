package dev.turtywurty.tasklistcli;

import dev.turtywurty.tasklistcli.command.TaskCommand;
import dev.turtywurty.tasklistcli.storage.TaskStorage;
import org.junit.jupiter.api.Test;
import picocli.CommandLine;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

public class CompleteTaskTest {
    private static CommandLine createCLI(TaskStorage storage) {
        return new CommandLine(new TaskCommand(), new CommandLine.IFactory() {
            @Override
            public <K> K create(Class<K> cls) throws Exception {
                return cls.getConstructor(TaskStorage.class).newInstance(storage);
            }
        });
    }

    @Test
    public void testCompleteTask() throws IOException {
        Path tempPath = Files.createTempFile("tasklistcli-test", ".json");
        var storage = new TaskStorage(tempPath, false);
        CommandLine cli = createCLI(storage);
        cli.execute("add", "--title", "Buy milk");
        int exitCode = cli.execute("complete", "--list", "default", "--task", "Buy milk");

        assertEquals(0, exitCode);
        assertTrue(storage.getTaskList("default", false).getTask("Buy milk", false).isCompleted());

        Files.deleteIfExists(tempPath);
    }

    @Test
    public void testCompleteNonExistentTask() throws IOException {
        Path tempPath = Files.createTempFile("tasklistcli-test", ".json");
        var storage = new TaskStorage(tempPath, false);
        CommandLine cli = createCLI(storage);
        int exitCode = cli.execute("complete", "--list", "default", "--task", "Buy milk");

        assertEquals(1, exitCode);

        Files.deleteIfExists(tempPath);
    }

    @Test
    public void testCompleteTaskInNonExistentList() throws IOException {
        Path tempPath = Files.createTempFile("tasklistcli-test", ".json");
        var storage = new TaskStorage(tempPath, false);
        CommandLine cli = createCLI(storage);
        cli.execute("add", "--title", "Buy milk");
        int exitCode = cli.execute("complete", "--list", "nonexistent", "--task", "Buy milk");

        assertEquals(1, exitCode);

        Files.deleteIfExists(tempPath);
    }

    @Test
    public void testMarkTaskAsNotCompleted() throws IOException {
        Path tempPath = Files.createTempFile("tasklistcli-test", ".json");
        var storage = new TaskStorage(tempPath, false);
        CommandLine cli = createCLI(storage);
        cli.execute("add", "--title", "Buy milk");
        cli.execute("complete", "--list", "default", "--task", "Buy milk");
        int exitCode = cli.execute("complete", "--list", "default", "--task", "Buy milk", "--completed=false");

        assertEquals(0, exitCode);
        assertFalse(storage.getTaskList("default", false).getTask("Buy milk", false).isCompleted());

        Files.deleteIfExists(tempPath);
    }
}