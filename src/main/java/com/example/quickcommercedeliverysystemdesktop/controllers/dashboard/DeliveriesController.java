package com.example.quickcommercedeliverysystemdesktop.controllers.dashboard;

import com.example.quickcommercedeliverysystemdesktop.database.DeliveryDAO;
import com.example.quickcommercedeliverysystemdesktop.database.DeliveryDAO.DeliveryStats;
import com.example.quickcommercedeliverysystemdesktop.models.Order;
import com.example.quickcommercedeliverysystemdesktop.models.Order.OrderStatus;
import com.example.quickcommercedeliverysystemdesktop.utils.UserSession;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import java.util.List;

public class DeliveriesController {

    @FXML private TabPane deliveryTabPane;

    // Available Orders Tab - Card View
    @FXML private ScrollPane availableCardsScrollPane;
    @FXML private GridPane availableCardsContainer;
    @FXML private VBox availableEmptyState;
    @FXML private Label availableCountLabel;

    // My Deliveries Tab - Card View
    @FXML private ComboBox<String> deliveryStatusFilter;
    @FXML private ScrollPane myDeliveriesCardsScrollPane;
    @FXML private GridPane myDeliveriesCardsContainer;
    @FXML private VBox myDeliveriesEmptyState;
    @FXML private Label myDeliveriesCountLabel;

    // Statistics
    @FXML private Label totalDeliveriesLabel;
    @FXML private Label completedDeliveriesLabel;
    @FXML private Label inProgressLabel;
    @FXML private Label totalEarningsLabel;

    private ObservableList<Order> availableOrders;
    private ObservableList<Order> myDeliveries;
    private FilteredList<Order> filteredDeliveries;
    private int currentUserId;

    @FXML
    public void initialize() {
        currentUserId = UserSession.getInstance().getUserId();

        availableOrders = FXCollections.observableArrayList();
        myDeliveries = FXCollections.observableArrayList();
        filteredDeliveries = new FilteredList<>(myDeliveries, p -> true);

        setupFilters();
        loadData();
    }

    private void renderAvailableOrderCards() {
        availableCardsContainer.getChildren().clear();
        availableCardsContainer.getRowConstraints().clear();
        availableCardsContainer.getColumnConstraints().clear();

        if (availableOrders.isEmpty()) {
            availableEmptyState.setVisible(true);
            availableEmptyState.setManaged(true);
            availableCardsScrollPane.setVisible(false);
            availableCardsScrollPane.setManaged(false);
            return;
        }

        availableEmptyState.setVisible(false);
        availableEmptyState.setManaged(false);
        availableCardsScrollPane.setVisible(true);
        availableCardsScrollPane.setManaged(true);

        // Setup column constraints for 2 cards per row
        ColumnConstraints col1 = new ColumnConstraints();
        col1.setPercentWidth(50);
        ColumnConstraints col2 = new ColumnConstraints();
        col2.setPercentWidth(50);
        availableCardsContainer.getColumnConstraints().addAll(col1, col2);

        int row = 0;
        int col = 0;

        for (Order order : availableOrders) {
            VBox card = createAvailableOrderCard(order);
            availableCardsContainer.add(card, col, row);

            col++;
            if (col > 1) {
                col = 0;
                row++;
            }
        }
    }

