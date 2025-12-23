package com.example.quickcommercedeliverysystemdesktop.controllers.dashboard;

import com.example.quickcommercedeliverysystemdesktop.utils.UserSession;
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

    @FXML
    public void initialize() {
        loadPage("Home.fxml");
    }

    @FXML
    public void loadHome() {
        loadPage("Home.fxml");
    }

    @FXML
    public void loadCreateOrder() {
        loadPage("CreateOrder.fxml");
    }

    @FXML
    public void loadMyOrders() {
        loadPage("MyOrders.fxml");
    }

    @FXML
    public void loadOrders() {
        // Redirect to MyOrders for customer view
        loadPage("MyOrders.fxml");
    }

    @FXML
    public void loadDeliveries() {
        loadPage("Deliveries.fxml");
    }

    @FXML
    public void loadEarnings() {
        loadPage("Earnings.fxml");
    }

    @FXML
    public void loadProfile() {
        loadPage("Profile.fxml");
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

            Scene scene = new Scene(root);

            scene.getStylesheets().add(
                    getClass().getResource("/com/example/quickcommercedeliverysystemdesktop/styles/style.css")
                            .toExternalForm()
            );

            stage.setScene(scene);
            stage.show(); // optional but recommended

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
