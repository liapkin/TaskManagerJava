package com.medialab.tasksystem.view;

import com.medialab.tasksystem.model.Reminder;
import com.medialab.tasksystem.model.ReminderType;
import com.medialab.tasksystem.model.Task;
import com.medialab.tasksystem.service.ReminderService;
import com.medialab.tasksystem.service.TaskService;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import java.time.LocalDate;

public class ReminderManagementPane extends VBox {
    private final ReminderService reminderService;
    private final TaskService taskService;
    private TableView<Reminder> reminderTable;

    public ReminderManagementPane(ReminderService reminderService, TaskService taskService) {
        this.reminderService = reminderService;
        this.taskService = taskService;
        setPadding(new Insets(10));
        setSpacing(10);
        setupUI();
    }

    private void setupUI() {
        // Create toolbar
        HBox toolbar = new HBox(10);
        Button addButton = new Button("New Reminder");
        Button editButton = new Button("Edit");
        Button deleteButton = new Button("Delete");

        addButton.setOnAction(e -> showAddReminderDialog());
        editButton.setOnAction(e -> showEditReminderDialog());
        deleteButton.setOnAction(e -> deleteSelectedReminder());

        toolbar.getChildren().addAll(addButton, editButton, deleteButton);

        // Create reminder table
        reminderTable = createReminderTable();
        VBox.setVgrow(reminderTable, Priority.ALWAYS);

        // Add components to the pane
        getChildren().addAll(toolbar, reminderTable);

        // Load initial data
        refreshReminderList();
    }

    private TableView<Reminder> createReminderTable() {
        TableView<Reminder> table = new TableView<>();

        TableColumn<Reminder, String> taskCol = new TableColumn<>("Task");
        taskCol.setCellValueFactory(cellData -> {
            Task task = taskService.getTaskById(cellData.getValue().getTaskId());
            return task != null ? javafx.beans.binding.Bindings.createStringBinding(
                    () -> task.getTitle()
            ) : null;
        });

        TableColumn<Reminder, ReminderType> typeCol = new TableColumn<>("Reminder Type");
        typeCol.setCellValueFactory(new PropertyValueFactory<>("type"));
        typeCol.setCellFactory(column -> new TableCell<Reminder, ReminderType>() {
            @Override
            protected void updateItem(ReminderType item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? "" : item.toString());
            }
        });

        TableColumn<Reminder, LocalDate> dateCol = new TableColumn<>("Reminder Date");
        dateCol.setCellValueFactory(new PropertyValueFactory<>("reminderDate"));

