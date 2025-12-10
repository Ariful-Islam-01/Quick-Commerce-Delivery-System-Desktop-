package com.example.quickcommercedeliverysystemdesktop.controllers.dashboard;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.scene.control.Button;

public class MainDashboardController {

    @FXML private Button logoutButton;

    @FXML
    public void handleLogout() {
        try {
            Stage stage = (Stage) logoutButton.getScene().getWindow();

            Parent root = FXMLLoader.load(
                    getClass().getResource("/com/example/quickcommercedeliverysystemdesktop/views/auth/Login.fxml")
            );

            Scene scene = new Scene(root);
            scene.getStylesheets().add(
                    getClass().getResource("/com/example/quickcommercedeliverysystemdesktop/styles/style.css")
                            .toExternalForm()
            );

            stage.setScene(scene);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
