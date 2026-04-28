package com.loom.app;
import javafx.scene.layout.VBox;

public abstract class ChatBubbleDecorator implements ChatBubble {
    protected ChatBubble wrappee;

    public ChatBubbleDecorator(ChatBubble wrappee) {
        this.wrappee = wrappee;
    }

    @Override
    public VBox render() {
        return wrappee.render(); // Pass the rendering down the chain
    }
}