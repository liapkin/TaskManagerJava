package com.medialab.tasksystem.service;

import com.medialab.tasksystem.model.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service class responsible for managing tasks in the task management system.
 * This class handles CRUD operations for tasks, task filtering, status management,
 * and persistence through a DataPersistenceService.
 * <p>
 * The TaskService maintains an observable list of tasks that can be used
 * for UI binding, and delegates data persistence to the DataPersistenceService.
 * It also integrates with the ReminderService for task reminder management.
 * </p>
 */
public class TaskService {
    private final ObservableList<Task> tasks = FXCollections.observableArrayList();
    private final DataPersistenceService persistenceService;
    private final ReminderService reminderService;

    /**
     * Constructs a new TaskService with the given persistence and reminder services.
     * Loads existing tasks from the persistence service upon initialization.
     *
     * @param persistenceService The service responsible for loading and saving tasks
     * @param reminderService The service responsible for managing task reminders
     */
    public TaskService(DataPersistenceService persistenceService, ReminderService reminderService) {
        this.persistenceService = persistenceService;
        this.reminderService = reminderService;
        loadTasks();
    }

    /**
     * Loads tasks from the persistence service into the observable list.
     * Checks task deadlines after loading to update status for any overdue tasks.
     */
    private void loadTasks() {
        tasks.addAll(persistenceService.loadTasks());
        checkDeadlines();
    }

    /**
     * Saves the current list of tasks to the persistence service.
     */
    private void saveTasks() {
        persistenceService.saveTasks(new ArrayList<>(tasks));
    }

    /**
     * Retrieves a task by its unique identifier.
     *
     * @param id The unique identifier of the task to retrieve
     * @return The task with the specified ID, or null if no such task exists
     */
    public Task getTaskById(String id) {
        return tasks.stream()
                .filter(task -> task.getId().equals(id))
                .findFirst().orElse(null);
    }

    /**
     * Returns the observable list of all tasks.
     * This list can be directly bound to UI components for automatic updates.
     *
     * @return An ObservableList containing all tasks
     */
    public ObservableList<Task> getObservableTasks() {
        return tasks;
    }

    /**
     * Returns a new list containing all tasks.
     * Unlike getObservableTasks(), this returns a disconnected copy of the tasks list.
     *
     * @return A new List containing all tasks
     */
    public List<Task> getAllTasks() {
        return new ArrayList<>(tasks);
    }

    /**
     * Returns a list of all tasks that have not been completed.
     *
     * @return A List of tasks with status other than COMPLETED
     */
    public List<Task> getUncompletedTasks() {
        return tasks.stream()
                .filter(task -> task.getStatus() != TaskStatus.COMPLETED)
                .collect(Collectors.toList());
    }

    /**
     * Reconciles task categories with the provided CategoryService.
     * For each task, finds a matching category (by name) in the CategoryService
     * and updates the task's category reference to ensure consistency.
     *
     * @param categoryService The service containing the canonical list of categories
     */
    public void reconcileCategories(CategoryService categoryService) {
        for (Task task : tasks) {
            Category current = task.getCategory();
            if (current != null) {
                // Find the category in the CategoryService that matches by name (case-insensitive)
                Category reconciled = categoryService.getAllCategories().stream()
                        .filter(c -> c.getName().equalsIgnoreCase(current.getName()))
                        .findFirst()
                        .orElse(current);
                task.setCategory(reconciled);
            }
        }
        saveTasks();
    }

    /**
     * Reconciles task priority levels with the provided PriorityService.
     * For each task, finds a matching priority level (by name) in the PriorityService
     * and updates the task's priority reference to ensure consistency.
     *
     * @param priorityService The service containing the canonical list of priority levels
     */
    public void reconcilePriorities(PriorityService priorityService) {
        for (Task task : tasks) {
            PriorityLevel current = task.getPriority();
            if (current != null) {
                // Find the priority from PriorityService that matches by name.
                PriorityLevel reconciled = priorityService.getAllPriorityLevels().stream()
                        .filter(p -> p.getName().equalsIgnoreCase(current.getName()))
                        .findFirst()
                        .orElse(current);
                task.setPriority(reconciled);
            }
        }
        // Optionally, save tasks if persistence is required.
        saveTasks();
    }

