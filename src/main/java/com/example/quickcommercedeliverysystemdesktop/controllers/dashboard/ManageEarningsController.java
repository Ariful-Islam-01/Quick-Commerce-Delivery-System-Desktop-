package com.example.quickcommercedeliverysystemdesktop.controllers.dashboard;

import com.example.quickcommercedeliverysystemdesktop.database.DeliveryDAO;
import com.example.quickcommercedeliverysystemdesktop.database.DeliveryDAO.AdminEarningRecord;
import com.example.quickcommercedeliverysystemdesktop.database.DeliveryDAO.DeliveryPersonSummary;
import com.example.quickcommercedeliverysystemdesktop.utils.ErrorHandler;
import com.example.quickcommercedeliverysystemdesktop.utils.ValidationUtil;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.FileChooser;

import java.io.File;
import java.io.FileWriter;
import java.time.LocalDate;
import java.util.List;

/**
 * Manage Earnings Controller - Day 13 & 14
 * Admin panel for viewing and managing system-wide earnings
 * Enhanced with better error handling and validation
 */
public class ManageEarningsController {

    @FXML private ComboBox<DeliveryPersonSummary> deliveryPersonComboBox;
    @FXML private DatePicker fromDatePicker;
    @FXML private DatePicker toDatePicker;
    @FXML private TableView<AdminEarningRecord> earningsTable;
    @FXML private TableColumn<AdminEarningRecord, Integer> earningIdColumn;
    @FXML private TableColumn<AdminEarningRecord, Integer> orderIdColumn;
    @FXML private TableColumn<AdminEarningRecord, String> deliveryPersonColumn;
    @FXML private TableColumn<AdminEarningRecord, String> customerColumn;
    @FXML private TableColumn<AdminEarningRecord, String> productColumn;
    @FXML private TableColumn<AdminEarningRecord, String> amountColumn;
    @FXML private TableColumn<AdminEarningRecord, String> dateColumn;
    @FXML private Label totalEarningsLabel;
    @FXML private Label earningsCountLabel;

    private ObservableList<AdminEarningRecord> allEarnings;

    @FXML
    public void initialize() {
        allEarnings = FXCollections.observableArrayList();

        try {
            setupTable();
            loadDeliveryPersons();
            loadAllEarnings();
            ErrorHandler.logInfo("Manage Earnings page initialized successfully");
        } catch (Exception e) {
            ErrorHandler.handleException(e, "Failed to initialize Manage Earnings page");
        }
    }

    private void setupTable() {
        // Set up column value factories
        earningIdColumn.setCellValueFactory(cellData ->
            new javafx.beans.property.SimpleIntegerProperty(cellData.getValue().getEarningId()).asObject()
        );
        orderIdColumn.setCellValueFactory(cellData ->
            new javafx.beans.property.SimpleIntegerProperty(cellData.getValue().getOrderId()).asObject()
        );
        deliveryPersonColumn.setCellValueFactory(cellData ->
            new SimpleStringProperty(cellData.getValue().getDeliveryPersonName())
        );
        customerColumn.setCellValueFactory(cellData ->
            new SimpleStringProperty(cellData.getValue().getCustomerName())
        );
        productColumn.setCellValueFactory(cellData ->
            new SimpleStringProperty(cellData.getValue().getProductName())
        );
        amountColumn.setCellValueFactory(cellData ->
            new SimpleStringProperty(cellData.getValue().getFormattedAmount())
        );
        dateColumn.setCellValueFactory(cellData ->
            new SimpleStringProperty(cellData.getValue().getFormattedDateTime())
        );

        earningsTable.setItems(allEarnings);

        // Set placeholder for empty table
        earningsTable.setPlaceholder(createEmptyStatePlaceholder());
    }

    private Label createEmptyStatePlaceholder() {
        Label placeholder = new Label("📊 No earnings found\n\nEarnings will appear here once deliveries are completed.");
        placeholder.setStyle("-fx-text-fill: #95a5a6; -fx-font-size: 14px; -fx-padding: 20px;");
        return placeholder;
    }

