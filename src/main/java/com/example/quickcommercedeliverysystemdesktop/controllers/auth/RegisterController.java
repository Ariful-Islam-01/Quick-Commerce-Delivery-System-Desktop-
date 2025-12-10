package com.example.quickcommercedeliverysystemdesktop.controllers.auth;

import com.example.quickcommercedeliverysystemdesktop.database.UserDAO;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

public class RegisterController {

    @FXML private TextField nameField;
    @FXML private TextField emailField;
    @FXML private TextField phoneField;
    @FXML private PasswordField passwordField;
    @FXML private PasswordField confirmPasswordField;
    @FXML private Label messageLabel;

    @FXML
    public void initialize() {
        messageLabel.setText("");
    }

    @FXML
    public void handleRegister() {
        String name = nameField.getText().trim();
        String email = emailField.getText().trim();
        String phone = phoneField.getText().trim();
        String pass = passwordField.getText();
        String confirm = confirmPasswordField.getText();

        if (name.isEmpty() || email.isEmpty() || pass.isEmpty() || confirm.isEmpty()) {
            messageLabel.setText("Please fill required fields.");
            return;
        }

        if (!pass.equals(confirm)) {
            messageLabel.setText("Passwords do not match.");
            return;
        }

        boolean ok = UserDAO.register(name, email, phone, pass);
        if (ok) {
            messageLabel.setStyle("-fx-text-fill: #2b7a78;");
            messageLabel.setText("Account created. Redirecting to login...");
            // go to login after small wait
            try { Thread.sleep(700); } catch (InterruptedException ignored) {}
            loadScene("/com/example/quickcommercedeliverysystemdesktop/views/auth/Login.fxml");
        } else {
            messageLabel.setStyle("-fx-text-fill: #b00020;");
            messageLabel.setText("Registration failed. Email may already exist.");
        }
    }

    @FXML
    public void goToLogin() {
        loadScene("/com/example/quickcommercedeliverysystemdesktop/views/auth/Login.fxml");
    }

    private void loadScene(String fxmlPath) {
        try {
            Stage stage = (Stage) nameField.getScene().getWindow();
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
