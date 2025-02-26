package com.medialab.tasksystem.service;

import com.medialab.tasksystem.model.PriorityLevel;
import com.medialab.tasksystem.model.Task;
import java.util.*;
import java.util.stream.Collectors;

public class PriorityService {
    private final Map<String, PriorityLevel> priorityLevels = new HashMap<>();
    private final DataPersistenceService persistenceService;
    private final TaskService taskService;
    private PriorityLevel defaultPriority;

    public PriorityService(DataPersistenceService persistenceService, TaskService taskService) {
        this.persistenceService = persistenceService;
        this.taskService = taskService;
        loadPriorityLevels();
    }

    private void loadPriorityLevels() {
        List<PriorityLevel> levels = persistenceService.loadPriorityLevels();
        System.out.println("Initially loaded " + levels.size() + " priority levels");

        if (levels.isEmpty()) {
            System.out.println("Creating default priority levels...");
            defaultPriority = new PriorityLevel("Default", true);
            levels.add(defaultPriority);
            levels.add(new PriorityLevel("High", false));
            levels.add(new PriorityLevel("Low", false));
            levels.add(new PriorityLevel("Urgent", false));
            System.out.println("Created " + levels.size() + " default levels");

            // Save the default levels
            persistenceService.savePriorityLevels(levels);
            System.out.println("Saved priority levels to storage");
        } else {
            // Populate the map and ensure the default priority is set
            for (PriorityLevel level : levels) {
                priorityLevels.put(level.getId(), level);
                if (level.isDefault()) {
                    defaultPriority = level;
                }
            }
            // If no default is found, choose one (or create one if necessary)
            if (defaultPriority == null && !levels.isEmpty()) {
                defaultPriority = levels.get(0);
            }
            System.out.println("Found existing priority levels");
        }

        System.out.println("Final priority levels count: " + priorityLevels.size());
    }

    private void savePriorityLevels() {
        persistenceService.savePriorityLevels(new ArrayList<>(priorityLevels.values()));
    }

    public PriorityLevel createPriorityLevel(String name, boolean isDefault) {
        PriorityLevel priority = new PriorityLevel(name, isDefault);
        priorityLevels.put(priority.getId(), priority);
        if (isDefault) {
            defaultPriority = priority;
        }
        savePriorityLevels();
        return priority;
    }

    public void updatePriorityLevel(PriorityLevel priority) {
        if (priority.isDefault()) {
            throw new IllegalArgumentException("Cannot modify default priority level");
        }
        priorityLevels.put(priority.getId(), priority);
        savePriorityLevels();
    }

    public void deletePriorityLevel(String priorityId) {
        // First, get the default priority
        PriorityLevel defaultPriority = getDefaultPriorityLevel();

        // Get all tasks that have the priority being deleted
        List<Task> affectedTasks = taskService.getAllTasks().stream()
                .filter(task -> task.getPriority().getId().equals(priorityId))
                .collect(Collectors.toList());

        // Update each task to use the default priority
        for (Task task : affectedTasks) {
            task.setPriority(defaultPriority);
            taskService.updateTask(task);
        }

        // Now delete the priority level
        priorityLevels.remove(priorityId);
        savePriorityLevels();
    }

    public List<PriorityLevel> getAllPriorityLevels() {
        return new ArrayList<>(priorityLevels.values());
    }

    public PriorityLevel getDefaultPriorityLevel() {
        return defaultPriority;
    }

    public PriorityLevel getPriorityLevelById(String id) {
        return priorityLevels.get(id);
    }
}