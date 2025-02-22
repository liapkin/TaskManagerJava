package com.medialab.tasksystem.service;

import com.medialab.tasksystem.model.*;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

public class TaskService {
    private final Map<String, Task> tasks = new HashMap<>();
    private final DataPersistenceService persistenceService;
    private final ReminderService reminderService;

    public TaskService(DataPersistenceService persistenceService, ReminderService reminderService) {
        this.persistenceService = persistenceService;
        this.reminderService = reminderService;
        loadTasks();
    }

    private void loadTasks() {
        persistenceService.loadTasks().forEach(task -> tasks.put(task.getId(), task));
        checkDeadlines();
    }

    private void saveTasks() {
        persistenceService.saveTasks(new ArrayList<>(tasks.values()));
    }

    public Task getTaskById(String id) {
        return tasks.get(id);
    }

    public List<Task> getAllTasks() {
        return new ArrayList<>(tasks.values());
    }

    public List<Task> getUncompletedTasks() {
        return tasks.values().stream()
                .filter(task -> task.getStatus() != TaskStatus.COMPLETED)
                .collect(Collectors.toList());
    }

    public void checkDeadlines() {
        boolean changes = false;
        for (Task task : tasks.values()) {
            if (!task.getStatus().equals(TaskStatus.COMPLETED) &&
                    task.getDeadline().isBefore(LocalDate.now()) &&
                    !task.getStatus().equals(TaskStatus.DELAYED)) {
                task.setStatus(TaskStatus.DELAYED);
                changes = true;
            }
        }
        if (changes) {
            saveTasks();
        }
    }

    public Task createTask(String title, String description, Category category,
                           PriorityLevel priority, LocalDate deadline) {
        Task task = new Task(title, description, category, priority, deadline);
        tasks.put(task.getId(), task);
        saveTasks();
        return task;
    }

    public void updateTask(Task task) {
        if (task.getStatus() == TaskStatus.COMPLETED) {
            reminderService.deleteRemindersForTask(task.getId());
        }
        tasks.put(task.getId(), task);
        saveTasks();
    }

    public void deleteTask(String taskId) {
        tasks.remove(taskId);
        reminderService.deleteRemindersForTask(taskId);
        saveTasks();
    }

    public List<Task> searchTasks(String title, Category category, PriorityLevel priority) {
        return tasks.values().stream()
                .filter(task -> title == null || task.getTitle().toLowerCase().contains(title.toLowerCase()))
                .filter(task -> category == null || task.getCategory().equals(category))
                .filter(task -> priority == null || task.getPriority().equals(priority))
                .collect(Collectors.toList());
    }

    public List<Task> getTasksByCategory(Category category) {
        return tasks.values().stream()
                .filter(task -> task.getCategory().equals(category))
                .collect(Collectors.toList());
    }

    public int getDelayedTasksCount() {
        return (int) tasks.values().stream()
                .filter(task -> task.getStatus() == TaskStatus.DELAYED)
                .count();
    }

    public int getCompletedTasksCount() {
        return (int) tasks.values().stream()
                .filter(task -> task.getStatus() == TaskStatus.COMPLETED)
                .count();
    }

    public int getTasksWithinDaysCount(int days) {
        LocalDate deadline = LocalDate.now().plusDays(days);
        return (int) tasks.values().stream()
                .filter(task -> task.getStatus() != TaskStatus.COMPLETED)
                .filter(task -> !task.getDeadline().isAfter(deadline))
                .count();
    }
}