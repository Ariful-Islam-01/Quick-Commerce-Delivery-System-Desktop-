package com.example.quickcommercedeliverysystemdesktop.controllers.dashboard;

import com.example.quickcommercedeliverysystemdesktop.database.NotificationDAO;
import com.example.quickcommercedeliverysystemdesktop.database.OrderDAO;
import com.example.quickcommercedeliverysystemdesktop.models.Notification;
import com.example.quickcommercedeliverysystemdesktop.models.Notification.NotificationType;
import com.example.quickcommercedeliverysystemdesktop.models.Order;
import com.example.quickcommercedeliverysystemdesktop.utils.UserSession;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class NotificationsController {

    @FXML private Label unreadCountLabel;
    @FXML private VBox notificationsContainer;
    @FXML private VBox emptyStateContainer;

    // Filter toggle buttons
    @FXML private ToggleGroup filterToggleGroup;
    @FXML private ToggleButton allFilterBtn;
    @FXML private ToggleButton unreadFilterBtn;
    @FXML private ToggleButton orderFilterBtn;
    @FXML private ToggleButton deliveryFilterBtn;
    @FXML private ToggleButton earningFilterBtn;

    private int currentUserId;
    private List<Notification> allNotifications;
    private String currentFilter = "ALL";

    @FXML
    public void initialize() {
        currentUserId = UserSession.getInstance().getUserId();

        setupFilterListeners();
        loadNotifications();
    }

    private void setupFilterListeners() {
        filterToggleGroup.selectedToggleProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                ToggleButton selected = (ToggleButton) newVal;
                currentFilter = selected.getText().toUpperCase();
                applyFilter();
            }
        });
    }

    public void loadNotifications() {
        System.out.println("Loading notifications for user ID: " + currentUserId);
        allNotifications = NotificationDAO.getNotificationsByUser(currentUserId);

        if (allNotifications == null) {
            System.out.println("Warning: allNotifications is null, initializing empty list");
            allNotifications = new ArrayList<>();
        }

        System.out.println("Found " + allNotifications.size() + " notifications");
        updateUnreadCount();
        applyFilter();
    }

    private void applyFilter() {
        System.out.println("Applying filter: " + currentFilter);
        System.out.println("Total notifications: " + (allNotifications != null ? allNotifications.size() : "null"));

        notificationsContainer.getChildren().clear();

        List<Notification> filteredNotifications = allNotifications.stream()
                .filter(n -> {
                    return switch (currentFilter) {
                        case "UNREAD" -> !n.isRead();
                        case "ORDERS" -> n.getType() == NotificationType.ORDER_UPDATE;
                        case "DELIVERIES" -> n.getType() == NotificationType.DELIVERY_UPDATE;
                        case "EARNINGS" -> n.getType() == NotificationType.EARNING;
                        default -> true;
                    };
                })
                .toList();

        System.out.println("Filtered notifications: " + filteredNotifications.size());

        if (filteredNotifications.isEmpty()) {
            System.out.println("Showing empty state");
            showEmptyState();
        } else {
            System.out.println("Showing " + filteredNotifications.size() + " notification cards");
            hideEmptyState();
            for (Notification notification : filteredNotifications) {
                notificationsContainer.getChildren().add(createNotificationCard(notification));
            }
        }
    }

    private VBox createNotificationCard(Notification notification) {
        VBox card = new VBox(8);
        card.setPadding(new Insets(15));
        card.setStyle(
            "-fx-background-color: " + (notification.isRead() ? "#f8f9fa" : "#e3f2fd") + ";" +
            "-fx-background-radius: 8;" +
            "-fx-border-color: " + (notification.isRead() ? "#dee2e6" : "#3498db") + ";" +
            "-fx-border-width: " + (notification.isRead() ? "1" : "2") + ";" +
            "-fx-border-radius: 8;" +
            "-fx-cursor: hand;"
        );

        // Header with title and time
        HBox header = new HBox(10);
        header.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

        // Notification icon based on type
        String icon = getIconForType(notification.getType());
        Label iconLabel = new Label(icon);
        iconLabel.setStyle("-fx-font-size: 20px;");

        // Title
        Label titleLabel = new Label(notification.getTitle());
        titleLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");
        HBox.setHgrow(titleLabel, Priority.ALWAYS);

        // Time ago
        Label timeLabel = new Label(notification.getTimeAgo());
        timeLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #666;");

        // Unread indicator
        if (!notification.isRead()) {
            Label unreadDot = new Label("●");
            unreadDot.setStyle("-fx-text-fill: #3498db; -fx-font-size: 16px;");
            header.getChildren().addAll(iconLabel, titleLabel, timeLabel, unreadDot);
        } else {
            header.getChildren().addAll(iconLabel, titleLabel, timeLabel);
        }

        // Message
        Label messageLabel = new Label(notification.getMessage());
        messageLabel.setWrapText(true);
        messageLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #333;");

        // Action buttons
        HBox actionBox = new HBox(8);
        actionBox.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

        if (notification.getOrderId() != null && notification.getOrderId() > 0) {
            Button viewOrderBtn = new Button("View Order");
            viewOrderBtn.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-padding: 5 10; -fx-background-radius: 4; -fx-font-size: 11px;");
            viewOrderBtn.setOnAction(e -> handleViewOrder(notification.getOrderId()));
            actionBox.getChildren().add(viewOrderBtn);
        }

        if (!notification.isRead()) {
            Button markReadBtn = new Button("Mark as Read");
            markReadBtn.setStyle("-fx-background-color: #27ae60; -fx-text-fill: white; -fx-padding: 5 10; -fx-background-radius: 4; -fx-font-size: 11px;");
            markReadBtn.setOnAction(e -> handleMarkAsRead(notification));
            actionBox.getChildren().add(markReadBtn);
        }

        Button deleteBtn = new Button("Delete");
        deleteBtn.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-padding: 5 10; -fx-background-radius: 4; -fx-font-size: 11px;");
        deleteBtn.setOnAction(e -> handleDeleteNotification(notification));
        actionBox.getChildren().add(deleteBtn);

        card.getChildren().addAll(header, messageLabel, actionBox);

        // Click to mark as read
        card.setOnMouseClicked(e -> {
            if (!notification.isRead()) {
                handleMarkAsRead(notification);
            }
        });

        return card;
    }

    private String getIconForType(NotificationType type) {
        return switch (type) {
            case ORDER_UPDATE -> "📦";
            case DELIVERY_UPDATE -> "🚚";
            case EARNING -> "💰";
            case SUCCESS -> "✅";
            case WARNING -> "⚠️";
            default -> "ℹ️";
        };
    }

    private void handleViewOrder(int orderId) {
        try {
            Order order = OrderDAO.getOrderById(orderId);
            if (order != null) {
                FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/com/example/quickcommercedeliverysystemdesktop/views/dialogs/OrderDetails.fxml")
                );
                Parent root = loader.load();

                var controller = loader.getController();
                // Use reflection to call setOrder and setUserRole
                controller.getClass().getMethod("setOrder", Order.class).invoke(controller, order);

                String userRole = UserSession.getInstance().getRole();
                controller.getClass().getMethod("setUserRole", String.class).invoke(controller, userRole);

                Stage stage = new Stage();
                stage.initModality(Modality.APPLICATION_MODAL);
                stage.setTitle("Order Details - #" + orderId);
                stage.setScene(new Scene(root, 650, 700));
                stage.showAndWait();

                loadNotifications(); // Refresh in case order was updated
            } else {
                showAlert("Order not found", Alert.AlertType.WARNING);
            }
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Failed to load order details", Alert.AlertType.ERROR);
        }
    }

    private void handleMarkAsRead(Notification notification) {
        boolean success = NotificationDAO.markAsRead(notification.getNotificationId());
        if (success) {
            loadNotifications();
        }
    }

    @FXML
    private void handleMarkAllAsRead() {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Mark All as Read");
        confirm.setHeaderText("Mark all notifications as read?");
        confirm.setContentText("This will mark " + getUnreadCount() + " notification(s) as read.");

        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            boolean success = NotificationDAO.markAllAsRead(currentUserId);
            if (success) {
                loadNotifications();
                showAlert("All notifications marked as read", Alert.AlertType.INFORMATION);
            }
        }
    }

    private void handleDeleteNotification(Notification notification) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Delete Notification");
        confirm.setHeaderText("Delete this notification?");
        confirm.setContentText("This action cannot be undone.");

        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            boolean success = NotificationDAO.deleteNotification(notification.getNotificationId());
            if (success) {
                loadNotifications();
            }
        }
    }

    @FXML
    private void handleClearAll() {
        long count = allNotifications.stream().filter(Notification::isRead).count();

        if (count == 0) {
            showAlert("No read notifications to clear", Alert.AlertType.INFORMATION);
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Clear All Read Notifications");
        confirm.setHeaderText("Delete all read notifications?");
        confirm.setContentText("This will delete " + count + " read notification(s). This action cannot be undone.");

        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            int deleted = 0;
            for (Notification n : allNotifications) {
                if (n.isRead()) {
                    if (NotificationDAO.deleteNotification(n.getNotificationId())) {
                        deleted++;
                    }
                }
            }
            loadNotifications();
            showAlert(deleted + " notification(s) deleted", Alert.AlertType.INFORMATION);
        }
    }

    private void updateUnreadCount() {
        int unreadCount = getUnreadCount();
        unreadCountLabel.setText(unreadCount + " unread");

        if (unreadCount == 0) {
            unreadCountLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #27ae60; -fx-font-weight: bold;");
        } else {
            unreadCountLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #e74c3c; -fx-font-weight: bold;");
        }
    }

    private int getUnreadCount() {
        return (int) allNotifications.stream().filter(n -> !n.isRead()).count();
    }

    private void showEmptyState() {
        emptyStateContainer.setVisible(true);
        emptyStateContainer.setManaged(true);
        notificationsContainer.setVisible(false);
        notificationsContainer.setManaged(false);
    }

    private void hideEmptyState() {
        emptyStateContainer.setVisible(false);
        emptyStateContainer.setManaged(false);
        notificationsContainer.setVisible(true);
        notificationsContainer.setManaged(true);
    }

    private void showAlert(String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setContentText(message);
        alert.showAndWait();
    }
}