    private void loadDeliveryPersons() {
        try {
            List<DeliveryPersonSummary> persons = DeliveryDAO.getDeliveryPersonsWithEarnings();

            // Add "All" option at the beginning
            DeliveryPersonSummary allOption = new DeliveryPersonSummary(0, "All Delivery Persons", 0, 0.0);
            persons.add(0, allOption);

            deliveryPersonComboBox.setItems(FXCollections.observableArrayList(persons));
            deliveryPersonComboBox.setValue(allOption);
        } catch (Exception e) {
            ErrorHandler.handleDatabaseException(e, "loading delivery persons");
            // Set default value even if loading fails
            DeliveryPersonSummary defaultOption = new DeliveryPersonSummary(0, "All Delivery Persons", 0, 0.0);
            deliveryPersonComboBox.setItems(FXCollections.observableArrayList(defaultOption));
            deliveryPersonComboBox.setValue(defaultOption);
        }
    }

    private void loadAllEarnings() {
        try {
            List<AdminEarningRecord> earnings = DeliveryDAO.getAllEarningsWithDetails();
            allEarnings.setAll(earnings);
            updateStatistics();

            if (earnings.isEmpty()) {
                ErrorHandler.logInfo("No earnings found in the system");
            } else {
                ErrorHandler.logInfo("Loaded " + earnings.size() + " earning records");
            }
        } catch (Exception e) {
            ErrorHandler.handleDatabaseException(e, "loading earnings");
            allEarnings.clear();
            updateStatistics();
        }
    }

    private void updateStatistics() {
        try {
            double total = allEarnings.stream()
                .mapToDouble(AdminEarningRecord::getAmount)
                .sum();

            totalEarningsLabel.setText(String.format("$%.2f", total));
            earningsCountLabel.setText(allEarnings.size() + " transaction" + (allEarnings.size() != 1 ? "s" : ""));
        } catch (Exception e) {
            ErrorHandler.logError(e);
            totalEarningsLabel.setText("$0.00");
            earningsCountLabel.setText("0 transactions");
        }
    }

    @FXML
    private void handleApplyFilters() {
        try {
            DeliveryPersonSummary selectedPerson = deliveryPersonComboBox.getValue();
            LocalDate fromDate = fromDatePicker.getValue();
            LocalDate toDate = toDatePicker.getValue();

            // Validate date range
            if (fromDate != null && toDate != null) {
                if (fromDate.isAfter(toDate)) {
                    ValidationUtil.showAlert("Invalid Date Range",
                        "From date must be before or equal to To date",
                        Alert.AlertType.WARNING);
                    return;
                }

                // Check if date range is too far in the future
                if (fromDate.isAfter(LocalDate.now())) {
                    ValidationUtil.showAlert("Invalid Date Range",
                        "From date cannot be in the future",
                        Alert.AlertType.WARNING);
                    return;
                }
            }

            List<AdminEarningRecord> filteredEarnings;

            // Apply filters
            if (fromDate != null && toDate != null) {
                if (selectedPerson != null && selectedPerson.getUserId() > 0) {
                    // Filter by both person and date
                    filteredEarnings = DeliveryDAO.getEarningsByDateRange(
                        fromDate.toString(),
                        toDate.toString()
                    );
                    int personId = selectedPerson.getUserId();
                    filteredEarnings = filteredEarnings.stream()
                        .filter(e -> e.getDeliveryPersonId() == personId)
                        .toList();
                } else {
                    // Filter by date only
                    filteredEarnings = DeliveryDAO.getEarningsByDateRange(
                        fromDate.toString(),
                        toDate.toString()
                    );
                }
            } else if (selectedPerson != null && selectedPerson.getUserId() > 0) {
                // Filter by person only
                filteredEarnings = DeliveryDAO.getEarningsByDeliveryPerson(selectedPerson.getUserId());
            } else {
                // No filter - show all
                filteredEarnings = DeliveryDAO.getAllEarningsWithDetails();
            }

            allEarnings.setAll(filteredEarnings);
            updateStatistics();

            ErrorHandler.logInfo("Applied filters: " + filteredEarnings.size() + " records found");

        } catch (Exception e) {
            ErrorHandler.handleDatabaseException(e, "applying filters");
        }
    }

