package com.loom.app;

import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

// ITERATOR PATTERN: Flawlessly traverses three separate lists as if they were one.
public class WorkspaceIterator implements Iterator<String> {

    private final List<String> todo;
    private final List<String> inProgress;
    private final List<String> done;

    private int currentList = 0; // 0 = todo, 1 = inProgress, 2 = done
    private int currentIndex = 0;

    public WorkspaceIterator(WorkspaceSnapshot snapshot) {
        this.todo = snapshot.getTodoTasks();
        this.inProgress = snapshot.getInProgressTasks();
        this.done = snapshot.getDoneTasks();
    }

    @Override
    public boolean hasNext() {
        if (currentList == 0 && currentIndex < todo.size()) return true;
        if (currentList == 0) { currentList++; currentIndex = 0; } // Jump to List 1

        if (currentList == 1 && currentIndex < inProgress.size()) return true;
        if (currentList == 1) { currentList++; currentIndex = 0; } // Jump to List 2

        return currentList == 2 && currentIndex < done.size();
    }

    @Override
    public String next() {
        if (!hasNext()) {
            throw new NoSuchElementException("No more tasks in the workspace!");
        }

        String task = "";
        if (currentList == 0) {
            task = "[TO DO] " + todo.get(currentIndex);
        } else if (currentList == 1) {
            task = "[IN PROGRESS] " + inProgress.get(currentIndex);
        } else if (currentList == 2) {
            task = "[DONE] " + done.get(currentIndex);
        }

        currentIndex++;
        return task;
    }
}