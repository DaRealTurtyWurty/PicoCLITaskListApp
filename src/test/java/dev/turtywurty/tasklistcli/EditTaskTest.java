package dev.turtywurty.tasklistcli;

import dev.turtywurty.tasklistcli.storage.TaskStorage;
import dev.turtywurty.tasklistcli.storage.data.Task;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import picocli.CommandLine;

import java.nio.file.Path;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

public class EditTaskTest {
    @TempDir
    Path tempDir;

    private TaskStorage storage;
    private CommandLine cli;
    private Task initialTask;
    private String initialTaskId;

    @BeforeEach
    void setup() {
        this.storage = new TaskStorage(tempDir.resolve("tasks.json"), false);
        this.cli = CommandTestUtils.createCommandLine(this.storage);
        this.cli.execute("add", "--title", "Buy milk", "--description", "Whole milk", "--priority", "medium");
        this.initialTask = this.storage.getTaskList("default", false).getTask("Buy milk", false);
        this.initialTaskId = this.initialTask.getUuid().toString();
    }

    @Test
    void editsMultipleFieldsSuccessfully() {
        int exitCode = this.cli.execute(
                "edit",
                "--id", this.initialTaskId,
                "--name", "Buy bread",
                "--description", "Wholemeal",
                "--priority", "high",
                "--due-date", "2025-06-01",
                "--add-tags", "food",
                "--add-tags", "bakery");

        assertEquals(0, exitCode);
        Task task = this.storage.getTaskList("default", false).getTask("Buy bread", false);
        assertNotNull(task);
        assertEquals("Buy bread", task.getName());
        assertEquals("Wholemeal", task.getDescription());
        assertEquals(Task.Priority.HIGH, task.getPriority());
        assertEquals("2025-06-01", task.getDueDate());
        assertTrue(task.getTags().containsAll(List.of("food", "bakery")));
    }

    @Test
    void invalidUuidFails() {
        try (var io = CommandTestUtils.captureIO()) {
            int exitCode = this.cli.execute("edit", "--id", "not-a-uuid", "--name", "Something");
            assertEquals(1, exitCode);
            assertTrue(io.err().contains("Invalid task UUID provided"));
        }
    }

    @Test
    void missingTaskFails() {
        String missingId = UUID.randomUUID().toString();
        try (var io = CommandTestUtils.captureIO()) {
            int exitCode = this.cli.execute("edit", "--id", missingId, "--name", "New name");
            assertEquals(1, exitCode);
            assertTrue(io.err().contains("No task found with the ID '" + missingId + "'"));
        }
    }

    @Test
    void invalidPriorityDoesNotOverrideExistingPriority() {
        Task.Priority originalPriority = this.initialTask.getPriority();
        try (var io = CommandTestUtils.captureIO()) {
            int exitCode = this.cli.execute("edit", "--id", this.initialTaskId, "--priority", "urgent");
            assertEquals(1, exitCode);
            assertTrue(io.err().contains("Invalid priority found 'urgent'. Keeping priority as 'Medium'"));
        }

        Task task = this.storage.findTaskByUUID(UUID.fromString(this.initialTaskId));
        assertEquals(originalPriority, task.getPriority());
    }

    @Test
    void invalidDueDateDoesNotUpdateTask() {
        try (var io = CommandTestUtils.captureIO()) {
            int exitCode = this.cli.execute("edit", "--id", this.initialTaskId, "--due-date", "12-31-2025");
            assertEquals(1, exitCode);
            assertTrue(io.err().contains("Invalid due date format, should be YYYY-MM-DD."));
        }

        Task task = this.storage.findTaskByUUID(UUID.fromString(this.initialTaskId));
        assertNull(task.getDueDate());
    }

    @Test
    void canClearAndRemoveTags() {
        this.cli.execute("edit", "--id", this.initialTaskId, "--add-tags", "home", "--add-tags", "urgent");
        int exitCode = this.cli.execute("edit", "--id", this.initialTaskId, "--clear-tags", "--remove-tags", "home");

        assertEquals(0, exitCode);
        Task task = this.storage.findTaskByUUID(UUID.fromString(this.initialTaskId));
        assertTrue(task.getTags().isEmpty());
    }
}
