package com.loom.app;

import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import java.util.ArrayList;
import java.util.List;

public class CodeSnippetDecorator extends ChatBubbleDecorator {

    public CodeSnippetDecorator(ChatBubble wrappee) { super(wrappee); }

    @Override
    public VBox render() {
        VBox bubble = super.render();
        Node lastNode = bubble.getChildren().getLast();

        if (lastNode instanceof TextFlow flow) {
            List<Node> newNodes = new ArrayList<>();

            for (Node n : flow.getChildren()) {
                if (n instanceof Text textNode) {
                    String txt = textNode.getText();

                    if (txt.contains("```")) {
                        String[] parts = txt.split("```");
                        for (int i = 0; i < parts.length; i++) {
                            if (parts[i].trim().isEmpty()) continue;

                            if (i % 2 != 0) {
                                Label codeBox = new Label(parts[i].trim());
                                codeBox.setWrapText(true);

                                codeBox.setStyle("-fx-background-color: #1e1f22; -fx-text-fill: #00ffcc; -fx-font-family: 'Consolas'; -fx-padding: 10; -fx-background-radius: 6; -fx-border-color: #3f4147; -fx-border-radius: 6;");

                                // Bind to the bubble width so it fits perfectly on its new line
                                codeBox.prefWidthProperty().bind(bubble.widthProperty().subtract(15));

                                VBox container = new VBox(codeBox);
                                container.setStyle("-fx-padding: 5 0 5 0;");

                                // --- THE FIX: FORCE IT TO A SEPARATE LINE ---
                                newNodes.add(new Text("\n")); // Drops the code box to a new line
                                newNodes.add(container);      // Adds the code box
                                newNodes.add(new Text("\n")); // Drops the following text below the code box

                            } else {
                                Text t = new Text(parts[i]);
                                t.setStyle(textNode.getStyle());
                                newNodes.add(t);
                            }
                        }
                    } else {
                        newNodes.add(n);
                    }
                } else {
                    newNodes.add(n);
                }
            }
            flow.getChildren().setAll(newNodes);
        }
        return bubble;
    }
}