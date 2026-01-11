package com.example.quickcommercedeliverysystemdesktop.controllers.dialogs;

import com.example.quickcommercedeliverysystemdesktop.database.OrderDAO;
import com.example.quickcommercedeliverysystemdesktop.models.Order;
import com.example.quickcommercedeliverysystemdesktop.utils.ValidationUtil;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.time.LocalDate;

public class EditOrderController {

    @FXML private Label orderIdLabel;
    @FXML private ComboBox<String> categoryComboBox;
    @FXML private ComboBox<String> productNameComboBox;
    @FXML private TextArea descriptionArea;
    @FXML private TextField deliveryLocationField;
    @FXML private DatePicker deliveryDatePicker;
    @FXML private ComboBox<String> timeFromComboBox;
    @FXML private ComboBox<String> timeToComboBox;
    @FXML private TextField deliveryFeeField;
    @FXML private Label messageLabel;

    private Order order;
    private boolean saved = false;

    // Category options
    private static final String[] CATEGORIES = {
        "Select Category",
        "Groceries",
        "Electronics",
        "Pharma",
        "Food & Beverages",
        "Clothing",
        "Books & Stationery",
        "Home & Garden",
        "Sports & Outdoors",
        "Other"
    };

    // Product items for each category
    private static final java.util.Map<String, String[]> CATEGORY_PRODUCTS = new java.util.HashMap<>() {{
        put("Groceries", new String[]{
            "Select Product", "Fresh Vegetables", "Fresh Fruits", "Dairy Products",
            "Bakery Items", "Meat & Fish", "Rice & Grains", "Snacks & Beverages",
            "Cooking Oil", "Spices & Condiments"
        });
        put("Electronics", new String[]{
            "Select Product", "Mobile Phone", "Laptop", "Tablet", "Smart Watch",
            "Headphones", "Speaker", "Camera", "Gaming Console", "TV", "Other Electronics"
        });
        put("Pharma", new String[]{
            "Select Product", "Prescription Medicine", "OTC Medicine", "Vitamins & Supplements",
            "First Aid Supplies", "Medical Equipment", "Baby Care Products",
            "Personal Care Items", "Health Monitors"
        });
        put("Food & Beverages", new String[]{
            "Select Product", "Restaurant Food", "Fast Food", "Coffee & Tea",
            "Soft Drinks", "Juices", "Packaged Food", "Desserts", "Baked Goods"
        });
        put("Clothing", new String[]{
            "Select Product", "Men's Clothing", "Women's Clothing", "Kids Clothing",
            "Shoes & Footwear", "Accessories", "Bags", "Watches", "Jewelry"
        });
        put("Books & Stationery", new String[]{
            "Select Product", "Textbooks", "Novels & Fiction", "Educational Books",
            "Notebooks", "Pens & Pencils", "Art Supplies", "Office Supplies"
        });
        put("Home & Garden", new String[]{
            "Select Product", "Furniture", "Home Decor", "Kitchen Items",
            "Garden Tools", "Plants & Seeds", "Cleaning Supplies", "Bed & Bath"
        });
        put("Sports & Outdoors", new String[]{
            "Select Product", "Sports Equipment", "Fitness Gear", "Camping Gear",
            "Outdoor Clothing", "Cycling Accessories", "Swimming Items"
        });
        put("Other", new String[]{
            "Select Product", "Custom Item"
        });
    }};

    // Time options
    private static final String[] TIME_OPTIONS = {
        "12:00 AM", "12:30 AM", "1:00 AM", "1:30 AM", "2:00 AM", "2:30 AM",
        "3:00 AM", "3:30 AM", "4:00 AM", "4:30 AM", "5:00 AM", "5:30 AM",
        "6:00 AM", "6:30 AM", "7:00 AM", "7:30 AM", "8:00 AM", "8:30 AM",
        "9:00 AM", "9:30 AM", "10:00 AM", "10:30 AM", "11:00 AM", "11:30 AM",
        "12:00 PM", "12:30 PM", "1:00 PM", "1:30 PM", "2:00 PM", "2:30 PM",
        "3:00 PM", "3:30 PM", "4:00 PM", "4:30 PM", "5:00 PM", "5:30 PM",
        "6:00 PM", "6:30 PM", "7:00 PM", "7:30 PM", "8:00 PM", "8:30 PM",
        "9:00 PM", "9:30 PM", "10:00 PM", "10:30 PM", "11:00 PM", "11:30 PM"
    };

    @FXML
    public void initialize() {
        // Setup categories
        categoryComboBox.getItems().addAll(CATEGORIES);
        categoryComboBox.setValue(CATEGORIES[0]);
        categoryComboBox.setOnAction(e -> updateProductDropdown());

        // Setup product name combo box
        productNameComboBox.setEditable(true);
        productNameComboBox.setPromptText("Select or type product name");

        // Setup time options
        timeFromComboBox.getItems().addAll(TIME_OPTIONS);
        timeToComboBox.getItems().addAll(TIME_OPTIONS);

        // Setup date picker to disable past dates
        deliveryDatePicker.setDayCellFactory(picker -> new DateCell() {
            @Override
            public void updateItem(LocalDate date, boolean empty) {
                super.updateItem(date, empty);
                setDisable(empty || date.isBefore(LocalDate.now()));
            }
        });

        ValidationUtil.clearMessage(messageLabel);
    }

