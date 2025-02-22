package com.medialab.tasksystem.controller;

import com.medialab.tasksystem.model.*;
import com.medialab.tasksystem.service.*;
import com.medialab.tasksystem.view.CategoryManagementPane;
import com.medialab.tasksystem.view.PriorityManagementPane;
import com.medialab.tasksystem.view.ReminderManagementPane;
import com.medialab.tasksystem.view.TaskManagementPane;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import java.net.URL;
import java.util.ResourceBundle;

public class MainController implements Initializable {
    @FXML private Label totalTasksLabel;
    @FXML private Label completedTasksLabel;
    @FXML private Label delayedTasksLabel;
    @FXML private Label upcomingTasksLabel;
    @FXML private TabPane mainTabPane;

    private TaskService taskService;
    private CategoryService categoryService;
    private ReminderService reminderService;
    private DataPersistenceService persistenceService;
    private PriorityService priorityService;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        initializeServices();
        setupTabs();
        updateStatistics();
        checkForDelayedTasks();
    }

    private void initializeServices() {
        persistenceService = new DataPersistenceService();
        reminderService = new ReminderService(persistenceService);
        taskService = new TaskService(persistenceService, reminderService);
        categoryService = new CategoryService(persistenceService, taskService);
        priorityService = new PriorityService(persistenceService, taskService);

        // Set the services in DataPersistenceService
        persistenceService.setServices(taskService, categoryService, priorityService, reminderService);
    }

    private void setupTabs() {
        Tab tasksTab = new Tab("Tasks");
        tasksTab.setContent(new TaskManagementPane(taskService, categoryService, priorityService, this::updateStatistics));

        Tab categoriesTab = new Tab("Categories");
        categoriesTab.setContent(new CategoryManagementPane(categoryService));

        Tab prioritiesTab = new Tab("Priorities");
        prioritiesTab.setContent(new PriorityManagementPane(priorityService));

        Tab remindersTab = new Tab("Reminders");
        remindersTab.setContent(new ReminderManagementPane(reminderService, taskService));

        mainTabPane.getTabs().addAll(tasksTab, categoriesTab, prioritiesTab, remindersTab);
    }

    private void updateStatistics() {
        int totalTasks = taskService.getAllTasks().size();
        int completedTasks = taskService.getCompletedTasksCount();
        int delayedTasks = taskService.getDelayedTasksCount();
        int upcomingTasks = taskService.getTasksWithinDaysCount(7);

        totalTasksLabel.setText("Total Tasks: " + totalTasks);
        completedTasksLabel.setText("Completed: " + completedTasks);
        delayedTasksLabel.setText("Delayed: " + delayedTasks);
        upcomingTasksLabel.setText("Due in 7 days: " + upcomingTasks);
    }

    private void checkForDelayedTasks() {
        int delayedTasks = taskService.getDelayedTasksCount();
        if (delayedTasks > 0) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Delayed Tasks");
            alert.setHeaderText("You have delayed tasks!");
            alert.setContentText("Number of delayed tasks: " + delayedTasks);
            alert.show();
        }
    }
}