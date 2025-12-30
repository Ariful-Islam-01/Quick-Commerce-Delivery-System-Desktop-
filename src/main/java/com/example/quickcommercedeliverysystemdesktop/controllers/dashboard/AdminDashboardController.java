package com.example.quickcommercedeliverysystemdesktop.controllers.dashboard;

import com.example.quickcommercedeliverysystemdesktop.database.DeliveryDAO;
import com.example.quickcommercedeliverysystemdesktop.database.OrderDAO;
import com.example.quickcommercedeliverysystemdesktop.database.UserDAO;
import com.example.quickcommercedeliverysystemdesktop.models.Order;
import com.example.quickcommercedeliverysystemdesktop.models.User;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;

import java.util.List;

/**
 * Admin Dashboard Controller - Day 11
 * Shows overall system statistics for administrators
 */
public class AdminDashboardController {

    // Statistics Cards
    @FXML private Label totalUsersLabel;
    @FXML private Label todayUsersLabel;
    @FXML private Label totalOrdersLabel;
    @FXML private Label todayOrdersLabel;
    @FXML private Label totalDeliveriesLabel;
    @FXML private Label todayDeliveriesLabel;
    @FXML private Label totalEarningsLabel;
    @FXML private Label todayEarningsLabel;

    // Order Status Breakdown
    @FXML private Label pendingOrdersLabel;
    @FXML private Label acceptedOrdersLabel;
    @FXML private Label pickedUpOrdersLabel;
    @FXML private Label onTheWayOrdersLabel;
    @FXML private Label deliveredOrdersLabel;
    @FXML private Label cancelledOrdersLabel;

    // Recent Users Table
    @FXML private TableView<User> recentUsersTable;
    @FXML private TableColumn<User, Integer> userIdColumn;
    @FXML private TableColumn<User, String> userNameColumn;
    @FXML private TableColumn<User, String> userEmailColumn;
    @FXML private TableColumn<User, String> userPhoneColumn;

    // Recent Orders Table
    @FXML private TableView<Order> recentOrdersTable;
    @FXML private TableColumn<Order, Integer> orderIdColumn;
    @FXML private TableColumn<Order, String> productColumn;
    @FXML private TableColumn<Order, String> customerColumn;
    @FXML private TableColumn<Order, String> statusColumn;
    @FXML private TableColumn<Order, String> feeColumn;

    // Statistics Chart
    @FXML private BarChart<String, Number> statsChart;
    @FXML private CategoryAxis xAxis;
    @FXML private NumberAxis yAxis;

    private ObservableList<User> recentUsers;
    private ObservableList<Order> recentOrders;

    @FXML
    public void initialize() {
        setupTables();
        loadStatistics();
        loadRecentData();
        setupChart();
    }

    /**
     * Setup table columns
     */
    private void setupTables() {
        // Users Table
        userIdColumn.setCellValueFactory(new PropertyValueFactory<>("userId"));
        userNameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        userEmailColumn.setCellValueFactory(new PropertyValueFactory<>("email"));
        userPhoneColumn.setCellValueFactory(new PropertyValueFactory<>("phone"));

        // Orders Table
        orderIdColumn.setCellValueFactory(new PropertyValueFactory<>("orderId"));
        productColumn.setCellValueFactory(new PropertyValueFactory<>("productName"));
        customerColumn.setCellValueFactory(new PropertyValueFactory<>("customerName"));
        statusColumn.setCellValueFactory(cellData ->
            new javafx.beans.property.SimpleStringProperty(cellData.getValue().getStatus().getDisplayName())
        );
        feeColumn.setCellValueFactory(cellData ->
            new javafx.beans.property.SimpleStringProperty(String.format("$%.2f", cellData.getValue().getDeliveryFee()))
        );

        recentUsers = FXCollections.observableArrayList();
        recentOrders = FXCollections.observableArrayList();

        recentUsersTable.setItems(recentUsers);
        recentOrdersTable.setItems(recentOrders);
    }

    /**
     * Load all statistics
     */
    private void loadStatistics() {
        // Users Statistics
        int totalUsers = UserDAO.getTotalUserCount();
        int todayUsers = UserDAO.getTodayUserCount();
        totalUsersLabel.setText(String.valueOf(totalUsers));
        todayUsersLabel.setText("+" + todayUsers + " today");

        // Orders Statistics
        int totalOrders = OrderDAO.getTotalOrderCount();
        int todayOrders = OrderDAO.getTodayOrderCount();
        totalOrdersLabel.setText(String.valueOf(totalOrders));
        todayOrdersLabel.setText("+" + todayOrders + " today");

        // Deliveries Statistics
        int totalDeliveries = DeliveryDAO.getTotalDeliveryCount();
        int todayDeliveries = DeliveryDAO.getTodayDeliveryCount();
        totalDeliveriesLabel.setText(String.valueOf(totalDeliveries));
        todayDeliveriesLabel.setText("+" + todayDeliveries + " today");

        // Earnings Statistics
        double totalEarnings = DeliveryDAO.getTotalEarnings();
        double todayEarnings = DeliveryDAO.getTodayEarnings();
        totalEarningsLabel.setText(String.format("$%.2f", totalEarnings));
        todayEarningsLabel.setText(String.format("+$%.2f today", todayEarnings));

        // Order Status Breakdown
        pendingOrdersLabel.setText(String.valueOf(OrderDAO.getOrderCountByStatus("PENDING")));
        acceptedOrdersLabel.setText(String.valueOf(OrderDAO.getOrderCountByStatus("ACCEPTED")));
        pickedUpOrdersLabel.setText(String.valueOf(OrderDAO.getOrderCountByStatus("PICKED_UP")));
        onTheWayOrdersLabel.setText(String.valueOf(OrderDAO.getOrderCountByStatus("ON_THE_WAY")));
        deliveredOrdersLabel.setText(String.valueOf(OrderDAO.getOrderCountByStatus("DELIVERED")));
        cancelledOrdersLabel.setText(String.valueOf(OrderDAO.getOrderCountByStatus("CANCELLED")));
    }

    /**
     * Load recent users and orders
     */
    private void loadRecentData() {
        // Load recent users (top 10)
        List<User> allUsers = UserDAO.getAllUsers();
        recentUsers.setAll(allUsers.stream().limit(10).toList());

        // Load recent orders (top 10)
        List<Order> allOrders = OrderDAO.getAllOrders();
        recentOrders.setAll(allOrders.stream().limit(10).toList());
    }

    /**
     * Setup statistics chart
     */
    private void setupChart() {
        statsChart.setTitle("System Overview");
        xAxis.setLabel("Category");
        yAxis.setLabel("Count");

        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Statistics");

        // Add data to chart
        series.getData().add(new XYChart.Data<>("Users", UserDAO.getTotalUserCount()));
        series.getData().add(new XYChart.Data<>("Orders", OrderDAO.getTotalOrderCount()));
        series.getData().add(new XYChart.Data<>("Deliveries", DeliveryDAO.getTotalDeliveryCount()));
        series.getData().add(new XYChart.Data<>("Pending", OrderDAO.getOrderCountByStatus("PENDING")));
        series.getData().add(new XYChart.Data<>("Delivered", OrderDAO.getOrderCountByStatus("DELIVERED")));

        statsChart.getData().clear();
        statsChart.getData().add(series);
    }

    /**
     * Refresh all data
     */
    @FXML
    private void handleRefresh() {
        loadStatistics();
        loadRecentData();
        setupChart();
        System.out.println("Admin dashboard refreshed");
    }
}

