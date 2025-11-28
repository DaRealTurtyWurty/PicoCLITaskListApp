package dev.turtywurty.tasklistcli;

import dev.turtywurty.tasklistcli.command.TaskCommand;
import dev.turtywurty.tasklistcli.storage.TaskStorage;
import org.junit.jupiter.api.Test;
import picocli.CommandLine;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class RemoveTaskTest {
    private static CommandLine createCLI(TaskStorage storage) {
        return new CommandLine(new TaskCommand(), new CommandLine.IFactory() {
            @Override
            public <K> K create(Class<K> cls) throws Exception {
                return cls.getConstructor(TaskStorage.class).newInstance(storage);
            }
        });
    }

    @Test
    public void testRemoveTask() throws IOException {
        Path tempPath = Files.createTempFile("tasklistcli-test", ".json");
        var storage = new TaskStorage(tempPath, false);
        CommandLine cli = createCLI(storage);
        cli.execute("add", "--title", "Buy milk");
        int exitCode = cli.execute("remove", "--name", "Buy milk");

        assertEquals(0, exitCode);
        assertEquals(0, storage.getAllTasks().size());

        Files.deleteIfExists(tempPath);
    }

    @Test
    public void testRemoveNonExistentTask() throws IOException {
        Path tempPath = Files.createTempFile("tasklistcli-test", ".json");
        var storage = new TaskStorage(tempPath, false);
        CommandLine cli = createCLI(storage);
        int exitCode = cli.execute("remove", "--name", "Buy milk");

        assertEquals(1, exitCode);

        Files.deleteIfExists(tempPath);
    }
}