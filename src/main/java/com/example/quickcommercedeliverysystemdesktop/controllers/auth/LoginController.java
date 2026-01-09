package com.example.quickcommercedeliverysystemdesktop.controllers.auth;

import com.example.quickcommercedeliverysystemdesktop.database.UserDAO;
import com.example.quickcommercedeliverysystemdesktop.models.User;
import com.example.quickcommercedeliverysystemdesktop.utils.ErrorHandler;
import com.example.quickcommercedeliverysystemdesktop.utils.UserSession;
import com.example.quickcommercedeliverysystemdesktop.utils.ValidationUtil;
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
        ValidationUtil.clearMessage(messageLabel);
    }

    @FXML
    public void handleLogin() {
        // Clear previous messages and styles
        ValidationUtil.clearMessage(messageLabel);
        ValidationUtil.clearFieldStyle(emailField, passwordField);

        String email = emailField.getText().trim();
        String password = passwordField.getText();

        // Validate email field
        if (!ValidationUtil.validateEmailField(emailField, messageLabel)) {
            return;
        }

        // Validate password field
        if (!ValidationUtil.validateField(passwordField, "Password", messageLabel)) {
            return;
        }

        try {
            User user = UserDAO.login(email, password);

            if (user == null) {
                emailField.setStyle("-fx-border-color: #e74c3c; -fx-border-width: 2px;");
                passwordField.setStyle("-fx-border-color: #e74c3c; -fx-border-width: 2px;");
                ValidationUtil.showError(messageLabel, "Invalid email or password.");
                ErrorHandler.logWarning("Failed login attempt for email: " + email);
            } else if (user.isBanned()) {
                ValidationUtil.showError(messageLabel, "Your account has been banned. Contact support.");
                ErrorHandler.logWarning("Banned user login attempt: " + email);
            } else {
                // Save user to session
                UserSession.getInstance().setCurrentUser(user);

                ValidationUtil.showSuccess(messageLabel, "Login successful. Loading...");
                ErrorHandler.logInfo("User logged in: " + user.getName() + " (" + user.getEmail() + ")");

                // Small delay for user to see success message
                new Thread(() -> {
                    try {
                        Thread.sleep(500);
                        javafx.application.Platform.runLater(() -> {
                            // Route to appropriate dashboard based on user role
                            if (user.isAdmin()) {
                                loadScene("/com/example/quickcommercedeliverysystemdesktop/views/dashboard/AdminMainDashboard.fxml");
                                ErrorHandler.logInfo("Admin user redirected to Admin Dashboard");
                            } else {
                                loadScene("/com/example/quickcommercedeliverysystemdesktop/views/dashboard/MainDashboard.fxml");
                                ErrorHandler.logInfo("Regular user redirected to User Dashboard");
                            }
                        });
                    } catch (InterruptedException e) {
                        ErrorHandler.logError(e);
                    }
                }).start();
            }
        } catch (Exception e) {
            ErrorHandler.handleDatabaseException(e, "logging in");
            ValidationUtil.showError(messageLabel, "Login failed. Please try again.");
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
            Scene scene = new Scene(root, 1200, 750);
            scene.getStylesheets().add(getClass().getResource("/com/example/quickcommercedeliverysystemdesktop/styles/style.css").toExternalForm());
            stage.setScene(scene);

            // Maintain window properties
            stage.setResizable(true);
            stage.setMinWidth(1000);
            stage.setMinHeight(650);

            ErrorHandler.logInfo("Loaded scene: " + fxmlPath);
        } catch (Exception ex) {
            ErrorHandler.handleException(ex, "Failed to load screen. Please try again.");
        }
    }
}
