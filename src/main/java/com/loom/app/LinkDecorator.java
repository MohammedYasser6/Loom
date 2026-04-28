package com.loom.app;

import javafx.scene.Node;
import javafx.scene.control.Hyperlink;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import java.awt.Desktop;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LinkDecorator extends ChatBubbleDecorator {

    public LinkDecorator(ChatBubble wrappee) { super(wrappee); }

    @Override
    public VBox render() {
        VBox bubble = super.render();
        // FIXED: Using Java 21's getLast()
        Node lastNode = bubble.getChildren().getLast();

        if (lastNode instanceof TextFlow flow) {
            List<Node> newNodes = new ArrayList<>();
            // FIXED: Simplified regex to \S
            Pattern urlPattern = Pattern.compile("(https?://\\S+)");

            for (Node n : flow.getChildren()) {
                if (n instanceof Text textNode) {
                    String txt = textNode.getText();
                    Matcher matcher = urlPattern.matcher(txt);
                    int lastEnd = 0;

                    while (matcher.find()) {
                        if (matcher.start() > lastEnd) {
                            Text before = new Text(txt.substring(lastEnd, matcher.start()));
                            before.setStyle(textNode.getStyle());
                            newNodes.add(before);
                        }

                        String url = matcher.group(1);
                        Hyperlink link = new Hyperlink(url);
                        link.setStyle("-fx-text-fill: #00A8FC; -fx-underline: true; -fx-padding: 0; -fx-border-width: 0;");
                        link.setOnAction(e -> {
                            try { Desktop.getDesktop().browse(new URI(url)); }
                            // FIXED: Replaced printStackTrace with a safe error log
                            catch (Exception ex) { System.err.println("Failed to open link: " + ex.getMessage()); }
                        });
                        newNodes.add(link);
                        lastEnd = matcher.end();
                    }
                    if (lastEnd < txt.length()) {
                        Text after = new Text(txt.substring(lastEnd));
                        after.setStyle(textNode.getStyle());
                        newNodes.add(after);
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