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
            yAxis.setLabel("Earnings ($)");
            earningsChart.setTitle("Daily Earnings (Last 7 Days)");
            earningsChart.setLegendVisible(false);
        }
    }

    private void loadData() {
        loadStatistics();
        loadEarningsHistory();
        loadChartData();
    }

    private void loadStatistics() {
        // Get overall stats
        DeliveryStats stats = DeliveryDAO.getDeliveryStats(currentUserId);
        totalEarningsLabel.setText(String.format("$%.2f", stats.getTotalEarnings()));
        completedDeliveriesLabel.setText(String.valueOf(stats.getCompletedDeliveries()));

        // Calculate average
        double average = stats.getCompletedDeliveries() > 0
                ? stats.getTotalEarnings() / stats.getCompletedDeliveries()
                : 0.0;
        averageEarningLabel.setText(String.format("$%.2f", average));

        // Get period-specific earnings
        double todayEarnings = DeliveryDAO.getEarningsForPeriod(currentUserId, "TODAY");
        todayEarningsLabel.setText(String.format("$%.2f", todayEarnings));

        double weekEarnings = DeliveryDAO.getEarningsForPeriod(currentUserId, "WEEK");
        weekEarningsLabel.setText(String.format("$%.2f", weekEarnings));

        double monthEarnings = DeliveryDAO.getEarningsForPeriod(currentUserId, "MONTH");
        monthEarningsLabel.setText(String.format("$%.2f", monthEarnings));
    }

    private void loadEarningsHistory() {
        List<EarningRecord> earnings = DeliveryDAO.getEarningsHistory(currentUserId);
        allEarnings.setAll(earnings);
        applyFilter();
    }

    private void loadChartData() {
        if (earningsChart != null) {
            List<DailyEarning> dailyEarnings = DeliveryDAO.getDailyEarnings(currentUserId, 7);

            XYChart.Series<String, Number> series = new XYChart.Series<>();
            series.setName("Daily Earnings");

            for (DailyEarning daily : dailyEarnings) {
                series.getData().add(new XYChart.Data<>(daily.getDate(), daily.getTotalAmount()));
            }

            earningsChart.getData().clear();
            earningsChart.getData().add(series);
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

    private void showAlert(String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
