package com.example.quickcommercedeliverysystemdesktop.controllers.auth;

import com.example.quickcommercedeliverysystemdesktop.database.UserDAO;
import com.example.quickcommercedeliverysystemdesktop.models.User;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

public class LoginController {

    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
    @FXML private Label messageLabel;

    @FXML
    public void initialize() {
        messageLabel.setText("");
    }

    @FXML
    public void handleLogin() {
        String email = emailField.getText().trim();
        String password = passwordField.getText();

        if (email.isEmpty() || password.isEmpty()) {
            messageLabel.setText("Please enter email and password.");
            return;
        }

        User user = UserDAO.login(email, password);
        if (user == null) {
            messageLabel.setText("Invalid email or password.");
        } else {
            messageLabel.setStyle("-fx-text-fill: #2b7a78;");
            messageLabel.setText("Login successful. Loading...");
            loadScene("/com/example/quickcommercedeliverysystemdesktop/views/dashboard/MainDashboard.fxml");
        }
    }

    @FXML
    public void goToRegister() {
        loadScene("/com/example/quickcommercedeliverysystemdesktop/views/auth/Register.fxml");
    }

    private void loadScene(String fxmlPath) {
        try {
            Stage stage = (Stage) emailField.getScene().getWindow();
            Parent root = FXMLLoader.load(getClass().getResource(fxmlPath));
            Scene scene = new Scene(root);
            scene.getStylesheets().add(getClass().getResource("/com/example/quickcommercedeliverysystemdesktop/styles/style.css").toExternalForm());
            stage.setScene(scene);
        } catch (Exception ex) {
            ex.printStackTrace();
            messageLabel.setText("Failed to load screen.");
        }
    }
}
