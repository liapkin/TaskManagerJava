package com.medialab.tasksystem.service;

import com.medialab.tasksystem.model.*;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

public class ReminderService {
    private final Map<String, Reminder> reminders = new HashMap<>();
    private final DataPersistenceService persistenceService;

    public ReminderService(DataPersistenceService persistenceService) {
        this.persistenceService = persistenceService;
        loadReminders();
    }

    private void loadReminders() {
        persistenceService.loadReminders().forEach(reminder ->
                reminders.put(reminder.getId(), reminder));
    }

    private void saveReminders() {
        persistenceService.saveReminders(new ArrayList<>(reminders.values()));
    }

    public Reminder createReminder(Task task, ReminderType type, LocalDate customDate) {
        if (task.getStatus() == TaskStatus.COMPLETED) {
            throw new IllegalStateException("Cannot create reminder for completed task");
        }

        LocalDate reminderDate = calculateReminderDate(task.getDeadline(), type, customDate);
        validateReminderDate(reminderDate, task.getDeadline());

        Reminder reminder = new Reminder(task.getId(), type, reminderDate);
        reminders.put(reminder.getId(), reminder);
        saveReminders();
        return reminder;
    }

    public LocalDate calculateReminderDate(LocalDate deadline, ReminderType type, LocalDate customDate) {
        switch (type) {
            case ONE_DAY_BEFORE:
                return deadline.minusDays(1);
            case ONE_WEEK_BEFORE:
                return deadline.minusWeeks(1);
            case ONE_MONTH_BEFORE:
                return deadline.minusMonths(1);
            case CUSTOM_DATE:
                return customDate;
            default:
                throw new IllegalArgumentException("Invalid reminder type");
        }
    }

    private void validateReminderDate(LocalDate reminderDate, LocalDate deadline) {
        if (reminderDate.isAfter(deadline)) {
            throw new IllegalArgumentException("Reminder date cannot be after task deadline");
        }
        if (reminderDate.isBefore(LocalDate.now())) {
            throw new IllegalArgumentException("Reminder date cannot be in the past");
        }
    }

    public void deleteReminder(String reminderId) {
        reminders.remove(reminderId);
        saveReminders();
    }

    public void deleteRemindersForTask(String taskId) {
        reminders.values().removeIf(reminder -> reminder.getTaskId().equals(taskId));
        saveReminders();
    }

    public List<Reminder> getActiveReminders() {
        return new ArrayList<>(reminders.values());
    }

    public void updateReminder(Reminder reminder) {
        if (reminders.containsKey(reminder.getId())) {
            reminders.put(reminder.getId(), reminder);
            saveReminders();
        } else {
            throw new IllegalArgumentException("Reminder not found.");
        }
    }



}