    @FXML
    private void handleClearFilters() {
        try {
            deliveryPersonComboBox.setValue(deliveryPersonComboBox.getItems().get(0));
            fromDatePicker.setValue(null);
            toDatePicker.setValue(null);
            loadAllEarnings();
            ErrorHandler.logInfo("Filters cleared");
        } catch (Exception e) {
            ErrorHandler.handleException(e, "Failed to clear filters");
        }
    }

    @FXML
    private void handleExportCSV() {
        if (allEarnings.isEmpty()) {
            ValidationUtil.showAlert("No Data",
                "There are no earnings to export.\n\nComplete some deliveries first to generate earnings.",
                Alert.AlertType.WARNING);
            return;
        }

        try {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Export Earnings Report");
            fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("CSV Files", "*.csv")
            );

            String timestamp = LocalDate.now().toString();
            fileChooser.setInitialFileName("earnings_report_" + timestamp + ".csv");

            File file = fileChooser.showSaveDialog(earningsTable.getScene().getWindow());

            if (file != null) {
                exportToCSV(file);
            }
        } catch (Exception e) {
            ErrorHandler.handleFileException(e, "exporting earnings to CSV");
        }
    }

    private void exportToCSV(File file) {
        try (FileWriter writer = new FileWriter(file)) {
            // Write header
            writer.write("Earning ID,Order ID,Delivery Person,Customer,Product,Amount,Date\n");

            // Write data
            for (AdminEarningRecord record : allEarnings) {
                writer.write(String.format("%d,%d,\"%s\",\"%s\",\"%s\",%.2f,%s\n",
                    record.getEarningId(),
                    record.getOrderId(),
                    escapeCSV(record.getDeliveryPersonName()),
                    escapeCSV(record.getCustomerName()),
                    escapeCSV(record.getProductName()),
                    record.getAmount(),
                    record.getFormattedDateTime()
                ));
            }

            // Write summary
            double total = allEarnings.stream()
                .mapToDouble(AdminEarningRecord::getAmount)
                .sum();
            writer.write(String.format("\nTotal:,,,,,%.2f,\n", total));
            writer.write(String.format("Transactions:,,,,,%d,\n", allEarnings.size()));
            writer.write(String.format("Export Date:,,,,,,%s\n", LocalDate.now()));

            ValidationUtil.showAlert("Export Successful",
                "Earnings report exported successfully!\n\n" +
                "Location: " + file.getAbsolutePath() + "\n" +
                "Records: " + allEarnings.size(),
                Alert.AlertType.INFORMATION);

            ErrorHandler.logInfo("Exported " + allEarnings.size() + " earnings to CSV: " + file.getName());

        } catch (Exception e) {
            ErrorHandler.handleFileException(e, "writing CSV file");
        }
    }

    /**
     * Escape special characters in CSV fields
     */
    private String escapeCSV(String value) {
        if (value == null) {
            return "";
        }
        // Escape quotes and wrap in quotes if contains comma
        String escaped = value.replace("\"", "\"\"");
        if (escaped.contains(",")) {
            return "\"" + escaped + "\"";
        }
        return escaped;
    }

    @FXML
    private void handleRefresh() {
        try {
            handleClearFilters();
            ErrorHandler.logInfo("Earnings data refreshed");
        } catch (Exception e) {
            ErrorHandler.handleException(e, "Failed to refresh data");
        }
    }

    private void showAlert(String title, String message, Alert.AlertType type) {
        ValidationUtil.showAlert(title, message, type);
    }
}

