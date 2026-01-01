package com.example.quickcommercedeliverysystemdesktop.controllers.dashboard;

import com.example.quickcommercedeliverysystemdesktop.database.OrderDAO;
import com.example.quickcommercedeliverysystemdesktop.models.Order;
import com.example.quickcommercedeliverysystemdesktop.utils.ErrorHandler;
import com.example.quickcommercedeliverysystemdesktop.utils.UserSession;
import com.example.quickcommercedeliverysystemdesktop.utils.ValidationUtil;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.FileChooser;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

public class CreateOrderController {

    @FXML private TextField productNameField;
    @FXML private TextArea descriptionArea;
    @FXML private TextField deliveryLocationField;
    @FXML private TextField timeFromField;
    @FXML private TextField timeToField;
    @FXML private TextField deliveryFeeField;
    @FXML private TextArea notesArea;
    @FXML private Label imageLabel;
    @FXML private Label messageLabel;

    private String selectedImagePath = null;

    @FXML
    public void initialize() {
        ValidationUtil.clearMessage(messageLabel);
    }

    @FXML
    private void handleChooseImage() {
        try {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Select Product Image");
            fileChooser.getExtensionFilters().addAll(
                    new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg")
            );

            File selectedFile = fileChooser.showOpenDialog(imageLabel.getScene().getWindow());
            if (selectedFile != null) {
                // Validate file size (max 5MB)
                if (selectedFile.length() > 5 * 1024 * 1024) {
                    ValidationUtil.showError(messageLabel, "Image file too large. Maximum size is 5MB.");
                    return;
                }

                // Create assets/products directory if not exists
                Path productsDir = Paths.get("src/main/resources/com/example/quickcommercedeliverysystemdesktop/assets/products");
                Files.createDirectories(productsDir);

                // Copy file with unique name
                String fileName = System.currentTimeMillis() + "_" + selectedFile.getName();
                Path destPath = productsDir.resolve(fileName);
                Files.copy(selectedFile.toPath(), destPath, StandardCopyOption.REPLACE_EXISTING);

                selectedImagePath = fileName;
                imageLabel.setText("✓ " + selectedFile.getName());
                imageLabel.setStyle("-fx-text-fill: #27ae60; -fx-font-weight: bold;");
                ErrorHandler.logInfo("Image uploaded: " + fileName);
            }
        } catch (Exception e) {
            ErrorHandler.handleFileException(e, "uploading image");
            ValidationUtil.showError(messageLabel, "Failed to upload image. Please try again.");
        }
    }

    @FXML
    private void handleCreateOrder() {
        // Clear previous messages and styles
        ValidationUtil.clearMessage(messageLabel);
        ValidationUtil.clearFieldStyle(productNameField, deliveryLocationField,
                                       timeFromField, timeToField, deliveryFeeField);

        // Get input values
        String productName = productNameField.getText().trim();
        String description = descriptionArea.getText().trim();
        String location = deliveryLocationField.getText().trim();
        String timeFrom = timeFromField.getText().trim();
        String timeTo = timeToField.getText().trim();
        String feeStr = deliveryFeeField.getText().trim();
        String notes = notesArea.getText().trim();

        // Validate required fields
        if (!ValidationUtil.validateField(productNameField, "Product Name", messageLabel)) {
            return;
        }

        if (!ValidationUtil.isNotEmpty(description)) {
            descriptionArea.setStyle("-fx-border-color: #e74c3c; -fx-border-width: 2px;");
            ValidationUtil.showError(messageLabel, "Description is required");
            return;
        }

        if (!ValidationUtil.validateField(deliveryLocationField, "Delivery Location", messageLabel)) {
            return;
        }

        if (!ValidationUtil.validateField(timeFromField, "Time From", messageLabel)) {
            return;
        }

        if (!ValidationUtil.validateField(timeToField, "Time To", messageLabel)) {
            return;
        }

        // Validate delivery fee
        if (!ValidationUtil.isPositiveNumber(feeStr)) {
            deliveryFeeField.setStyle("-fx-border-color: #e74c3c; -fx-border-width: 2px;");
            ValidationUtil.showError(messageLabel, "Please enter a valid positive delivery fee");
            return;
        }

        double fee = Double.parseDouble(feeStr);

        // Validate fee range (reasonable range)
        if (!ValidationUtil.isInRange(fee, 0.01, 1000.00)) {
            deliveryFeeField.setStyle("-fx-border-color: #e74c3c; -fx-border-width: 2px;");
            ValidationUtil.showError(messageLabel, "Delivery fee must be between $0.01 and $1000.00");
            return;
        }

        try {
            // Get current user info
            int userId = UserSession.getInstance().getUserId();
            String userName = UserSession.getInstance().getUserName();
            String userPhone = UserSession.getInstance().getUserPhone();

            // Create time range
            String timeRange = timeFrom + " - " + timeTo;

            // Create Order object
            Order order = new Order(userId, productName, description, location, timeRange,
                                   fee, notes, userName, userPhone);
            order.setProductPhoto(selectedImagePath);

            // Save to database (notification will be created automatically in OrderDAO)
            boolean success = OrderDAO.createOrder(order);

            if (success) {
                ValidationUtil.showSuccess(messageLabel, "✓ Order created successfully! Waiting for a delivery person to accept.");
                ErrorHandler.logInfo("Order created by user " + userId + ": " + productName);
                handleClearForm();
            } else {
                ValidationUtil.showError(messageLabel, "Failed to create order. Please try again.");
                ErrorHandler.logWarning("Order creation failed for user " + userId);
            }
        } catch (Exception e) {
            ErrorHandler.handleDatabaseException(e, "creating order");
            ValidationUtil.showError(messageLabel, "Failed to create order. Please try again.");
        }
    }

    @FXML
    private void handleClearForm() {
        productNameField.clear();
        descriptionArea.clear();
        deliveryLocationField.clear();
        timeFromField.clear();
        timeToField.clear();
        deliveryFeeField.clear();
        notesArea.clear();
        selectedImagePath = null;
        imageLabel.setText("No image selected");
        imageLabel.setStyle("");
        ValidationUtil.clearMessage(messageLabel);
        ValidationUtil.clearFieldStyle(productNameField, deliveryLocationField,
                                       timeFromField, timeToField, deliveryFeeField);
        descriptionArea.setStyle("");
    }

    private void showError(String message) {
        ValidationUtil.showError(messageLabel, message);
    }

    private void showSuccess(String message) {
        ValidationUtil.showSuccess(messageLabel, message);
    }
}

