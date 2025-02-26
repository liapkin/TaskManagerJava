package com.medialab.tasksystem.model;

import java.time.LocalDate;

public class Reminder {
    private String id;
    private String taskId;  // Store only the ID to avoid circular reference
    private ReminderType type;
    private LocalDate reminderDate;


    public Reminder() {
        this.id = java.util.UUID.randomUUID().toString();
    }

    public Reminder(String taskId, ReminderType type, LocalDate reminderDate) {
        this();
        this.taskId = taskId;
        this.type = type;
        this.reminderDate = reminderDate;
    }


    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTaskId() {
        return taskId;
    }

    public void setTaskId(String taskId) {
        this.taskId = taskId;
    }

    public ReminderType getType() {
        return type;
    }

    public void setType(ReminderType type) {
        this.type = type;
    }

    public LocalDate getReminderDate() {
        return reminderDate;
    }

    public void setReminderDate(LocalDate reminderDate) {
        this.reminderDate = reminderDate;
    }

    @Override
    public String toString() {
        String typeStr = type != null ? type.toString() : "Unknown";
        String dateStr = reminderDate != null ? reminderDate.toString() : "No date";
        return typeStr + " (" + dateStr + ")";
    }
}
