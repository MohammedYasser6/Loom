package com.loom.app;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import java.util.Comparator;

public class AlphabeticalSortStrategy implements ColumnSortStrategy {

    @Override
    public void sort(VBox column) {
        ObservableList<Node> children = FXCollections.observableArrayList(column.getChildren());

        ObservableList<Node> taskCards = FXCollections.observableArrayList();
        ObservableList<Node> staticUI = FXCollections.observableArrayList(); // Headers, inputs, etc.

        // 1. Separate the Task Cards from the headers
        for (Node n : children) {
            if (n instanceof VBox && n.getStyleClass().contains("task-card")) {
                taskCards.add(n);
            } else {
                staticUI.add(n);
            }
        }

        // 2. Sort the Task Cards by reading their Title Label
        taskCards.sort(Comparator.comparing(node -> {
            VBox card = (VBox) node;
            HBox topRow = (HBox) card.getChildren().get(0);
            Label titleLabel = (Label) topRow.getChildren().get(0);
            return titleLabel.getText().toLowerCase();
        }));

        // 3. Put everything back together!
        column.getChildren().clear();
        column.getChildren().addAll(staticUI);
        column.getChildren().addAll(taskCards);
    }
}