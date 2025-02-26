package com.medialab.tasksystem.service;

import com.medialab.tasksystem.model.Reminder;
import com.medialab.tasksystem.model.ReminderType;
import com.medialab.tasksystem.model.Task;
import com.medialab.tasksystem.model.TaskStatus;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class ReminderService {
    // Use an ObservableList to store reminders so UI bindings update automatically.
    private final ObservableList<Reminder> reminders = FXCollections.observableArrayList();
    private final DataPersistenceService persistenceService;

    public ReminderService(DataPersistenceService persistenceService) {
        this.persistenceService = persistenceService;
        loadReminders();
    }

    // Load reminders from persistent storage and add them to the ObservableList.
    private void loadReminders() {
        List<Reminder> loadedReminders = persistenceService.loadReminders();
        reminders.addAll(loadedReminders);
    }

    // Save the current list of reminders to persistent storage.
    private void saveReminders() {
        persistenceService.saveReminders(new ArrayList<>(reminders));
    }

    // Create a new reminder for a task.
    public Reminder createReminder(Task task, ReminderType type, LocalDate customDate) {
        if (task.getStatus() == TaskStatus.COMPLETED) {
            throw new IllegalStateException("Cannot create reminder for completed task");
        }

        LocalDate reminderDate = calculateReminderDate(task.getDeadline(), type, customDate);
        validateReminderDate(reminderDate, task.getDeadline());

        Reminder reminder = new Reminder(task.getId(), type, reminderDate);
        reminders.add(reminder);
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

    // Delete a reminder by its ID.
    public void deleteReminder(String reminderId) {
        reminders.removeIf(r -> r.getId().equals(reminderId));
        saveReminders();
    }

    // Delete all reminders associated with a given task ID.
    public void deleteRemindersForTask(String taskId) {
        reminders.removeIf(reminder -> reminder.getTaskId().equals(taskId));
        saveReminders();
    }

    // Return a list of active reminders.
    public List<Reminder> getActiveReminders() {
        return new ArrayList<>(reminders);
    }

    // Update an existing reminder by finding its index and replacing it.
    public void updateReminder(Reminder reminder) {
        int index = -1;
        for (int i = 0; i < reminders.size(); i++) {
            if (reminders.get(i).getId().equals(reminder.getId())) {
                index = i;
                break;
            }
        }
        if (index != -1) {
            reminders.set(index, reminder);
            saveReminders();
        } else {
            throw new IllegalArgumentException("Reminder not found.");
        }
    }

    // Provide access to the ObservableList for UI binding.
    public ObservableList<Reminder> getObservableReminders() {
        return reminders;
    }
}
