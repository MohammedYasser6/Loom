package com.loom.app;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.Dragboard;
import javafx.scene.input.KeyCode;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.nio.file.Files;
import java.util.Base64;

public class MainDashboardController implements NetworkObserver {

    @FXML private Label networkStatusLabel;

    @FXML private VBox todoColumn;
    @FXML private VBox inProgressColumn;
    @FXML private VBox doneColumn;

    @FXML private TextArea newTaskInput;
    @FXML private ComboBox<String> assigneeDropdown;
    @FXML private ComboBox<String> boardSortDropdown;

    @FXML private TextField chatInput;
    @FXML private VBox chatHistoryBox;

    @FXML
    public void initialize() {
        NetworkManager network = NetworkManager.getInstance();
        network.addObserver(this);

        if (network.getUsername().equals("Unknown")) {
            network.setUsername(network.isHosting() ? "Manager" : "Teammate_" + (int)(Math.random() * 100));
        }

        if (network.getActiveSnapshot() == null) {
            network.setActiveSnapshot(new WorkspaceSnapshot());
        }

        if (network.isHosting()) {
            networkStatusLabel.setText("Status: HOSTING | Role: " + network.getRole());
            assigneeDropdown.setVisible(true);
            assigneeDropdown.setManaged(true);
            assigneeDropdown.getItems().addAll("Unassigned", network.getUsername());
            assigneeDropdown.getSelectionModel().selectFirst();
            loadDynamicTasks();
        } else {
            networkStatusLabel.setText("Status: CONNECTED | Role: " + network.getRole());
            network.sendMessage("SYS_REQUEST_SYNC");
            network.sendMessage("SYS_JOIN|" + network.getUsername());

            Label joinNotification = new Label("🚀 You joined the workspace as " + network.getUsername() + "!");
            joinNotification.setStyle("-fx-text-fill: #5865F2; -fx-font-weight: bold; -fx-font-style: italic; -fx-padding: 5 0 5 0;");
            chatHistoryBox.getChildren().add(joinNotification);
        }

        setupDropTarget(todoColumn, "TODO");
        setupDropTarget(inProgressColumn, "IN_PROGRESS");
        setupDropTarget(doneColumn, "DONE");

        newTaskInput.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER && !event.isShiftDown()) {
                event.consume();
                handleAddTask();
            }
        });

        if (boardSortDropdown != null) {
            boardSortDropdown.getItems().addAll("Sort A-Z", "Sort by Assignee");

            boardSortDropdown.setOnAction(e -> {
                String selection = boardSortDropdown.getValue();
                if (selection == null) return;

                Platform.runLater(() -> {
                    ColumnSortStrategy strategy = null;

                    if ("Sort A-Z".equals(selection)) {
                        strategy = new AlphabeticalSortStrategy();
                    } else if ("Sort by Assignee".equals(selection)) {
                        strategy = new AssigneeSortStrategy();
                    }

                    if (strategy != null) {
                        strategy.sort(todoColumn);
                        strategy.sort(inProgressColumn);
                        strategy.sort(doneColumn);
                    }

                    boardSortDropdown.getSelectionModel().clearSelection();
                });
            });
        }
    }

    // --- THE FIX: Extracting the assignee from the saved string ---
    private void loadDynamicTasks() {
        WorkspaceSnapshot snapshot = NetworkManager.getInstance().getActiveSnapshot();
        if (snapshot != null) {
            for (String data : snapshot.getTodoTasks()) {
                String[] p = data.split("~~");
                processTaskAdd("TODO", p[0], p.length > 1 ? p[1] : "Unassigned", false);
            }
            for (String data : snapshot.getInProgressTasks()) {
                String[] p = data.split("~~");
                processTaskAdd("IN_PROGRESS", p[0], p.length > 1 ? p[1] : "Unassigned", false);
            }
            for (String data : snapshot.getDoneTasks()) {
                String[] p = data.split("~~");
                processTaskAdd("DONE", p[0], p.length > 1 ? p[1] : "Unassigned", false);
            }
        }
    }

    @FXML
    public void handleAddTask() {
        String rawText = newTaskInput.getText();

        if (rawText != null && !rawText.trim().isEmpty()) {

            // --- THE FIX: SANITIZE THE DATA ---
            // Remove any hidden newlines, carriage returns, or pipe symbols that would break the network stream!
            String taskText = rawText.replace("\n", " ")
                    .replace("\r", "")
                    .replace("|", "-")
                    .trim();

            newTaskInput.clear();
            String assignee = "Unassigned";

            if (NetworkManager.getInstance().getRole().equals("MANAGER")) {
                assignee = assigneeDropdown.getValue();
                if (assignee == null) assignee = "Unassigned";
            }

            // Send the clean data to the UI and the Network
            processTaskAdd("TODO", taskText, assignee, true);
            NetworkManager.getInstance().sendMessage("SYS_ADD|TODO|" + taskText + "|" + assignee);
        }
    }

    private void processTaskAdd(String column, String text, String assignee, boolean isNewTask) {
        String myRole = NetworkManager.getInstance().getRole();
        String myName = NetworkManager.getInstance().getUsername();

        boolean isManager = myRole.equals("MANAGER");
        boolean isAssignedToMe = myName.equals(assignee);
        boolean isUnassigned = assignee.equals("Unassigned");
        boolean canSeeTask = isManager || isAssignedToMe || isUnassigned || !column.equals("IN_PROGRESS");

        if (canSeeTask) {
            WorkspaceComponentFactory factory = new TaskCardFactory();
            Task newTask = new Task.Builder(text, column).assignedTo(assignee).build();

            switch (column) {
                case "TODO" -> todoColumn.getChildren().add(factory.createComponent(newTask));
                case "IN_PROGRESS" -> inProgressColumn.getChildren().add(factory.createComponent(newTask));
                case "DONE" -> doneColumn.getChildren().add(factory.createComponent(newTask));
            }
        }

        if (isNewTask) {
            updateSnapshotState(column, text, assignee, true);
        }
    }

    private void processTaskMove(String source, String target, String text, String assignee) {
        removeTaskFromUI(source, text);
        processTaskAdd(target, text, assignee, true);
        updateSnapshotState(source, text, assignee, false);
    }

    private void processTaskDelete(String column, String text, String assignee) {
        removeTaskFromUI(column, text);
        updateSnapshotState(column, text, assignee, false);
    }

    private void removeTaskFromUI(String column, String text) {
        VBox targetCol = switch (column) {
            case "TODO" -> todoColumn;
            case "IN_PROGRESS" -> inProgressColumn;
            case "DONE" -> doneColumn;
            default -> null;
        };
        if (targetCol != null) {
            targetCol.getChildren().removeIf(node -> node instanceof VBox && containsText((VBox)node, text));
        }
    }

    // --- THE FIX: Packing the Assignee into the Snapshot using "~~" ---
    private void updateSnapshotState(String column, String text, String assignee, boolean isAdd) {
        WorkspaceSnapshot snapshot = NetworkManager.getInstance().getActiveSnapshot();
        if (snapshot == null) return;

        String savedData = text + "~~" + assignee;

        if (isAdd) {
            switch (column) {
                case "TODO" -> snapshot.addTodo(savedData);
                case "IN_PROGRESS" -> snapshot.addInProgress(savedData);
                case "DONE" -> snapshot.addDone(savedData);
            }
        } else {
            switch (column) {
                case "TODO" -> snapshot.removeTodo(savedData);
                case "IN_PROGRESS" -> snapshot.removeInProgress(savedData);
                case "DONE" -> snapshot.removeDone(savedData);
            }
        }
        File saveFile = NetworkManager.getInstance().getActiveSaveFile();
        if (saveFile != null) new WorkspaceCaretaker().save(snapshot, saveFile);
    }

    private boolean containsText(Parent parent, String targetText) {
        for (javafx.scene.Node node : parent.getChildrenUnmodifiable()) {
            if (node instanceof Label label && targetText.equals(label.getText())) return true;
            if (node instanceof Parent childParent && containsText(childParent, targetText)) return true;
        }
        return false;
    }

    private void setupDropTarget(VBox column, String targetStatus) {
        column.setOnDragOver(event -> {
            if (event.getGestureSource() != column && event.getDragboard().hasString()) event.acceptTransferModes(TransferMode.MOVE);
            event.consume();
        });

        column.setOnDragDropped(event -> {
            Dragboard db = event.getDragboard();
            boolean success = false;
            if (db.hasString()) {
                String[] data = db.getString().split("\\|");
                String sourceStatus = data[0], taskText = data[1], assignee = data.length > 2 ? data[2] : "Unassigned";
                if (!sourceStatus.equals(targetStatus)) {
                    processTaskMove(sourceStatus, targetStatus, taskText, assignee);
                    NetworkManager.getInstance().sendMessage("SYS_MOVE|" + sourceStatus + "|" + targetStatus + "|" + taskText + "|" + assignee);
                }
                success = true;
            }
            event.setDropCompleted(success);
            event.consume();
        });
    }

    @Override
    public void onMessageReceived(String message) {
        Platform.runLater(() -> {
            if (message.startsWith("SYS_")) {
                if (message.startsWith("SYS_JOIN|")) {
                    String[] parts = message.split("\\|");
                    if (parts.length >= 2) {
                        String newMember = parts[1];
                        if (NetworkManager.getInstance().isHosting() && assigneeDropdown != null && !assigneeDropdown.getItems().contains(newMember)) {
                            assigneeDropdown.getItems().add(newMember);
                        }
                        Label joinNotification = new Label("🚀 " + newMember + " has joined the workspace!");
                        joinNotification.setStyle("-fx-text-fill: #5865F2; -fx-font-weight: bold; -fx-font-style: italic; -fx-padding: 5 0 5 0;");
                        chatHistoryBox.getChildren().add(joinNotification);
                    }
                }
                else if (message.startsWith("SYS_FILE|")) {
                    String[] parts = message.split("\\|", 4);
                    if (parts.length >= 4) {
                        chatHistoryBox.getChildren().add(createMediaBubble(parts[1], parts[2], parts[3]));
                    }
                }
                else if (message.startsWith("SYS_ADD|")) {
                    String[] parts = message.split("\\|");
                    if (parts.length >= 4) processTaskAdd(parts[1], parts[2], parts[3], true);
                }
                else if (message.startsWith("SYS_MOVE|")) {
                    String[] parts = message.split("\\|");
                    if (parts.length >= 5) processTaskMove(parts[1], parts[2], parts[3], parts[4]);
                }
                else if (message.startsWith("SYS_DELETE|")) {
                    String[] parts = message.split("\\|");
                    if (parts.length >= 4) processTaskDelete(parts[1], parts[2], parts[3]);
                    else if (parts.length == 3) processTaskDelete(parts[1], parts[2], "Unassigned");
                }
                else if (message.equals("SYS_REQUEST_SYNC")) {
                    if (NetworkManager.getInstance().isHosting()) {
                        WorkspaceSnapshot snapshot = NetworkManager.getInstance().getActiveSnapshot();
                        if (snapshot != null) {
                            // --- THE FIX: Send the actual assignee over the network during Sync ---
                            for (String data : snapshot.getTodoTasks()) {
                                String[] p = data.split("~~");
                                NetworkManager.getInstance().sendMessage("SYS_ADD|TODO|" + p[0] + "|" + (p.length > 1 ? p[1] : "Unassigned"));
                            }
                            for (String data : snapshot.getInProgressTasks()) {
                                String[] p = data.split("~~");
                                NetworkManager.getInstance().sendMessage("SYS_ADD|IN_PROGRESS|" + p[0] + "|" + (p.length > 1 ? p[1] : "Unassigned"));
                            }
                            for (String data : snapshot.getDoneTasks()) {
                                String[] p = data.split("~~");
                                NetworkManager.getInstance().sendMessage("SYS_ADD|DONE|" + p[0] + "|" + (p.length > 1 ? p[1] : "Unassigned"));
                            }
                        }
                    }
                }
                return;
            }

            int colonIndex = message.indexOf(":");
            if (colonIndex != -1) {
                String sender = message.substring(0, colonIndex);
                String text = message.substring(colonIndex + 1).trim();
                chatHistoryBox.getChildren().add(buildDecoratedBubble(sender, text));
            }
        });
    }

    @FXML
    public void handleSendMessage() {
        String text = chatInput.getText();
        if (text != null && !text.trim().isEmpty()) {
            String myName = NetworkManager.getInstance().getUsername();
            NetworkManager.getInstance().sendMessage(myName + ":" + text);
            chatHistoryBox.getChildren().add(buildDecoratedBubble(myName + " (You)", text));
            chatInput.clear();
        }
    }

    private VBox buildDecoratedBubble(String sender, String rawText) {
        ChatBubble bubble = new SimpleChatBubble(sender, rawText);
        bubble = new CodeSnippetDecorator(bubble);
        bubble = new LinkDecorator(bubble);
        bubble = new BoldDecorator(bubble);
        bubble = new TimestampDecorator(bubble);
        return bubble.render();
    }

    @FXML
    public void handleAttachFile() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select File to Send");

        File selectedFile = fileChooser.showOpenDialog(chatInput.getScene().getWindow());

        if (selectedFile != null) {
            try {
                byte[] fileContent = Files.readAllBytes(selectedFile.toPath());
                String base64File = Base64.getEncoder().encodeToString(fileContent);

                String myName = NetworkManager.getInstance().getUsername();
                NetworkManager.getInstance().sendMessage("SYS_FILE|" + myName + "|" + selectedFile.getName() + "|" + base64File);

                chatHistoryBox.getChildren().add(createMediaBubble(myName + " (You)", selectedFile.getName(), base64File));

            } catch (Exception e) {
                System.err.println("Failed to attach file: " + e.getMessage());
            }
        }
    }

    private VBox createMediaBubble(String senderName, String fileName, String base64Data) {
        VBox bubble = new VBox();
        bubble.setSpacing(10);
        bubble.setStyle("-fx-background-color: #2b2d31; -fx-padding: 15; -fx-background-radius: 8;");

        Label senderLabel = new Label(senderName + " shared a file:");
        senderLabel.setStyle("-fx-text-fill: #949ba4; -fx-font-weight: bold;");

        Label fileLabel = new Label("📄 " + fileName);
        fileLabel.setStyle("-fx-text-fill: white; -fx-font-weight: bold; -fx-wrap-text: true;");

        String lowerName = fileName.toLowerCase();
        boolean isImage = lowerName.endsWith(".png") || lowerName.endsWith(".jpg") || lowerName.endsWith(".jpeg") || lowerName.endsWith(".gif");

        ImageView imageView = null;
        if (isImage) {
            try {
                byte[] imgBytes = Base64.getDecoder().decode(base64Data);
                Image img = new Image(new ByteArrayInputStream(imgBytes));
                imageView = new ImageView(img);
                imageView.setFitWidth(220);
                imageView.setPreserveRatio(true);
            } catch (Exception e) {
                System.err.println("Image preview failed to load.");
            }
        }

        Button downloadBtn = createDownloadButton(fileName, base64Data);

        bubble.getChildren().add(senderLabel);
        if (imageView != null) {
            bubble.getChildren().add(imageView);
        }

        HBox bottomRow = new HBox(10);
        bottomRow.setAlignment(Pos.CENTER_LEFT);
        bottomRow.getChildren().addAll(downloadBtn, fileLabel);

        bubble.getChildren().add(bottomRow);

        return bubble;
    }

    private Button createDownloadButton(String fileName, String base64Data) {
        Button downloadBtn = new Button("Download");
        downloadBtn.setStyle("-fx-background-color: #5865F2; -fx-text-fill: white; -fx-cursor: hand; -fx-padding: 6 12 6 12; -fx-font-weight: bold; -fx-background-radius: 4;");

        downloadBtn.setOnAction(e -> {
            FileChooser saveDialog = new FileChooser();
            saveDialog.setTitle("Save File");
            saveDialog.setInitialFileName(fileName);
            File saveLocation = saveDialog.showSaveDialog(downloadBtn.getScene().getWindow());

            if (saveLocation != null) {
                try {
                    byte[] fileBytes = Base64.getDecoder().decode(base64Data);
                    Files.write(saveLocation.toPath(), fileBytes);
                    downloadBtn.setText("Saved!");
                    downloadBtn.setStyle("-fx-background-color: #23a559; -fx-text-fill: white; -fx-padding: 6 12 6 12; -fx-font-weight: bold; -fx-background-radius: 4;");
                } catch (Exception ex) {
                    System.err.println("Failed to save file.");
                }
            }
        });
        return downloadBtn;
    }

    @FXML
    public void handleAuditTasks() {
        WorkspaceSnapshot snapshot = NetworkManager.getInstance().getActiveSnapshot();
        if (snapshot == null) return;
        Label header = new Label("--- WORKSPACE AUDIT REPORT ---");
        header.setStyle("-fx-text-fill: #f5c518; -fx-font-weight: bold;");
        chatHistoryBox.getChildren().add(header);
        for (String taskData : snapshot) {
            Label taskLabel = new Label(taskData);
            taskLabel.setStyle("-fx-text-fill: #dbdee1; -fx-font-style: italic;");
            chatHistoryBox.getChildren().add(taskLabel);
        }
    }
}
