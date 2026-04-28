package com.loom.app;

import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;

public class SimpleChatBubble implements ChatBubble {
    private final String sender;
    private final String message;

    public SimpleChatBubble(String sender, String message) {
        this.sender = sender;
        this.message = message;
    }

    @Override
    public VBox render() {
        VBox bubble = new VBox();
        bubble.setSpacing(2);

        // Name Tag
        Text nameText = new Text(sender + "\n");
        nameText.setStyle("-fx-fill: #5865F2; -fx-font-weight: bold; -fx-font-size: 14px;");

        // Raw Message Text
        Text msgText = new Text(message);
        msgText.setStyle("-fx-fill: #dbdee1; -fx-font-size: 13px;");

        // Put them together in a Flow
        TextFlow flow = new TextFlow(nameText, msgText);
        bubble.getChildren().add(flow);

        return bubble;
    }
}