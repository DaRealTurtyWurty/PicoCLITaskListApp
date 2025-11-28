package dev.turtywurty.tasklistcli;

import dev.turtywurty.tasklistcli.command.TaskCommand;
import dev.turtywurty.tasklistcli.storage.TaskStorage;
import org.junit.jupiter.api.Test;
import picocli.CommandLine;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

public class ListTasksTest {
    private static CommandLine createCLI(TaskStorage storage) {
        return new CommandLine(new TaskCommand(), new CommandLine.IFactory() {
            @Override
            public <K> K create(Class<K> cls) throws Exception {
                return cls.getConstructor(TaskStorage.class).newInstance(storage);
            }
        });
    }

    @Test
    public void testListNoTasks() throws IOException {
        Path tempPath = Files.createTempFile("tasklistcli-test", ".json");
        var storage = new TaskStorage(tempPath, false);
        CommandLine cli = createCLI(storage);

        var baos = new ByteArrayOutputStream();
        System.setOut(new PrintStream(baos));

        int exitCode = cli.execute("list");

        System.setOut(System.out);

        assertEquals(1, exitCode);
        assertTrue(baos.toString().contains("Task list 'default' does not exist."));

        Files.deleteIfExists(tempPath);
    }

    @Test
    public void testListNonExistentList() throws IOException {
        Path tempPath = Files.createTempFile("tasklistcli-test", ".json");
        var storage = new TaskStorage(tempPath, false);
        CommandLine cli = createCLI(storage);

        var baos = new ByteArrayOutputStream();
        System.setOut(new PrintStream(baos));

        int exitCode = cli.execute("list", "--list", "nonexistent");

        System.setOut(System.out);

        assertEquals(1, exitCode);
        assertTrue(baos.toString().contains("Task list 'nonexistent' does not exist."));

        Files.deleteIfExists(tempPath);
    }

    @Test
    public void testListAllTasks() throws IOException {
        Path tempPath = Files.createTempFile("tasklistcli-test", ".json");
        var storage = new TaskStorage(tempPath, false);
        CommandLine cli = createCLI(storage);
        cli.execute("add", "--title", "Buy milk");
        cli.execute("add", "--title", "Walk the dog");
        cli.execute("complete", "--list", "default", "--task", "Walk the dog");

        var baos = new ByteArrayOutputStream();
        System.setOut(new PrintStream(baos));

        int exitCode = cli.execute("list", "--all");

        System.setOut(System.out);

        assertEquals(0, exitCode);
        String output = baos.toString();
        assertTrue(output.contains("Buy milk"));
        assertTrue(output.contains("Walk the dog"));

        Files.deleteIfExists(tempPath);
    }

    @Test
    public void testFilterByPriority() throws IOException {
        Path tempPath = Files.createTempFile("tasklistcli-test", ".json");
        var storage = new TaskStorage(tempPath, false);
        CommandLine cli = createCLI(storage);
        cli.execute("add", "--title", "Buy milk", "--priority", "high");
        cli.execute("add", "--title", "Walk the dog", "--priority", "low");

        var baos = new ByteArrayOutputStream();
        System.setOut(new PrintStream(baos));

        int exitCode = cli.execute("list", "--priority", "high");

        System.setOut(System.out);

        assertEquals(0, exitCode);
        String output = baos.toString();
        assertTrue(output.contains("Buy milk"));
        assertTrue(output.contains("[Priority: HIGH]"));
        assertFalse(output.contains("Walk the dog"));

        Files.deleteIfExists(tempPath);
    }

    @Test
    public void testFilterByDueDate() throws IOException {
        Path tempPath = Files.createTempFile("tasklistcli-test", ".json");
        var storage = new TaskStorage(tempPath, false);
        CommandLine cli = createCLI(storage);
        cli.execute("add", "--title", "Buy milk", "--due-date", "2025-12-01");
        cli.execute("add", "--title", "Walk the dog", "--due-date", "2025-12-31");

        var baos = new ByteArrayOutputStream();
        System.setOut(new PrintStream(baos));

        int exitCode = cli.execute("list", "--due-before", "2025-12-15");

        System.setOut(System.out);

        assertEquals(0, exitCode);
        String output = baos.toString();
        assertTrue(output.contains("Buy milk"));
        assertTrue(output.contains("(Due: 2025-12-01)"));
        assertFalse(output.contains("Walk the dog"));

        Files.deleteIfExists(tempPath);
    }

    @Test
    public void testShowOnlyCompleted() throws IOException {
        Path tempPath = Files.createTempFile("tasklistcli-test", ".json");
        var storage = new TaskStorage(tempPath, false);
        CommandLine cli = createCLI(storage);
        cli.execute("add", "--title", "Buy milk");
        cli.execute("add", "--title", "Walk the dog");
        cli.execute("complete", "--list", "default", "--task", "Walk the dog");

        var baos = new ByteArrayOutputStream();
        System.setOut(new PrintStream(baos));

        int exitCode = cli.execute("list", "--completed");

        System.setOut(System.out);

        assertEquals(0, exitCode);
        String output = baos.toString();
        assertFalse(output.contains("Buy milk"));
        assertTrue(output.contains("Walk the dog"));
        assertTrue(output.contains("[x]"));

        Files.deleteIfExists(tempPath);
    }
}