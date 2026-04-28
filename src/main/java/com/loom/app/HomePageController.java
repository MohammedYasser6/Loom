package com.loom.app;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputDialog;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.util.Optional;

public class HomePageController {

    @FXML private TextField nameInput;
    @FXML private Label errorLabel;

    @FXML
    public void handleHostAction(ActionEvent event) {
        if (!validateName()) return;

        NetworkManager.getInstance().setUsername(nameInput.getText().trim());

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Host Workspace");
        alert.setHeaderText("What type of workspace do you want to host?");

        ButtonType btnNew = new ButtonType("Create New");
        ButtonType btnLoad = new ButtonType("Load Existing (.json)");
        ButtonType btnCancel = new ButtonType("Cancel", ButtonType.CANCEL.getButtonData());
        alert.getButtonTypes().setAll(btnNew, btnLoad, btnCancel);

        Optional<ButtonType> result = alert.showAndWait();
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();

        if (result.isPresent() && result.get() == btnNew) {
            // --- CREATE NEW WORKSPACE LOOP ---
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Choose Save Location for New Workspace");
            fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Loom Save Data", "*.json"));
            fileChooser.setInitialFileName("my_loom_workspace.json");

            File newFile = fileChooser.showSaveDialog(stage);

            if (newFile != null) {
                WorkspaceSnapshot emptySnapshot = new WorkspaceSnapshot();
                NetworkManager.getInstance().setActiveSnapshot(emptySnapshot);
                NetworkManager.getInstance().setActiveSaveFile(newFile);

                new WorkspaceCaretaker().save(emptySnapshot, newFile);

                NetworkManager.getInstance().hostWorkspace();
                goToDashboard(event);
            }

        } else if (result.isPresent() && result.get() == btnLoad) {
            // --- LOAD EXISTING WORKSPACE LOOP ---
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Open Loom Workspace File");
            fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Loom Save Data", "*.json"));

            File selectedFile = fileChooser.showOpenDialog(stage);

            if (selectedFile != null) {
                WorkspaceCaretaker caretaker = new WorkspaceCaretaker();
                WorkspaceSnapshot snapshot = caretaker.load(selectedFile);

                NetworkManager.getInstance().setActiveSnapshot(snapshot);
                NetworkManager.getInstance().setActiveSaveFile(selectedFile);

                NetworkManager.getInstance().hostWorkspace();
                goToDashboard(event);
            }
        }
    }

    @FXML
    public void handleJoinAction(ActionEvent event) {
        if (!validateName()) return;

        NetworkManager.getInstance().setUsername(nameInput.getText().trim());

        // --- CLIENT JOIN LOOP ---
        TextInputDialog dialog = new TextInputDialog("localhost");
        dialog.setTitle("Join Workspace");
        dialog.setHeaderText("Enter the Host's IP Address:");
        dialog.setContentText("IP Address (e.g., 192.168.1.5 or localhost):");

        Optional<String> result = dialog.showAndWait();

        if (result.isPresent()) {
            String ipAddress = result.get().trim();

            boolean connected = NetworkManager.getInstance().joinWorkspace(ipAddress);

            if (connected) {
                // Clients do not load local files; they sync with the host's memory
                NetworkManager.getInstance().setActiveSnapshot(null);
                NetworkManager.getInstance().setActiveSaveFile(null);

                goToDashboard(event);
            } else {
                Alert error = new Alert(Alert.AlertType.ERROR);
                error.setTitle("Connection Failed");
                error.setHeaderText("Could not find a workspace at " + ipAddress);
                error.setContentText("Ensure the host is running Loom and your firewalls are not blocking Port 8080.");
                error.showAndWait();
            }
        }
    }

    private boolean validateName() {
        if (nameInput.getText() == null || nameInput.getText().trim().isEmpty()) {
            errorLabel.setText("Please enter a display name.");
            return false;
        }
        errorLabel.setText("");
        return true;
    }

    private void goToDashboard(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("MainDashboard.fxml"));
            Parent root = loader.load();

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root, 1024, 768));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Failed to load the dashboard.");
        }
    }
}