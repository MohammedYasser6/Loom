package com.loom.app;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import java.util.Comparator;

public class AssigneeSortStrategy implements ColumnSortStrategy {

    @Override
    public void sort(VBox column) {
        ObservableList<Node> children = FXCollections.observableArrayList(column.getChildren());

        ObservableList<Node> taskCards = FXCollections.observableArrayList();
        ObservableList<Node> staticUI = FXCollections.observableArrayList();

        for (Node n : children) {
            if (n instanceof VBox && n.getStyleClass().contains("task-card")) {
                taskCards.add(n);
            } else {
                staticUI.add(n);
            }
        }

        // Sort by the Assignee Badge (which is the second item inside the card)
        taskCards.sort(Comparator.comparing(node -> {
            VBox card = (VBox) node;
            Label assigneeLabel = (Label) card.getChildren().get(1);
            return assigneeLabel.getText().toLowerCase();
        }));

        column.getChildren().clear();
        column.getChildren().addAll(staticUI);
        column.getChildren().addAll(taskCards);
    }
}