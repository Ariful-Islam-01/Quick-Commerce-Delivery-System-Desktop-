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
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;

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

    // Card View Components
    @FXML private ScrollPane cardsScrollPane;
    @FXML private VBox orderCardsContainer;
    @FXML private VBox emptyStateContainer;
    @FXML private ToggleButton cardViewToggle;
    @FXML private ToggleButton tableViewToggle;

    private ObservableList<Order> allOrders;
    private FilteredList<Order> filteredOrders;
    private boolean isCardView = true;

    @FXML
    public void initialize() {
        allOrders = FXCollections.observableArrayList();
        filteredOrders = new FilteredList<>(allOrders, p -> true);

        setupTable();
        setupFilters();
        setupViewToggles();
        loadOrders();
    }

    private void setupViewToggles() {
        ToggleGroup viewGroup = new ToggleGroup();
        cardViewToggle.setToggleGroup(viewGroup);
        tableViewToggle.setToggleGroup(viewGroup);

        cardViewToggle.setSelected(true);

        viewGroup.selectedToggleProperty().addListener((obs, oldToggle, newToggle) -> {
            if (newToggle == cardViewToggle) {
                switchToCardView();
            } else if (newToggle == tableViewToggle) {
                switchToTableView();
            } else {
                // Ensure one is always selected
                if (oldToggle != null) {
                    oldToggle.setSelected(true);
                }
            }
        });
    }

    private void switchToCardView() {
        isCardView = true;
        cardsScrollPane.setVisible(true);
        cardsScrollPane.setManaged(true);
        ordersTable.setVisible(false);
        ordersTable.setManaged(false);
        renderOrderCards();
    }

    private void switchToTableView() {
        isCardView = false;
        cardsScrollPane.setVisible(false);
        cardsScrollPane.setManaged(false);
        ordersTable.setVisible(true);
        ordersTable.setManaged(true);
    }

    private void renderOrderCards() {
        orderCardsContainer.getChildren().clear();

        if (filteredOrders.isEmpty()) {
            emptyStateContainer.setVisible(true);
            emptyStateContainer.setManaged(true);
            cardsScrollPane.setVisible(false);
            cardsScrollPane.setManaged(false);
        } else {
            emptyStateContainer.setVisible(false);
            emptyStateContainer.setManaged(false);
            cardsScrollPane.setVisible(true);
            cardsScrollPane.setManaged(true);

            for (Order order : filteredOrders) {
                VBox card = createOrderCard(order);
                orderCardsContainer.getChildren().add(card);
            }
        }
    }

    private VBox createOrderCard(Order order) {
        VBox card = new VBox(12);
        card.getStyleClass().add("order-card");
        card.setPadding(new Insets(20));

        // Header Row: Order ID, Category, and Status
        HBox headerRow = new HBox(12);
        headerRow.setAlignment(Pos.CENTER_LEFT);

        Label orderIdLabel = new Label("#" + order.getOrderId());
        orderIdLabel.getStyleClass().add("order-card-id");

        // Extract category from product name (format: [Category] Product)
        String category = extractCategory(order.getProductName());
        if (category != null && !category.isEmpty()) {
            Label categoryLabel = new Label(category);
            categoryLabel.getStyleClass().add("order-card-category");
        }

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Label statusLabel = new Label(order.getStatus().getDisplayName());
        statusLabel.getStyleClass().add("order-card-status");
        statusLabel.setStyle(getStatusCardStyle(order.getStatus()));

        // Add category label if exists
        if (category != null && !category.isEmpty()) {
            Label categoryBadge = new Label(category);
            categoryBadge.getStyleClass().add("order-card-category");
            headerRow.getChildren().addAll(orderIdLabel, categoryBadge, spacer, statusLabel);
        } else {
            headerRow.getChildren().addAll(orderIdLabel, spacer, statusLabel);
        }

        // Product Row: Product Name and Description
        VBox productSection = new VBox(8);

        // Clean product name (remove category prefix if exists)
        String cleanProductName = order.getProductName();
        if (cleanProductName.contains("]")) {
            cleanProductName = cleanProductName.substring(cleanProductName.indexOf("]") + 1).trim();
        }

        Label productLabel = new Label(cleanProductName);
        productLabel.getStyleClass().add("order-card-product");
        productLabel.setWrapText(true);
        productLabel.setMaxWidth(Double.MAX_VALUE);

        productSection.getChildren().add(productLabel);

        // Description (if available)
        if (order.getDescription() != null && !order.getDescription().isEmpty()) {
            Label descLabel = new Label(order.getDescription());
            descLabel.getStyleClass().add("order-card-description");
            descLabel.setWrapText(true);
            descLabel.setMaxWidth(Double.MAX_VALUE);
            productSection.getChildren().add(descLabel);
        }

        Separator separator1 = new Separator();

        // Location Row
        HBox locationRow = new HBox(10);
        locationRow.setAlignment(Pos.CENTER_LEFT);

        Label locationIcon = new Label("📍");
        locationIcon.setStyle("-fx-font-size: 16px;");

        VBox locationBox = new VBox(3);
        Label locationTitle = new Label("Delivery Location");
        locationTitle.getStyleClass().add("order-card-label");

        Label locationText = new Label(order.getDeliveryLocation());
        locationText.getStyleClass().add("order-card-value");
        locationText.setWrapText(true);

        locationBox.getChildren().addAll(locationTitle, locationText);
        HBox.setHgrow(locationBox, Priority.ALWAYS);

        locationRow.getChildren().addAll(locationIcon, locationBox);

        // Time and Fee Row
        HBox detailsRow = new HBox(20);
        detailsRow.setAlignment(Pos.CENTER_LEFT);

        // Time
        VBox timeBox = new VBox(3);
        HBox.setHgrow(timeBox, Priority.ALWAYS);

        HBox timeHeader = new HBox(5);
        timeHeader.setAlignment(Pos.CENTER_LEFT);
        Label timeIcon = new Label("🕐");
        Label timeTitle = new Label("Delivery Time");
        timeTitle.getStyleClass().add("order-card-label");
        timeHeader.getChildren().addAll(timeIcon, timeTitle);

        Label timeText = new Label(order.getDeliveryTimeRange());
        timeText.getStyleClass().add("order-card-value");

        timeBox.getChildren().addAll(timeHeader, timeText);

        // Fee
        VBox feeBox = new VBox(3);

        HBox feeHeader = new HBox(5);
        feeHeader.setAlignment(Pos.CENTER_LEFT);
        Label feeIcon = new Label("💵");
        Label feeTitle = new Label("Delivery Fee");
        feeTitle.getStyleClass().add("order-card-label");
        feeHeader.getChildren().addAll(feeIcon, feeTitle);

        Label feeText = new Label(order.getFormattedDeliveryFee());
        feeText.getStyleClass().add("order-card-fee");

        feeBox.getChildren().addAll(feeHeader, feeText);

        detailsRow.getChildren().addAll(timeBox, feeBox);

        Separator separator2 = new Separator();

        // Footer: Date and Actions
        HBox footerRow = new HBox(15);
        footerRow.setAlignment(Pos.CENTER_LEFT);

        VBox dateBox = new VBox(3);
        Label dateTitle = new Label("Created");
        dateTitle.getStyleClass().add("order-card-label-small");

        Label dateText = new Label(order.getFormattedOrderDate());
        dateText.getStyleClass().add("order-card-date");

        dateBox.getChildren().addAll(dateTitle, dateText);

        Region footerSpacer = new Region();
        HBox.setHgrow(footerSpacer, Priority.ALWAYS);

        // Action Buttons
        HBox actionsBox = new HBox(8);
        actionsBox.setAlignment(Pos.CENTER_RIGHT);

        Button viewButton = new Button("👁 View");
        viewButton.getStyleClass().add("card-action-button-primary");
        viewButton.setOnAction(e -> viewOrderDetails(order));

        actionsBox.getChildren().add(viewButton);

        if (order.canEdit()) {
            Button editButton = new Button("✏ Edit");
            editButton.getStyleClass().add("card-action-button");
            editButton.setOnAction(e -> editOrder(order));
            actionsBox.getChildren().add(editButton);
        }

        if (order.canCancel()) {
            Button cancelButton = new Button("✖ Cancel");
            cancelButton.getStyleClass().add("card-action-button-danger");
            cancelButton.setOnAction(e -> cancelOrder(order));
            actionsBox.getChildren().add(cancelButton);
        }

        footerRow.getChildren().addAll(dateBox, footerSpacer, actionsBox);

        // Add all rows to card
        card.getChildren().addAll(
            headerRow,
            productSection,
            separator1,
            locationRow,
            detailsRow,
            separator2,
            footerRow
        );

        return card;
    }

    // Helper method to extract category from product name
    private String extractCategory(String productName) {
        if (productName != null && productName.startsWith("[") && productName.contains("]")) {
            return productName.substring(1, productName.indexOf("]"));
        }
        return null;
    }

    private String getStatusCardStyle(OrderStatus status) {
        String baseStyle = "-fx-padding: 7px 16px; -fx-border-radius: 18px; " +
                "-fx-background-radius: 18px; -fx-font-weight: bold; -fx-font-size: 12px; " +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.15), 4, 0, 0, 2);";

        return switch (status) {
            case DELIVERED -> baseStyle + " -fx-background-color: linear-gradient(to right, #27ae60, #2ecc71); -fx-text-fill: #ffffff;";
            case CANCELLED -> baseStyle + " -fx-background-color: linear-gradient(to right, #e74c3c, #ec7063); -fx-text-fill: #ffffff;";
            case ON_THE_WAY -> baseStyle + " -fx-background-color: linear-gradient(to right, #f39c12, #f1c40f); -fx-text-fill: #ffffff;";
            case PICKED_UP -> baseStyle + " -fx-background-color: linear-gradient(to right, #3498db, #5dade2); -fx-text-fill: #ffffff;";
            case ACCEPTED -> baseStyle + " -fx-background-color: linear-gradient(to right, #1abc9c, #48c9b0); -fx-text-fill: #ffffff;";
            case PENDING -> baseStyle + " -fx-background-color: linear-gradient(to right, #95a5a6, #bdc3c7); -fx-text-fill: #ffffff;";
        };
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
        if (isCardView) {
            renderOrderCards();
        }
    }

    private void loadOrders() {
        int userId = UserSession.getInstance().getUserId();
        allOrders.setAll(OrderDAO.getOrdersByUser(userId));
        updateStatistics();
        if (isCardView) {
            renderOrderCards();
        }
    }

    private void updateStatistics() {
        totalOrdersLabel.setText("Total: " + filteredOrders.size());
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
            controller.setUserRole("CUSTOMER");

            javafx.stage.Stage stage = new javafx.stage.Stage();
            stage.initModality(javafx.stage.Modality.APPLICATION_MODAL);
            stage.setTitle("Order Details - #" + order.getOrderId());
            stage.setScene(new javafx.scene.Scene(root, 650, 700));
            stage.showAndWait();

            // Refresh orders after dialog closes
            loadOrders();

        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Failed to load order details: " + e.getMessage(), Alert.AlertType.ERROR);
        }
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

