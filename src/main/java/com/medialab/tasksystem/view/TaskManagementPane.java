package com.medialab.tasksystem.view;

import com.medialab.tasksystem.model.*;
import com.medialab.tasksystem.service.TaskService;
import com.medialab.tasksystem.service.CategoryService;
import com.medialab.tasksystem.service.PriorityService;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import java.time.LocalDate;
import java.util.Optional;

public class TaskManagementPane extends VBox {
    private final TaskService taskService;
    private final CategoryService categoryService;
    private final PriorityService priorityService;
    private final Runnable statisticsUpdateCallback;
    private TableView<Task> taskTable;
    private ComboBox<Category> categoryFilter;
    private ComboBox<PriorityLevel> priorityFilter;

    public TaskManagementPane(TaskService taskService, CategoryService categoryService,
                              PriorityService priorityService, Runnable statisticsUpdateCallback) {
        this.taskService = taskService;
        this.categoryService = categoryService;
        this.priorityService = priorityService;
        this.statisticsUpdateCallback = statisticsUpdateCallback;
        setPadding(new Insets(10));
        setSpacing(10);
        setupUI();
    }

    private void setupUI() {
        // Create toolbar
        HBox toolbar = new HBox(10);
        Button addButton = new Button("New Task");
        Button editButton = new Button("Edit");
        Button deleteButton = new Button("Delete");

        addButton.setOnAction(e -> showAddTaskDialog());
        editButton.setOnAction(e -> showEditTaskDialog());
        deleteButton.setOnAction(e -> deleteSelectedTask());

        toolbar.getChildren().addAll(addButton, editButton, deleteButton);

        // Create search/filter section
        HBox filterBox = createFilterBox();

        // Create task table
        taskTable = createTaskTable();
        VBox.setVgrow(taskTable, Priority.ALWAYS);

        // Add all components
        getChildren().addAll(toolbar, filterBox, taskTable);

        // Load initial data
        refreshTaskList();

        // Populate filters with data
        updateFilters();
    }

    private void updateFilters() {
        // Get all categories and update the filter
        categoryFilter.setCellFactory(listView -> new ListCell<Category>() {
            @Override
            protected void updateItem(Category item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? "" : item.getName());
            }
        });

