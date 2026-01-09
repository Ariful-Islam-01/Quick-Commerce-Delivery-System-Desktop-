package com.example.quickcommercedeliverysystemdesktop.controllers.dashboard;

import com.example.quickcommercedeliverysystemdesktop.utils.ErrorHandler;
import com.example.quickcommercedeliverysystemdesktop.utils.UserSession;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

import java.util.Optional;

/**
 * Admin Main Dashboard Controller
 * Handles navigation for admin-specific features
 */
public class AdminMainDashboardController {

    @FXML private StackPane contentArea;
    @FXML private Button adminDashboardBtn;
    @FXML private Button manageUsersBtn;
    @FXML private Button manageOrdersBtn;
    @FXML private Button manageEarningsBtn;
    @FXML private Button profileBtn;
    @FXML private Button notificationsBtn;
    @FXML private Button logoutButton;

    private Button activeButton = null;

    @FXML
    public void initialize() {
        try {
            // Load Admin Dashboard by default
            loadAdminDashboard();
            ErrorHandler.logInfo("Admin Main Dashboard initialized");
        } catch (Exception e) {
            ErrorHandler.handleException(e, "Failed to initialize Admin Main Dashboard");
        }
    }

    @FXML
    public void loadAdminDashboard() {
        loadPage("AdminDashboard.fxml");
        setActiveButton(adminDashboardBtn);
    }

    @FXML
    public void loadManageUsers() {
        loadPage("ManageUsers.fxml");
        setActiveButton(manageUsersBtn);
    }

    @FXML
    public void loadManageOrders() {
        loadPage("ManageOrders.fxml");
        setActiveButton(manageOrdersBtn);
    }

    @FXML
    public void loadManageEarnings() {
        loadPage("ManageEarnings.fxml");
        setActiveButton(manageEarningsBtn);
    }

    @FXML
    public void loadProfile() {
        loadPage("Profile.fxml");
        setActiveButton(profileBtn);
    }

    @FXML
    public void loadNotifications() {
        loadPage("Notifications.fxml");
        setActiveButton(notificationsBtn);
    }

    @FXML
    public void handleLogout() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Logout Confirmation");
        alert.setHeaderText("Are you sure you want to logout?");
        alert.setContentText("You will be redirected to the login screen.");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                // Clear user session
                UserSession.getInstance().clearSession();
                ErrorHandler.logInfo("Admin logged out");

                // Navigate to login
                Stage stage = (Stage) logoutButton.getScene().getWindow();
                Parent root = FXMLLoader.load(
                    getClass().getResource("/com/example/quickcommercedeliverysystemdesktop/views/auth/Login.fxml")
                );
                Scene scene = new Scene(root, 1200, 750);
                scene.getStylesheets().add(
                    getClass().getResource("/com/example/quickcommercedeliverysystemdesktop/styles/style.css").toExternalForm()
                );
                stage.setScene(scene);
                stage.setTitle("Quick Commerce Delivery System - Login");
            } catch (Exception e) {
                ErrorHandler.handleException(e, "Failed to logout");
            }
        }
    }

    /**
     * Load a page into the content area
     */
    private void loadPage(String fxmlFile) {
        try {
            Parent page = FXMLLoader.load(
                getClass().getResource("/com/example/quickcommercedeliverysystemdesktop/views/dashboard/" + fxmlFile)
            );
            contentArea.getChildren().clear();
            contentArea.getChildren().add(page);
            ErrorHandler.logInfo("Loaded admin page: " + fxmlFile);
        } catch (Exception e) {
            ErrorHandler.handleException(e, "Failed to load page: " + fxmlFile);
        }
    }

    /**
     * Set active navigation button styling
     */
    private void setActiveButton(Button button) {
        // Remove active class from previous button
        if (activeButton != null) {
            activeButton.getStyleClass().remove("nav-btn-active");
        }

        // Add active class to current button
        if (button != null) {
            button.getStyleClass().add("nav-btn-active");
            activeButton = button;
        }
    }
}

