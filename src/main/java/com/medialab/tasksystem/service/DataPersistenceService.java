package com.medialab.tasksystem.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.CollectionType;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.medialab.tasksystem.model.*;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class DataPersistenceService {
    private static final Logger LOGGER = Logger.getLogger(DataPersistenceService.class.getName());
    private static final String BASE_DIR = "medialab";
    private static final String TASKS_FILE = "tasks.json";
    private static final String CATEGORIES_FILE = "categories.json";
    private static final String PRIORITIES_FILE = "priorities.json";
    private static final String REMINDERS_FILE = "reminders.json";

    private final ObjectMapper objectMapper;
    private final File baseDir;

    private TaskService taskService;
    private CategoryService categoryService;
    private PriorityService priorityService;
    private ReminderService reminderService;

    public DataPersistenceService() {
        this.objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        this.baseDir = new File(BASE_DIR);
        initializeStorage();
    }

    public void setServices(TaskService taskService, CategoryService categoryService,
                            PriorityService priorityService, ReminderService reminderService) {
        this.taskService = taskService;
        this.categoryService = categoryService;
        this.priorityService = priorityService;
        this.reminderService = reminderService;
    }

    private void initializeStorage() {
        if (!baseDir.exists() && !baseDir.mkdirs()) {
            LOGGER.severe("Failed to create medialab directory");
            throw new RuntimeException("Failed to create storage directory");
        }
    }

    public void saveAll() {
        try {
            saveTasks(new ArrayList<>(taskService.getAllTasks()));
            saveCategories(new ArrayList<>(categoryService.getAllCategories()));
            savePriorityLevels(new ArrayList<>(priorityService.getAllPriorityLevels()));
            saveReminders(new ArrayList<>(reminderService.getActiveReminders()));
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Failed to save all data", e);
            throw new RuntimeException("Failed to save application data", e);
        }
    }

    private <T> void saveToFile(List<T> items, String filename) {
        try {
            File file = new File(baseDir, filename);
            objectMapper.writeValue(file, items);
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Failed to save to " + filename, e);
            throw new RuntimeException("Failed to save data", e);
        }
    }

    private <T> List<T> loadFromFile(String filename, Class<T> type) {
        File file = new File(baseDir, filename);
        if (!file.exists()) {
            return new ArrayList<>();
        }

        try {
            CollectionType listType = objectMapper.getTypeFactory()
                    .constructCollectionType(ArrayList.class, type);
            return objectMapper.readValue(file, listType);
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Failed to load from " + filename, e);
            return new ArrayList<>();
        }
    }

    public void saveTasks(List<Task> tasks) {
        saveToFile(tasks, TASKS_FILE);
    }

    public List<Task> loadTasks() {
        return loadFromFile(TASKS_FILE, Task.class);
    }

    public void saveCategories(List<Category> categories) {
        saveToFile(categories, CATEGORIES_FILE);
    }

    public List<Category> loadCategories() {
        return loadFromFile(CATEGORIES_FILE, Category.class);
    }

    public void savePriorityLevels(List<PriorityLevel> priorityLevels) {
        saveToFile(priorityLevels, PRIORITIES_FILE);
    }

    public List<PriorityLevel> loadPriorityLevels() {
        List<PriorityLevel> levels = loadFromFile(PRIORITIES_FILE, PriorityLevel.class);
        if (levels.isEmpty()) {
            // Initialize with default priority level
            levels.add(new PriorityLevel("Default", true));
        }
        return levels;
    }

    public void saveReminders(List<Reminder> reminders) {
        saveToFile(reminders, REMINDERS_FILE);
    }

    public List<Reminder> loadReminders() {
        return loadFromFile(REMINDERS_FILE, Reminder.class);
    }
}