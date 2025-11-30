package dev.turtywurty.tasklistcli;

import dev.turtywurty.tasklistcli.storage.TaskStorage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import picocli.CommandLine;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class TaskCommandTest {
    @TempDir
    Path tempDir;

    private CommandLine cli;

    @BeforeEach
    void setup() {
        TaskStorage storage = new TaskStorage(tempDir.resolve("tasks.json"), false);
        this.cli = CommandTestUtils.createCommandLine(storage);
    }

    @Test
    void printsUsageMessageWhenNoSubcommandProvided() {
        try (var io = CommandTestUtils.captureIO()) {
            int exitCode = this.cli.execute();
            assertEquals(0, exitCode);
            assertTrue(io.out().contains("Use one of the subcommands to manage your tasks."));
        }
    }
}
