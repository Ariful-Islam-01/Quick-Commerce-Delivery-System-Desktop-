package com.example.quickcommercedeliverysystemdesktop.controllers.dashboard;

import com.example.quickcommercedeliverysystemdesktop.database.OrderDAO;
import com.example.quickcommercedeliverysystemdesktop.models.Order;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;

import java.util.Optional;

/**
 * Manage Orders Controller - Day 13
 * Admin panel for viewing and managing all orders
 */
public class ManageOrdersController {

    @FXML private TextField searchField;
    @FXML private ComboBox<String> statusFilterComboBox;
    @FXML private TableView<Order> ordersTable;
    @FXML private TableColumn<Order, Integer> orderIdColumn;
    @FXML private TableColumn<Order, String> customerColumn;
    @FXML private TableColumn<Order, String> productColumn;
    @FXML private TableColumn<Order, String> locationColumn;
    @FXML private TableColumn<Order, String> feeColumn;
    @FXML private TableColumn<Order, String> statusColumn;
    @FXML private TableColumn<Order, String> dateColumn;
    @FXML private TableColumn<Order, Void> actionsColumn;
    @FXML private Label totalOrdersLabel;
    @FXML private Label pendingOrdersLabel;
    @FXML private Label deliveredOrdersLabel;
    @FXML private Label cancelledOrdersLabel;

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
        // Set up column value factories
        orderIdColumn.setCellValueFactory(cellData ->
            new javafx.beans.property.SimpleIntegerProperty(cellData.getValue().getOrderId()).asObject()
        );
        customerColumn.setCellValueFactory(cellData ->
            new SimpleStringProperty(cellData.getValue().getCustomerName())
        );
        productColumn.setCellValueFactory(cellData ->
            new SimpleStringProperty(cellData.getValue().getProductName())
        );
        locationColumn.setCellValueFactory(cellData ->
            new SimpleStringProperty(cellData.getValue().getDeliveryLocation())
        );
        feeColumn.setCellValueFactory(cellData ->
            new SimpleStringProperty(String.format("$%.2f", cellData.getValue().getDeliveryFee()))
        );
        statusColumn.setCellValueFactory(cellData ->
            new SimpleStringProperty(getStatusBadge(cellData.getValue().getStatus().name()))
        );
        dateColumn.setCellValueFactory(cellData ->
            new SimpleStringProperty(cellData.getValue().getOrderDate().toString().substring(0, 16))
        );

        // Actions column with buttons
        actionsColumn.setCellFactory(param -> new TableCell<>() {
            private final Button viewButton = new Button("View");
            private final Button deleteButton = new Button("Delete");

            {
                viewButton.getStyleClass().add("action-button");
                deleteButton.getStyleClass().add("action-button-danger");

                viewButton.setOnAction(event -> {
                    Order order = getTableView().getItems().get(getIndex());
                    viewOrderDetails(order);
                });

                deleteButton.setOnAction(event -> {
                    Order order = getTableView().getItems().get(getIndex());
                    deleteOrder(order);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    HBox buttons = new HBox(5, viewButton, deleteButton);
                    buttons.setAlignment(Pos.CENTER);
                    setGraphic(buttons);
                }
            }
        });

        ordersTable.setItems(filteredOrders);
    }

    private void setupFilters() {
        // Status filter options
        statusFilterComboBox.setItems(FXCollections.observableArrayList(
            "All Orders", "PENDING", "ACCEPTED", "PICKED_UP", "ON_THE_WAY", "DELIVERED", "CANCELLED"
        ));
        statusFilterComboBox.setValue("All Orders");

        // Search listener
        searchField.textProperty().addListener((observable, oldValue, newValue) -> applyFilters());

        // Status filter listener
        statusFilterComboBox.valueProperty().addListener((observable, oldValue, newValue) -> applyFilters());
    }

