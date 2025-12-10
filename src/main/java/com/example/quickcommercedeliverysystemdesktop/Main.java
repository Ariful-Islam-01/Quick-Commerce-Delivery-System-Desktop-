package com.example.quickcommercedeliverysystemdesktop;

import com.example.quickcommercedeliverysystemdesktop.database.Database;
import com.example.quickcommercedeliverysystemdesktop.database.DatabaseInitializer;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.sql.Connection;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) {
        try {
            // Initialize DB (creates tables if missing)
            DatabaseInitializer.initialize();

            // Test DB connection
            try (Connection c = Database.getConnection()) {
                if (c != null) System.out.println("DB connected.");
            } catch (Exception ex) {
                System.err.println("DB connection check failed: " + ex.getMessage());
            }

            Parent root = FXMLLoader.load(getClass().getResource("/com/example/quickcommercedeliverysystemdesktop/views/auth/Login.fxml"));

            Scene scene = new Scene(root);
            scene.getStylesheets().add(getClass().getResource("/com/example/quickcommercedeliverysystemdesktop/styles/style.css").toExternalForm());

            primaryStage.setTitle("Quick Commerce Delivery System");
            primaryStage.setScene(scene);
            primaryStage.setResizable(false);
            primaryStage.show();

        } catch (Exception ex) {
            ex.printStackTrace();
            System.err.println("Failed to start app: " + ex.getMessage());
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
