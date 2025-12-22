package com.example.quickcommercedeliverysystemdesktop.controllers.dashboard;

import com.example.quickcommercedeliverysystemdesktop.database.UserDAO;
import com.example.quickcommercedeliverysystemdesktop.models.User;
import com.example.quickcommercedeliverysystemdesktop.utils.UserSession;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

public class ProfileController {

    @FXML private ImageView profileImageView;
    @FXML private TextField nameField;
    @FXML private TextField emailField;
    @FXML private TextField phoneField;
    @FXML private TextArea addressField;
    @FXML private PasswordField currentPasswordField;
    @FXML private PasswordField newPasswordField;
    @FXML private PasswordField confirmPasswordField;

    @FXML private Label imageMessageLabel;
    @FXML private Label profileMessageLabel;
    @FXML private Label addressMessageLabel;
    @FXML private Label passwordMessageLabel;

    private User currentUser;
    private String selectedImagePath = "";

    @FXML
    public void initialize() {
        currentUser = UserSession.getInstance().getCurrentUser();

        if (currentUser == null) {
            showError(profileMessageLabel, "No user logged in!");
            return;
        }

        loadUserData();
        loadProfileImage();
    }

    private void loadUserData() {
        // Refresh user data from database
        currentUser = UserDAO.getUserById(currentUser.getUserId());

        if (currentUser != null) {
            nameField.setText(currentUser.getName());
            emailField.setText(currentUser.getEmail());
            phoneField.setText(currentUser.getPhone());
            addressField.setText(currentUser.getDefaultAddress() != null ? currentUser.getDefaultAddress() : "");
        }
    }

    private void loadProfileImage() {
        if (currentUser.getProfileImage() != null && !currentUser.getProfileImage().isEmpty()) {
            try {
                File imageFile = new File(currentUser.getProfileImage());
                if (imageFile.exists()) {
                    Image image = new Image(imageFile.toURI().toString());
                    profileImageView.setImage(image);
                    return;
                }
            } catch (Exception e) {
                System.err.println("Error loading profile image: " + e.getMessage());
            }
        }

        // Load default avatar
        try {
            Image defaultImage = new Image(getClass().getResourceAsStream(
                "/com/example/quickcommercedeliverysystemdesktop/assets/default-avatar.png"));
            profileImageView.setImage(defaultImage);
        } catch (Exception e) {
            // If default avatar doesn't exist, create a placeholder
            System.err.println("Default avatar not found");
        }
    }

    @FXML
    public void handleChooseImage() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Choose Profile Picture");
        fileChooser.getExtensionFilters().addAll(
            new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg", "*.gif")
        );

        File selectedFile = fileChooser.showOpenDialog(profileImageView.getScene().getWindow());

        if (selectedFile != null) {
            try {
                // Create profiles directory if it doesn't exist
                Path profilesDir = Paths.get("src/main/resources/com/example/quickcommercedeliverysystemdesktop/assets/profiles");
                if (!Files.exists(profilesDir)) {
                    Files.createDirectories(profilesDir);
                }

                // Copy image to profiles directory with unique name
                String fileName = currentUser.getUserId() + "_" + System.currentTimeMillis() + getFileExtension(selectedFile.getName());
                Path targetPath = profilesDir.resolve(fileName);
                Files.copy(selectedFile.toPath(), targetPath, StandardCopyOption.REPLACE_EXISTING);

                // Update database
                String relativePath = targetPath.toString();
                boolean success = UserDAO.updateProfileImage(currentUser.getUserId(), relativePath);

                if (success) {
                    // Update session and UI
                    currentUser.setProfileImage(relativePath);
                    UserSession.getInstance().setCurrentUser(currentUser);

                    // Display image
                    Image image = new Image(selectedFile.toURI().toString());
                    profileImageView.setImage(image);

                    showSuccess(imageMessageLabel, "Profile picture updated successfully!");
                } else {
                    showError(imageMessageLabel, "Failed to update profile picture in database.");
                }

            } catch (IOException e) {
                showError(imageMessageLabel, "Error saving image: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    @FXML
    public void handleUpdateProfile() {
        String name = nameField.getText().trim();
        String email = emailField.getText().trim();
        String phone = phoneField.getText().trim();

        // Validation
        if (name.isEmpty() || email.isEmpty()) {
            showError(profileMessageLabel, "Name and email are required!");
            return;
        }

        if (!isValidEmail(email)) {
            showError(profileMessageLabel, "Invalid email format!");
            return;
        }

        if (!phone.isEmpty() && !isValidPhone(phone)) {
            showError(profileMessageLabel, "Invalid phone number format!");
            return;
        }

        // Update database
        boolean success = UserDAO.updateProfile(currentUser.getUserId(), name, email, phone);

        if (success) {
            // Update session
            currentUser.setName(name);
            currentUser.setEmail(email);
            currentUser.setPhone(phone);
            UserSession.getInstance().setCurrentUser(currentUser);

            showSuccess(profileMessageLabel, "Profile updated successfully!");
        } else {
            showError(profileMessageLabel, "Failed to update profile. Email might already be in use.");
        }
    }

    @FXML
    public void handleUpdateAddress() {
        String address = addressField.getText().trim();

        if (address.isEmpty()) {
            showError(addressMessageLabel, "Address cannot be empty!");
            return;
        }

        // Update database
        boolean success = UserDAO.updateAddress(currentUser.getUserId(), address);

        if (success) {
            // Update session
            currentUser.setDefaultAddress(address);
            UserSession.getInstance().setCurrentUser(currentUser);

            showSuccess(addressMessageLabel, "Address updated successfully!");
        } else {
            showError(addressMessageLabel, "Failed to update address.");
        }
    }

    @FXML
    public void handleChangePassword() {
        String currentPassword = currentPasswordField.getText();
        String newPassword = newPasswordField.getText();
        String confirmPassword = confirmPasswordField.getText();

        // Validation
        if (currentPassword.isEmpty() || newPassword.isEmpty() || confirmPassword.isEmpty()) {
            showError(passwordMessageLabel, "All password fields are required!");
            return;
        }

        if (newPassword.length() < 6) {
            showError(passwordMessageLabel, "New password must be at least 6 characters!");
            return;
        }

        if (!newPassword.equals(confirmPassword)) {
            showError(passwordMessageLabel, "New passwords do not match!");
            return;
        }

        // Update database
        boolean success = UserDAO.updatePassword(currentUser.getUserId(), currentPassword, newPassword);

        if (success) {
            // Clear password fields
            currentPasswordField.clear();
            newPasswordField.clear();
            confirmPasswordField.clear();

            showSuccess(passwordMessageLabel, "Password changed successfully!");
        } else {
            showError(passwordMessageLabel, "Failed to change password. Current password might be incorrect.");
        }
    }

    // Helper methods for validation
    private boolean isValidEmail(String email) {
        return email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
    }

    private boolean isValidPhone(String phone) {
        return phone.matches("^[0-9+\\-\\s()]{10,15}$");
    }

    private String getFileExtension(String fileName) {
        int lastIndexOf = fileName.lastIndexOf(".");
        if (lastIndexOf == -1) {
            return ""; // No extension
        }
        return fileName.substring(lastIndexOf);
    }

    private void showSuccess(Label label, String message) {
        label.setText(message);
        label.setStyle("-fx-text-fill: #27ae60; -fx-font-weight: bold;");
    }

    private void showError(Label label, String message) {
        label.setText(message);
        label.setStyle("-fx-text-fill: #e74c3c; -fx-font-weight: bold;");
    }
}

