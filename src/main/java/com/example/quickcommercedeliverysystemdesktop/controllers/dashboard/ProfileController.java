package com.example.quickcommercedeliverysystemdesktop.controllers.dashboard;

import com.example.quickcommercedeliverysystemdesktop.database.UserDAO;
import com.example.quickcommercedeliverysystemdesktop.models.User;
import com.example.quickcommercedeliverysystemdesktop.utils.ErrorHandler;
import com.example.quickcommercedeliverysystemdesktop.utils.UserSession;
import com.example.quickcommercedeliverysystemdesktop.utils.ValidationUtil;
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
            ValidationUtil.showError(profileMessageLabel, "No user logged in!");
            return;
        }

        loadUserData();
        loadProfileImage();

        // Clear all message labels
        ValidationUtil.clearMessage(imageMessageLabel);
        ValidationUtil.clearMessage(profileMessageLabel);
        ValidationUtil.clearMessage(addressMessageLabel);
        ValidationUtil.clearMessage(passwordMessageLabel);
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
        ValidationUtil.clearMessage(imageMessageLabel);

        try {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Choose Profile Picture");
            fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg", "*.gif")
            );

            File selectedFile = fileChooser.showOpenDialog(profileImageView.getScene().getWindow());

            if (selectedFile != null) {
                // Validate file size (max 5MB)
                if (selectedFile.length() > 5 * 1024 * 1024) {
                    ValidationUtil.showError(imageMessageLabel, "Image file too large. Maximum size is 5MB.");
                    return;
                }

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

                    ValidationUtil.showSuccess(imageMessageLabel, "✓ Profile picture updated successfully!");
                    ErrorHandler.logInfo("Profile picture updated for user " + currentUser.getUserId());
                } else {
                    ValidationUtil.showError(imageMessageLabel, "Failed to update profile picture in database.");
                    ErrorHandler.logWarning("Failed to update profile picture for user " + currentUser.getUserId());
                }
            }
        } catch (IOException e) {
            ErrorHandler.handleFileException(e, "uploading profile picture");
            ValidationUtil.showError(imageMessageLabel, "Error saving image. Please try again.");
        }
    }

    @FXML
    public void handleUpdateProfile() {
        ValidationUtil.clearMessage(profileMessageLabel);
        ValidationUtil.clearFieldStyle(nameField, emailField, phoneField);

        String name = nameField.getText().trim();
        String email = emailField.getText().trim();
        String phone = phoneField.getText().trim();

        // Validate name
        if (!ValidationUtil.validateField(nameField, "Name", profileMessageLabel)) {
            return;
        }

        // Validate email
        if (!ValidationUtil.validateEmailField(emailField, profileMessageLabel)) {
            return;
        }

        // Validate phone format (optional field)
        if (!phone.isEmpty() && !ValidationUtil.isValidPhone(phone)) {
            phoneField.setStyle("-fx-border-color: #e74c3c; -fx-border-width: 2px;");
            ValidationUtil.showError(profileMessageLabel, "Invalid phone number format (10-15 digits)!");
            return;
        }

        try {
            // Update database
            boolean success = UserDAO.updateProfile(currentUser.getUserId(), name, email, phone);

            if (success) {
                // Update session
                currentUser.setName(name);
                currentUser.setEmail(email);
                currentUser.setPhone(phone);
                UserSession.getInstance().setCurrentUser(currentUser);

                ValidationUtil.showSuccess(profileMessageLabel, "✓ Profile updated successfully!");
                ErrorHandler.logInfo("Profile updated for user " + currentUser.getUserId());
            } else {
                emailField.setStyle("-fx-border-color: #e74c3c; -fx-border-width: 2px;");
                ValidationUtil.showError(profileMessageLabel, "Failed to update profile. Email might already be in use.");
                ErrorHandler.logWarning("Profile update failed for user " + currentUser.getUserId());
            }
        } catch (Exception e) {
            ErrorHandler.handleDatabaseException(e, "updating profile");
            ValidationUtil.showError(profileMessageLabel, "Failed to update profile. Please try again.");
        }
    }

    @FXML
    public void handleUpdateAddress() {
        ValidationUtil.clearMessage(addressMessageLabel);
        addressField.setStyle("");

        String address = addressField.getText().trim();

        if (!ValidationUtil.isNotEmpty(address)) {
            addressField.setStyle("-fx-border-color: #e74c3c; -fx-border-width: 2px;");
            ValidationUtil.showError(addressMessageLabel, "Address cannot be empty!");
            return;
        }

        try {
            // Update database
            boolean success = UserDAO.updateAddress(currentUser.getUserId(), address);

            if (success) {
                // Update session
                currentUser.setDefaultAddress(address);
                UserSession.getInstance().setCurrentUser(currentUser);

                ValidationUtil.showSuccess(addressMessageLabel, "✓ Address updated successfully!");
                ErrorHandler.logInfo("Address updated for user " + currentUser.getUserId());
            } else {
                ValidationUtil.showError(addressMessageLabel, "Failed to update address.");
                ErrorHandler.logWarning("Address update failed for user " + currentUser.getUserId());
            }
        } catch (Exception e) {
            ErrorHandler.handleDatabaseException(e, "updating address");
            ValidationUtil.showError(addressMessageLabel, "Failed to update address. Please try again.");
        }
    }

    @FXML
    public void handleChangePassword() {
        ValidationUtil.clearMessage(passwordMessageLabel);
        ValidationUtil.clearFieldStyle(currentPasswordField, newPasswordField, confirmPasswordField);

        String currentPassword = currentPasswordField.getText();
        String newPassword = newPasswordField.getText();
        String confirmPassword = confirmPasswordField.getText();

        // Validate current password
        if (!ValidationUtil.validateField(currentPasswordField, "Current Password", passwordMessageLabel)) {
            return;
        }

        // Validate password match
        if (!ValidationUtil.validatePasswordMatch(newPasswordField, confirmPasswordField, passwordMessageLabel)) {
            return;
        }

        try {
            // Update database
            boolean success = UserDAO.updatePassword(currentUser.getUserId(), currentPassword, newPassword);

            if (success) {
                // Clear password fields
                currentPasswordField.clear();
                newPasswordField.clear();
                confirmPasswordField.clear();

                ValidationUtil.showSuccess(passwordMessageLabel, "✓ Password changed successfully!");
                ErrorHandler.logInfo("Password changed for user " + currentUser.getUserId());
            } else {
                currentPasswordField.setStyle("-fx-border-color: #e74c3c; -fx-border-width: 2px;");
                ValidationUtil.showError(passwordMessageLabel, "Failed to change password. Current password is incorrect.");
                ErrorHandler.logWarning("Password change failed for user " + currentUser.getUserId());
            }
        } catch (Exception e) {
            ErrorHandler.handleDatabaseException(e, "changing password");
            ValidationUtil.showError(passwordMessageLabel, "Failed to change password. Please try again.");
        }
    }

    // Helper methods for validation
    private String getFileExtension(String fileName) {
        int lastIndexOf = fileName.lastIndexOf(".");
        if (lastIndexOf == -1) {
            return ""; // No extension
        }
        return fileName.substring(lastIndexOf);
    }
}

