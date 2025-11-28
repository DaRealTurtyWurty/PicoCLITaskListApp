package dev.turtywurty.tasklistcli.storage.data;

import dev.turtywurty.tasklistcli.util.TimeUtils;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

public class Task {
    private final UUID uuid;
    private String name;
    private final Set<String> tags = new HashSet<>();
    private String description;
    private Priority priority;
    private String dueDate;
    private boolean completed;

    public Task(String name, UUID uuid) {
        Objects.requireNonNull(uuid, "UUID cannot be null");
        Objects.requireNonNull(name, "Name cannot be null");
        this.name = name;
        this.uuid = uuid;
    }

    public Task(String name) {
        this(name, UUID.randomUUID());
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        Objects.requireNonNull(name, "Name cannot be null");
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public Set<String> getTags() {
        return this.tags;
    }

    public UUID getUuid() {
        return uuid;
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

        if (TimeUtils.parseOrNull(dueDate) == null)
            throw new IllegalArgumentException("Due date %s must be a valid date in the format YYYY-MM-DD".formatted(dueDate));

        this.dueDate = dueDate;
    }

    public boolean isCompleted() {
        return completed;
    }

    public void setCompleted(boolean completed) {
        this.completed = completed;
    }

    public boolean addTag(String tag) {
        Objects.requireNonNull(tag, "Tag cannot be null");
        return this.tags.add(tag);
    }

    public boolean removeTag(String tag) {
        Objects.requireNonNull(tag, "Tag cannot be null");
        return this.tags.remove(tag);
    }

    @Override
    public String toString() {
        return toString(false);
    }

    public String toString(boolean includeIDs) {
        if (includeIDs) {
            String withoutId = toString(false);
            int closeBracketIndex = withoutId.indexOf(']');
            return withoutId.substring(0, closeBracketIndex + 1).trim() +
                    " (ID: %s) ".formatted(this.uuid.toString()) +
                    withoutId.substring(closeBracketIndex + 1).trim();
        }

        return "[" + (completed ? "X" : " ") + "] " + name +
                (priority != null ? " (Priority: " + priority + ")" : "") +
                (dueDate != null ? " (Due: " + dueDate + ")" : "") +
                (description != null ? " - " + description : "");
    }

    public enum Priority {
        LOW("Low"),
        MEDIUM("Medium"),
        HIGH("High");

        private final String displayName;

        Priority(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return this.displayName;
        }
    }
}
