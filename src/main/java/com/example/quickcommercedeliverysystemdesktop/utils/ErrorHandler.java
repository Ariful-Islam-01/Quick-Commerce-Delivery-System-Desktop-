package com.example.quickcommercedeliverysystemdesktop.utils;

import javafx.scene.control.Alert;
import javafx.scene.control.TextArea;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Error Handler Utility - Day 14
 * Centralized error handling and logging
 */
public class ErrorHandler {

    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /**
     * Handle general exception with user-friendly message
     */
    public static void handleException(Exception e, String userMessage) {
        logError(e);
        showErrorDialog("Error", userMessage, e);
    }

    /**
     * Handle database exception
     */
    public static void handleDatabaseException(Exception e, String operation) {
        logError(e);
        String message = "Database error during " + operation + ".\n" +
                        "Please try again or contact support if the problem persists.";
        showErrorDialog("Database Error", message, e);
    }

    /**
     * Handle file operation exception
     */
    public static void handleFileException(Exception e, String operation) {
        logError(e);
        String message = "File operation failed: " + operation + ".\n" +
                        "Please check file permissions and try again.";
        showErrorDialog("File Error", message, e);
    }

    /**
     * Handle network/connection exception
     */
    public static void handleNetworkException(Exception e) {
        logError(e);
        String message = "Network connection error.\n" +
                        "Please check your internet connection and try again.";
        showSimpleError("Connection Error", message);
    }

    /**
     * Show error dialog with expandable exception details
     */
    public static void showErrorDialog(String title, String message, Exception e) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);

        // Create expandable Exception area
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        e.printStackTrace(pw);
        String exceptionText = sw.toString();

        TextArea textArea = new TextArea(exceptionText);
        textArea.setEditable(false);
        textArea.setWrapText(true);
        textArea.setMaxWidth(Double.MAX_VALUE);
        textArea.setMaxHeight(Double.MAX_VALUE);
        GridPane.setVgrow(textArea, Priority.ALWAYS);
        GridPane.setHgrow(textArea, Priority.ALWAYS);

        GridPane expContent = new GridPane();
        expContent.setMaxWidth(Double.MAX_VALUE);
        expContent.add(textArea, 0, 0);

        alert.getDialogPane().setExpandableContent(expContent);
        alert.showAndWait();
    }

    /**
     * Show simple error dialog without exception details
     */
    public static void showSimpleError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    /**
     * Show warning dialog
     */
    public static void showWarning(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    /**
     * Log error to console (in production, would log to file)
     */
    public static void logError(Exception e) {
        String timestamp = LocalDateTime.now().format(formatter);
        System.err.println("=== ERROR LOG [" + timestamp + "] ===");
        System.err.println("Exception: " + e.getClass().getSimpleName());
        System.err.println("Message: " + e.getMessage());
        e.printStackTrace();
        System.err.println("=== END ERROR LOG ===\n");
    }

    /**
     * Log info message
     */
    public static void logInfo(String message) {
        String timestamp = LocalDateTime.now().format(formatter);
        System.out.println("[INFO " + timestamp + "] " + message);
    }

    /**
     * Log warning message
     */
    public static void logWarning(String message) {
        String timestamp = LocalDateTime.now().format(formatter);
        System.out.println("[WARNING " + timestamp + "] " + message);
    }

    /**
     * Handle 404 - Not Found errors
     */
    public static void handle404(String resourceName) {
        String message = "The requested resource '" + resourceName + "' was not found.\n" +
                        "Please make sure the resource exists and try again.";
        showSimpleError("Not Found", message);
        logWarning("404 - Resource not found: " + resourceName);
    }

    /**
     * Handle empty state
     */
    public static String getEmptyStateMessage(String entityType) {
        return switch (entityType.toLowerCase()) {
            case "orders" -> "No orders found. Create your first order to get started!";
            case "deliveries" -> "No deliveries yet. Accept orders to start delivering.";
            case "earnings" -> "No earnings recorded yet. Complete deliveries to earn!";
            case "notifications" -> "No notifications. You're all caught up!";
            case "users" -> "No users found. Try adjusting your filters.";
            default -> "No " + entityType + " found.";
        };
    }

    /**
     * Validate and handle null objects
     */
    public static <T> T requireNonNull(T obj, String objectName) {
        if (obj == null) {
            String message = objectName + " cannot be null";
            logError(new NullPointerException(message));
            throw new IllegalArgumentException(message);
        }
        return obj;
    }
}

