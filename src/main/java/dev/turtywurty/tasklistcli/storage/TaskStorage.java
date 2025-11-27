package dev.turtywurty.tasklistcli.storage;

import com.google.gson.*;
import dev.turtywurty.tasklistcli.TaskListCLI;
import dev.turtywurty.tasklistcli.storage.data.Task;
import dev.turtywurty.tasklistcli.storage.data.TaskList;
import dev.turtywurty.tasklistcli.util.OperatingSystem;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicLong;

public class TaskStorage {
    private static final Path STORAGE_PATH = getConfigDirectory().resolve("tasks.json");
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private final List<TaskList> taskLists = new ArrayList<>();
    private final AtomicLong lastWrittenTime = new AtomicLong(System.currentTimeMillis());

    public TaskStorage() {
        readTaskLists();
        registerWatchService();
    }

    private void registerWatchService() {
        FileSystem fileSystem = FileSystems.getDefault();
        try {
            WatchService watchService = fileSystem.newWatchService();
            STORAGE_PATH.getParent().register(watchService, StandardWatchEventKinds.ENTRY_MODIFY);
            var thread = new Thread(() -> listenToFileChanges(watchService), "task-storage-listener");
            thread.setDaemon(true);
            thread.start();
        } catch (IOException exception) {
            System.err.printf("An error occurred watching for changes to file: %s%n Error message: %s%n",
                    STORAGE_PATH, exception.getLocalizedMessage());
        }
    }

    private void listenToFileChanges(WatchService watchService) {
        while (true) {
            try {
                WatchKey key = watchService.take();
                for (WatchEvent<?> pollEvent : key.pollEvents()) {
                    WatchEvent.Kind<?> kind = pollEvent.kind();
                    if (kind != StandardWatchEventKinds.ENTRY_MODIFY)
                        continue;

                    Path changed = (Path) pollEvent.context();
                    if (!changed.endsWith(STORAGE_PATH.getFileName()))
                        continue;

                    try {
                        long lastModifiedTime = Files.getLastModifiedTime(STORAGE_PATH).toMillis();
                        if (lastModifiedTime <= this.lastWrittenTime.get())
                            continue;
                    } catch (IOException exception) {
                        System.err.printf("Failed to get last modified time for file: %s%nError message: %s%n",
                                STORAGE_PATH, exception.getLocalizedMessage());
                        continue;
                    }

                    this.taskLists.clear();
                    readTaskLists();
                }

                key.reset();
            } catch (ClosedWatchServiceException | InterruptedException exception) {
                System.err.printf("An issue occurred whilst listening for file changes to file: %s%nError message: %s%n",
                        STORAGE_PATH, exception.getLocalizedMessage());
                return;
            }
        }
    }

    public List<TaskList> getTaskLists() {
        return this.taskLists;
    }

    public void addTaskList(TaskList list) {
        Objects.requireNonNull(list, "Task list cannot be null");
        this.taskLists.add(list);
        updateFileChanged();
    }

    public void updateFileChanged() {
        try {
            if (Files.notExists(STORAGE_PATH)) {
                Files.createDirectories(STORAGE_PATH.getParent());
                Files.createFile(STORAGE_PATH);
            }

            Files.writeString(STORAGE_PATH, GSON.toJson(this.taskLists));
            this.lastWrittenTime.set(System.currentTimeMillis());
        } catch (IOException exception) {
            System.err.printf("An issue occurred updating the file contents: %s%nError message:%s%n",
                    STORAGE_PATH, exception.getLocalizedMessage());
        }
    }

    private void readTaskLists() {
        try {
            if (Files.notExists(STORAGE_PATH)) {
                Files.createDirectories(STORAGE_PATH.getParent());
                Files.writeString(STORAGE_PATH, "[]");
                return;
            }

            JsonArray array = GSON.fromJson(Files.readString(STORAGE_PATH, StandardCharsets.UTF_8), JsonArray.class);
            for (JsonElement element : array) {
                try {
                    TaskList list = GSON.fromJson(element, TaskList.class);
                    this.taskLists.add(list);
                } catch (JsonSyntaxException exception) {
                    System.err.printf("Failed to read task list from storage!%nPath:%s%nRaw Element: %s%nException message:%s%n",
                            STORAGE_PATH, element, exception.getLocalizedMessage());

                }
            }
        } catch (IOException | JsonSyntaxException exception) {
            System.err.printf("A problem occurred reading %s%nError message: %s%n",
                    STORAGE_PATH, exception.getLocalizedMessage());
        }
    }

    private static Path getConfigDirectory() {
        String userHome = System.getProperty("user.home");
        return switch (OperatingSystem.CURRENT) {
            case WINDOWS -> {
                String roaming = System.getenv("APPDATA");
                if (roaming != null && !roaming.isBlank()) {
                    yield Path.of(roaming, TaskListCLI.APPLICATION_NAME);
                }

                yield Path.of(userHome, "AppData", "Roaming", TaskListCLI.APPLICATION_NAME);
            }
            case MAC -> Path.of(userHome, "Library", "Application Support", TaskListCLI.APPLICATION_NAME);
            case LINUX -> {
                String xdgConfigHome = System.getenv("XDG_CONFIG_HOME");
                if (xdgConfigHome != null && !xdgConfigHome.isBlank()) {
                    yield Path.of(xdgConfigHome, TaskListCLI.APPLICATION_NAME);
                }

                yield Path.of(userHome, ".config", TaskListCLI.APPLICATION_NAME);
            }
            case UNKNOWN -> Path.of(userHome, TaskListCLI.APPLICATION_NAME);
        };
    }

    public TaskList getTaskList(String listName) {
        for (TaskList list : this.taskLists) {
            if (list.getName().equalsIgnoreCase(listName))
                return list;
        }

        var newList = new TaskList(listName);
        addTaskList(newList);
        return newList;
    }

    public Collection<Task> getAllTasks() {
        return this.taskLists.stream().flatMap(list -> list.getTasks().stream()).toList();
    }
}
