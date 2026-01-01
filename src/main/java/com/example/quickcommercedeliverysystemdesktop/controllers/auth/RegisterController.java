package com.example.quickcommercedeliverysystemdesktop.controllers.auth;

import com.example.quickcommercedeliverysystemdesktop.database.UserDAO;
import com.example.quickcommercedeliverysystemdesktop.utils.ErrorHandler;
import com.example.quickcommercedeliverysystemdesktop.utils.ValidationUtil;
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
        ValidationUtil.clearMessage(messageLabel);
    }

    @FXML
    public void handleRegister() {
        // Clear previous messages and styles
        ValidationUtil.clearMessage(messageLabel);
        ValidationUtil.clearFieldStyle(nameField, emailField, phoneField, passwordField, confirmPasswordField);

        String name = nameField.getText().trim();
        String email = emailField.getText().trim();
        String phone = phoneField.getText().trim();
        String pass = passwordField.getText();
        String confirm = confirmPasswordField.getText();

        // Validate name
        if (!ValidationUtil.validateField(nameField, "Name", messageLabel)) {
            return;
        }

        // Validate email
        if (!ValidationUtil.validateEmailField(emailField, messageLabel)) {
            return;
        }

        // Validate phone (optional but check format if provided)
        if (!phone.isEmpty() && !ValidationUtil.isValidPhone(phone)) {
            phoneField.setStyle("-fx-border-color: #e74c3c; -fx-border-width: 2px;");
            ValidationUtil.showError(messageLabel, "Please enter a valid phone number (10-15 digits)");
            return;
        }

        // Validate password match
        if (!ValidationUtil.validatePasswordMatch(passwordField, confirmPasswordField, messageLabel)) {
            return;
        }

        try {
            boolean ok = UserDAO.register(name, email, phone, pass);

            if (ok) {
                ValidationUtil.showSuccess(messageLabel, "Account created. Redirecting to login...");
                ErrorHandler.logInfo("New user registered: " + name + " (" + email + ")");

                // Redirect to login after delay
                new Thread(() -> {
                    try {
                        Thread.sleep(1500);
                        javafx.application.Platform.runLater(() ->
                            loadScene("/com/example/quickcommercedeliverysystemdesktop/views/auth/Login.fxml")
                        );
                    } catch (InterruptedException e) {
                        ErrorHandler.logError(e);
                    }
                }).start();
            } else {
                emailField.setStyle("-fx-border-color: #e74c3c; -fx-border-width: 2px;");
                ValidationUtil.showError(messageLabel, "Email already exists. Please use a different email.");
                ErrorHandler.logWarning("Registration failed - duplicate email: " + email);
            }
        } catch (Exception e) {
            ErrorHandler.handleDatabaseException(e, "registering account");
            ValidationUtil.showError(messageLabel, "Registration failed. Please try again.");
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
            ErrorHandler.logInfo("Loaded scene: " + fxmlPath);
        } catch (Exception ex) {
            ErrorHandler.handleException(ex, "Failed to load screen. Please try again.");
        }
    }
}
