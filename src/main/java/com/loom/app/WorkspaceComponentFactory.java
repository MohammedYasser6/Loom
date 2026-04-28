package com.loom.app;

import javafx.scene.layout.VBox;

// ABSTRACT FACTORY PATTERN: The Blueprint
public interface WorkspaceComponentFactory {
    VBox createComponent(Task task);
}