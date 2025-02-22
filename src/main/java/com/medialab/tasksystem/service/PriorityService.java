package com.medialab.tasksystem.service;

import com.medialab.tasksystem.model.PriorityLevel;
import com.medialab.tasksystem.model.Task;
import java.util.*;

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

        // If no priorities exist, create some defaults
        if (levels.isEmpty()) {
            System.out.println("Creating default priority levels...");
            defaultPriority = new PriorityLevel("Normal", true);
            levels.add(defaultPriority);

            // Add a few more priority levels
            levels.add(new PriorityLevel("High", false));
            levels.add(new PriorityLevel("Low", false));
            levels.add(new PriorityLevel("Urgent", false));
            System.out.println("Created " + levels.size() + " default levels");

            // Save the default levels
            persistenceService.savePriorityLevels(levels);
            System.out.println("Saved priority levels to storage");
        } else {
            System.out.println("Found existing priority levels");
            // Rest of the code...
        }

        // After adding to map
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
        PriorityLevel priority = priorityLevels.get(priorityId);
        if (priority == null) {
            return;
        }

        if (priority.isDefault()) {
            throw new IllegalArgumentException("Cannot delete default priority level");
        }

        // Update all tasks using this priority to use default priority
        for (Task task : taskService.getAllTasks()) {
            if (task.getPriority() != null && task.getPriority().getId().equals(priorityId)) {
                task.setPriority(defaultPriority);
                taskService.updateTask(task);
            }
        }

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