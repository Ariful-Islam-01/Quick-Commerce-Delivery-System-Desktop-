package com.example.quickcommercedeliverysystemdesktop.utils;

import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.TextInputControl;

import java.util.Optional;

/**
 * Validation Utility Class - Day 14
 * Provides validation methods and error handling for the entire application
 */
public class ValidationUtil {

    // Email validation
    public static boolean isValidEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            return false;
        }
        return email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
    }

    // Phone validation
    public static boolean isValidPhone(String phone) {
        if (phone == null || phone.trim().isEmpty()) {
            return true; // Phone is optional in some cases
        }
        return phone.matches("^[0-9+\\-\\s()]{10,15}$");
    }

    // Password strength validation
    public static boolean isValidPassword(String password) {
        if (password == null || password.isEmpty()) {
            return false;
        }
        return password.length() >= 6;
    }

    // Check if string is not empty
    public static boolean isNotEmpty(String text) {
        return text != null && !text.trim().isEmpty();
    }

    // Validate number (double)
    public static boolean isValidNumber(String text) {
        if (text == null || text.trim().isEmpty()) {
            return false;
        }
        try {
            Double.parseDouble(text);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    // Validate positive number
    public static boolean isPositiveNumber(String text) {
        if (!isValidNumber(text)) {
            return false;
        }
        try {
            return Double.parseDouble(text) > 0;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    // Show error message on label
    public static void showError(Label label, String message) {
        if (label != null) {
            label.setText(message);
            label.setStyle("-fx-text-fill: #e74c3c; -fx-font-weight: bold;");
            label.setVisible(true);
        }
    }

    // Show success message on label
    public static void showSuccess(Label label, String message) {
        if (label != null) {
            label.setText(message);
            label.setStyle("-fx-text-fill: #27ae60; -fx-font-weight: bold;");
            label.setVisible(true);
        }
    }

    // Show warning message on label
    public static void showWarning(Label label, String message) {
        if (label != null) {
            label.setText(message);
            label.setStyle("-fx-text-fill: #f39c12; -fx-font-weight: bold;");
            label.setVisible(true);
        }
    }

    // Clear message from label
    public static void clearMessage(Label label) {
        if (label != null) {
            label.setText("");
            label.setVisible(false);
        }
    }

    // Show alert dialog
    public static void showAlert(String title, String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    // Show confirmation dialog
    public static boolean showConfirmation(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);

        Optional<ButtonType> result = alert.showAndWait();
        return result.isPresent() && result.get() == ButtonType.OK;
    }

    // Validate and highlight field
    public static boolean validateField(TextInputControl field, String fieldName, Label messageLabel) {
        if (field.getText() == null || field.getText().trim().isEmpty()) {
            field.setStyle("-fx-border-color: #e74c3c; -fx-border-width: 2px;");
            showError(messageLabel, fieldName + " is required");
            return false;
        }
        field.setStyle(""); // Clear error style
        return true;
    }

    // Clear all field styles
    public static void clearFieldStyle(TextInputControl... fields) {
        for (TextInputControl field : fields) {
            if (field != null) {
                field.setStyle("");
            }
        }
    }

    // Validate email field
    public static boolean validateEmailField(TextInputControl field, Label messageLabel) {
        String email = field.getText().trim();
        if (email.isEmpty()) {
            field.setStyle("-fx-border-color: #e74c3c; -fx-border-width: 2px;");
            showError(messageLabel, "Email is required");
            return false;
        }
        if (!isValidEmail(email)) {
            field.setStyle("-fx-border-color: #e74c3c; -fx-border-width: 2px;");
            showError(messageLabel, "Please enter a valid email address");
            return false;
        }
        field.setStyle("");
        return true;
    }

    // Validate password match
    public static boolean validatePasswordMatch(TextInputControl passwordField,
                                               TextInputControl confirmField,
                                               Label messageLabel) {
        String password = passwordField.getText();
        String confirm = confirmField.getText();

        if (!password.equals(confirm)) {
            confirmField.setStyle("-fx-border-color: #e74c3c; -fx-border-width: 2px;");
            showError(messageLabel, "Passwords do not match");
            return false;
        }

        if (!isValidPassword(password)) {
            passwordField.setStyle("-fx-border-color: #e74c3c; -fx-border-width: 2px;");
            showError(messageLabel, "Password must be at least 6 characters");
            return false;
        }

        passwordField.setStyle("");
        confirmField.setStyle("");
        return true;
    }

    // Sanitize input to prevent XSS
    public static String sanitizeInput(String input) {
        if (input == null) {
            return "";
        }
        return input.trim()
                   .replaceAll("<", "&lt;")
                   .replaceAll(">", "&gt;")
                   .replaceAll("\"", "&quot;")
                   .replaceAll("'", "&#x27;");
    }

    // Check if value is within range
    public static boolean isInRange(double value, double min, double max) {
        return value >= min && value <= max;
    }

    // Format error message
    public static String formatErrorMessage(String fieldName, String errorType) {
        return switch (errorType) {
            case "required" -> fieldName + " is required";
            case "invalid" -> "Please enter a valid " + fieldName.toLowerCase();
            case "too_short" -> fieldName + " is too short";
            case "too_long" -> fieldName + " is too long";
            case "mismatch" -> fieldName + " do not match";
            default -> "Invalid " + fieldName.toLowerCase();
        };
    }
}

