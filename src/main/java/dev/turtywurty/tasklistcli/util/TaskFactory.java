package dev.turtywurty.tasklistcli.util;

import dev.turtywurty.tasklistcli.storage.AcceptsTaskStorage;
import dev.turtywurty.tasklistcli.storage.TaskStorage;
import picocli.CommandLine;

public class TaskFactory implements CommandLine.IFactory {
    private final CommandLine.IFactory delegate = CommandLine.defaultFactory();
    private final TaskStorage storage = new TaskStorage();

    @Override
    public <K> K create(Class<K> cls) throws Exception {
        return AcceptsTaskStorage.class.isAssignableFrom(cls)
                ? cls.getConstructor(TaskStorage.class).newInstance(storage)
                : delegate.create(cls);
    }
}
