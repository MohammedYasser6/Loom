package com.loom.app;

import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

import java.io.File;

public class TaskCardFactory implements WorkspaceComponentFactory {

    @Override
    public VBox createComponent(Task task) {
        VBox card = new VBox();
        card.getStyleClass().add("task-card");
        card.setSpacing(12);

        // --- ROW 1: THE TASK TEXT AND DELETE BUTTON ---
        HBox topRow = new HBox();
        topRow.setAlignment(Pos.TOP_LEFT); // Aligns the X to the top right if text is very long
        topRow.setSpacing(10);

        Label title = new Label(task.getTitle());
        title.setStyle("-fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 14px;");

        // FIX: Force the text to wrap and expand the card vertically!
        title.setWrapText(true);
        title.setMaxWidth(Double.MAX_VALUE);
        title.setMinHeight(Region.USE_PREF_SIZE); // Prevents JavaFX from clipping the bottom of long paragraphs
        HBox.setHgrow(title, Priority.ALWAYS);    // Pushes the delete button perfectly to the right

        Button deleteBtn = new Button("X");
        deleteBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: #fa777c; -fx-font-weight: bold; -fx-cursor: hand; -fx-font-size: 14px; -fx-padding: 0 4 0 4;");

        // --- THE DELETE LOGIC ---
        deleteBtn.setOnAction(e -> {
            VBox parentColumn = (VBox) card.getParent();
            if (parentColumn != null) parentColumn.getChildren().remove(card);

            WorkspaceSnapshot snapshot = NetworkManager.getInstance().getActiveSnapshot();
            if (snapshot != null) {
                switch (task.getStatus()) {
                    case "TODO" -> snapshot.removeTodo(task.getTitle());
                    case "IN_PROGRESS" -> snapshot.removeInProgress(task.getTitle());
                    case "DONE" -> snapshot.removeDone(task.getTitle());
                }
                File saveFile = NetworkManager.getInstance().getActiveSaveFile();
                if (saveFile != null) new WorkspaceCaretaker().save(snapshot, saveFile);
            }
            NetworkManager.getInstance().sendMessage("SYS_DELETE|" + task.getStatus() + "|" + task.getTitle());
        });

        topRow.getChildren().addAll(title, deleteBtn);

        // --- ROW 2: THE ASSIGNEE BADGE (BOTTOM) ---
        Label assigneeBadge = new Label(task.getAssignedTo());
        if (task.getAssignedTo().equals("Unassigned")) {
            assigneeBadge.setStyle("-fx-background-color: #4e5058; -fx-text-fill: #b5bac1; -fx-padding: 4 8 4 8; -fx-background-radius: 4; -fx-font-size: 11px;");
        } else {
            assigneeBadge.setStyle("-fx-background-color: #5865F2; -fx-text-fill: white; -fx-padding: 4 8 4 8; -fx-background-radius: 4; -fx-font-size: 11px; -fx-font-weight: bold;");
        }

        // Add the rows to the card in order (Top Row first, Badge at the bottom)
        card.getChildren().addAll(topRow, assigneeBadge);

        // --- DRAG AND DROP ---
        card.setOnDragDetected(event -> {
            Dragboard db = card.startDragAndDrop(TransferMode.MOVE);
            ClipboardContent content = new ClipboardContent();
            content.putString(task.getStatus() + "|" + task.getTitle() + "|" + task.getAssignedTo());
            db.setContent(content);
            event.consume();
        });

        return card;
    }
}