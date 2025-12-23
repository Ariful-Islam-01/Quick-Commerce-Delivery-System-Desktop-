package com.example.quickcommercedeliverysystemdesktop.controllers.dashboard;

import com.example.quickcommercedeliverysystemdesktop.database.OrderDAO;
import com.example.quickcommercedeliverysystemdesktop.models.Order;
import com.example.quickcommercedeliverysystemdesktop.models.Order.OrderStatus;
import com.example.quickcommercedeliverysystemdesktop.utils.UserSession;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;

public class MyOrdersController {

    @FXML private ComboBox<String> statusFilterComboBox;
    @FXML private TableView<Order> ordersTable;
    @FXML private TableColumn<Order, String> orderIdColumn;
    @FXML private TableColumn<Order, String> productNameColumn;
    @FXML private TableColumn<Order, String> deliveryLocationColumn;
    @FXML private TableColumn<Order, String> deliveryFeeColumn;
    @FXML private TableColumn<Order, String> statusColumn;
    @FXML private TableColumn<Order, String> orderDateColumn;
    @FXML private TableColumn<Order, Void> actionsColumn;
    @FXML private Label totalOrdersLabel;

    private ObservableList<Order> allOrders;
    private FilteredList<Order> filteredOrders;

    @FXML
    public void initialize() {
        allOrders = FXCollections.observableArrayList();
        filteredOrders = new FilteredList<>(allOrders, p -> true);

        setupTable();
        setupFilters();
        loadOrders();
    }

    private void setupTable() {
        orderIdColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty("#" + cellData.getValue().getOrderId()));

        productNameColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getProductName()));

        deliveryLocationColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getDeliveryLocation()));

        deliveryFeeColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getFormattedDeliveryFee()));

        statusColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getStatus().getDisplayName()));

        // Custom cell factory for status with colors
        statusColumn.setCellFactory(column -> new TableCell<>() {
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

        orderDateColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getFormattedOrderDate()));

        // Actions column with Edit/Cancel/View buttons
        actionsColumn.setCellFactory(param -> new TableCell<>() {
            private final Button viewButton = new Button("View");
            private final Button editButton = new Button("Edit");
            private final Button cancelButton = new Button("Cancel");

            {
                viewButton.getStyleClass().add("action-button");
                editButton.getStyleClass().add("action-button");
                cancelButton.getStyleClass().add("action-button-danger");

                viewButton.setOnAction(event -> {
                    Order order = getTableView().getItems().get(getIndex());
                    viewOrderDetails(order);
                });

                editButton.setOnAction(event -> {
                    Order order = getTableView().getItems().get(getIndex());
                    editOrder(order);
                });

                cancelButton.setOnAction(event -> {
                    Order order = getTableView().getItems().get(getIndex());
                    cancelOrder(order);
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
                    buttons.getChildren().add(viewButton);

                    if (order.canEdit()) {
                        buttons.getChildren().add(editButton);
                    }
                    if (order.canCancel()) {
                        buttons.getChildren().add(cancelButton);
                    }

                    setGraphic(buttons);
                }
            }
        });

        ordersTable.setItems(filteredOrders);
    }

    private void setupFilters() {
        ObservableList<String> filterOptions = FXCollections.observableArrayList(
                "All Status",
                OrderStatus.PENDING.getDisplayName(),
                OrderStatus.ACCEPTED.getDisplayName(),
                OrderStatus.PICKED_UP.getDisplayName(),
                OrderStatus.ON_THE_WAY.getDisplayName(),
                OrderStatus.DELIVERED.getDisplayName(),
                OrderStatus.CANCELLED.getDisplayName()
        );
        statusFilterComboBox.setItems(filterOptions);
        statusFilterComboBox.setValue("All Status");

        statusFilterComboBox.valueProperty().addListener((obs, oldVal, newVal) -> applyFilters());
    }

    private void applyFilters() {
        String statusFilter = statusFilterComboBox.getValue();

        filteredOrders.setPredicate(order -> {
            if (statusFilter != null && !statusFilter.equals("All Status")) {
                return order.getStatus().getDisplayName().equals(statusFilter);
            }
            return true;
        });

        updateStatistics();
    }

    private void loadOrders() {
        int userId = UserSession.getInstance().getUserId();
        allOrders.setAll(OrderDAO.getOrdersByUser(userId));
        updateStatistics();
    }

    private void updateStatistics() {
        totalOrdersLabel.setText("Total: " + filteredOrders.size());
    }

    private void viewOrderDetails(Order order) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Order Details");
        alert.setHeaderText("Order #" + order.getOrderId());

        String details = String.format("""
                Product: %s
                Description: %s
                Delivery Location: %s
                Time: %s
                Fee: %s
                Status: %s
                Created: %s
                Notes: %s
                """,
                order.getProductName(),
                order.getDescription(),
                order.getDeliveryLocation(),
                order.getDeliveryTimeRange(),
                order.getFormattedDeliveryFee(),
                order.getStatus().getDisplayName(),
                order.getFormattedOrderDate(),
                order.getNotesForDelivery() != null ? order.getNotesForDelivery() : "None"
        );

        alert.setContentText(details);
        alert.showAndWait();
    }

    private void editOrder(Order order) {
        // TODO: Open edit dialog or navigate to edit page
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Edit Order");
        alert.setContentText("Edit functionality will be implemented in next phase.");
        alert.showAndWait();
    }

    private void cancelOrder(Order order) {
        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle("Cancel Order");
        confirmation.setHeaderText("Cancel Order #" + order.getOrderId());
        confirmation.setContentText("Are you sure you want to cancel this order?");

        confirmation.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                boolean success = OrderDAO.cancelOrder(order.getOrderId());
                if (success) {
                    showAlert("Order cancelled successfully", Alert.AlertType.INFORMATION);
                    loadOrders();
                } else {
                    showAlert("Failed to cancel order", Alert.AlertType.ERROR);
                }
            }
        });
    }

    @FXML
    private void handleRefresh() {
        loadOrders();
        showAlert("Orders refreshed", Alert.AlertType.INFORMATION);
    }

    private String getStatusStyle(OrderStatus status) {
        String baseStyle = "-fx-padding: 5px 10px; -fx-border-radius: 12px; " +
                "-fx-background-radius: 12px; -fx-font-weight: bold; ";

        return switch (status) {
            case DELIVERED -> baseStyle + "-fx-background-color: #d4edda; -fx-text-fill: #155724;";
            case CANCELLED -> baseStyle + "-fx-background-color: #f8d7da; -fx-text-fill: #721c24;";
            case ON_THE_WAY -> baseStyle + "-fx-background-color: #fff3cd; -fx-text-fill: #856404;";
            case PICKED_UP -> baseStyle + "-fx-background-color: #d1ecf1; -fx-text-fill: #0c5460;";
            case ACCEPTED -> baseStyle + "-fx-background-color: #cce5ff; -fx-text-fill: #004085;";
            case PENDING -> baseStyle + "-fx-background-color: #e2e3e5; -fx-text-fill: #383d41;";
        };
    }

    private void showAlert(String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setContentText(message);
        alert.showAndWait();
    }
}

