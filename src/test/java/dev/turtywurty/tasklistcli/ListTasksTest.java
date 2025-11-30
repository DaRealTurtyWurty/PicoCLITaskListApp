package dev.turtywurty.tasklistcli;

import dev.turtywurty.tasklistcli.storage.TaskStorage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import picocli.CommandLine;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

public class ListTasksTest {
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
    void listingNonexistentListFails() {
        try (var io = CommandTestUtils.captureIO()) {
            int exitCode = this.cli.execute("list", "--list", "nonexistent");
            assertEquals(1, exitCode);
            assertTrue(io.out().contains("Task list 'nonexistent' does not exist."));
        }
    }

    @Test
    void listingAllWithNoTasksShowsHint() {
        try (var io = CommandTestUtils.captureIO()) {
            int exitCode = this.cli.execute("list", "--everything");
            assertEquals(0, exitCode);
            assertTrue(io.out().contains("You have no tasks. Use 'add --list \"list name\" --name \"task name\"' to add a task."));
        }
    }

    @Test
    void listsTasksFromSpecificList() {
        this.cli.execute("add", "--title", "Buy milk");
        this.cli.execute("add", "--title", "Walk the dog");

        try (var io = CommandTestUtils.captureIO()) {
            int exitCode = this.cli.execute("list", "--list", "default");
            assertEquals(0, exitCode);
            String output = io.out();
            assertTrue(output.contains("Tasks in list: default"));
            assertTrue(output.contains("Buy milk"));
            assertTrue(output.contains("Walk the dog"));
        }
    }

    @Test
    void filtersByPriority() {
        this.cli.execute("add", "--title", "Buy milk", "--priority", "high");
        this.cli.execute("add", "--title", "Walk the dog", "--priority", "low");

        try (var io = CommandTestUtils.captureIO()) {
            int exitCode = this.cli.execute("list", "--priority", "high");
            assertEquals(0, exitCode);
            String output = io.out();
            assertTrue(output.contains("Buy milk"));
            assertTrue(output.contains("(Priority: HIGH)"));
            assertFalse(output.contains("Walk the dog"));
        }
    }

    @Test
    void filtersByDueDate() {
        this.cli.execute("add", "--title", "Buy milk", "--due-date", "2025-12-01");
        this.cli.execute("add", "--title", "Walk the dog", "--due-date", "2025-12-31");

        try (var io = CommandTestUtils.captureIO()) {
            int exitCode = this.cli.execute("list", "--due-before", "2025-12-15");
            assertEquals(0, exitCode);
            String output = io.out();
            assertTrue(output.contains("Buy milk"));
            assertTrue(output.contains("(Due: 2025-12-01)"));
            assertFalse(output.contains("Walk the dog"));
        }
    }

    @Test
    void showsOnlyCompletedTasks() {
        this.cli.execute("add", "--title", "Buy milk");
        this.cli.execute("add", "--title", "Walk the dog");
        this.cli.execute("complete", "--list", "default", "--task", "Walk the dog");

        try (var io = CommandTestUtils.captureIO()) {
            int exitCode = this.cli.execute("list", "--completed");
            assertEquals(0, exitCode);
            String output = io.out();
            assertFalse(output.contains("Buy milk"));
            assertTrue(output.contains("Walk the dog"));
            assertTrue(output.contains("[X]"));
        }
    }

    @Test
    void showsOnlyIncompleteTasks() {
        this.cli.execute("add", "--title", "Buy milk");
        this.cli.execute("add", "--title", "Walk the dog");
        this.cli.execute("complete", "--list", "default", "--task", "Walk the dog");

        try (var io = CommandTestUtils.captureIO()) {
            int exitCode = this.cli.execute("list", "--incomplete");
            assertEquals(0, exitCode);
            String output = io.out();
            assertTrue(output.contains("Buy milk"));
            assertFalse(output.contains("Walk the dog"));
        }
    }

    @Test
    void listsAcrossAllListsAndIncludesIds() {
        this.cli.execute("add", "--title", "Buy milk");
        this.cli.execute("add", "--list", "Work", "--title", "Write report", "--priority", "high");

        try (var io = CommandTestUtils.captureIO()) {
            int exitCode = this.cli.execute("list", "--everything", "--include-ids");
            assertEquals(0, exitCode);
            String output = io.out();
            assertTrue(output.contains("Tasks in list: default"));
            assertTrue(output.contains("Tasks in list: Work"));
            assertTrue(output.contains("Buy milk"));
            assertTrue(output.contains("Write report"));
            assertTrue(output.contains("ID:"));
        }
    }
}
