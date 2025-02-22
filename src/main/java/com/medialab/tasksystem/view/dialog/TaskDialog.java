package com.medialab.tasksystem.view.dialog;

import com.medialab.tasksystem.model.*;
import javafx.scene.control.*;

public class TaskDialog extends BaseDialog<Task> {
    private final TextField titleField;
    private final TextArea descriptionArea;
    private final ComboBox<Category> categoryCombo;
    private final ComboBox<PriorityLevel> priorityCombo;
    private final DatePicker deadlinePicker;
    private final ComboBox<TaskStatus> statusCombo;

    public TaskDialog(Task task, boolean isNew) {
        super(isNew ? "Add New Task" : "Edit Task");

        // Initialize controls
        titleField = new TextField();
        descriptionArea = new TextArea();
        categoryCombo = new ComboBox<>();
        priorityCombo = new ComboBox<>();
        deadlinePicker = new DatePicker();
        statusCombo = new ComboBox<>();

        if (!isNew && task != null) {
            // Fill in existing task data
            titleField.setText(task.getTitle());
            descriptionArea.setText(task.getDescription());
            categoryCombo.setValue(task.getCategory());
            priorityCombo.setValue(task.getPriority());
            deadlinePicker.setValue(task.getDeadline());
            statusCombo.setValue(task.getStatus());
        }
    }

    @Override
    protected void initializeContent() {
        // Add form fields to the content
        content.getChildren().addAll(
                new Label("Title:"),
                titleField,
                new Label("Description:"),
                descriptionArea,
                new Label("Category:"),
                categoryCombo,
                new Label("Priority:"),
                priorityCombo,
                new Label("Deadline:"),
                deadlinePicker,
                new Label("Status:"),
                statusCombo
        );
    }

    @Override
    protected Task convertResult(ButtonType buttonType) {
        if (buttonType == ButtonType.OK) {
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
    }
}