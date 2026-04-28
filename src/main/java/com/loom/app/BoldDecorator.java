package com.loom.app;

import javafx.scene.Node;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import java.util.ArrayList;
import java.util.List;

public class BoldDecorator extends ChatBubbleDecorator {

    public BoldDecorator(ChatBubble wrappee) { super(wrappee); }

    @Override
    public VBox render() {
        VBox bubble = super.render();
        // FIXED: Using Java 21's getLast()
        Node lastNode = bubble.getChildren().getLast();

        if (lastNode instanceof TextFlow flow) {
            List<Node> newNodes = new ArrayList<>();

            for (Node n : flow.getChildren()) {
                if (n instanceof Text textNode) {
                    String txt = textNode.getText();
                    if (txt.contains("**")) {
                        String[] parts = txt.split("\\*\\*");
                        for (int i = 0; i < parts.length; i++) {
                            Text t = new Text(parts[i]);
                            t.setStyle(textNode.getStyle());
                            if (i % 2 != 0) {
                                t.setStyle(t.getStyle() + "-fx-font-weight: bold; -fx-fill: white;");
                            }
                            newNodes.add(t);
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