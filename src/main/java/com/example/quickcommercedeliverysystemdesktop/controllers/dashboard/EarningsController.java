package com.example.quickcommercedeliverysystemdesktop.controllers.dashboard;

import com.example.quickcommercedeliverysystemdesktop.database.DeliveryDAO;
import com.example.quickcommercedeliverysystemdesktop.database.DeliveryDAO.DailyEarning;
import com.example.quickcommercedeliverysystemdesktop.database.DeliveryDAO.DeliveryStats;
import com.example.quickcommercedeliverysystemdesktop.database.DeliveryDAO.EarningRecord;
import com.example.quickcommercedeliverysystemdesktop.utils.UserSession;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;

import java.time.LocalDate;
import java.util.List;

public class EarningsController {

    // Statistics Labels
    @FXML private Label totalEarningsLabel;
    @FXML private Label todayEarningsLabel;
    @FXML private Label weekEarningsLabel;
    @FXML private Label monthEarningsLabel;
    @FXML private Label completedDeliveriesLabel;
    @FXML private Label averageEarningLabel;

    // Chart
    @FXML private BarChart<String, Number> earningsChart;
    @FXML private CategoryAxis xAxis;
    @FXML private NumberAxis yAxis;

    // Earnings History Table
    @FXML private TableView<EarningRecord> earningsTable;
    @FXML private TableColumn<EarningRecord, String> dateColumn;
    @FXML private TableColumn<EarningRecord, String> timeColumn;
    @FXML private TableColumn<EarningRecord, String> orderIdColumn;
    @FXML private TableColumn<EarningRecord, String> productColumn;
    @FXML private TableColumn<EarningRecord, String> locationColumn;
    @FXML private TableColumn<EarningRecord, String> amountColumn;

    // Filters
    @FXML private ComboBox<String> periodFilter;
    @FXML private DatePicker fromDatePicker;
    @FXML private DatePicker toDatePicker;
    @FXML private Label recordsCountLabel;

    private ObservableList<EarningRecord> allEarnings;
    private int currentUserId;

    @FXML
    public void initialize() {
        currentUserId = UserSession.getInstance().getUserId();
        allEarnings = FXCollections.observableArrayList();

        setupTable();
        setupFilters();
        setupChart();
        loadData();
    }

