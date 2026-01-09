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
    @FXML private Label activeOrdersLabel;
    @FXML private Label completedDeliveriesLabel;
    @FXML private Label avgEarningsLabel;
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

        // Calculate active orders
        long activeOrders = myOrders.stream()
                .filter(order -> order.getStatus() != Order.OrderStatus.DELIVERED
                        && order.getStatus() != Order.OrderStatus.CANCELLED)
                .count();
        if (activeOrdersLabel != null) {
            activeOrdersLabel.setText(activeOrders + " active");
        }

        // Load deliveries count (active deliveries)
        List<Order> myDeliveries = DeliveryDAO.getDeliveriesByPartner(userId);
        long activeDeliveries = myDeliveries.stream()
                .filter(order -> order.getStatus() != Order.OrderStatus.DELIVERED
                        && order.getStatus() != Order.OrderStatus.CANCELLED)
                .count();
        deliveriesCountLabel.setText(String.valueOf(activeDeliveries));

        // Calculate completed deliveries
        long completedDeliveries = myDeliveries.stream()
                .filter(order -> order.getStatus() == Order.OrderStatus.DELIVERED)
                .count();
        if (completedDeliveriesLabel != null) {
            completedDeliveriesLabel.setText(completedDeliveries + " completed");
        }

        // Load earnings
        DeliveryDAO.DeliveryStats stats = DeliveryDAO.getDeliveryStats(userId);
        earningsLabel.setText(String.format("৳%.2f", stats.getTotalEarnings()));

        // Calculate average earnings
        if (avgEarningsLabel != null) {
            double avgEarnings = stats.getCompletedDeliveries() > 0
                    ? stats.getTotalEarnings() / stats.getCompletedDeliveries()
                    : 0.0;
            avgEarningsLabel.setText(String.format("৳%.2f avg", avgEarnings));
        }

        // Load recent activity
        loadRecentActivity(myOrders, myDeliveries);
    }

    private void loadRecentActivity(List<Order> myOrders, List<Order> myDeliveries) {
        ObservableList<String> activities = FXCollections.observableArrayList();

        // Combine all activities with type and timestamp
        class Activity {
            final String type;
            final Order order;

            Activity(String type, Order order) {
                this.type = type;
                this.order = order;
            }
        }

        java.util.List<Activity> allActivities = new java.util.ArrayList<>();

        // Add orders
        myOrders.forEach(order -> allActivities.add(new Activity("📦 Order", order)));

        // Add deliveries
        myDeliveries.forEach(order -> allActivities.add(new Activity("🚚 Delivery", order)));

        // Sort by order ID (descending - most recent first)
        allActivities.sort((a, b) -> Integer.compare(b.order.getOrderId(), a.order.getOrderId()));

        // Take top 10 and format with truncated product names
        allActivities.stream()
                .limit(10)
                .forEach(activity -> {
                    String productName = activity.order.getProductName();
                    // Truncate long product names
                    if (productName != null && productName.length() > 30) {
                        productName = productName.substring(0, 27) + "...";
                    }

                    activities.add(
                            String.format("%s #%d - %s (%s)",
                                    activity.type,
                                    activity.order.getOrderId(),
                                    productName,
                                    activity.order.getStatus().getDisplayName())
                    );
                });

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
