package dev.turtywurty.tasklistcli;

import dev.turtywurty.tasklistcli.storage.TaskStorage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import picocli.CommandLine;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class RemoveTaskTest {
    @TempDir
    Path tempDir;

    private TaskStorage storage;
    private CommandLine cli;

    @BeforeEach
    void setup() {
        this.storage = new TaskStorage(tempDir.resolve("tasks.json"), false);
        this.cli = CommandTestUtils.createCommandLine(this.storage);
    }

    @Test
    void removesExistingTask() {
        this.cli.execute("add", "--title", "Buy milk");
        int exitCode = this.cli.execute("remove", "--name", "Buy milk");

        assertEquals(0, exitCode);
        assertTrue(this.storage.getAllTasks().isEmpty());
    }

    @Test
    void removingMissingTaskFails() {
        // Ensure the default list exists but is empty
        this.storage.getTaskList("default", true);

        try (var io = CommandTestUtils.captureIO()) {
            int exitCode = this.cli.execute("remove", "--name", "Buy milk");
            assertEquals(1, exitCode);
            assertTrue(io.out().contains("Task 'Buy milk' not found in list 'default'."));
        }
    }

    @Test
    void removingFromMissingListFails() {
        this.cli.execute("add", "--list", "Errands", "--title", "Buy groceries");

        try (var io = CommandTestUtils.captureIO()) {
            int exitCode = this.cli.execute("remove", "--list", "Work", "--name", "Buy groceries");
            assertEquals(1, exitCode);
            assertTrue(io.out().contains("Task list 'Work' does not exist."));
        }
    }
}
