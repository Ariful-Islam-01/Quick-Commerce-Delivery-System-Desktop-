package com.example.quickcommercedeliverysystemdesktop.controllers.dashboard;

import com.example.quickcommercedeliverysystemdesktop.database.DeliveryDAO;
import com.example.quickcommercedeliverysystemdesktop.database.OrderDAO;
import com.example.quickcommercedeliverysystemdesktop.models.Order;
import com.example.quickcommercedeliverysystemdesktop.models.User;
import com.example.quickcommercedeliverysystemdesktop.utils.UserSession;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.layout.StackPane;

import java.util.List;

public class HomeController {

    @FXML private Label welcomeLabel;
    @FXML private Label myOrdersCountLabel;
    @FXML private Label deliveriesCountLabel;
    @FXML private Label earningsLabel;
    @FXML private ListView<String> recentActivityList;

    private User currentUser;

    @FXML
    public void initialize() {
        currentUser = UserSession.getInstance().getCurrentUser();
        loadDashboardData();
    }

    private void loadDashboardData() {
        // Set welcome message
        if (currentUser != null) {
            welcomeLabel.setText("Welcome back, " + currentUser.getName() + "!");
        }

        int userId = UserSession.getInstance().getUserId();

        // Load my orders count
        List<Order> myOrders = OrderDAO.getOrdersByUser(userId);
        myOrdersCountLabel.setText(String.valueOf(myOrders.size()));

        // Load deliveries count (active deliveries)
        List<Order> myDeliveries = DeliveryDAO.getDeliveriesByPartner(userId);
        long activeDeliveries = myDeliveries.stream()
                .filter(order -> order.getStatus() != Order.OrderStatus.DELIVERED
                        && order.getStatus() != Order.OrderStatus.CANCELLED)
                .count();
        deliveriesCountLabel.setText(String.valueOf(activeDeliveries));

        // Load earnings
        DeliveryDAO.DeliveryStats stats = DeliveryDAO.getDeliveryStats(userId);
        earningsLabel.setText(String.format("$%.2f", stats.getTotalEarnings()));

        // Load recent activity
        loadRecentActivity(myOrders, myDeliveries);
    }

    private void loadRecentActivity(List<Order> myOrders, List<Order> myDeliveries) {
        ObservableList<String> activities = FXCollections.observableArrayList();

        // Add recent orders (limit to 5)
        myOrders.stream()
                .limit(5)
                .forEach(order -> activities.add(
                        String.format("📦 Order #%d - %s (%s)",
                                order.getOrderId(),
                                order.getProductName(),
                                order.getStatus().getDisplayName())
                ));

        // Add recent deliveries (limit to 5)
        myDeliveries.stream()
                .limit(5)
                .forEach(order -> activities.add(
                        String.format("🚚 Delivery #%d - %s (%s)",
                                order.getOrderId(),
                                order.getProductName(),
                                order.getStatus().getDisplayName())
                ));

        if (activities.isEmpty()) {
            activities.add("No recent activity. Start by creating an order or accepting a delivery!");
        }

        recentActivityList.setItems(activities);
    }

    @FXML
    private void handleCreateOrder() {
        navigateToPage("CreateOrder.fxml");
    }

    @FXML
    private void handleBrowseOrders() {
        navigateToPage("Deliveries.fxml");
    }

    @FXML
    private void handleMyDeliveries() {
        navigateToPage("Deliveries.fxml");
    }

    private void navigateToPage(String page) {
        try {
            Parent root = FXMLLoader.load(
                    getClass().getResource("/com/example/quickcommercedeliverysystemdesktop/views/dashboard/" + page)
            );

            // Find the content area in the main dashboard and load the page
            StackPane contentArea = (StackPane) welcomeLabel.getScene().getRoot().lookup("#contentArea");
            if (contentArea != null) {
                contentArea.getChildren().setAll(root);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
