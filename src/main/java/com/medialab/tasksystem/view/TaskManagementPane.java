package com.medialab.tasksystem.view;

import com.medialab.tasksystem.model.*;
import com.medialab.tasksystem.service.TaskService;
import com.medialab.tasksystem.service.CategoryService;
import com.medialab.tasksystem.service.PriorityService;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.collections.ListChangeListener;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

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
    private FilteredList<Task> filteredTasks;

    public TaskManagementPane(TaskService taskService, CategoryService categoryService,
                              PriorityService priorityService, Runnable statisticsUpdateCallback) {
        this.taskService = taskService;
        this.categoryService = categoryService;
        this.priorityService = priorityService;
        this.statisticsUpdateCallback = statisticsUpdateCallback;
        setPadding(new Insets(10));
        setSpacing(10);
        setupUI();
        startDeadlineChecker();
    }

    private void setupUI() {
        // Create toolbar with add, edit, and delete buttons.
        HBox toolbar = new HBox(10);
        Button addButton = new Button("New Task");
        Button editButton = new Button("Edit");
        Button deleteButton = new Button("Delete");

        addButton.setOnAction(e -> showAddTaskDialog());
        editButton.setOnAction(e -> showEditTaskDialog());
        deleteButton.setOnAction(e -> deleteSelectedTask());

        toolbar.getChildren().addAll(addButton, editButton, deleteButton);

        HBox filterBox = createFilterBox();

        taskTable = createTaskTable();
        VBox.setVgrow(taskTable, Priority.ALWAYS);

        filteredTasks = new FilteredList<>(taskService.getObservableTasks(), p -> true);
        SortedList<Task> sortedTasks = new SortedList<>(filteredTasks);
        sortedTasks.comparatorProperty().bind(taskTable.comparatorProperty());
        taskTable.setItems(sortedTasks);

        // Debug listener to log changes in the tasks list.
        taskService.getObservableTasks().addListener(new ListChangeListener<Task>() {
            @Override
            public void onChanged(Change<? extends Task> change) {
                while (change.next()) {
                    if (change.wasAdded()) {
                        System.out.println("Tasks added: " + change.getAddedSubList());
                    }
                    if (change.wasRemoved()) {
                        System.out.println("Tasks removed: " + change.getRemoved());
                    }
                }
            }
        });


        getChildren().addAll(toolbar, filterBox, taskTable);

        updateFilters();
    }

    // Use a Timeline to periodically check deadlines so that overdue tasks become "DELAYED".
    private void startDeadlineChecker() {
        Timeline deadlineChecker = new Timeline(new KeyFrame(Duration.minutes(1), event -> {
            taskService.checkDeadlines();
            taskTable.refresh(); // Force UI update if needed.
        }));
        deadlineChecker.setCycleCount(Timeline.INDEFINITE);
        deadlineChecker.play();
    }

    private void updateFilters() {
        // Update category filter.
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

        // Update priority filter.
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
        filterBox.setAlignment(Pos.CENTER_LEFT);

        TextField searchField = new TextField();
        searchField.setPromptText("Search by title...");

        categoryFilter = new ComboBox<>();
        categoryFilter.setPromptText("Category");

        priorityFilter = new ComboBox<>();
        priorityFilter.setPromptText("Priority");

        Button searchButton = new Button("Search");
        searchButton.setOnAction(e -> performSearch(searchField.getText()));

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button resetButton = new Button("Reset Search");
        resetButton.setOnAction(e -> {
            searchField.clear();
            categoryFilter.getSelectionModel().clearSelection();
            priorityFilter.getSelectionModel().clearSelection();
            filteredTasks.setPredicate(task -> true);
        });

        filterBox.getChildren().addAll(
                new Label("Search:"), searchField,
                new Label("Category:"), categoryFilter,
                new Label("Priority:"), priorityFilter,
                searchButton,
                spacer,
                resetButton
        );
        return filterBox;
    }

    private TableView<Task> createTaskTable() {
        TableView<Task> table = new TableView<>();

        TableColumn<Task, String> titleCol = new TableColumn<>("Title");
        titleCol.setCellValueFactory(new PropertyValueFactory<>("title"));
        titleCol.setPrefWidth(200);
        titleCol.setSortable(true);
        titleCol.setComparator((s1, s2) -> s1.compareToIgnoreCase(s2));

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
        categoryCol.setSortable(true);
        categoryCol.setComparator((c1, c2) -> c1.getName().compareToIgnoreCase(c2.getName()));

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
        priorityCol.setSortable(true);
        priorityCol.setComparator((p1, p2) -> {
            int p1Value = getPriorityValue(p1.getName());
            int p2Value = getPriorityValue(p2.getName());
            return Integer.compare(p1Value, p2Value);
        });

        TableColumn<Task, TaskStatus> statusCol = new TableColumn<>("Status");
        statusCol.setCellValueFactory(new PropertyValueFactory<>("status"));
        statusCol.setPrefWidth(100);
        statusCol.setSortable(true);
        statusCol.setComparator((s1, s2) -> {
            int s1Value = getStatusValue(s1);
            int s2Value = getStatusValue(s2);
            return Integer.compare(s1Value, s2Value);
        });

        TableColumn<Task, LocalDate> deadlineCol = new TableColumn<>("Deadline");
        deadlineCol.setCellValueFactory(new PropertyValueFactory<>("deadline"));
        deadlineCol.setPrefWidth(100);
        deadlineCol.setSortable(true);

        table.getColumns().addAll(titleCol, categoryCol, priorityCol, statusCol, deadlineCol);
        return table;
    }

    // Helper methods for sorting comparators.
    private int getPriorityValue(String priorityName) {
        switch (priorityName) {
            case "Default": return 1;
            case "Low": return 2;
            case "High": return 3;
            case "Urgent": return 4;
            default: return 5; // for any other priorities
        }
    }

    private int getStatusValue(TaskStatus status) {
        switch (status) {
            case OPEN: return 1;
            case IN_PROGRESS: return 2;
            case POSTPONED: return 3;
            case COMPLETED: return 4;
            case DELAYED: return 5;
            default: return 6;
        }
    }

    private void showAddTaskDialog() {
        // Create a custom dialog for adding a new task.
        Dialog<Task> dialog = new Dialog<>();
        dialog.setTitle("Add New Task");
        dialog.setHeaderText("Create a new task");

        // Set the button types.
        ButtonType saveButtonType = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

        // Create the form grid.
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        // Form fields.
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

        // Add fields to grid.
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

        // Request focus on the title field by default.
        Platform.runLater(() -> titleField.requestFocus());

        // Convert the result to a Task object when the save button is clicked.
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
            // Use createTask only.
            taskService.createTask(
                    task.getTitle(),
                    task.getDescription(),
                    task.getCategory(),
                    task.getPriority(),
                    task.getDeadline()
            );
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

        // Create a custom dialog for editing an existing task.
        Dialog<Task> dialog = new Dialog<>();
        dialog.setTitle("Edit Task");
        dialog.setHeaderText("Edit task details");

        // Set the button types.
        ButtonType saveButtonType = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

        // Create the form grid.
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        // Form fields with pre-filled values.
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

        // Add fields to grid.
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

        // Request focus on the title field by default.
        Platform.runLater(() -> titleField.requestFocus());

        // Convert the result to a Task object when the save button is clicked.
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                if (titleField.getText().trim().isEmpty()) {
                    showAlert("Invalid Input", "Task title is required");
                    return null;
                }
                // Update the selected task with new values.
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
                if (statisticsUpdateCallback != null) {
                    statisticsUpdateCallback.run();
                }
            }
        });
    }

    private void performSearch(String searchText) {
        Category selectedCategory = categoryFilter.getValue();
        PriorityLevel selectedPriority = priorityFilter.getValue();

        // Update the predicate of the filtered list based on the search criteria.
        filteredTasks.setPredicate(task -> {
            boolean matchesTitle = (searchText == null || searchText.isEmpty()) ||
                    task.getTitle().toLowerCase().contains(searchText.toLowerCase());
            // Compare by name instead of ID.
            boolean matchesCategory = (selectedCategory == null) ||
                    task.getCategory().getName().equals(selectedCategory.getName());
            // Compare by name instead of ID.
            boolean matchesPriority = (selectedPriority == null) ||
                    task.getPriority().getName().equals(selectedPriority.getName());
            return matchesTitle && matchesCategory && matchesPriority;
        });
    }

    // Refresh method that updates filters and refreshes the table.
    private void refreshTaskList() {
        updateFilters();
        taskTable.refresh();
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
