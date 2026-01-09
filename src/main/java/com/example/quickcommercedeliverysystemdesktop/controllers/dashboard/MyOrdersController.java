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
    @FXML private GridPane orderCardsContainer;
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
        orderCardsContainer.getRowConstraints().clear();
        orderCardsContainer.getColumnConstraints().clear();

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

            // Set up 2 columns with equal width
            ColumnConstraints col1 = new ColumnConstraints();
            col1.setPercentWidth(50);
            ColumnConstraints col2 = new ColumnConstraints();
            col2.setPercentWidth(50);
            orderCardsContainer.getColumnConstraints().addAll(col1, col2);

            // Add cards in 2-column layout
            int row = 0;
            int col = 0;
            for (Order order : filteredOrders) {
                VBox card = createOrderCard(order);
                GridPane.setColumnIndex(card, col);
                GridPane.setRowIndex(card, row);
                orderCardsContainer.getChildren().add(card);

                col++;
                if (col >= 2) {
                    col = 0;
                    row++;
                }
            }
        }
    }

    private VBox createOrderCard(Order order) {
        VBox card = new VBox(12);
        card.getStyleClass().add("order-card");
        card.setPadding(new Insets(20));
        card.setMaxWidth(Double.MAX_VALUE);
        card.setPrefHeight(Region.USE_COMPUTED_SIZE);

        // Header Row: Order ID, Category, and Status
        HBox headerRow = new HBox(12);
        headerRow.setAlignment(Pos.CENTER_LEFT);

        Label orderIdLabel = new Label("#" + order.getOrderId());
        orderIdLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #2c3e50; " +
                "-fx-background-color: #e8f4f8; -fx-padding: 5px 12px; -fx-background-radius: 12px; " +
                "-fx-border-color: #2b7a78; -fx-border-width: 1.5px; -fx-border-radius: 12px;");

        // Extract category from product name (format: [Category] Product)
        String category = extractCategory(order.getProductName());
        if (category != null && !category.isEmpty()) {
            Label categoryLabel = new Label(category);
            categoryLabel.setStyle("-fx-font-size: 12px; -fx-font-weight: bold; -fx-text-fill: #ffffff; " +
                    "-fx-background-color: linear-gradient(to right, #6c5ce7, #a29bfe); " +
                    "-fx-padding: 4px 12px; -fx-background-radius: 12px;");
            headerRow.getChildren().add(categoryLabel);
        }

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Label statusLabel = new Label(order.getStatus().getDisplayName());
        statusLabel.setStyle(getStatusCardStyle(order.getStatus()));

        headerRow.getChildren().addAll(orderIdLabel, spacer, statusLabel);

        // Product Row: Product Name and Description
        VBox productSection = new VBox(6);

        // Clean product name (remove category prefix if exists)
        String cleanProductName = order.getProductName();
        if (cleanProductName.contains("]")) {
            cleanProductName = cleanProductName.substring(cleanProductName.indexOf("]") + 1).trim();
        }

        Label productLabel = new Label(cleanProductName);
        productLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");
        productLabel.setWrapText(true);
        productLabel.setMaxWidth(Double.MAX_VALUE);

        productSection.getChildren().add(productLabel);

        // Description (if available and not too long)
        if (order.getDescription() != null && !order.getDescription().isEmpty()) {
            String desc = order.getDescription();
            if (desc.length() > 80) {
                desc = desc.substring(0, 77) + "...";
            }
            Label descLabel = new Label(desc);
            descLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #5a6c7d; -fx-font-style: italic;");
            descLabel.setWrapText(true);
            descLabel.setMaxWidth(Double.MAX_VALUE);
            productSection.getChildren().add(descLabel);
        }

        Separator separator1 = new Separator();
        separator1.setStyle("-fx-background-color: #e0e6ed;");

        // Details Section
        VBox detailsSection = new VBox(10);

        // Location
        VBox locationBox = new VBox(3);
        Label locationTitle = new Label("Delivery Location");
        locationTitle.setStyle("-fx-font-size: 11px; -fx-font-weight: bold; -fx-text-fill: #7f8c8d; " +
                "-fx-text-transform: uppercase;");

        Label locationText = new Label(order.getDeliveryLocation());
        locationText.setStyle("-fx-font-size: 14px; -fx-font-weight: normal; -fx-text-fill: #2c3e50;");
        locationText.setWrapText(true);
        locationText.setMaxWidth(Double.MAX_VALUE);

        locationBox.getChildren().addAll(locationTitle, locationText);

        // Time and Fee Row
        HBox timeAndFeeRow = new HBox(20);
        timeAndFeeRow.setAlignment(Pos.CENTER_LEFT);

        // Time
        VBox timeBox = new VBox(3);
        HBox.setHgrow(timeBox, Priority.ALWAYS);

        Label timeTitle = new Label("Delivery Time");
        timeTitle.setStyle("-fx-font-size: 11px; -fx-font-weight: bold; -fx-text-fill: #7f8c8d; " +
                "-fx-text-transform: uppercase;");

        Label timeText = new Label(order.getDeliveryTimeRange());
        timeText.setStyle("-fx-font-size: 14px; -fx-font-weight: normal; -fx-text-fill: #2c3e50;");
        timeText.setWrapText(true);

        timeBox.getChildren().addAll(timeTitle, timeText);

        // Fee
        VBox feeBox = new VBox(3);

        Label feeTitle = new Label("Delivery Fee");
        feeTitle.setStyle("-fx-font-size: 11px; -fx-font-weight: bold; -fx-text-fill: #7f8c8d; " +
                "-fx-text-transform: uppercase;");

        Label feeText = new Label(order.getFormattedDeliveryFee());
        feeText.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #27ae60;");

        feeBox.getChildren().addAll(feeTitle, feeText);

        timeAndFeeRow.getChildren().addAll(timeBox, feeBox);

        detailsSection.getChildren().addAll(locationBox, timeAndFeeRow);

        Separator separator2 = new Separator();
        separator2.setStyle("-fx-background-color: #e0e6ed;");

        // Footer: Date and Actions
        HBox footerRow = new HBox(15);
        footerRow.setAlignment(Pos.CENTER_LEFT);

        VBox dateBox = new VBox(3);
        Label dateTitle = new Label("Created Date");
        dateTitle.setStyle("-fx-font-size: 11px; -fx-font-weight: bold; -fx-text-fill: #7f8c8d; " +
                "-fx-text-transform: uppercase;");

        Label dateText = new Label(order.getFormattedOrderDate());
        dateText.setStyle("-fx-font-size: 13px; -fx-font-weight: normal; -fx-text-fill: #2c3e50;");

        dateBox.getChildren().addAll(dateTitle, dateText);

        Region footerSpacer = new Region();
        HBox.setHgrow(footerSpacer, Priority.ALWAYS);

        // Action Buttons
        HBox actionsBox = new HBox(8);
        actionsBox.setAlignment(Pos.CENTER_RIGHT);

        Button viewButton = new Button("View");
        viewButton.setStyle("-fx-background-color: linear-gradient(to bottom, #2b7a78, #1f5e5c); " +
                "-fx-text-fill: white; -fx-font-size: 12px; -fx-font-weight: bold; " +
                "-fx-padding: 8px 16px; -fx-background-radius: 6px; -fx-cursor: hand;");
        viewButton.setOnAction(e -> viewOrderDetails(order));

        actionsBox.getChildren().add(viewButton);

        if (order.canEdit()) {
            Button editButton = new Button("Edit");
            editButton.setStyle("-fx-background-color: linear-gradient(to bottom, #95a5a6, #7f8c8d); " +
                    "-fx-text-fill: white; -fx-font-size: 12px; -fx-font-weight: bold; " +
                    "-fx-padding: 8px 16px; -fx-background-radius: 6px; -fx-cursor: hand;");
            editButton.setOnAction(e -> editOrder(order));
            actionsBox.getChildren().add(editButton);
        }

        if (order.canCancel()) {
            Button cancelButton = new Button("Cancel");
            cancelButton.setStyle("-fx-background-color: linear-gradient(to bottom, #e74c3c, #c0392b); " +
                    "-fx-text-fill: white; -fx-font-size: 12px; -fx-font-weight: bold; " +
                    "-fx-padding: 8px 16px; -fx-background-radius: 6px; -fx-cursor: hand;");
            cancelButton.setOnAction(e -> cancelOrder(order));
            actionsBox.getChildren().add(cancelButton);
        }

        footerRow.getChildren().addAll(dateBox, footerSpacer, actionsBox);

        // Add all rows to card
        card.getChildren().addAll(
            headerRow,
            productSection,
            separator1,
            detailsSection,
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
        String baseStyle = "-fx-padding: 6px 14px; -fx-border-radius: 15px; " +
                "-fx-background-radius: 15px; -fx-font-weight: bold; -fx-font-size: 11px; " +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.12), 3, 0, 0, 1);";

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

