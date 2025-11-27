package dev.turtywurty.tasklistcli;

import dev.turtywurty.tasklistcli.command.TaskCommand;
import dev.turtywurty.tasklistcli.storage.TaskStorage;
import org.junit.jupiter.api.Test;
import picocli.CommandLine;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class AddTaskTest {
    @Test
    public void testAddTask() throws IOException {
        Path tempPath = Files.createTempFile("tasklistcli-test", ".json");
        var storage = new TaskStorage(tempPath);
        CommandLine cli = createCLI(storage);
        int exitCode = cli.execute("add", "--title", "Buy milk");

        assertEquals(0, exitCode);
        assertEquals(1, storage.getTaskLists().size());
        assertEquals(1, storage.getAllTasks().size());
        assertEquals("Buy milk", storage.getAllTasks().getFirst().getName());

        Files.deleteIfExists(tempPath);
    }

    @Test
    public void testAddMultipleTasks() throws IOException {
        Path tempPath = Files.createTempFile("tasklistcli-test", ".json");
        var storage = new TaskStorage(tempPath);
        var cli = createCLI(storage);
        cli.execute("add", "--title", "Buy milk");
        cli.execute("add", "--title", "Walk the dog");

        assertEquals(2, storage.getAllTasks().size());
        assertEquals("Buy milk", storage.getAllTasks().get(0).getName());
        assertEquals("Walk the dog", storage.getAllTasks().get(1).getName());

        Files.deleteIfExists(tempPath);
    }

    @Test
    public void testAddTaskToSpecificList() throws IOException {
        Path tempPath = Files.createTempFile("tasklistcli-test", ".json");
        var storage = new TaskStorage(tempPath);
        var cli = createCLI(storage);
        cli.execute("add-list", "--name", "Errands");
        cli.execute("add", "--title", "Buy groceries", "--list", "Errands");

        assertEquals(1, storage.getTaskLists().size());
        assertEquals(1, storage.getAllTasks().size());
        assertEquals("Buy groceries", storage.getAllTasks().getFirst().getName());
        assertEquals("Errands", storage.getTaskLists().getFirst().getName());

        Files.deleteIfExists(tempPath);
    }

    private static CommandLine createCLI(TaskStorage storage) {
        return new CommandLine(new TaskCommand(), new CommandLine.IFactory() {
            @Override
            public <K> K create(Class<K> cls) throws Exception {
                return cls.getConstructor(TaskStorage.class).newInstance(storage);
            }
        });
    }
}