        table.getColumns().addAll(taskCol, typeCol, dateCol);
        return table;
    }

    private void showAddReminderDialog() {
        Dialog<Reminder> dialog = new Dialog<>();
        dialog.setTitle("New Reminder");
        dialog.setHeaderText("Create New Reminder");

        // Create dialog content
        VBox content = new VBox(10);
        content.setPadding(new Insets(10));

        ComboBox<Task> taskCombo = new ComboBox<>();
        taskCombo.setCellFactory(listView -> new ListCell<Task>() {
            @Override
            protected void updateItem(Task item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? "" : item.getTitle());
            }
        });
        taskCombo.setButtonCell(new ListCell<Task>() {
            @Override
            protected void updateItem(Task item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? "" : item.getTitle());
            }
        });
        taskCombo.getItems().addAll(taskService.getUncompletedTasks());
        taskCombo.setPromptText("Select Task");

        ComboBox<ReminderType> typeCombo = new ComboBox<>();
        typeCombo.setCellFactory(listView -> new ListCell<ReminderType>() {
            @Override
            protected void updateItem(ReminderType item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? "" : item.toString());
            }
        });
        typeCombo.setButtonCell(new ListCell<ReminderType>() {
            @Override
            protected void updateItem(ReminderType item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? "" : item.toString());
            }
        });
        typeCombo.getItems().addAll(ReminderType.values());
        typeCombo.setPromptText("Select Reminder Type");

        DatePicker datePicker = new DatePicker();
        datePicker.setDisable(true);

        // Enable date picker only for custom date type
        typeCombo.setOnAction(e -> {
            datePicker.setDisable(typeCombo.getValue() != ReminderType.CUSTOM_DATE);
        });

        content.getChildren().addAll(
                new Label("Task:"), taskCombo,
                new Label("Reminder Type:"), typeCombo,
                new Label("Custom Date:"), datePicker
        );

        dialog.getDialogPane().setContent(content);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        dialog.setResultConverter(buttonType -> {
            if (buttonType == ButtonType.OK) {
                Task selectedTask = taskCombo.getValue();
                ReminderType selectedType = typeCombo.getValue();
                LocalDate customDate = datePicker.getValue();

                if (selectedTask == null || selectedType == null) {
                    showAlert("Invalid Input", "Please select both task and reminder type.");
                    return null;
                }

                if (selectedType == ReminderType.CUSTOM_DATE && customDate == null) {
                    showAlert("Invalid Input", "Please select a custom date.");
                    return null;
                }

                try {
                    return reminderService.createReminder(selectedTask, selectedType, customDate);
                } catch (IllegalArgumentException e) {
                    showAlert("Invalid Reminder", e.getMessage());
                    return null;
                }
            }
            return null;
        });

        dialog.showAndWait().ifPresent(reminder -> refreshReminderList());
    }

    private void showEditReminderDialog() {
        Reminder selectedReminder = reminderTable.getSelectionModel().getSelectedItem();
        if (selectedReminder == null) {
            showAlert("No Reminder Selected", "Please select a reminder to edit.");
            return;
        }

        Task associatedTask = taskService.getTaskById(selectedReminder.getTaskId());
        if (associatedTask == null) {
            showAlert("Error", "Associated task not found.");
            return;
        }

        Dialog<Reminder> dialog = new Dialog<>();
        dialog.setTitle("Edit Reminder");
        dialog.setHeaderText("Edit Reminder for Task: " + associatedTask.getTitle());

        VBox content = new VBox(10);
        content.setPadding(new Insets(10));

        TextField taskField = new TextField(associatedTask.getTitle());
        taskField.setDisable(true);

        ComboBox<ReminderType> typeCombo = new ComboBox<>();
        typeCombo.setCellFactory(listView -> new ListCell<ReminderType>() {
            @Override
            protected void updateItem(ReminderType item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? "" : item.toString());
            }
        });
        typeCombo.setButtonCell(new ListCell<ReminderType>() {
            @Override
            protected void updateItem(ReminderType item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? "" : item.toString());
            }
        });
        typeCombo.getItems().addAll(ReminderType.values());
        typeCombo.setValue(selectedReminder.getType());

        DatePicker datePicker = new DatePicker(selectedReminder.getReminderDate());
        datePicker.setDisable(selectedReminder.getType() != ReminderType.CUSTOM_DATE);

        typeCombo.setOnAction(e -> {
            datePicker.setDisable(typeCombo.getValue() != ReminderType.CUSTOM_DATE);
            if (typeCombo.getValue() != ReminderType.CUSTOM_DATE) {
                datePicker.setValue(null);
            }
        });

        content.getChildren().addAll(
                new Label("Task:"), taskField,
                new Label("Reminder Type:"), typeCombo,
                new Label("Custom Date:"), datePicker
        );

        dialog.getDialogPane().setContent(content);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        dialog.setResultConverter(buttonType -> {
            if (buttonType == ButtonType.OK) {
                ReminderType selectedType = typeCombo.getValue();
                LocalDate customDate = datePicker.getValue();

                if (selectedType == null) {
                    showAlert("Invalid Input", "Please select a reminder type.");
                    return null;
                }

                if (selectedType == ReminderType.CUSTOM_DATE && customDate == null) {
                    showAlert("Invalid Input", "Please select a custom date.");
                    return null;
                }

                try {
                    selectedReminder.setType(selectedType);
                    if (selectedType == ReminderType.CUSTOM_DATE) {
                        selectedReminder.setReminderDate(customDate);
                    } else {
                        selectedReminder.setReminderDate(
                                reminderService.calculateReminderDate(associatedTask.getDeadline(), selectedType, null)
                        );
                    }
                    reminderService.updateReminder(selectedReminder);
                    return selectedReminder;
                } catch (IllegalArgumentException e) {
                    showAlert("Invalid Reminder", e.getMessage());
                    return null;
                }
            }
            return null;
        });

        dialog.showAndWait().ifPresent(reminder -> refreshReminderList());
    }

    private void deleteSelectedReminder() {
        Reminder selectedReminder = reminderTable.getSelectionModel().getSelectedItem();
        if (selectedReminder == null) {
            showAlert("No Reminder Selected", "Please select a reminder to delete.");
            return;
        }

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Delete Reminder");
        alert.setHeaderText("Delete Reminder");
        alert.setContentText("Are you sure you want to delete this reminder?");

        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                reminderService.deleteReminder(selectedReminder.getId());
                refreshReminderList();
            }
        });
    }

    private void refreshReminderList() {
        reminderTable.getItems().setAll(reminderService.getActiveReminders());
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}