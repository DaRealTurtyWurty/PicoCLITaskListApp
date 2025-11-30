package dev.turtywurty.tasklistcli;

import dev.turtywurty.tasklistcli.storage.TaskStorage;
import dev.turtywurty.tasklistcli.storage.data.Task;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import picocli.CommandLine;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

public class AddTaskTest {
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
    void addsTaskWithDefaults() {
        int exitCode = this.cli.execute("add", "--title", "Buy milk");

        assertEquals(0, exitCode);
        assertEquals(1, this.storage.getTaskLists().size());
        Task task = this.storage.getTaskList("default", false).getTask("Buy milk", false);
        assertNotNull(task);
        assertEquals(Task.Priority.MEDIUM, task.getPriority());
        assertEquals("No description", task.getDescription());
        assertNull(task.getDueDate());
    }

    @Test
    void rejectsDuplicateTasksInSameList() {
        this.cli.execute("add", "--title", "Buy milk");
        int exitCode = this.cli.execute("add", "--title", "Buy milk");

        assertEquals(1, exitCode);
        assertEquals(1, this.storage.getAllTasks().size());
    }

    @Test
    void handlesInvalidPriorityGracefully() {
        try (var io = CommandTestUtils.captureIO()) {
            int exitCode = this.cli.execute("add", "--title", "Buy milk", "--priority", "urgent");
            assertEquals(0, exitCode);
            assertTrue(io.out().contains("Invalid priority level"));
        }

        Task task = this.storage.getTaskList("default", false).getTask("Buy milk", false);
        assertEquals(Task.Priority.MEDIUM, task.getPriority());
    }

    @Test
    void addsTaskWithTagsAndDueDate() {
        int exitCode = this.cli.execute(
                "add",
                "--title", "Buy milk",
                "--description", "Whole milk",
                "--priority", "high",
                "--due-date", "2025-12-01",
                "--tags", "home",
                "--tags", "groceries");

        assertEquals(0, exitCode);

        Task task = this.storage.getTaskList("default", false).getTask("Buy milk", false);
        assertEquals(Task.Priority.HIGH, task.getPriority());
        assertEquals("Whole milk", task.getDescription());
        assertEquals("2025-12-01", task.getDueDate());
        assertTrue(task.getTags().contains("home"));
        assertTrue(task.getTags().contains("groceries"));
    }

    @Test
    void addsTaskToSpecificList() {
        int exitCode = this.cli.execute("add", "--list", "Errands", "--title", "Buy groceries");

        assertEquals(0, exitCode);
        assertEquals(1, this.storage.getTaskLists().size());
        assertEquals("Errands", this.storage.getTaskLists().get(0).getName());
        Task task = this.storage.getTaskList("Errands", false).getTask("Buy groceries", false);
        assertNotNull(task);
    }
}
