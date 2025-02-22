package com.medialab.tasksystem;

import com.medialab.tasksystem.service.*;
import com.medialab.tasksystem.view.*;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;

public class TaskManagementApp extends Application {
    private DataPersistenceService persistenceService;
    private TaskService taskService;
    private CategoryService categoryService;
    private PriorityService priorityService;
    private ReminderService reminderService;

    // UI Components
    private Label totalTasksLabel;
    private Label completedTasksLabel;
    private Label delayedTasksLabel;
    private Label upcomingTasksLabel;

    @Override
    public void start(Stage primaryStage) {
        initializeServices();
        createAndShowGUI(primaryStage);
        checkForDelayedTasks();
    }

    private void initializeServices() {
        ServiceManager serviceManager = new ServiceManager();
        persistenceService = serviceManager.getPersistenceService();
        taskService = serviceManager.getTaskService();
        categoryService = serviceManager.getCategoryService();
        priorityService = serviceManager.getPriorityService();
        reminderService = serviceManager.getReminderService();
    }

    private void createAndShowGUI(Stage primaryStage) {
        // Create main layout
        VBox root = new VBox(10);
        root.setPadding(new Insets(10));

        // Create statistics panel
        GridPane statsPanel = createStatsPanel();

        // Create main tab pane
        TabPane tabPane = createMainTabPane();
        VBox.setVgrow(tabPane, Priority.ALWAYS);

        // Add components to root
        root.getChildren().addAll(statsPanel, tabPane);

        // Create scene and show stage
        Scene scene = new Scene(root, 800, 600);
        primaryStage.setTitle("MediaLab Assistant");
        primaryStage.setScene(scene);
        primaryStage.show();

        // Update statistics initially
        updateStatistics();

        // Add shutdown hook
        primaryStage.setOnCloseRequest(e -> {
            e.consume(); // Prevent immediate closing
            handleApplicationShutdown(primaryStage);
        });
    }

    private GridPane createStatsPanel() {
        GridPane statsPane = new GridPane();
        statsPane.setHgap(20);
        statsPane.setVgap(10);
        statsPane.setPadding(new Insets(10));
        statsPane.setStyle("-fx-background-color: #f0f0f0; -fx-border-color: #cccccc;");

        // Initialize statistics labels
        totalTasksLabel = new Label("Total Tasks: 0");
        completedTasksLabel = new Label("Completed: 0");
        delayedTasksLabel = new Label("Delayed: 0");
        upcomingTasksLabel = new Label("Due in 7 days: 0");

        // Style the labels
        String labelStyle = "-fx-font-weight: bold;";
        totalTasksLabel.setStyle(labelStyle);
        completedTasksLabel.setStyle(labelStyle);
        delayedTasksLabel.setStyle(labelStyle);
        upcomingTasksLabel.setStyle(labelStyle);

        // Add labels to grid
        statsPane.addRow(0,
                totalTasksLabel,
                completedTasksLabel,
                delayedTasksLabel,
                upcomingTasksLabel
        );

        return statsPane;
    }

    private TabPane createMainTabPane() {
        TabPane tabPane = new TabPane();
        tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);

        // Create Tasks tab
        Tab tasksTab = new Tab("Tasks");
        tasksTab.setContent(new TaskManagementPane(taskService, categoryService, priorityService, this::updateStatistics));

        // Create Categories tab
        Tab categoriesTab = new Tab("Categories");
        categoriesTab.setContent(new CategoryManagementPane(categoryService));

        // Create Priorities tab
        Tab prioritiesTab = new Tab("Priorities");
        prioritiesTab.setContent(new PriorityManagementPane(priorityService));

        // Create Reminders tab
        Tab remindersTab = new Tab("Reminders");
        remindersTab.setContent(new ReminderManagementPane(reminderService, taskService));

        // Add all tabs
        tabPane.getTabs().addAll(tasksTab, categoriesTab, prioritiesTab, remindersTab);

        return tabPane;
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
            Platform.runLater(() -> {
                Alert alert = new Alert(Alert.AlertType.WARNING);
                alert.setTitle("Delayed Tasks");
                alert.setHeaderText("You have delayed tasks!");
                alert.setContentText("Number of delayed tasks: " + delayedTasks);
                alert.show();
            });
        }
    }

    private void handleApplicationShutdown(Stage primaryStage) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirm Exit");
        alert.setHeaderText("Are you sure you want to exit?");
        alert.setContentText("All changes will be saved.");

        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                // Save all data
                persistenceService.saveAll();
                Platform.exit();
                primaryStage.close();
            }
        });
    }

    public static void main(String[] args) {
        launch(args);
    }
}