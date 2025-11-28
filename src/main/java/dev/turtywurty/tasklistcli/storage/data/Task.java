package dev.turtywurty.tasklistcli.storage.data;

import java.util.Objects;

public class Task {
    private final String name;
    private String description;
    private Priority priority;
    private String dueDate;
    private boolean completed;

    public Task(String name) {
        Objects.requireNonNull(name, "Name cannot be null");
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        Objects.requireNonNull(description, "Description cannot be null");
        this.description = description;
    }

    public Priority getPriority() {
        return priority;
    }

    public void setPriority(Priority priority) {
        Objects.requireNonNull(priority, "Priority cannot be null");
        this.priority = priority;
    }

    public String getDueDate() {
        return dueDate;
    }

    public void setDueDate(String dueDate) {
        Objects.requireNonNull(dueDate, "Due date cannot be null");
        this.dueDate = dueDate;
    }

    public boolean isCompleted() {
        return completed;
    }

    public void setCompleted(boolean completed) {
        this.completed = completed;
    }

    @Override
    public String toString() {
        return "[" + (completed ? "X" : " ") + "] " + name +
                (priority != null ? " (Priority: " + priority + ")" : "") +
                (dueDate != null ? " (Due: " + dueDate + ")" : "") +
                (description != null ? " - " + description : "");
    }

    public enum Priority {
        LOW,
        MEDIUM,
        HIGH
    }
}