    private VBox createAvailableOrderCard(Order order) {
        VBox card = new VBox(12);
        card.setStyle("-fx-background-color: white; -fx-padding: 20; -fx-background-radius: 12; " +
                     "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 10, 0, 0, 2); " +
                     "-fx-border-color: #e0e0e0; -fx-border-radius: 12; -fx-border-width: 1;");
        card.setPrefHeight(Region.USE_COMPUTED_SIZE);

        // Header Row: Order ID and Fee
        HBox headerRow = new HBox(10);
        headerRow.setAlignment(Pos.CENTER_LEFT);

        Label orderIdLabel = new Label("Order #" + order.getOrderId());
        orderIdLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Label feeLabel = new Label(order.getFormattedDeliveryFee());
        feeLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #27ae60;");

        headerRow.getChildren().addAll(orderIdLabel, spacer, feeLabel);

        // Product Section
        VBox productSection = new VBox(5);
        Label productTitle = new Label("Product:");
        productTitle.setStyle("-fx-font-size: 11px; -fx-font-weight: bold; -fx-text-fill: #7f8c8d;");
        Label productLabel = new Label(order.getProductName());
        productLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #34495e;");
        productLabel.setWrapText(true);
        productSection.getChildren().addAll(productTitle, productLabel);


        // Separator
        Separator separator1 = new Separator();
        separator1.setStyle("-fx-background-color: #ecf0f1;");

        // Location Section
        VBox locationSection = new VBox(5);
        Label locationTitle = new Label("Delivery Location:");
        locationTitle.setStyle("-fx-font-size: 11px; -fx-font-weight: bold; -fx-text-fill: #7f8c8d;");
        HBox locationBox = new HBox(8);
        locationBox.setAlignment(Pos.CENTER_LEFT);
        Label locationIcon = new Label("📍");
        locationIcon.setStyle("-fx-font-size: 14px;");
        Label locationLabel = new Label(order.getDeliveryLocation());
        locationLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #555;");
        locationLabel.setWrapText(true);
        locationBox.getChildren().addAll(locationIcon, locationLabel);
        locationSection.getChildren().addAll(locationTitle, locationBox);

        // Time Range Section
        VBox timeSection = new VBox(5);
        Label timeTitle = new Label("Delivery Time:");
        timeTitle.setStyle("-fx-font-size: 11px; -fx-font-weight: bold; -fx-text-fill: #7f8c8d;");
        HBox timeBox = new HBox(8);
        timeBox.setAlignment(Pos.CENTER_LEFT);
        Label timeIcon = new Label("🕐");
        timeIcon.setStyle("-fx-font-size: 14px;");
        Label timeLabel = new Label(order.getDeliveryTimeRange());
        timeLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #555;");
        timeBox.getChildren().addAll(timeIcon, timeLabel);
        timeSection.getChildren().addAll(timeTitle, timeBox);

        // Separator
        Separator separator2 = new Separator();
        separator2.setStyle("-fx-background-color: #ecf0f1;");

        // Actions
        HBox actionsBox = new HBox(10);
        actionsBox.setAlignment(Pos.CENTER);

        Button acceptButton = new Button("Accept Order");
        acceptButton.setStyle("-fx-background-color: #27ae60; -fx-text-fill: white; " +
                             "-fx-font-weight: bold; -fx-padding: 10 20; -fx-background-radius: 8; " +
                             "-fx-cursor: hand; -fx-font-size: 13px;");
        acceptButton.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(acceptButton, Priority.ALWAYS);

        acceptButton.setOnAction(e -> acceptOrder(order));

        // Hover effect
        acceptButton.setOnMouseEntered(e ->
            acceptButton.setStyle("-fx-background-color: #229954; -fx-text-fill: white; " +
                                 "-fx-font-weight: bold; -fx-padding: 10 20; -fx-background-radius: 8; " +
                                 "-fx-cursor: hand; -fx-font-size: 13px;"));
        acceptButton.setOnMouseExited(e ->
            acceptButton.setStyle("-fx-background-color: #27ae60; -fx-text-fill: white; " +
                                 "-fx-font-weight: bold; -fx-padding: 10 20; -fx-background-radius: 8; " +
                                 "-fx-cursor: hand; -fx-font-size: 13px;"));

        actionsBox.getChildren().add(acceptButton);

        card.getChildren().addAll(headerRow, productSection, separator1, locationSection, timeSection, separator2, actionsBox);

        return card;
    }