    private void applyFilters() {
        String searchText = searchField.getText().toLowerCase();
        String statusFilter = statusFilterComboBox.getValue();

        filteredOrders.setPredicate(order -> {
            // Apply search filter
            boolean matchesSearch = searchText.isEmpty() ||
                order.getCustomerName().toLowerCase().contains(searchText) ||
                order.getProductName().toLowerCase().contains(searchText) ||
                order.getDeliveryLocation().toLowerCase().contains(searchText);

            // Apply status filter
            boolean matchesStatus = statusFilter.equals("All Orders") ||
                order.getStatus().name().equals(statusFilter);

            return matchesSearch && matchesStatus;
        });

        updateStatistics();
    }

    private void loadOrders() {
        allOrders.setAll(OrderDAO.getAllOrders());
        updateStatistics();
    }

    private void updateStatistics() {
        totalOrdersLabel.setText(String.valueOf(filteredOrders.size()));

        long pending = allOrders.stream()
            .filter(o -> o.getStatus().name().equals("PENDING"))
            .count();
        pendingOrdersLabel.setText(String.valueOf(pending));

        long delivered = allOrders.stream()
            .filter(o -> o.getStatus().name().equals("DELIVERED"))
            .count();
        deliveredOrdersLabel.setText(String.valueOf(delivered));

        long cancelled = allOrders.stream()
            .filter(o -> o.getStatus().name().equals("CANCELLED"))
            .count();
        cancelledOrdersLabel.setText(String.valueOf(cancelled));
    }

    private void viewOrderDetails(Order order) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Order Details");
        alert.setHeaderText("Order #" + order.getOrderId());

        VBox content = new VBox(10);
        content.setPadding(new Insets(10));

        content.getChildren().addAll(
            new Label("Customer: " + order.getCustomerName()),
            new Label("Phone: " + (order.getCustomerPhone() != null ? order.getCustomerPhone() : "N/A")),
            new Label("Product: " + order.getProductName()),
            new Label("Description: " + (order.getDescription() != null ? order.getDescription() : "N/A")),
            new Label("Location: " + order.getDeliveryLocation()),
            new Label("Time Range: " + order.getDeliveryTimeRange()),
            new Label("Delivery Fee: $" + String.format("%.2f", order.getDeliveryFee())),
            new Label("Status: " + order.getStatus().name()),
            new Label("Created: " + order.getOrderDate().toString()),
            new Label("Notes: " + (order.getNotesForDelivery() != null ? order.getNotesForDelivery() : "None"))
        );

        alert.getDialogPane().setContent(content);
        alert.getDialogPane().setPrefWidth(500);
        alert.showAndWait();
    }

    private void deleteOrder(Order order) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirm Delete");
        confirm.setHeaderText("Delete Order #" + order.getOrderId());
        confirm.setContentText(
            "Are you sure you want to delete this order?\n\n" +
            "This will also delete:\n" +
            "• Related deliveries\n" +
            "• Earnings records\n" +
            "• Order history\n" +
            "• Notifications\n" +
            "• Ratings\n\n" +
            "This action cannot be undone!"
        );

        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            boolean success = OrderDAO.adminDeleteOrder(order.getOrderId());

            if (success) {
                showAlert("Success", "Order deleted successfully", Alert.AlertType.INFORMATION);
                loadOrders();
            } else {
                showAlert("Error", "Failed to delete order", Alert.AlertType.ERROR);
            }
        }
    }

    @FXML
    private void handleRefresh() {
        loadOrders();
        searchField.clear();
        statusFilterComboBox.setValue("All Orders");
    }

    private String getStatusBadge(String status) {
        return switch (status) {
            case "PENDING" -> "⏳Pending";
            case "ACCEPTED" -> "✅Accepted";
            case "PICKED_UP" -> "📦Picked Up";
            case "ON_THE_WAY" -> "🚚On the Way";
            case "DELIVERED" -> "✔Delivered";
            case "CANCELLED" -> "❌Cancelled";
            default -> status;
        };
    }

    private void showAlert(String title, String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}

