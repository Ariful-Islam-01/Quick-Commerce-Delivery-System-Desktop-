package com.example.quickcommercedeliverysystemdesktop.controllers.dashboard;

import com.example.quickcommercedeliverysystemdesktop.utils.UserSession;
import com.example.quickcommercedeliverysystemdesktop.models.User;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import javafx.scene.control.Button;

public class MainDashboardController {

    @FXML private StackPane contentArea;
    @FXML private Button logoutButton;

    // Navigation Buttons
    @FXML private Button homeBtn;
    @FXML private Button createOrderBtn;
    @FXML private Button myOrdersBtn;
    @FXML private Button deliveriesBtn;
    @FXML private Button earningsBtn;
    @FXML private Button notificationsBtn;
    @FXML private Button adminDashboardBtn;
    @FXML private Button manageUsersBtn;
    @FXML private Button manageOrdersBtn;
    @FXML private Button manageEarningsBtn;
    @FXML private Button profileBtn;

    // Active state CSS class
    private static final String ACTIVE_CLASS = "nav-btn-active";

    // Track currently active button
    private Button activeButton;

    @FXML
    public void initialize() {
        // Check if user is admin and show/hide admin features
        User currentUser = UserSession.getInstance().getCurrentUser();
        if (currentUser != null && !currentUser.isAdmin()) {
            // Hide admin buttons for non-admin users
            if (adminDashboardBtn != null) {
                adminDashboardBtn.setVisible(false);
                adminDashboardBtn.setManaged(false);
            }
            if (manageUsersBtn != null) {
                manageUsersBtn.setVisible(false);
                manageUsersBtn.setManaged(false);
            }
            if (manageOrdersBtn != null) {
                manageOrdersBtn.setVisible(false);
                manageOrdersBtn.setManaged(false);
            }
            if (manageEarningsBtn != null) {
                manageEarningsBtn.setVisible(false);
                manageEarningsBtn.setManaged(false);
            }
        }

        loadPage("Home.fxml");
        setActiveButton(homeBtn); // Set Home as active by default
    }

    @FXML
    public void loadHome() {
        loadPage("Home.fxml");
        setActiveButton(homeBtn);
    }

    @FXML
    public void loadCreateOrder() {
        loadPage("CreateOrder.fxml");
        setActiveButton(createOrderBtn);
    }

    @FXML
    public void loadMyOrders() {
        loadPage("MyOrders.fxml");
        setActiveButton(myOrdersBtn);
    }

    @FXML
    public void loadOrders() {
        // Redirect to MyOrders for customer view
        loadPage("MyOrders.fxml");
        setActiveButton(myOrdersBtn);
    }

    @FXML
    public void loadDeliveries() {
        loadPage("Deliveries.fxml");
        setActiveButton(deliveriesBtn);
    }

    @FXML
    public void loadEarnings() {
        loadPage("Earnings.fxml");
        setActiveButton(earningsBtn);
    }

    @FXML
    public void loadNotifications() {
        loadPage("Notifications.fxml");
        setActiveButton(notificationsBtn);
    }

    @FXML
    public void loadProfile() {
        loadPage("Profile.fxml");
        setActiveButton(profileBtn);
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

    private void loadPage(String page) {
        try {
            Parent root = FXMLLoader.load(
                    getClass().getResource("/com/example/quickcommercedeliverysystemdesktop/views/dashboard/" + page)
            );
            contentArea.getChildren().setAll(root);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Sets the active state for the navigation button
     * Removes active class from previous button and adds it to the new one
     * @param button The button to set as active
     */
    private void setActiveButton(Button button) {
        // Remove active class from previously active button
        if (activeButton != null) {
            activeButton.getStyleClass().remove(ACTIVE_CLASS);
        }

        // Set new active button
        if (button != null) {
            activeButton = button;
            if (!activeButton.getStyleClass().contains(ACTIVE_CLASS)) {
                activeButton.getStyleClass().add(ACTIVE_CLASS);
            }
        }
    }

    @FXML
    public void handleLogout() {
        try {
            // Clear user session
            UserSession.getInstance().clearSession();

            Stage stage = (Stage) logoutButton.getScene().getWindow();

            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/com/example/quickcommercedeliverysystemdesktop/views/auth/Login.fxml")
            );
            Parent root = loader.load();

            Scene scene = new Scene(root, 1000, 650);

            scene.getStylesheets().add(
                    getClass().getResource("/com/example/quickcommercedeliverysystemdesktop/styles/style.css")
                            .toExternalForm()
            );

            stage.setScene(scene);

            // Maintain window properties
            stage.setResizable(true);
            stage.setMinWidth(800);
            stage.setMinHeight(600);

            stage.show();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