    private void renderMyDeliveriesCards() {
        myDeliveriesCardsContainer.getChildren().clear();
        myDeliveriesCardsContainer.getRowConstraints().clear();
        myDeliveriesCardsContainer.getColumnConstraints().clear();

        if (filteredDeliveries.isEmpty()) {
            myDeliveriesEmptyState.setVisible(true);
            myDeliveriesEmptyState.setManaged(true);
            myDeliveriesCardsScrollPane.setVisible(false);
            myDeliveriesCardsScrollPane.setManaged(false);
            return;
        }

        myDeliveriesEmptyState.setVisible(false);
        myDeliveriesEmptyState.setManaged(false);
        myDeliveriesCardsScrollPane.setVisible(true);
        myDeliveriesCardsScrollPane.setManaged(true);

        // Setup column constraints for 2 cards per row
        ColumnConstraints col1 = new ColumnConstraints();
        col1.setPercentWidth(50);
        ColumnConstraints col2 = new ColumnConstraints();
        col2.setPercentWidth(50);
        myDeliveriesCardsContainer.getColumnConstraints().addAll(col1, col2);

        int row = 0;
        int col = 0;

        for (Order order : filteredDeliveries) {
            VBox card = createMyDeliveryCard(order);
            myDeliveriesCardsContainer.add(card, col, row);

            col++;
            if (col > 1) {
                col = 0;
                row++;
            }
        }
    }

    private VBox createMyDeliveryCard(Order order) {
        VBox card = new VBox(12);
        card.setStyle("-fx-background-color: white; -fx-padding: 20; -fx-background-radius: 12; " +
                     "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 10, 0, 0, 2); " +
                     "-fx-border-color: #e0e0e0; -fx-border-radius: 12; -fx-border-width: 1;");
        card.setPrefHeight(Region.USE_COMPUTED_SIZE);

        // Header Row: Order ID, Status, and Fee
        HBox headerRow = new HBox(10);
        headerRow.setAlignment(Pos.CENTER_LEFT);

        Label orderIdLabel = new Label("Order #" + order.getOrderId());
        orderIdLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");

        // Status Badge
        Label statusBadge = new Label(order.getStatus().getDisplayName());
        statusBadge.setStyle(getStatusBadgeStyle(order.getStatus()));

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Label feeLabel = new Label(order.getFormattedDeliveryFee());
        feeLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #27ae60;");

        headerRow.getChildren().addAll(orderIdLabel, statusBadge, spacer, feeLabel);

        // Product Section
        VBox productSection = new VBox(5);
        Label productTitle = new Label("Product:");
        productTitle.setStyle("-fx-font-size: 11px; -fx-font-weight: bold; -fx-text-fill: #7f8c8d;");
        Label productLabel = new Label(order.getProductName());
        productLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #34495e;");
        productLabel.setWrapText(true);
        productSection.getChildren().addAll(productTitle, productLabel);


        // Separator
        Separator separator1 = new Separator();
        separator1.setStyle("-fx-background-color: #ecf0f1;");

        // Location Section
        VBox locationSection = new VBox(5);
        Label locationTitle = new Label("Delivery Location:");
        locationTitle.setStyle("-fx-font-size: 11px; -fx-font-weight: bold; -fx-text-fill: #7f8c8d;");
        HBox locationBox = new HBox(8);
        locationBox.setAlignment(Pos.CENTER_LEFT);
        Label locationIcon = new Label("📍");
        locationIcon.setStyle("-fx-font-size: 14px;");
        Label locationLabel = new Label(order.getDeliveryLocation());
        locationLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #555;");
        locationLabel.setWrapText(true);
        locationBox.getChildren().addAll(locationIcon, locationLabel);
        locationSection.getChildren().addAll(locationTitle, locationBox);

        // Separator
        Separator separator2 = new Separator();
        separator2.setStyle("-fx-background-color: #ecf0f1;");

        // Actions based on status
        HBox actionsBox = new HBox(10);
        actionsBox.setAlignment(Pos.CENTER);

        OrderStatus status = order.getStatus();

        if (status == OrderStatus.ACCEPTED) {
            Button pickupButton = createActionButton("Pick Up", "#3498db");
            pickupButton.setOnAction(e -> markAsPickedUp(order));

            Button viewButton = createActionButton("View Details", "#95a5a6");
            viewButton.setOnAction(e -> viewOrderDetails(order));

            actionsBox.getChildren().addAll(pickupButton, viewButton);

        } else if (status == OrderStatus.PICKED_UP) {
            Button onWayButton = createActionButton("On The Way", "#f39c12");
            onWayButton.setOnAction(e -> markAsOnTheWay(order));

            Button deliverButton = createActionButton("Deliver", "#27ae60");
            deliverButton.setOnAction(e -> completeDelivery(order));

            Button viewButton = createActionButton("View", "#95a5a6");
            viewButton.setOnAction(e -> viewOrderDetails(order));

            actionsBox.getChildren().addAll(onWayButton, deliverButton, viewButton);

        } else if (status == OrderStatus.ON_THE_WAY) {
            Button deliverButton = createActionButton("Deliver", "#27ae60");
            deliverButton.setOnAction(e -> completeDelivery(order));

            Button viewButton = createActionButton("View Details", "#95a5a6");
            viewButton.setOnAction(e -> viewOrderDetails(order));

            actionsBox.getChildren().addAll(deliverButton, viewButton);

        } else {
            Button viewButton = createActionButton("View Details", "#3498db");
            viewButton.setOnAction(e -> viewOrderDetails(order));
            actionsBox.getChildren().add(viewButton);
        }

        card.getChildren().addAll(headerRow, productSection, separator1, locationSection, separator2, actionsBox);

        return card;
    }

