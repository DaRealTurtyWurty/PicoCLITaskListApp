package dev.turtywurty.tasklistcli;

import dev.turtywurty.tasklistcli.storage.TaskStorage;
import dev.turtywurty.tasklistcli.storage.data.TaskList;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import picocli.CommandLine;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

public class CompleteTaskTest {
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
    void completesExistingTask() {
        this.cli.execute("add", "--title", "Buy milk");
        int exitCode = this.cli.execute("complete", "--list", "default", "--task", "Buy milk");

        assertEquals(0, exitCode);
        assertTrue(this.storage.getTaskList("default", false).getTask("Buy milk", false).isCompleted());
    }

    @Test
    void completingTaskInMissingListFails() {
        try (var io = CommandTestUtils.captureIO()) {
            int exitCode = this.cli.execute("complete", "--list", "missing", "--task", "Buy milk");
            assertEquals(1, exitCode);
            assertTrue(io.err().contains("Task list 'missing' does not exist."));
        }
    }

    @Test
    void completingMissingTaskFails() {
        TaskList list = this.storage.getTaskList("default", true);
        list.addTask(new dev.turtywurty.tasklistcli.storage.data.Task("Walk the dog"));

        try (var io = CommandTestUtils.captureIO()) {
            int exitCode = this.cli.execute("complete", "--list", "default", "--task", "Buy milk");
            assertEquals(1, exitCode);
            assertTrue(io.err().contains("Task 'Buy milk' does not exist in list 'default'."));
        }
    }

    @Test
    void canMarkTaskAsIncomplete() {
        this.cli.execute("add", "--title", "Buy milk");
        this.cli.execute("complete", "--list", "default", "--task", "Buy milk");
        int exitCode = this.cli.execute("complete", "--list", "default", "--task", "Buy milk", "--completed=false");

        assertEquals(0, exitCode);
        assertFalse(this.storage.getTaskList("default", false).getTask("Buy milk", false).isCompleted());
    }
}