    public void setOrder(Order order) {
        this.order = order;
        orderIdLabel.setText("Order #" + order.getOrderId());

        // Extract category from product name if it exists
        String productName = order.getProductName();
        String category = extractCategory(productName);

        if (category != null && !category.isEmpty()) {
            categoryComboBox.setValue(category);
            updateProductDropdown();
            // Remove category prefix from product name
            if (productName.contains("]")) {
                productName = productName.substring(productName.indexOf("]") + 1).trim();
            }
        } else {
            categoryComboBox.setValue("Other");
            updateProductDropdown();
        }

        productNameComboBox.setValue(productName);
        descriptionArea.setText(order.getDescription());
        deliveryLocationField.setText(order.getDeliveryLocation());

        // Parse time range
        String timeRange = order.getDeliveryTimeRange();
        String[] times = timeRange.split(" - ");
        if (times.length >= 2) {
            timeFromComboBox.setValue(times[0].trim());
            timeToComboBox.setValue(times[1].trim());
        }

        deliveryFeeField.setText(String.valueOf(order.getDeliveryFee()));

        // For date, we need to extract it from the time range or use current date
        // Since we don't store the date separately, we'll use today as default
        deliveryDatePicker.setValue(LocalDate.now());
    }

    private String extractCategory(String productName) {
        if (productName != null && productName.startsWith("[") && productName.contains("]")) {
            return productName.substring(1, productName.indexOf("]"));
        }
        return null;
    }

    private void updateProductDropdown() {
        String selectedCategory = categoryComboBox.getValue();
        productNameComboBox.getItems().clear();

        if (selectedCategory != null && !selectedCategory.equals("Select Category")) {
            String[] products = CATEGORY_PRODUCTS.get(selectedCategory);
            if (products != null) {
                productNameComboBox.getItems().addAll(products);
            }
        }
    }

    @FXML
    private void handleSave() {
        ValidationUtil.clearMessage(messageLabel);

        // Validate inputs
        if (!validateInputs()) {
            return;
        }

        // Get values
        String category = categoryComboBox.getValue();
        String productName = productNameComboBox.getValue();
        String description = descriptionArea.getText().trim();
        String location = deliveryLocationField.getText().trim();
        String timeFrom = timeFromComboBox.getValue();
        String timeTo = timeToComboBox.getValue();
        double fee;

        try {
            fee = Double.parseDouble(deliveryFeeField.getText().trim());
        } catch (NumberFormatException e) {
            ValidationUtil.showError(messageLabel, "Invalid delivery fee!");
            return;
        }

        // Build product name with category
        String fullProductName = productName;
        if (category != null && !category.equals("Select Category") && !category.equals("Other")) {
            fullProductName = "[" + category + "] " + productName;
        }

        // Update order object
        order.setProductName(fullProductName);
        order.setDescription(description);
        order.setDeliveryLocation(location);
        order.setDeliveryTimeRange(timeFrom + " - " + timeTo);
        order.setDeliveryFee(fee);

        // Save to database
        boolean success = OrderDAO.updateOrder(order);

        if (success) {
            saved = true;
            ValidationUtil.showSuccess(messageLabel, "Order updated successfully!");

            // Close dialog after a short delay
            new Thread(() -> {
                try {
                    Thread.sleep(1000);
                    javafx.application.Platform.runLater(() -> closeDialog());
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }).start();
        } else {
            ValidationUtil.showError(messageLabel, "Failed to update order. Please try again.");
        }
    }

    @FXML
    private void handleCancel() {
        closeDialog();
    }

    private boolean validateInputs() {
        String category = categoryComboBox.getValue();
        String productName = productNameComboBox.getValue();
        String description = descriptionArea.getText().trim();
        String location = deliveryLocationField.getText().trim();
        LocalDate date = deliveryDatePicker.getValue();
        String timeFrom = timeFromComboBox.getValue();
        String timeTo = timeToComboBox.getValue();
        String feeText = deliveryFeeField.getText().trim();

        if (category == null || category.equals("Select Category")) {
            ValidationUtil.showError(messageLabel, "Please select a category!");
            return false;
        }

        if (productName == null || productName.trim().isEmpty() || productName.equals("Select Product")) {
            ValidationUtil.showError(messageLabel, "Please enter or select a product name!");
            return false;
        }

        if (description.isEmpty()) {
            ValidationUtil.showError(messageLabel, "Please provide a description!");
            return false;
        }

        if (location.isEmpty()) {
            ValidationUtil.showError(messageLabel, "Please provide a delivery location!");
            return false;
        }

        if (date == null) {
            ValidationUtil.showError(messageLabel, "Please select a delivery date!");
            return false;
        }

        if (timeFrom == null || timeFrom.isEmpty()) {
            ValidationUtil.showError(messageLabel, "Please select a start time!");
            return false;
        }

        if (timeTo == null || timeTo.isEmpty()) {
            ValidationUtil.showError(messageLabel, "Please select an end time!");
            return false;
        }

        if (feeText.isEmpty()) {
            ValidationUtil.showError(messageLabel, "Please enter a delivery fee!");
            return false;
        }

        try {
            double fee = Double.parseDouble(feeText);
            if (fee <= 0) {
                ValidationUtil.showError(messageLabel, "Delivery fee must be greater than 0!");
                return false;
            }
        } catch (NumberFormatException e) {
            ValidationUtil.showError(messageLabel, "Invalid delivery fee format!");
            return false;
        }

        return true;
    }

    private void closeDialog() {
        Stage stage = (Stage) messageLabel.getScene().getWindow();
        stage.close();
    }

    public boolean isSaved() {
        return saved;
    }
}

