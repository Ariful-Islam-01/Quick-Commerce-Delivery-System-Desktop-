package com.example.quickcommercedeliverysystemdesktop;

import com.example.quickcommercedeliverysystemdesktop.database.Database;
import com.example.quickcommercedeliverysystemdesktop.database.DatabaseInitializer;
import com.example.quickcommercedeliverysystemdesktop.utils.ErrorHandler;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.stage.Stage;

import java.sql.Connection;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) {
        try {
            // Initialize DB (creates tables if missing)
            System.out.println("Initializing database...");
            DatabaseInitializer.initialize();

            // Test DB connection
            try (Connection c = Database.getConnection()) {
                if (c != null) {
                    System.out.println("✓ Database connected successfully.");
                } else {
                    throw new RuntimeException("Database connection returned null");
                }
            } catch (Exception ex) {
                ErrorHandler.logError(ex);
                showStartupError("Database connection failed. Please ensure the database is accessible.");
                System.exit(1);
                return;
            }

            // Load main login screen
            System.out.println("Loading application UI...");
            Parent root = FXMLLoader.load(
                getClass().getResource("/com/example/quickcommercedeliverysystemdesktop/views/auth/Login.fxml")
            );

            Scene scene = new Scene(root);

            // Load CSS
            String cssPath = getClass().getResource(
                "/com/example/quickcommercedeliverysystemdesktop/styles/style.css"
            ).toExternalForm();
            scene.getStylesheets().add(cssPath);

            primaryStage.setTitle("Quick Commerce Delivery System");
            primaryStage.setScene(scene);

            // Enable window resizing with minimize and maximize
            primaryStage.setResizable(true);

            // Set minimum window dimensions to prevent too small sizing
            primaryStage.setMinWidth(800);
            primaryStage.setMinHeight(600);

            // Set preferred/default size
            primaryStage.setWidth(1000);
            primaryStage.setHeight(650);

            // Set application icon (if exists)
            try {
                // You can add an icon here if you have one
                // primaryStage.getIcons().add(new Image("/path/to/icon.png"));
            } catch (Exception e) {
                ErrorHandler.logWarning("Application icon not found");
            }

            primaryStage.show();

            System.out.println("✓ Application started successfully!");
            ErrorHandler.logInfo("Quick Commerce Delivery System started");

        } catch (Exception ex) {
            ErrorHandler.logError(ex);
            showStartupError(
                "Failed to start the application.\n\n" +
                "Error: " + ex.getMessage() + "\n\n" +
                "Please check the console for detailed error information."
            );
            System.exit(1);
        }
    }

    /**
     * Show startup error dialog
     */
    private void showStartupError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Startup Error");
        alert.setHeaderText("Failed to Start Application");
        alert.setContentText(message);
        alert.showAndWait();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
