package com.loom.app;

import java.io.*;
import java.nio.file.Files;

// This is the "Caretaker" - it safely stores and retrieves the Memento
public class WorkspaceCaretaker {

    // Saves the snapshot to a file
    public void save(WorkspaceSnapshot snapshot, File file) {
        try (PrintWriter writer = new PrintWriter(new FileWriter(file))) {
            writer.println("{");
            writeListToJson(writer, "TODO", snapshot.getTodoTasks(), true);
            writeListToJson(writer, "IN_PROGRESS", snapshot.getInProgressTasks(), true);
            writeListToJson(writer, "DONE", snapshot.getDoneTasks(), false);
            writer.println("}");
            System.out.println("Workspace saved successfully to: " + file.getAbsolutePath());
        } catch (IOException e) {
            System.err.println("Failed to save workspace.");
            e.printStackTrace();
        }
    }

    // Helper method to format the arrays
    private void writeListToJson(PrintWriter writer, String key, java.util.List<String> list, boolean addComma) {
        writer.print("  \"" + key + "\": [");
        for (int i = 0; i < list.size(); i++) {
            writer.print("\"" + list.get(i).replace("\"", "\\\"") + "\"");
            if (i < list.size() - 1) writer.print(", ");
        }
        writer.println("]" + (addComma ? "," : ""));
    }

    // Loads the file and rebuilds the snapshot
    public WorkspaceSnapshot load(File file) {
        WorkspaceSnapshot snapshot = new WorkspaceSnapshot();
        if (file == null || !file.exists()) return snapshot;

        try {
            String content = new String(Files.readAllBytes(file.toPath()));

            // Extract the lists using basic string parsing
            extractTasks(content, "\"TODO\": [", snapshot.getTodoTasks());
            extractTasks(content, "\"IN_PROGRESS\": [", snapshot.getInProgressTasks());
            extractTasks(content, "\"DONE\": [", snapshot.getDoneTasks());

            System.out.println("Workspace loaded successfully.");
        } catch (IOException e) {
            System.err.println("Failed to read the save file.");
        }
        return snapshot;
    }

    // Helper method to pull the strings out of the JSON arrays
    private void extractTasks(String json, String key, java.util.List<String> targetList) {
        int startIndex = json.indexOf(key);
        if (startIndex == -1) return;

        startIndex += key.length();
        int endIndex = json.indexOf("]", startIndex);

        String arrayContent = json.substring(startIndex, endIndex).trim();
        if (arrayContent.isEmpty()) return;

        // Split by comma and clean up the quotes
        String[] tasks = arrayContent.split("\",\\s*\"");
        for (String task : tasks) {
            targetList.add(task.replace("\"", "").trim());
        }
    }
}