    /**
     * Checks all tasks for overdue deadlines and updates their status to DELAYED if necessary.
     * A task is considered overdue if its deadline is before the current date,
     * it is not already completed, and it is not already marked as delayed.
     */
    public void checkDeadlines() {
        boolean changes = false;
        for (Task task : tasks) {
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

    /**
     * Creates a new task with the specified properties and adds it to the task list.
     *
     * @param title The title of the new task
     * @param description The description of the new task
     * @param category The category of the new task
     * @param priority The priority level of the new task
     * @param deadline The deadline date for the new task
     * @return The newly created Task object
     */
    public Task createTask(String title, String description, Category category,
                           PriorityLevel priority, LocalDate deadline) {
        Task task = new Task(title, description, category, priority, deadline);
        tasks.add(task); // Add directly to the ObservableList.
        saveTasks();
        return task;
    }

    /**
     * Updates an existing task with new values or adds the task if it doesn't exist.
     * If the task status is set to COMPLETED, any associated reminders are deleted.
     *
     * @param updatedTask The task with updated values
     */
    public void updateTask(Task updatedTask) {
        boolean found = false;
        for (int i = 0; i < tasks.size(); i++) {
            if (tasks.get(i).getId().equals(updatedTask.getId())) {
                tasks.set(i, updatedTask);
                found = true;
                break;
            }
        }
        if (!found) {
            tasks.add(updatedTask);
        }
        if (updatedTask.getStatus() == TaskStatus.COMPLETED) {
            reminderService.deleteRemindersForTask(updatedTask.getId());
        }
        saveTasks();
    }

    /**
     * Deletes a task with the specified ID and removes any associated reminders.
     *
     * @param taskId The ID of the task to delete
     */
    public void deleteTask(String taskId) {
        tasks.removeIf(task -> task.getId().equals(taskId));
        reminderService.deleteRemindersForTask(taskId);
        saveTasks();
    }

    /**
     * Searches for tasks matching the specified criteria.
     * Any criteria that is null is treated as a wildcard (matches anything).
     *
     * @param title The title to search for (partial match, case-insensitive)
     * @param category The category to match exactly
     * @param priority The priority level to match exactly
     * @return A list of tasks matching all the specified criteria
     */
    public List<Task> searchTasks(String title, Category category, PriorityLevel priority) {
        return tasks.stream()
                .filter(task -> title == null || task.getTitle().toLowerCase().contains(title.toLowerCase()))
                .filter(task -> category == null || task.getCategory().equals(category))
                .filter(task -> priority == null || task.getPriority().equals(priority))
                .collect(Collectors.toList());
    }

    /**
     * Returns all tasks belonging to the specified category.
     *
     * @param category The category to filter tasks by
     * @return A list of tasks in the specified category
     */
    public List<Task> getTasksByCategory(Category category) {
        return tasks.stream()
                .filter(task -> task.getCategory().equals(category))
                .collect(Collectors.toList());
    }

    /**
     * Counts the number of tasks with DELAYED status.
     *
     * @return The count of delayed tasks
     */
    public int getDelayedTasksCount() {
        return (int) tasks.stream()
                .filter(task -> task.getStatus() == TaskStatus.DELAYED)
                .count();
    }

    /**
     * Counts the number of tasks with COMPLETED status.
     *
     * @return The count of completed tasks
     */
    public int getCompletedTasksCount() {
        return (int) tasks.stream()
                .filter(task -> task.getStatus() == TaskStatus.COMPLETED)
                .count();
    }

    /**
     * Counts the number of uncompleted tasks due within the specified number of days.
     *
     * @param days The number of days from the current date
     * @return The count of uncompleted tasks with deadlines within the specified period
     */
    public int getTasksWithinDaysCount(int days) {
        LocalDate deadline = LocalDate.now().plusDays(days);
        return (int) tasks.stream()
                .filter(task -> task.getStatus() != TaskStatus.COMPLETED)
                .filter(task -> !task.getDeadline().isAfter(deadline))
                .count();
    }
}