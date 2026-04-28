module com.loom.app {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.desktop;

    // Grants JavaFX access to your UI controllers and FXML files
    opens com.loom.app to javafx.fxml;

    // Exposes the launcher to the JVM
    exports com.loom.app;
}