        categoryFilter.setButtonCell(new ListCell<Category>() {
            @Override
            protected void updateItem(Category item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? "" : item.getName());
            }
        });

        categoryFilter.getItems().clear();
        categoryFilter.getItems().addAll(categoryService.getAllCategories());

        // Get all priority levels and update the filter
        priorityFilter.setCellFactory(listView -> new ListCell<PriorityLevel>() {
            @Override
            protected void updateItem(PriorityLevel item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? "" : item.getName());
            }
        });

        priorityFilter.setButtonCell(new ListCell<PriorityLevel>() {
            @Override
            protected void updateItem(PriorityLevel item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? "" : item.getName());
            }
        });

        priorityFilter.getItems().clear();
        priorityFilter.getItems().addAll(priorityService.getAllPriorityLevels());
    }

    private HBox createFilterBox() {
        HBox filterBox = new HBox(10);
        filterBox.setPadding(new Insets(5));

        TextField searchField = new TextField();
        searchField.setPromptText("Search by title...");

        categoryFilter = new ComboBox<>();
        categoryFilter.setPromptText("Category");

        priorityFilter = new ComboBox<>();
        priorityFilter.setPromptText("Priority");

        Button searchButton = new Button("Search");
        searchButton.setOnAction(e -> performSearch(searchField.getText()));

        filterBox.getChildren().addAll(
                new Label("Search:"), searchField,
                new Label("Category:"), categoryFilter,
                new Label("Priority:"), priorityFilter,
                searchButton
        );

        return filterBox;
    }

    private TableView<Task> createTaskTable() {
        TableView<Task> table = new TableView<>();

        TableColumn<Task, String> titleCol = new TableColumn<>("Title");
        titleCol.setCellValueFactory(new PropertyValueFactory<>("title"));
        titleCol.setPrefWidth(200);

        TableColumn<Task, Category> categoryCol = new TableColumn<>("Category");
        categoryCol.setCellValueFactory(new PropertyValueFactory<>("category"));
        categoryCol.setCellFactory(column -> new TableCell<Task, Category>() {
            @Override
            protected void updateItem(Category item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? "" : item.getName());
            }
        });
        categoryCol.setPrefWidth(100);

        TableColumn<Task, PriorityLevel> priorityCol = new TableColumn<>("Priority");
        priorityCol.setCellValueFactory(new PropertyValueFactory<>("priority"));
        priorityCol.setCellFactory(column -> new TableCell<Task, PriorityLevel>() {
            @Override
            protected void updateItem(PriorityLevel item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? "" : item.getName());
            }
        });
        priorityCol.setPrefWidth(100);

        TableColumn<Task, TaskStatus> statusCol = new TableColumn<>("Status");
        statusCol.setCellValueFactory(new PropertyValueFactory<>("status"));
        statusCol.setPrefWidth(100);

        TableColumn<Task, LocalDate> deadlineCol = new TableColumn<>("Deadline");
        deadlineCol.setCellValueFactory(new PropertyValueFactory<>("deadline"));
        deadlineCol.setPrefWidth(100);

        table.getColumns().addAll(titleCol, categoryCol, priorityCol, statusCol, deadlineCol);
        return table;
    }

    private void showAddTaskDialog() {
        // Create a custom dialog
        Dialog<Task> dialog = new Dialog<>();
        dialog.setTitle("Add New Task");
        dialog.setHeaderText("Create a new task");

        // Set the button types
        ButtonType saveButtonType = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

        // Create the form grid
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        // Form fields
        TextField titleField = new TextField();
        titleField.setPromptText("Task title");

        TextArea descriptionArea = new TextArea();
        descriptionArea.setPromptText("Task description");
        descriptionArea.setPrefRowCount(3);

        ComboBox<Category> categoryCombo = new ComboBox<>();
        categoryCombo.getItems().addAll(categoryService.getAllCategories());
        if (!categoryCombo.getItems().isEmpty()) {
            categoryCombo.setValue(categoryCombo.getItems().get(0));
        }

        ComboBox<PriorityLevel> priorityCombo = new ComboBox<>();
        priorityCombo.getItems().addAll(priorityService.getAllPriorityLevels());
        priorityCombo.setValue(priorityService.getDefaultPriorityLevel());

        DatePicker deadlinePicker = new DatePicker();
        deadlinePicker.setValue(LocalDate.now().plusDays(1));

        ComboBox<TaskStatus> statusCombo = new ComboBox<>();
        statusCombo.getItems().addAll(TaskStatus.values());
        statusCombo.setValue(TaskStatus.OPEN);

        // Add fields to grid
        grid.add(new Label("Title:"), 0, 0);
        grid.add(titleField, 1, 0);
        grid.add(new Label("Description:"), 0, 1);
        grid.add(descriptionArea, 1, 1);
        grid.add(new Label("Category:"), 0, 2);
        grid.add(categoryCombo, 1, 2);
        grid.add(new Label("Priority:"), 0, 3);
        grid.add(priorityCombo, 1, 3);
        grid.add(new Label("Deadline:"), 0, 4);
        grid.add(deadlinePicker, 1, 4);
        grid.add(new Label("Status:"), 0, 5);
        grid.add(statusCombo, 1, 5);

        dialog.getDialogPane().setContent(grid);

        // Request focus on the title field by default
        Platform.runLater(() -> titleField.requestFocus());

        // Convert the result to a Task object when the save button is clicked
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                if (titleField.getText().trim().isEmpty()) {
                    showAlert("Invalid Input", "Task title is required");
                    return null;
                }

                Task task = new Task();
                task.setTitle(titleField.getText());
                task.setDescription(descriptionArea.getText());
                task.setCategory(categoryCombo.getValue());
                task.setPriority(priorityCombo.getValue());
                task.setDeadline(deadlinePicker.getValue());
                task.setStatus(statusCombo.getValue());

                return task;
            }
            return null;
        });

        Optional<Task> result = dialog.showAndWait();

        result.ifPresent(task -> {
            taskService.updateTask(task);
            refreshTaskList();
            if (statisticsUpdateCallback != null) {
                statisticsUpdateCallback.run();
            }
        });
    }

    private void showEditTaskDialog() {
        Task selectedTask = taskTable.getSelectionModel().getSelectedItem();
        if (selectedTask == null) {
            showAlert("No Task Selected", "Please select a task to edit.");
            return;
        }

        // Create a custom dialog
        Dialog<Task> dialog = new Dialog<>();
        dialog.setTitle("Edit Task");
        dialog.setHeaderText("Edit task details");

        // Set the button types
        ButtonType saveButtonType = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

        // Create the form grid
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        // Form fields with pre-filled values
        TextField titleField = new TextField(selectedTask.getTitle());

        TextArea descriptionArea = new TextArea(selectedTask.getDescription());
        descriptionArea.setPrefRowCount(3);

        ComboBox<Category> categoryCombo = new ComboBox<>();
        categoryCombo.getItems().addAll(categoryService.getAllCategories());
        categoryCombo.setValue(selectedTask.getCategory());

        ComboBox<PriorityLevel> priorityCombo = new ComboBox<>();
        priorityCombo.getItems().addAll(priorityService.getAllPriorityLevels());
        priorityCombo.setValue(selectedTask.getPriority());

        DatePicker deadlinePicker = new DatePicker(selectedTask.getDeadline());

        ComboBox<TaskStatus> statusCombo = new ComboBox<>();
        statusCombo.getItems().addAll(TaskStatus.values());
        statusCombo.setValue(selectedTask.getStatus());

        // Add fields to grid
        grid.add(new Label("Title:"), 0, 0);
        grid.add(titleField, 1, 0);
        grid.add(new Label("Description:"), 0, 1);
        grid.add(descriptionArea, 1, 1);
        grid.add(new Label("Category:"), 0, 2);
        grid.add(categoryCombo, 1, 2);
        grid.add(new Label("Priority:"), 0, 3);
        grid.add(priorityCombo, 1, 3);
        grid.add(new Label("Deadline:"), 0, 4);
        grid.add(deadlinePicker, 1, 4);
        grid.add(new Label("Status:"), 0, 5);
        grid.add(statusCombo, 1, 5);

        dialog.getDialogPane().setContent(grid);

        // Request focus on the title field by default
        Platform.runLater(() -> titleField.requestFocus());

        // Convert the result to a Task object when the save button is clicked
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                if (titleField.getText().trim().isEmpty()) {
                    showAlert("Invalid Input", "Task title is required");
                    return null;
                }

                // Keep the same ID for updating
                selectedTask.setTitle(titleField.getText());
                selectedTask.setDescription(descriptionArea.getText());
                selectedTask.setCategory(categoryCombo.getValue());
                selectedTask.setPriority(priorityCombo.getValue());
                selectedTask.setDeadline(deadlinePicker.getValue());
                selectedTask.setStatus(statusCombo.getValue());

                return selectedTask;
            }
            return null;
        });

        Optional<Task> result = dialog.showAndWait();

        result.ifPresent(task -> {
            taskService.updateTask(task);
            refreshTaskList();
            if (statisticsUpdateCallback != null) {
                statisticsUpdateCallback.run();
            }
        });
    }

    private void deleteSelectedTask() {
        Task selectedTask = taskTable.getSelectionModel().getSelectedItem();
        if (selectedTask == null) {
            showAlert("No Task Selected", "Please select a task to delete.");
            return;
        }

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Delete Task");
        alert.setHeaderText("Delete Task");
        alert.setContentText("Are you sure you want to delete this task?");

        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                taskService.deleteTask(selectedTask.getId());
                refreshTaskList();
                if (statisticsUpdateCallback != null) {
                    statisticsUpdateCallback.run();
                }
            }
        });
    }

    private void performSearch(String searchText) {
        Category category = categoryFilter.getValue();
        PriorityLevel priority = priorityFilter.getValue();

        taskTable.getItems().setAll(
                taskService.searchTasks(searchText, category, priority)
        );
    }

    private void refreshTaskList() {
        taskTable.getItems().setAll(taskService.getAllTasks());
        updateFilters();
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}