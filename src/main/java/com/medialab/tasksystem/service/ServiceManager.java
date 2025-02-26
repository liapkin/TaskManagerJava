package com.medialab.tasksystem.service;

import com.medialab.tasksystem.model.Category;
import com.medialab.tasksystem.model.PriorityLevel;
import java.util.List;

public class ServiceManager {
    private final DataPersistenceService persistenceService;
    private final TaskService taskService;
    private final CategoryService categoryService;
    private final PriorityService priorityService;
    private final ReminderService reminderService;

    public ServiceManager() {
        // First create persistence service
        this.persistenceService = new DataPersistenceService();

        // Create reminder service
        this.reminderService = new ReminderService(persistenceService);

        // Create task service with reminder service
        this.taskService = new TaskService(persistenceService, reminderService);

        // Create category service
        this.categoryService = new CategoryService(persistenceService, taskService);

        // Create priority service
        this.priorityService = new PriorityService(persistenceService, taskService);

        // Initialize all services first
        // Set the services in DataPersistenceService
        this.persistenceService.setServices(taskService, categoryService, priorityService, reminderService);

        taskService.reconcilePriorities(priorityService);
        taskService.reconcileCategories(categoryService);

        // Now force load the data
        ensureDefaultPriorityLevels();
    }

    private void ensureDefaultPriorityLevels() {
        List<PriorityLevel> existingPriorities = priorityService.getAllPriorityLevels();
        System.out.println("Checking priorities: found " + existingPriorities.size());

        if (existingPriorities.size() <= 1) {
            System.out.println("Creating additional priority levels...");
            if (existingPriorities.isEmpty()) {
                priorityService.createPriorityLevel("Default", true); // Create default
            }
            priorityService.createPriorityLevel("High", false);
            priorityService.createPriorityLevel("Low", false);
            priorityService.createPriorityLevel("Urgent", false);
            System.out.println("Created additional priority levels");
        }
    }

    public DataPersistenceService getPersistenceService() { return persistenceService; }
    public TaskService getTaskService() { return taskService; }
    public CategoryService getCategoryService() { return categoryService; }
    public PriorityService getPriorityService() { return priorityService; }
    public ReminderService getReminderService() { return reminderService; }
}