package com.example.quickcommercedeliverysystemdesktop.controllers.dashboard;

import com.example.quickcommercedeliverysystemdesktop.database.DeliveryDAO;
import com.example.quickcommercedeliverysystemdesktop.database.DeliveryDAO.AdminEarningRecord;
import com.example.quickcommercedeliverysystemdesktop.database.DeliveryDAO.DeliveryPersonSummary;
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
 * Manage Earnings Controller - Day 13
 * Admin panel for viewing and managing system-wide earnings
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

        setupTable();
        loadDeliveryPersons();
        loadAllEarnings();
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
    }

    private void loadDeliveryPersons() {
        List<DeliveryPersonSummary> persons = DeliveryDAO.getDeliveryPersonsWithEarnings();

        // Add "All" option at the beginning
        DeliveryPersonSummary allOption = new DeliveryPersonSummary(0, "All Delivery Persons", 0, 0.0);
        persons.add(0, allOption);

        deliveryPersonComboBox.setItems(FXCollections.observableArrayList(persons));
        deliveryPersonComboBox.setValue(allOption);
    }

    private void loadAllEarnings() {
        List<AdminEarningRecord> earnings = DeliveryDAO.getAllEarningsWithDetails();
        allEarnings.setAll(earnings);
        updateStatistics();
    }

    private void updateStatistics() {
        double total = allEarnings.stream()
            .mapToDouble(AdminEarningRecord::getAmount)
            .sum();

        totalEarningsLabel.setText(String.format("$%.2f", total));
        earningsCountLabel.setText(allEarnings.size() + " transactions");
    }

    @FXML
    private void handleApplyFilters() {
        DeliveryPersonSummary selectedPerson = deliveryPersonComboBox.getValue();
        LocalDate fromDate = fromDatePicker.getValue();
        LocalDate toDate = toDatePicker.getValue();

        List<AdminEarningRecord> filteredEarnings = null;

        // Apply filters
        if (fromDate != null && toDate != null) {
            // Date range filter
            if (fromDate.isAfter(toDate)) {
                showAlert("Invalid Range", "From date must be before To date", Alert.AlertType.WARNING);
                return;
            }

            if (selectedPerson != null && selectedPerson.getUserId() > 0) {
                // Filter by both person and date - need to do this manually
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
    }

    @FXML
    private void handleClearFilters() {
        deliveryPersonComboBox.setValue(deliveryPersonComboBox.getItems().get(0));
        fromDatePicker.setValue(null);
        toDatePicker.setValue(null);
        loadAllEarnings();
    }

    @FXML
    private void handleExportCSV() {
        if (allEarnings.isEmpty()) {
            showAlert("No Data", "There are no earnings to export", Alert.AlertType.WARNING);
            return;
        }

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Export Earnings Report");
        fileChooser.getExtensionFilters().add(
            new FileChooser.ExtensionFilter("CSV Files", "*.csv")
        );
        fileChooser.setInitialFileName("earnings_report_" + LocalDate.now() + ".csv");

        File file = fileChooser.showSaveDialog(earningsTable.getScene().getWindow());

        if (file != null) {
            try (FileWriter writer = new FileWriter(file)) {
                // Write header
                writer.write("Earning ID,Order ID,Delivery Person,Customer,Product,Amount,Date\n");

                // Write data
                for (AdminEarningRecord record : allEarnings) {
                    writer.write(String.format("%d,%d,\"%s\",\"%s\",\"%s\",%.2f,%s\n",
                        record.getEarningId(),
                        record.getOrderId(),
                        record.getDeliveryPersonName(),
                        record.getCustomerName(),
                        record.getProductName(),
                        record.getAmount(),
                        record.getFormattedDateTime()
                    ));
                }

                // Write total
                double total = allEarnings.stream()
                    .mapToDouble(AdminEarningRecord::getAmount)
                    .sum();
                writer.write(String.format("\nTotal:,,,,,%.2f,\n", total));
                writer.write(String.format("Transactions:,,,,,%d,\n", allEarnings.size()));

                showAlert("Success",
                    "Earnings report exported successfully to:\n" + file.getAbsolutePath(),
                    Alert.AlertType.INFORMATION);

            } catch (Exception e) {
                showAlert("Error",
                    "Failed to export earnings: " + e.getMessage(),
                    Alert.AlertType.ERROR);
                e.printStackTrace();
            }
        }
    }

    @FXML
    private void handleRefresh() {
        handleClearFilters();
    }

    private void showAlert(String title, String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}

