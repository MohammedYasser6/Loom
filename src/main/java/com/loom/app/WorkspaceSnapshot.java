package com.loom.app;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

// Notice the "implements Iterable" addition here
public class WorkspaceSnapshot implements Iterable<String> {

    private List<String> todoTasks = new ArrayList<>();
    private List<String> inProgressTasks = new ArrayList<>();
    private List<String> doneTasks = new ArrayList<>();

    public void addTodo(String task) { todoTasks.add(task); }
    public void addInProgress(String task) { inProgressTasks.add(task); }
    public void addDone(String task) { doneTasks.add(task); }
    public void removeTodo(String task) { todoTasks.remove(task); }
    public void removeInProgress(String task) { inProgressTasks.remove(task); }
    public void removeDone(String task) { doneTasks.remove(task); }

    public List<String> getTodoTasks() { return todoTasks; }
    public List<String> getInProgressTasks() { return inProgressTasks; }
    public List<String> getDoneTasks() { return doneTasks; }

    // ITERATOR PATTERN: This allows the snapshot to be used in a standard Java foreach loop!
    @Override
    public Iterator<String> iterator() {
        return new WorkspaceIterator(this);
    }
}