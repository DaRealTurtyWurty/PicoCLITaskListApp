package dev.turtywurty.tasklistcli;

import dev.turtywurty.tasklistcli.command.TaskCommand;
import dev.turtywurty.tasklistcli.storage.TaskStorage;
import dev.turtywurty.tasklistcli.util.TaskFactory;
import org.junit.jupiter.api.Test;
import picocli.CommandLine;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class AddTaskTest {
    @Test
    public void testAddTask() {
        var storage = new TaskStorage();
        var cli = createCLI(storage);
        int exitCode = cli.execute("--add", "Buy milk");

        assertEquals(0, exitCode);
        assertEquals(1, storage.getTaskLists().size());
        assertEquals(1, storage.getAllTasks().size());
        assertEquals("Buy milk", storage.getAll().get(0));
    }

    private static CommandLine createCLI(TaskStorage storage) {
        return new CommandLine(new TaskCommand(), new TaskFactory() {
            @Override
            public <K> K create(Class<K> cls) throws Exception {
                return cls.getConstructor(TaskStorage.class).newInstance(storage);
            }
        });
    }
}
