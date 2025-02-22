package com.medialab.tasksystem.service;

import com.medialab.tasksystem.model.Category;
import java.util.*;

public class CategoryService {
    private final Map<String, Category> categories = new HashMap<>();
    private final DataPersistenceService persistenceService;
    private final TaskService taskService;

    public CategoryService(DataPersistenceService persistenceService, TaskService taskService) {
        this.persistenceService = persistenceService;
        this.taskService = taskService;
        loadCategories();
    }

    private void loadCategories() {
        List<Category> cats = persistenceService.loadCategories();

        // If no categories exist, create some defaults
        if (cats.isEmpty()) {
            // Create default categories
            cats.add(new Category("Work"));
            cats.add(new Category("Personal"));
            cats.add(new Category("Study"));
            cats.add(new Category("Health"));
            cats.add(new Category("Finance"));

            // Save the default categories
            persistenceService.saveCategories(cats);
        }

        cats.forEach(category -> categories.put(category.getId(), category));
    }

    private void saveCategories() {
        persistenceService.saveCategories(new ArrayList<>(categories.values()));
    }

    public Category createCategory(String name) {
        Category category = new Category(name);
        categories.put(category.getId(), category);
        saveCategories();
        return category;
    }

    public void updateCategory(Category category) {
        categories.put(category.getId(), category);
        saveCategories();
    }

    public void deleteCategory(String categoryId) {
        Category category = categories.get(categoryId);
        if (category != null) {
            // Delete all tasks in this category
            taskService.getTasksByCategory(category)
                    .forEach(task -> taskService.deleteTask(task.getId()));

            categories.remove(categoryId);
            saveCategories();
        }
    }

    public List<Category> getAllCategories() {
        return new ArrayList<>(categories.values());
    }

    public Category getCategoryById(String id) {
        return categories.get(id);
    }
}