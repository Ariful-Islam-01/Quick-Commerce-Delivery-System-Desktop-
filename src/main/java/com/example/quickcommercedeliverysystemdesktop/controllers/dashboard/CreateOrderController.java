package com.example.quickcommercedeliverysystemdesktop.controllers.dashboard;

import com.example.quickcommercedeliverysystemdesktop.database.OrderDAO;
import com.example.quickcommercedeliverysystemdesktop.models.Order;
import com.example.quickcommercedeliverysystemdesktop.utils.UserSession;
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
        messageLabel.setText("");
    }

    @FXML
    private void handleChooseImage() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select Product Image");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg")
        );

        File selectedFile = fileChooser.showOpenDialog(imageLabel.getScene().getWindow());
        if (selectedFile != null) {
            try {
                // Create assets/products directory if not exists
                Path productsDir = Paths.get("src/main/resources/com/example/quickcommercedeliverysystemdesktop/assets/products");
                Files.createDirectories(productsDir);

                // Copy file with unique name
                String fileName = System.currentTimeMillis() + "_" + selectedFile.getName();
                Path destPath = productsDir.resolve(fileName);
                Files.copy(selectedFile.toPath(), destPath, StandardCopyOption.REPLACE_EXISTING);

                selectedImagePath = fileName;
                imageLabel.setText(selectedFile.getName());
                imageLabel.setStyle("-fx-text-fill: #27ae60;");
            } catch (Exception e) {
                showError("Failed to upload image: " + e.getMessage());
            }
        }
    }

    @FXML
    private void handleCreateOrder() {
        // Validate inputs
        String productName = productNameField.getText().trim();
        String description = descriptionArea.getText().trim();
        String location = deliveryLocationField.getText().trim();
        String timeFrom = timeFromField.getText().trim();
        String timeTo = timeToField.getText().trim();
        String feeStr = deliveryFeeField.getText().trim();
        String notes = notesArea.getText().trim();

        if (productName.isEmpty() || description.isEmpty() || location.isEmpty() ||
            timeFrom.isEmpty() || timeTo.isEmpty() || feeStr.isEmpty()) {
            showError("Please fill all required fields (*)");
            return;
        }

        double fee;
        try {
            fee = Double.parseDouble(feeStr);
            if (fee < 0) {
                showError("Delivery fee must be positive");
                return;
            }
        } catch (NumberFormatException e) {
            showError("Invalid delivery fee. Please enter a valid number.");
            return;
        }

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

            showSuccess("Order created successfully! Waiting for a delivery person to accept.");
            handleClearForm();
        } else {
            showError("Failed to create order. Please try again.");
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
        messageLabel.setText("");
    }

    private void showError(String message) {
        messageLabel.setText(message);
        messageLabel.setStyle("-fx-text-fill: #e74c3c; -fx-font-weight: bold;");
    }

    private void showSuccess(String message) {
        messageLabel.setText(message);
        messageLabel.setStyle("-fx-text-fill: #27ae60; -fx-font-weight: bold;");
    }
}

