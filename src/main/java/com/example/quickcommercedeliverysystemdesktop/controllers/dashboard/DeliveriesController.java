package com.example.quickcommercedeliverysystemdesktop.controllers.dashboard;

import com.example.quickcommercedeliverysystemdesktop.database.DeliveryDAO;
import com.example.quickcommercedeliverysystemdesktop.database.DeliveryDAO.DeliveryStats;
import com.example.quickcommercedeliverysystemdesktop.models.Order;
import com.example.quickcommercedeliverysystemdesktop.models.Order.OrderStatus;
import com.example.quickcommercedeliverysystemdesktop.utils.UserSession;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;

public class DeliveriesController {

    @FXML private TabPane deliveryTabPane;

    // Available Orders Tab
    @FXML private TableView<Order> availableOrdersTable;
    @FXML private TableColumn<Order, String> availOrderIdColumn;
    @FXML private TableColumn<Order, String> availProductColumn;
    @FXML private TableColumn<Order, String> availLocationColumn;
    @FXML private TableColumn<Order, String> availFeeColumn;
    @FXML private TableColumn<Order, String> availTimeColumn;
    @FXML private TableColumn<Order, Void> availActionsColumn;
    @FXML private Label availableCountLabel;

    // My Deliveries Tab
    @FXML private ComboBox<String> deliveryStatusFilter;
    @FXML private TableView<Order> myDeliveriesTable;
    @FXML private TableColumn<Order, String> myOrderIdColumn;
    @FXML private TableColumn<Order, String> myProductColumn;
    @FXML private TableColumn<Order, String> myLocationColumn;
    @FXML private TableColumn<Order, String> myFeeColumn;
    @FXML private TableColumn<Order, String> myStatusColumn;
    @FXML private TableColumn<Order, Void> myActionsColumn;
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

        setupAvailableOrdersTable();
        setupMyDeliveriesTable();
        setupFilters();
        loadData();
    }

    private void setupAvailableOrdersTable() {
        availOrderIdColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty("#" + cellData.getValue().getOrderId()));

        availProductColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getProductName()));

        availLocationColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getDeliveryLocation()));

        availFeeColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getFormattedDeliveryFee()));

        availTimeColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getDeliveryTimeRange()));

        // Actions column with Accept button
        availActionsColumn.setCellFactory(param -> new TableCell<>() {
            private final Button acceptButton = new Button("Accept");

            {
                acceptButton.getStyleClass().add("action-button");
                acceptButton.setOnAction(event -> {
                    Order order = getTableView().getItems().get(getIndex());
                    acceptOrder(order);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    HBox buttons = new HBox(5);
                    buttons.setAlignment(Pos.CENTER);
                    buttons.getChildren().add(acceptButton);
                    setGraphic(buttons);
                }
            }
        });

        availableOrdersTable.setItems(availableOrders);
    }

    private void setupMyDeliveriesTable() {
        myOrderIdColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty("#" + cellData.getValue().getOrderId()));

        myProductColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getProductName()));

        myLocationColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getDeliveryLocation()));

        myFeeColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getFormattedDeliveryFee()));

        myStatusColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getStatus().getDisplayName()));

        // Custom cell factory for status with colors
        myStatusColumn.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item);
                    Order order = getTableView().getItems().get(getIndex());
                    setStyle(getStatusStyle(order.getStatus()));
                }
            }
        });

        // Actions column with status update buttons
        myActionsColumn.setCellFactory(param -> new TableCell<>() {
            private final Button pickupButton = new Button("Pick Up");
            private final Button onWayButton = new Button("On The Way");
            private final Button deliverButton = new Button("Deliver");
            private final Button viewButton = new Button("View");

            {
                pickupButton.getStyleClass().add("action-button");
                onWayButton.getStyleClass().add("action-button");
                deliverButton.getStyleClass().add("action-button");
                viewButton.getStyleClass().add("action-button");

                pickupButton.setOnAction(event -> {
                    Order order = getTableView().getItems().get(getIndex());
                    markAsPickedUp(order);
                });

                onWayButton.setOnAction(event -> {
                    Order order = getTableView().getItems().get(getIndex());
                    markAsOnTheWay(order);
                });

                deliverButton.setOnAction(event -> {
                    Order order = getTableView().getItems().get(getIndex());
                    completeDelivery(order);
                });

                viewButton.setOnAction(event -> {
                    Order order = getTableView().getItems().get(getIndex());
                    viewOrderDetails(order);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    Order order = getTableView().getItems().get(getIndex());
                    HBox buttons = new HBox(5);
                    buttons.setAlignment(Pos.CENTER);

                    OrderStatus status = order.getStatus();

                    if (status == OrderStatus.ACCEPTED) {
                        buttons.getChildren().addAll(pickupButton, viewButton);
                    } else if (status == OrderStatus.PICKED_UP) {
                        buttons.getChildren().addAll(onWayButton, deliverButton, viewButton);
                    } else if (status == OrderStatus.ON_THE_WAY) {
                        buttons.getChildren().addAll(deliverButton, viewButton);
                    } else {
                        buttons.getChildren().add(viewButton);
                    }

                    setGraphic(buttons);
                }
            }
        });

        myDeliveriesTable.setItems(filteredDeliveries);
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
    }

    private void loadData() {
        loadAvailableOrders();
        loadMyDeliveries();
        loadStatistics();
    }

    private void loadAvailableOrders() {
        availableOrders.setAll(DeliveryDAO.getAvailableOrders());
        availableCountLabel.setText("Available: " + availableOrders.size());
    }

    private void loadMyDeliveries() {
        myDeliveries.setAll(DeliveryDAO.getDeliveriesByPartner(currentUserId));
        updateCounts();
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

    private String getStatusStyle(OrderStatus status) {
        String baseStyle = "-fx-padding: 5px 10px; -fx-border-radius: 12px; " +
                "-fx-background-radius: 12px; -fx-font-weight: bold; ";

        return switch (status) {
            case DELIVERED -> baseStyle + "-fx-background-color: #d4edda; -fx-text-fill: #155724;";
            case ON_THE_WAY -> baseStyle + "-fx-background-color: #fff3cd; -fx-text-fill: #856404;";
            case PICKED_UP -> baseStyle + "-fx-background-color: #d1ecf1; -fx-text-fill: #0c5460;";
            case ACCEPTED -> baseStyle + "-fx-background-color: #cce5ff; -fx-text-fill: #004085;";
            default -> baseStyle + "-fx-background-color: #e2e3e5; -fx-text-fill: #383d41;";
        };
    }

    private void showAlert(String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
