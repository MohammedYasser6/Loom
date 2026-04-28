# 🚀 Loom - Agile Workspace

![Java](https://img.shields.io/badge/Java-21-orange.svg)
![JavaFX](https://img.shields.io/badge/JavaFX-UI-blue.svg)
![Architecture](https://img.shields.io/badge/Architecture-MVC-brightgreen.svg)

Loom is a real-time, networked Kanban board and team collaboration workspace built from the ground up in **JavaFX**. It was engineered with a strict adherence to SOLID principles and implements 8 unique Gang of Four (GoF) Design Patterns to ensure a highly scalable, modular, and maintainable codebase.

## ✨ Core Features
* **Real-Time Kanban Board:** Create, delete, and move tasks between *To Do*, *In Progress*, and *Done* columns.
* **Drag and Drop Engine:** Fluid UI manipulation using JavaFX Clipboard transfers.
* **Socket Networking (Host/Client):** Custom TCP/IP protocol syncing workspace state between a Host and multiple connected Clients in real-time.
* **Dynamic Chat Pipeline:** A Discord-style chat engine that parses formatting dynamically (Clickable Links, **Bold Text**, `Code Snippets`, and Timestamps) without character-by-character loops.
* **File Sharing:** Convert, compress, and transmit files and images over the network using Base64 encoding.
* **Workspace Memory:** Save and load workspace states (Tasks and Assignees) to local JSON files.

---

## 🏗️ Software Architecture & Design Patterns
This project serves as a practical demonstration of Enterprise software architecture.

### Structural Patterns
* **Decorator:** Powers the Chat Engine. `SimpleChatBubble` acts as the base, dynamically wrapped at runtime by `BoldDecorator`, `LinkDecorator`, `CodeSnippetDecorator`, and `TimestampDecorator` to apply rich text formatting without violating the Open/Closed Principle.

### Creational Patterns
* **Builder:** Used to construct `Task` data objects. Enforces immutability (private final fields) and resolves the Telescoping Constructor anti-pattern for optional parameters (like assignees).
* **Abstract Factory:** The `WorkspaceComponentFactory` translates backend data into frontend visuals. `TaskCardFactory` dynamically generates the JavaFX VBoxes, Labels, and Action Events, completely decoupling UI generation from data logic.
* **Singleton:** `NetworkManager` manages the active port binding (Port 8080) and ServerSocket, ensuring only one instance of the application controls the hardware layer at a time.

### Behavioral Patterns
* **Strategy:** Drives the Kanban sorting engine. `AlphabeticalSortStrategy` and `AssigneeSortStrategy` implement the `ColumnSortStrategy` interface, allowing the application to swap complex sorting algorithms at runtime using polymorphism.
* **Observer:** Decouples the network from the UI. The `NetworkManager` receives data and broadcasts to the `NetworkObserver` interface, allowing the `MainDashboardController` to update seamlessly.
* **Memento:** The `WorkspaceCaretaker` safely extracts the internal state of the board into a `WorkspaceSnapshot` (Memento) for saving to the hard drive, protecting encapsulation.
* **Iterator:** Flawlessly traverses the three separate memory arrays (TODO, IN_PROGRESS, DONE) as a single continuous stream to generate the Workspace Audit Report.

---

## 💻 Getting Started

### Prerequisites
* Java JDK 17 or higher (Recommended: Java 21)
* Maven or Gradle (for JavaFX dependencies)

### Running the App
1. Clone the repository: `git clone https://github.com/YOUR_USERNAME/Loom-Workspace.git`
2. Build the project using your IDE (IntelliJ IDEA recommended).
3. Run the `Launcher.java` class to bypass JavaFX module path restrictions.
4. **To Host:** Select "Host Workspace" and create a new save file.
5. **To Join:** Open a second instance, enter a username, select "Join", and input `localhost` (or the Host's IPv4 address if on a LAN).

---
*Developed as an advanced study in Object-Oriented Design and Systems Architecture.*