    private void setupTable() {
        dateColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getFormattedDate()));

        timeColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getFormattedTime()));

        orderIdColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty("#" + cellData.getValue().getOrderId()));

        productColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getProductName()));

        locationColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getLocation()));

        amountColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getFormattedAmount()));

        // Style amount column
        amountColumn.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item);
                    setStyle("-fx-font-weight: bold; -fx-text-fill: #27ae60;");
                }
            }
        });

        earningsTable.setItems(allEarnings);
    }

    private void setupFilters() {
        ObservableList<String> periods = FXCollections.observableArrayList(
                "All Time", "Today", "Last 7 Days", "Last 30 Days"
        );
        periodFilter.setItems(periods);
        periodFilter.setValue("All Time");

        periodFilter.valueProperty().addListener((obs, oldVal, newVal) -> applyFilter());
    }

    private void setupChart() {
        if (earningsChart != null) {
            xAxis.setLabel("Date");
            yAxis.setLabel("Earnings (৳)");
            earningsChart.setTitle("");
            earningsChart.setLegendVisible(false);

            // Apply styling to make bars visible
            earningsChart.setStyle(
                "-fx-bar-fill: #3498db; " +
                "-fx-background-color: white; " +
                "-fx-plot-background-color: #f8f9fa;"
            );

            // Set axis styling
            xAxis.setStyle("-fx-tick-label-fill: #34495e; -fx-font-size: 12px;");
            yAxis.setStyle("-fx-tick-label-fill: #34495e; -fx-font-size: 12px;");

            // Enable auto-ranging
            yAxis.setAutoRanging(true);
            yAxis.setForceZeroInRange(true);
        }
    }

    private void loadData() {
        loadStatistics();
        loadEarningsHistory();
        loadChartData();
    }

    private void loadStatistics() {
        System.out.println("=== LOADING EARNINGS STATISTICS ===");
        System.out.println("Current User ID: " + currentUserId);

        // Get overall stats
        DeliveryStats stats = DeliveryDAO.getDeliveryStats(currentUserId);
        System.out.println("Total Earnings: " + stats.getTotalEarnings());
        System.out.println("Completed Deliveries: " + stats.getCompletedDeliveries());

        totalEarningsLabel.setText(String.format("৳%.2f", stats.getTotalEarnings()));
        completedDeliveriesLabel.setText(String.valueOf(stats.getCompletedDeliveries()));

        // Calculate average
        double average = stats.getCompletedDeliveries() > 0
                ? stats.getTotalEarnings() / stats.getCompletedDeliveries()
                : 0.0;
        System.out.println("Average: " + average);
        averageEarningLabel.setText(String.format("৳%.2f", average));

        // Get period-specific earnings
        double todayEarnings = DeliveryDAO.getEarningsForPeriod(currentUserId, "TODAY");
        System.out.println("Today's Earnings: " + todayEarnings);
        todayEarningsLabel.setText(String.format("৳%.2f", todayEarnings));

        double weekEarnings = DeliveryDAO.getEarningsForPeriod(currentUserId, "WEEK");
        System.out.println("Week Earnings: " + weekEarnings);
        weekEarningsLabel.setText(String.format("৳%.2f", weekEarnings));

        double monthEarnings = DeliveryDAO.getEarningsForPeriod(currentUserId, "MONTH");
        System.out.println("Month Earnings: " + monthEarnings);
        monthEarningsLabel.setText(String.format("৳%.2f", monthEarnings));

        System.out.println("=== STATISTICS LOADED ===");
    }

    private void loadEarningsHistory() {
        List<EarningRecord> earnings = DeliveryDAO.getEarningsHistory(currentUserId);
        allEarnings.setAll(earnings);
        applyFilter();
    }

    private void loadChartData() {
        System.out.println("=== LOADING CHART DATA ===");
        if (earningsChart != null) {
            List<DailyEarning> dailyEarnings = DeliveryDAO.getDailyEarnings(currentUserId, 7);
            System.out.println("Retrieved " + dailyEarnings.size() + " daily earning records");

            earningsChart.getData().clear();

            XYChart.Series<String, Number> series = new XYChart.Series<>();
            series.setName("Daily Earnings");

            if (dailyEarnings != null && !dailyEarnings.isEmpty()) {
                // Format dates for better display
                java.time.format.DateTimeFormatter inputFormatter =
                    java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd");
                java.time.format.DateTimeFormatter outputFormatter =
                    java.time.format.DateTimeFormatter.ofPattern("MMM dd");

                for (DailyEarning daily : dailyEarnings) {
                    System.out.println("  Date: " + daily.getDate() + ", Amount: " + daily.getTotalAmount());
                    try {
                        // Parse and format the date
                        java.time.LocalDate date = java.time.LocalDate.parse(daily.getDate(), inputFormatter);
                        String formattedDate = date.format(outputFormatter);

                        XYChart.Data<String, Number> data = new XYChart.Data<>(
                            formattedDate,
                            daily.getTotalAmount()
                        );
                        series.getData().add(data);
                    } catch (Exception e) {
                        System.err.println("Error parsing date: " + daily.getDate());
                        // If date parsing fails, use original date
                        XYChart.Data<String, Number> data = new XYChart.Data<>(
                            daily.getDate(),
                            daily.getTotalAmount()
                        );
                        series.getData().add(data);
                    }
                }
            } else {
                System.out.println("No earnings data - showing placeholder");
                // Add placeholder data to show empty chart
                series.getData().add(new XYChart.Data<>("No Data", 0));
            }

            earningsChart.getData().add(series);

            // Apply CSS and force layout update
            earningsChart.applyCss();
            earningsChart.layout();

            // Style each bar to ensure visibility
            javafx.application.Platform.runLater(() -> {
                for (XYChart.Series<String, Number> s : earningsChart.getData()) {
                    for (XYChart.Data<String, Number> d : s.getData()) {
                        if (d.getNode() != null) {
                            d.getNode().setStyle("-fx-bar-fill: #3498db; -fx-background-color: #3498db;");
                        }
                    }
                }
            });
            System.out.println("=== CHART DATA LOADED ===");
        }
    }

    private void applyFilter() {
        String period = periodFilter.getValue();
        ObservableList<EarningRecord> filteredList = FXCollections.observableArrayList();

        for (EarningRecord record : allEarnings) {
            boolean include = switch (period) {
                case "Today" -> record.getEarnedAt().toLocalDate().equals(java.time.LocalDate.now());
                case "Last 7 Days" -> record.getEarnedAt().toLocalDate().isAfter(
                        java.time.LocalDate.now().minusDays(7));
                case "Last 30 Days" -> record.getEarnedAt().toLocalDate().isAfter(
                        java.time.LocalDate.now().minusDays(30));
                default -> true; // All Time
            };

            if (include) {
                filteredList.add(record);
            }
        }

        earningsTable.setItems(filteredList);
        recordsCountLabel.setText("Showing " + filteredList.size() + " record(s)");
    }

    @FXML
    private void handleRefresh() {
        loadData();
        showAlert("Data refreshed successfully!", Alert.AlertType.INFORMATION);
    }

    @FXML
    private void handleExportReport() {
        // Placeholder for export functionality
        showAlert("Export feature coming soon!", Alert.AlertType.INFORMATION);
    }

    @FXML
    private void handleDateSearch() {
        LocalDate fromDate = fromDatePicker.getValue();
        LocalDate toDate = toDatePicker.getValue();

        if (fromDate == null && toDate == null) {
            showAlert("Please select at least one date to search.", Alert.AlertType.WARNING);
            return;
        }

        // Validate date range
        if (fromDate != null && toDate != null && fromDate.isAfter(toDate)) {
            showAlert("'From Date' cannot be after 'To Date'.", Alert.AlertType.ERROR);
            return;
        }

        // Reset period filter to show custom date range
        periodFilter.setValue("All Time");

        // Apply date filter
        applyDateFilter(fromDate, toDate);
    }

    @FXML
    private void handleClearDateFilter() {
        fromDatePicker.setValue(null);
        toDatePicker.setValue(null);
        periodFilter.setValue("All Time");
        applyFilter();
    }

    private void applyDateFilter(LocalDate fromDate, LocalDate toDate) {
        ObservableList<EarningRecord> filteredList = FXCollections.observableArrayList();

        for (EarningRecord record : allEarnings) {
            LocalDate recordDate = record.getEarnedAt().toLocalDate();
            boolean include = true;

            // Check from date
            if (fromDate != null && recordDate.isBefore(fromDate)) {
                include = false;
            }

            // Check to date
            if (toDate != null && recordDate.isAfter(toDate)) {
                include = false;
            }

            if (include) {
                filteredList.add(record);
            }
        }

        earningsTable.setItems(filteredList);
        recordsCountLabel.setText("Showing " + filteredList.size() + " record(s)");

        // Show success message
        String dateRangeText;
        if (fromDate != null && toDate != null) {
            dateRangeText = "from " + fromDate + " to " + toDate;
        } else if (fromDate != null) {
            dateRangeText = "from " + fromDate + " onwards";
        } else {
            dateRangeText = "up to " + toDate;
        }

        if (filteredList.size() > 0) {
            showAlert("Found " + filteredList.size() + " record(s) " + dateRangeText, Alert.AlertType.INFORMATION);
        } else {
            showAlert("No records found " + dateRangeText, Alert.AlertType.INFORMATION);
        }
    }

    private void showAlert(String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