    private Button createActionButton(String text, String color) {
        Button button = new Button(text);
        button.setStyle("-fx-background-color: " + color + "; -fx-text-fill: white; " +
                       "-fx-font-weight: bold; -fx-padding: 8 15; -fx-background-radius: 6; " +
                       "-fx-cursor: hand; -fx-font-size: 12px;");
        button.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(button, Priority.ALWAYS);

        // Hover effect
        button.setOnMouseEntered(e -> button.setOpacity(0.8));
        button.setOnMouseExited(e -> button.setOpacity(1.0));

        return button;
    }

    private String getStatusBadgeStyle(OrderStatus status) {
        String baseStyle = "-fx-padding: 5 12; -fx-background-radius: 12; -fx-font-size: 11px; -fx-font-weight: bold; ";

        return switch (status) {
            case DELIVERED -> baseStyle + "-fx-background-color: #d4edda; -fx-text-fill: #155724;";
            case ON_THE_WAY -> baseStyle + "-fx-background-color: #fff3cd; -fx-text-fill: #856404;";
            case PICKED_UP -> baseStyle + "-fx-background-color: #d1ecf1; -fx-text-fill: #0c5460;";
            case ACCEPTED -> baseStyle + "-fx-background-color: #cce5ff; -fx-text-fill: #004085;";
            default -> baseStyle + "-fx-background-color: #e2e3e5; -fx-text-fill: #383d41;";
        };
    }

    private void setupFilters() {
        ObservableList<String> filterOptions = FXCollections.observableArrayList(
                "All Status",
                OrderStatus.ACCEPTED.getDisplayName(),
                OrderStatus.PICKED_UP.getDisplayName(),
                OrderStatus.ON_THE_WAY.getDisplayName(),
                OrderStatus.DELIVERED.getDisplayName()
        );
        deliveryStatusFilter.setItems(filterOptions);
        deliveryStatusFilter.setValue("All Status");

        deliveryStatusFilter.valueProperty().addListener((obs, oldVal, newVal) -> applyFilters());
    }

    private void applyFilters() {
        String statusFilter = deliveryStatusFilter.getValue();

        filteredDeliveries.setPredicate(order -> {
            if (statusFilter != null && !statusFilter.equals("All Status")) {
                return order.getStatus().getDisplayName().equals(statusFilter);
            }
            return true;
        });

        updateCounts();
        renderMyDeliveriesCards();
    }

    private void loadData() {
        loadAvailableOrders();
        loadMyDeliveries();
        loadStatistics();
    }

    private void loadAvailableOrders() {
        // Get all available orders
        List<Order> allAvailableOrders = DeliveryDAO.getAvailableOrders();

        // Filter out orders created by the current user (delivery partner shouldn't see their own orders)
        List<Order> filteredOrders = allAvailableOrders.stream()
                .filter(order -> order.getCreatedByUserId() != currentUserId)
                .collect(java.util.stream.Collectors.toList());

        availableOrders.setAll(filteredOrders);
        availableCountLabel.setText("Available: " + availableOrders.size());
        renderAvailableOrderCards();
    }

    private void loadMyDeliveries() {
        myDeliveries.setAll(DeliveryDAO.getDeliveriesByPartner(currentUserId));
        updateCounts();
        renderMyDeliveriesCards();
    }

