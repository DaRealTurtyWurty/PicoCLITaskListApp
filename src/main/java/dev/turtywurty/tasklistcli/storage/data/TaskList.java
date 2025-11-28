package dev.turtywurty.tasklistcli.storage.data;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class TaskList {
    private final String name;
    private final List<Task> tasks = new ArrayList<>();
    private String description;

    public TaskList(String name) {
        Objects.requireNonNull(name, "Name cannot be null");
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public List<Task> getTasks() {
        return tasks;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        Objects.requireNonNull(description, "Description cannot be null");
        this.description = description;
    }

    public void addTask(Task task) {
        Objects.requireNonNull(task, "Task cannot be null");

        if (hasTask(task.getName()))
            throw new IllegalArgumentException("A task with the name '" + task.getName() + "' already exists in the task list.");

        this.tasks.add(task);
    }

    public void removeTask(Task task) {
        Objects.requireNonNull(task, "Task cannot be null");
        this.tasks.remove(task);
    }

    public void clearTasks() {
        this.tasks.clear();
    }

    public void removeCompletedTasks() {
        this.tasks.removeIf(Task::isCompleted);
    }

    public boolean removeTaskByName(String taskName) {
        Objects.requireNonNull(taskName, "Task name cannot be null");
        return this.tasks.removeIf(t -> t.getName().equalsIgnoreCase(taskName));
    }

    public boolean hasTask(String taskName) {
        Objects.requireNonNull(taskName, "Task name cannot be null");
        return this.tasks.stream().anyMatch(t -> t.getName().equalsIgnoreCase(taskName));
    }

    public Task getTask(String taskName, boolean createI) {
        Objects.requireNonNull(taskName, "Task name cannot be null");
        for (Task task : this.tasks) {
            if (task.getName().equalsIgnoreCase(taskName))
                return task;
        }

        if (createI) {
            var newTask = new Task(taskName);
            addTask(newTask);
            return newTask;
        }

        return null;
    }
}
