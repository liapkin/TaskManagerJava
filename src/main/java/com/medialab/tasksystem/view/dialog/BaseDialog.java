package com.medialab.tasksystem.view.dialog;

import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.DialogPane;
import javafx.scene.layout.VBox;
import javafx.geometry.Insets;

public abstract class BaseDialog<T> extends Dialog<T> {
    protected final VBox content;

    public BaseDialog(String title) {
        setTitle(title);

        // Create the content pane
        content = new VBox(10);
        content.setPadding(new Insets(10));

        // Set up the dialog pane
        DialogPane dialogPane = getDialogPane();
        dialogPane.setContent(content);
        dialogPane.getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        // Initialize the dialog content
        initializeContent();

        // Set the result converter
        setResultConverter(this::convertResult);
    }

    protected abstract void initializeContent();

    protected abstract T convertResult(ButtonType buttonType);
}