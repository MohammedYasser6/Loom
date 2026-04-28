package com.loom.app;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
// --- ADDED IMPORTS FOR THE ICON ---
import javafx.scene.image.Image;
import java.util.Objects;
// ----------------------------------
import javafx.stage.Stage;

import java.io.IOException;

public class LoomApp extends Application {

    @Override
    public void start(Stage primaryStage) throws IOException {
        // Load your HomePage FXML
        FXMLLoader fxmlLoader = new FXMLLoader(LoomApp.class.getResource("HomePage.fxml"));
        Parent root = fxmlLoader.load();

        // --- ADD YOUR ICON HERE ---
        try {
            // Java looks inside the 'resources' folder by default when starting with a slash "/"
            Image appIcon = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/assets/loom-icon.png")));
            primaryStage.getIcons().add(appIcon);
        } catch (Exception e) {
            System.err.println("Could not load app icon. Make sure loom-icon.png is inside src/main/resources/assets/");
        }
        // --------------------------

        primaryStage.setTitle("Loom - Agile Workspace");
        primaryStage.setScene(new Scene(root, 1024, 768));
        primaryStage.show();

    }

    @Override
    public void stop() throws Exception {
        // 1. Tell the network to release the port
        NetworkManager.getInstance().stopNetwork();

        // 2. Brutally force any remaining background threads to terminate
        System.exit(0);
    }
}