package com.loom.app;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class NetworkManager {

    private static NetworkManager instance;
    private final int PORT = 8080;
    private boolean isHost = false;

    // Networking variables
    private ServerSocket serverSocket;
    private PrintWriter out;

    // Global State Variables (Passed between controllers)
    private String username = "Unknown";
    private String role = "USER"; // NEW: Defaults to standard user
    private WorkspaceSnapshot activeSnapshot;
    private File activeSaveFile;

    // OBSERVER PATTERN: A list to hold anyone watching the network
    private List<NetworkObserver> observers = new ArrayList<>();

    // Private constructor for Singleton
    private NetworkManager() { }

    public static synchronized NetworkManager getInstance() {
        if (instance == null) {
            instance = new NetworkManager();
        }
        return instance;
    }

    // --- GLOBAL STATE GETTERS & SETTERS ---

    public void setUsername(String username) { this.username = username; }
    public String getUsername() { return username; }

    // NEW: Role Getters and Setters
    public void setRole(String role) { this.role = role; }
    public String getRole() { return role; }

    public void setActiveSnapshot(WorkspaceSnapshot snapshot) { this.activeSnapshot = snapshot; }
    public WorkspaceSnapshot getActiveSnapshot() { return activeSnapshot; }

    public void setActiveSaveFile(File file) { this.activeSaveFile = file; }
    public File getActiveSaveFile() { return activeSaveFile; }

    public boolean isHosting() { return isHost; }


    // --- OBSERVER PATTERN LOGIC ---

    public void addObserver(NetworkObserver observer) {
        if (!observers.contains(observer)) {
            observers.add(observer);
        }
    }

    private void notifyObservers(String message) {
        for (NetworkObserver observer : observers) {
            observer.onMessageReceived(message);
        }
    }


    // --- SERVER (HOST) LOGIC ---

    public void hostWorkspace() {
        if (serverSocket == null || serverSocket.isClosed()) {
            try {
                serverSocket = new ServerSocket(PORT);
                isHost = true;
                this.role = "MANAGER"; // NEW: The Host automatically gets ultimate power!
                System.out.println("Success: Loom Workspace hosted on port " + PORT + " as MANAGER");

                // Start a NEW thread to listen for clients so the UI doesn't freeze
                new Thread(this::listenForClients).start();

            } catch (IOException e) {
                System.err.println("Critical Error: Port " + PORT + " is already in use.");
            }
        }
    }

    private void listenForClients() {
        try {
            while (isHost) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("A teammate connected!");

                // Capture the output stream so we can talk back to the Client
                out = new PrintWriter(clientSocket.getOutputStream(), true);

                BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                String incomingMessage;

                while ((incomingMessage = in.readLine()) != null) {
                    notifyObservers(incomingMessage);
                }
            }
        } catch (IOException e) {
            System.err.println("Server listener stopped.");
        }
    }


    // --- CLIENT (JOIN) LOGIC ---

    public boolean joinWorkspace(String ipAddress) {
        try {
            Socket clientSocket = new Socket(ipAddress, PORT);
            isHost = false;
            // Note: The role remains "USER" by default for clients joining.
            System.out.println("Success: Connected to Loom Workspace at " + ipAddress + " as USER");

            // Capture the output stream so we can send messages to the Host
            out = new PrintWriter(clientSocket.getOutputStream(), true);

            // Start a background thread to listen for incoming messages from the Host
            new Thread(() -> listenAsClient(clientSocket)).start();

            return true; // Connection successful!

        } catch (IOException e) {
            System.err.println("Critical Error: Could not connect to " + ipAddress);
            return false; // Connection failed!
        }
    }

    private void listenAsClient(Socket socket) {
        try {
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            String incomingMessage;

            while ((incomingMessage = in.readLine()) != null) {
                notifyObservers(incomingMessage);
            }
        } catch (IOException e) {
            System.err.println("Disconnected from Host.");
        }
    }
    public void stopNetwork() {
        isHost = false;
        try {
            if (serverSocket != null && !serverSocket.isClosed()) {
                serverSocket.close(); // Force the socket to release the port
            }
            if (out != null) {
                out.close();
            }
        } catch (IOException e) {
            System.err.println("Error closing server socket.");
        }
    }

    // --- MESSAGE SENDING LOGIC ---

    public void sendMessage(String message) {
        if (out != null) {
            out.println(message);
        } else {
            System.out.println("No one is connected yet! Message not sent over network.");
        }
    }
}