    private void loadStatistics() {
        DeliveryStats stats = DeliveryDAO.getDeliveryStats(currentUserId);
        totalDeliveriesLabel.setText(String.valueOf(stats.getTotalDeliveries()));
        completedDeliveriesLabel.setText(String.valueOf(stats.getCompletedDeliveries()));
        inProgressLabel.setText(String.valueOf(stats.getInProgressDeliveries()));
        totalEarningsLabel.setText(String.format("$%.2f", stats.getTotalEarnings()));
    }

    private void updateCounts() {
        myDeliveriesCountLabel.setText("My Deliveries: " + filteredDeliveries.size());
    }

    private void acceptOrder(Order order) {
        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle("Accept Order");
        confirmation.setHeaderText("Accept Order #" + order.getOrderId());
        confirmation.setContentText(String.format(
                "Do you want to accept this delivery?\n\nProduct: %s\nLocation: %s\nFee: %s",
                order.getProductName(),
                order.getDeliveryLocation(),
                order.getFormattedDeliveryFee()
        ));

        confirmation.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                boolean success = DeliveryDAO.acceptOrder(order.getOrderId(), currentUserId);
                if (success) {
                    showAlert("Order accepted successfully!", Alert.AlertType.INFORMATION);
                    loadData();
                } else {
                    showAlert("Failed to accept order. It may have been accepted by another partner.", Alert.AlertType.ERROR);
                }
            }
        });
    }

    private void markAsPickedUp(Order order) {
        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle("Mark as Picked Up");
        confirmation.setContentText("Have you picked up this order?");

        confirmation.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                boolean success = DeliveryDAO.markAsPickedUp(order.getOrderId(), currentUserId);
                if (success) {
                    showAlert("Order marked as picked up!", Alert.AlertType.INFORMATION);
                    loadData();
                } else {
                    showAlert("Failed to update order status.", Alert.AlertType.ERROR);
                }
            }
        });
    }

    private void markAsOnTheWay(Order order) {
        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle("Mark as On The Way");
        confirmation.setContentText("Are you on the way to deliver this order?");

        confirmation.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                boolean success = DeliveryDAO.markAsOnTheWay(order.getOrderId());
                if (success) {
                    showAlert("Order marked as on the way!", Alert.AlertType.INFORMATION);
                    loadData();
                } else {
                    showAlert("Failed to update order status.", Alert.AlertType.ERROR);
                }
            }
        });
    }

    private void completeDelivery(Order order) {
        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle("Complete Delivery");
        confirmation.setHeaderText("Mark Order #" + order.getOrderId() + " as Delivered");
        confirmation.setContentText(String.format(
                "Have you successfully delivered this order?\n\nYou will earn: %s",
                order.getFormattedDeliveryFee()
        ));

        confirmation.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                boolean success = DeliveryDAO.completeDelivery(
                        order.getOrderId(),
                        currentUserId,
                        order.getDeliveryFee()
                );
                if (success) {
                    showAlert("Delivery completed! Earnings recorded.", Alert.AlertType.INFORMATION);
                    loadData();
                } else {
                    showAlert("Failed to complete delivery.", Alert.AlertType.ERROR);
                }
            }
        });
    }

    private void viewOrderDetails(Order order) {
        try {
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(
                getClass().getResource("/com/example/quickcommercedeliverysystemdesktop/views/dialogs/OrderDetails.fxml")
            );
            javafx.scene.Parent root = loader.load();

            com.example.quickcommercedeliverysystemdesktop.controllers.dialogs.OrderDetailsController controller =
                loader.getController();
            controller.setOrder(order);
            controller.setUserRole("DELIVERY_PARTNER");

            javafx.stage.Stage stage = new javafx.stage.Stage();
            stage.initModality(javafx.stage.Modality.APPLICATION_MODAL);
            stage.setTitle("Order Details - #" + order.getOrderId());
            stage.setScene(new javafx.scene.Scene(root, 650, 700));
            stage.showAndWait();

            // Refresh data after dialog closes
            loadData();

        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Failed to load order details: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void handleRefresh() {
        loadData();
        showAlert("Data refreshed!", Alert.AlertType.INFORMATION);
    }


    private void showAlert(String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
