package com.medialab.tasksystem.model;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class Task {
    private String id;
    private String title;
    private String description;
    private Category category;
    private PriorityLevel priority;
    private LocalDate deadline;
    private TaskStatus status;
    private List<Reminder> reminders;


    public Task() {
        this.id = java.util.UUID.randomUUID().toString();
        this.status = TaskStatus.OPEN;
        this.reminders = new ArrayList<>();
    }

    public Task(String title, String description, Category category,
                PriorityLevel priority, LocalDate deadline) {
        this();
        this.title = title;
        this.description = description;
        this.category = category;
        this.priority = priority;
        this.deadline = deadline;
    }

    // Getters and setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Category getCategory() {
        return category;
    }

    public void setCategory(Category category) {
        this.category = category;
    }

    public PriorityLevel getPriority() {
        return priority;
    }

    public void setPriority(PriorityLevel priority) {
        this.priority = priority;
    }

    public LocalDate getDeadline() {
        return deadline;
    }

    public void setDeadline(LocalDate deadline) {
        this.deadline = deadline;
    }

    public TaskStatus getStatus() {
        return status;
    }

    public void setStatus(TaskStatus status) {
        this.status = status;
    }

    public List<Reminder> getReminders() {
        return reminders;
    }

    public void setReminders(List<Reminder> reminders) {
        this.reminders = reminders;
    }

    @JsonIgnore
    public boolean isDelayed() {
        return !status.equals(TaskStatus.COMPLETED) &&
                deadline != null &&
                deadline.isBefore(LocalDate.now());
    }

    public void checkAndUpdateStatus() {
        if (isDelayed()) {
            status = TaskStatus.DELAYED;
        }
    }
}