package com.loom.app;

public class Task {
    // 1. All fields are private and final (Immutable)
    private final String title;
    private final String status;
    private final String assignedTo;

    // 2. The Private Constructor - Only the Builder can call this!
    private Task(Builder builder) {
        this.title = builder.title;
        this.status = builder.status;
        this.assignedTo = builder.assignedTo;

    }

    // --- GETTERS ---
    public String getTitle() { return title; }
    public String getStatus() { return status; }
    public String getAssignedTo() { return assignedTo; }


    // 3. The Static Inner Builder Class
    public static class Builder {
        // Required parameters
        private final String title;
        private final String status;

        // Optional parameters - Initialized to default values
        private String assignedTo = "Unassigned";


        // Builder Constructor requires the mandatory fields
        public Builder(String title, String status) {
            this.title = title;
            this.status = status;
        }

        // Fluent setter methods for optional fields
        public Builder assignedTo(String assignee) {
            this.assignedTo = assignee;
            return this; // Returns the builder so we can chain methods!
        }



        // The final build method that generates the actual Task
        public Task build() {
            return new Task(this);
        }
    }
}