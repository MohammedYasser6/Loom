package com.loom.app;

import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public class TimestampDecorator extends ChatBubbleDecorator {

    public TimestampDecorator(ChatBubble wrappee) { super(wrappee); }

    @Override
    public VBox render() {
        VBox bubble = super.render(); // Get the bubble from the layer below

        String time = LocalTime.now().format(DateTimeFormatter.ofPattern("hh:mm a"));
        Label timeLabel = new Label(time);
        timeLabel.setStyle("-fx-text-fill: #80848e; -fx-font-size: 10px;");

        bubble.getChildren().add(0, timeLabel); // Push it to the very top
        return bubble;
    }
}