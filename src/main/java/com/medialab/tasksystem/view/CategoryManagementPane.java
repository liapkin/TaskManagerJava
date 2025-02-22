package com.medialab.tasksystem.view;

import com.medialab.tasksystem.model.Category;
import com.medialab.tasksystem.service.CategoryService;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

public class CategoryManagementPane extends VBox {
    private final CategoryService categoryService;
    private TableView<Category> categoryTable;

    public CategoryManagementPane(CategoryService categoryService) {
        this.categoryService = categoryService;
        setPadding(new Insets(10));
        setSpacing(10);
        setupUI();
    }

    private void setupUI() {
        // Create toolbar
        HBox toolbar = new HBox(10);
        Button addButton = new Button("New Category");
        Button editButton = new Button("Edit");
        Button deleteButton = new Button("Delete");

        addButton.setOnAction(e -> showAddCategoryDialog());
        editButton.setOnAction(e -> showEditCategoryDialog());
        deleteButton.setOnAction(e -> deleteSelectedCategory());

        toolbar.getChildren().addAll(addButton, editButton, deleteButton);

        // Create category table
        categoryTable = createCategoryTable();
        VBox.setVgrow(categoryTable, Priority.ALWAYS);

        // Add components to the pane
        getChildren().addAll(toolbar, categoryTable);

        // Load initial data
        refreshCategoryList();
    }

    private TableView<Category> createCategoryTable() {
        TableView<Category> table = new TableView<>();

        TableColumn<Category, String> nameCol = new TableColumn<>("Category Name");
        nameCol.setCellValueFactory(new PropertyValueFactory<>("name"));
        nameCol.setPrefWidth(200);

        table.getColumns().add(nameCol);
        return table;
    }

    private void showAddCategoryDialog() {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("New Category");
        dialog.setHeaderText("Create New Category");
        dialog.setContentText("Category name:");

        dialog.showAndWait().ifPresent(name -> {
            if (!name.trim().isEmpty()) {
                categoryService.createCategory(name.trim());
                refreshCategoryList();
            }
        });
    }

    private void showEditCategoryDialog() {
        Category selectedCategory = categoryTable.getSelectionModel().getSelectedItem();
        if (selectedCategory == null) {
            showAlert("No Category Selected", "Please select a category to edit.");
            return;
        }

        TextInputDialog dialog = new TextInputDialog(selectedCategory.getName());
        dialog.setTitle("Edit Category");
        dialog.setHeaderText("Edit Category Name");
        dialog.setContentText("New name:");

        dialog.showAndWait().ifPresent(newName -> {
            if (!newName.trim().isEmpty()) {
                selectedCategory.setName(newName.trim());
                categoryService.updateCategory(selectedCategory);
                refreshCategoryList();
            }
        });
    }

    private void deleteSelectedCategory() {
        Category selectedCategory = categoryTable.getSelectionModel().getSelectedItem();
        if (selectedCategory == null) {
            showAlert("No Category Selected", "Please select a category to delete.");
            return;
        }

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Delete Category");
        alert.setHeaderText("Delete Category and Associated Tasks");
        alert.setContentText("Are you sure you want to delete this category? All tasks in this category will also be deleted.");

        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                categoryService.deleteCategory(selectedCategory.getId());
                refreshCategoryList();
            }
        });
    }

    private void refreshCategoryList() {
        categoryTable.getItems().setAll(categoryService.getAllCategories());
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}