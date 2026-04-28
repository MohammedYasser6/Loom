package com.loom.app;

// This is the "Observer" in the design pattern
public interface NetworkObserver {
    // This method gets triggered whenever new data arrives over the network
    void onMessageReceived(String message);
}