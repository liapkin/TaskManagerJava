package com.medialab.tasksystem.view;

import com.medialab.tasksystem.model.PriorityLevel;
import com.medialab.tasksystem.service.PriorityService;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

public class PriorityManagementPane extends VBox {
    private final PriorityService priorityService;
    private TableView<PriorityLevel> priorityTable;

    public PriorityManagementPane(PriorityService priorityService) {
        this.priorityService = priorityService;
        setPadding(new Insets(10));
        setSpacing(10);
        setupUI();
    }

    private void setupUI() {
        // Create toolbar
        HBox toolbar = new HBox(10);
        Button addButton = new Button("New Priority Level");
        Button editButton = new Button("Edit");
        Button deleteButton = new Button("Delete");

        addButton.setOnAction(e -> showAddPriorityDialog());
        editButton.setOnAction(e -> showEditPriorityDialog());
        deleteButton.setOnAction(e -> deleteSelectedPriority());

        toolbar.getChildren().addAll(addButton, editButton, deleteButton);

        // Create priority table
        priorityTable = createPriorityTable();
        VBox.setVgrow(priorityTable, Priority.ALWAYS);

        // Add components to the pane
        getChildren().addAll(toolbar, priorityTable);

        // Load initial data
        refreshPriorityList();
    }

    private TableView<PriorityLevel> createPriorityTable() {
        TableView<PriorityLevel> table = new TableView<>();

        TableColumn<PriorityLevel, String> nameCol = new TableColumn<>("Priority Level");
        nameCol.setCellValueFactory(new PropertyValueFactory<>("name"));
        nameCol.setPrefWidth(200);

        TableColumn<PriorityLevel, Boolean> defaultCol = new TableColumn<>("Default");
        defaultCol.setCellValueFactory(new PropertyValueFactory<>("default"));
        defaultCol.setPrefWidth(100);

        table.getColumns().addAll(nameCol, defaultCol);
        return table;
    }

    private void showAddPriorityDialog() {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("New Priority Level");
        dialog.setHeaderText("Create New Priority Level");
        dialog.setContentText("Priority level name:");

        dialog.showAndWait().ifPresent(name -> {
            if (!name.trim().isEmpty()) {
                priorityService.createPriorityLevel(name.trim(), false);
                refreshPriorityList();
            }
        });
    }

    private void showEditPriorityDialog() {
        PriorityLevel selectedPriority = priorityTable.getSelectionModel().getSelectedItem();
        if (selectedPriority == null) {
            showAlert("No Priority Selected", "Please select a priority level to edit.");
            return;
        }

        if (selectedPriority.isDefault()) {
            showAlert("Cannot Edit Default", "The default priority level cannot be modified.");
            return;
        }

        TextInputDialog dialog = new TextInputDialog(selectedPriority.getName());
        dialog.setTitle("Edit Priority Level");
        dialog.setHeaderText("Edit Priority Level Name");
        dialog.setContentText("New name:");

        dialog.showAndWait().ifPresent(newName -> {
            if (!newName.trim().isEmpty()) {
                selectedPriority.setName(newName.trim());
                priorityService.updatePriorityLevel(selectedPriority);
                refreshPriorityList();
            }
        });
    }

    private void deleteSelectedPriority() {
        PriorityLevel selectedPriority = priorityTable.getSelectionModel().getSelectedItem();
        if (selectedPriority == null) {
            showAlert("No Priority Selected", "Please select a priority level to delete.");
            return;
        }

        if (selectedPriority.isDefault()) {
            showAlert("Cannot Delete Default", "The default priority level cannot be deleted.");
            return;
        }

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Delete Priority Level");
        alert.setHeaderText("Delete Priority Level");
        alert.setContentText("Are you sure you want to delete this priority level? Tasks with this priority will be set to the default priority level.");

        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                priorityService.deletePriorityLevel(selectedPriority.getId());
                refreshPriorityList();
            }
        });
    }

    private void refreshPriorityList() {
        priorityTable.getItems().setAll(priorityService.getAllPriorityLevels());
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}