package com.example.quickcommercedeliverysystemdesktop.controllers.dashboard;

import com.example.quickcommercedeliverysystemdesktop.database.OrderDAO;
import com.example.quickcommercedeliverysystemdesktop.models.Order;
import com.example.quickcommercedeliverysystemdesktop.utils.ErrorHandler;
import com.example.quickcommercedeliverysystemdesktop.utils.UserSession;
import com.example.quickcommercedeliverysystemdesktop.utils.ValidationUtil;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.FileChooser;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class CreateOrderController {

    @FXML private VBox orderCardsContainer;
    @FXML private Label messageLabel;
    @FXML private Label orderCountLabel;
    @FXML private Label totalOrdersLabel;
    @FXML private Label totalFeeLabel;

    private List<OrderCard> orderCards = new ArrayList<>();
    private int orderIdCounter = 1;

    // Category options with descriptions
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

    @FXML
    public void initialize() {
        ValidationUtil.clearMessage(messageLabel);
        // Add first order card by default
        handleAddNewOrder();
    }

    @FXML
    private void handleAddNewOrder() {
        OrderCard newCard = new OrderCard(orderIdCounter++);
        orderCards.add(newCard);
        orderCardsContainer.getChildren().add(newCard.getCardView());
        updateSummary();
        ValidationUtil.clearMessage(messageLabel);
    }

    @FXML
    private void handleClearAll() {
        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Clear All Orders");
        confirmAlert.setHeaderText("Are you sure?");
        confirmAlert.setContentText("This will remove all orders from the list.");

        if (confirmAlert.showAndWait().get() == ButtonType.OK) {
            orderCards.clear();
            orderCardsContainer.getChildren().clear();
            orderIdCounter = 1;
            updateSummary();
            ValidationUtil.clearMessage(messageLabel);

            // Add one empty card
            handleAddNewOrder();
        }
    }

    @FXML
    private void handleSubmitAllOrders() {
        ValidationUtil.clearMessage(messageLabel);

        if (orderCards.isEmpty()) {
            ValidationUtil.showError(messageLabel, "No orders to submit!");
            return;
        }

        // Validate all order cards
        List<Order> validOrders = new ArrayList<>();
        boolean hasErrors = false;

        for (OrderCard card : orderCards) {
            if (!card.validateCard()) {
                hasErrors = true;
                continue;
            }

            Order order = card.createOrder();
            if (order != null) {
                validOrders.add(order);
            }
        }

        if (hasErrors) {
            ValidationUtil.showError(messageLabel, "Please fix validation errors in the order cards above.");
            return;
        }

        if (validOrders.isEmpty()) {
            ValidationUtil.showError(messageLabel, "No valid orders to submit!");
            return;
        }

        // Confirm submission
        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Submit Orders");
        confirmAlert.setHeaderText("Submit " + validOrders.size() + " order(s)?");
        confirmAlert.setContentText("Total Fee: ৳" + String.format("%.2f", calculateTotalFee()));

        if (confirmAlert.showAndWait().get() != ButtonType.OK) {
            return;
        }

        // Submit all valid orders
        int successCount = 0;
        int failCount = 0;

        try {
            for (Order order : validOrders) {
                boolean success = OrderDAO.createOrder(order);
                if (success) {
                    successCount++;
                } else {
                    failCount++;
                }
            }

            if (successCount > 0) {
                ValidationUtil.showSuccess(messageLabel,
                    "✓ Successfully submitted " + successCount + " order(s)!" +
                    (failCount > 0 ? " (" + failCount + " failed)" : ""));

                ErrorHandler.logInfo("Submitted " + successCount + " orders successfully");

                // Clear all and start fresh
                orderCards.clear();
                orderCardsContainer.getChildren().clear();
                orderIdCounter = 1;
                handleAddNewOrder();
                updateSummary();
            } else {
                ValidationUtil.showError(messageLabel, "Failed to submit orders. Please try again.");
            }
        } catch (Exception e) {
            ErrorHandler.handleDatabaseException(e, "submitting orders");
            ValidationUtil.showError(messageLabel, "Error submitting orders. Please try again.");
        }
    }

    private void updateSummary() {
        int count = orderCards.size();
        double totalFee = calculateTotalFee();

        orderCountLabel.setText(count + " Order" + (count != 1 ? "s" : ""));
        totalOrdersLabel.setText(String.valueOf(count));
        totalFeeLabel.setText("৳" + String.format("%.2f", totalFee));
    }

    private double calculateTotalFee() {
        double total = 0.0;
        for (OrderCard card : orderCards) {
            try {
                String feeText = card.deliveryFeeField.getText().trim();
                if (!feeText.isEmpty()) {
                    total += Double.parseDouble(feeText);
                }
            } catch (NumberFormatException e) {
                // Skip invalid fees
            }
        }
        return total;
    }

    private void removeOrderCard(OrderCard card) {
        orderCards.remove(card);
        orderCardsContainer.getChildren().remove(card.getCardView());
        updateSummary();

        // Keep at least one card
        if (orderCards.isEmpty()) {
            handleAddNewOrder();
        }
    }

    // Inner class representing a single order card
    private class OrderCard {
        private int cardId;
        private VBox cardView;
        private ComboBox<String> categoryComboBox;
        private ComboBox<String> productNameComboBox; // Combined field for product selection/entry
        private TextArea descriptionArea;
        private TextField deliveryLocationField;
        private DatePicker deliveryDatePicker;
        private ComboBox<String> timeFromComboBox;
        private ComboBox<String> timeToComboBox;
        private TextField deliveryFeeField;
        private TextArea notesArea;
        private Label imageLabel;
        private String selectedImagePath = null;
        private Label cardMessageLabel;
        private VBox detailsContainer;
        private boolean isExpanded = true;
        private Button expandCollapseBtn;

        // Time options for combo boxes
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

        public OrderCard(int cardId) {
            this.cardId = cardId;
            buildCard();
        }

        private void buildCard() {
            // Main card container
            cardView = new VBox(15);
            cardView.getStyleClass().add("order-card");
            cardView.setPadding(new Insets(20));

            // Card Header
            HBox header = new HBox(10);
            header.setAlignment(Pos.CENTER_LEFT);

            Label cardTitle = new Label("Order #" + cardId);
            cardTitle.getStyleClass().add("order-card-title");

            Region spacer = new Region();
            HBox.setHgrow(spacer, Priority.ALWAYS);

            // Expand/Collapse Button
            expandCollapseBtn = new Button("▼");
            expandCollapseBtn.getStyleClass().add("expand-button");
            expandCollapseBtn.setOnAction(e -> toggleExpand());

            Button deleteBtn = new Button("🗑");
            deleteBtn.getStyleClass().add("delete-order-button");
            deleteBtn.setOnAction(e -> {
                if (orderCards.size() > 1) {
                    removeOrderCard(this);
                } else {
                    Alert alert = new Alert(Alert.AlertType.WARNING);
                    alert.setTitle("Cannot Delete");
                    alert.setHeaderText("At least one order is required");
                    alert.setContentText("You must have at least one order card.");
                    alert.showAndWait();
                }
            });

            header.getChildren().addAll(cardTitle, spacer, expandCollapseBtn, deleteBtn);

            // Details Container (collapsible)
            detailsContainer = new VBox(15);

            // Category Dropdown
            VBox categoryBox = new VBox(8);
            Label categoryLabel = new Label("Category *");
            categoryLabel.getStyleClass().add("form-label");

            categoryComboBox = new ComboBox<>();
            categoryComboBox.getItems().addAll(CATEGORIES);
            categoryComboBox.setValue(CATEGORIES[0]);
            categoryComboBox.getStyleClass().add("form-input");
            categoryComboBox.setMaxWidth(Double.MAX_VALUE);

            // Update product dropdown and placeholder when category changes
            categoryComboBox.setOnAction(e -> {
                updateProductDropdown();
                updateDescriptionPlaceholder();
            });

            categoryBox.getChildren().addAll(categoryLabel, categoryComboBox);

            // Product Name - Editable ComboBox (combined dropdown + text field)
            VBox productBox = new VBox(8);
            Label productLabel = new Label("Product Name *");
            productLabel.getStyleClass().add("form-label");

            productNameComboBox = new ComboBox<>();
            productNameComboBox.setEditable(true); // Allow custom entry
            productNameComboBox.setPromptText("Select a category first or type product name");
            productNameComboBox.getStyleClass().add("form-input");
            productNameComboBox.setMaxWidth(Double.MAX_VALUE);

            productBox.getChildren().addAll(productLabel, productNameComboBox);

            // Description
            VBox descBox = new VBox(8);
            Label descLabel = new Label("Description *");
            descLabel.getStyleClass().add("form-label");
            descriptionArea = new TextArea();
            descriptionArea.setPromptText("Describe the item to be delivered...");
            descriptionArea.setPrefRowCount(3);
            descriptionArea.setWrapText(true);
            descriptionArea.getStyleClass().add("form-input");
            descBox.getChildren().addAll(descLabel, descriptionArea);

            // Delivery Location
            VBox locationBox = new VBox(8);
            Label locationLabel = new Label("Delivery Location *");
            locationLabel.getStyleClass().add("form-label");
            deliveryLocationField = new TextField();
            deliveryLocationField.setPromptText("Full address (Street, City, ZIP)");
            deliveryLocationField.getStyleClass().add("form-input");
            locationBox.getChildren().addAll(locationLabel, deliveryLocationField);

            // Delivery Date
            VBox dateBox = new VBox(8);
            Label dateLabel = new Label("Delivery Date *");
            dateLabel.getStyleClass().add("form-label");
            deliveryDatePicker = new DatePicker();
            deliveryDatePicker.setPromptText("Select delivery date");
            deliveryDatePicker.getStyleClass().add("form-input");
            deliveryDatePicker.setMaxWidth(Double.MAX_VALUE);
            // Set minimum date to today
            deliveryDatePicker.setDayCellFactory(picker -> new DateCell() {
                @Override
                public void updateItem(LocalDate date, boolean empty) {
                    super.updateItem(date, empty);
                    setDisable(empty || date.isBefore(LocalDate.now()));
                }
            });
            dateBox.getChildren().addAll(dateLabel, deliveryDatePicker);

            // Time Range with ComboBoxes
            HBox timeBox = new HBox(15);

            // Time From
            VBox timeFromBox = new VBox(8);
            Label timeFromLabel = new Label("Time From *");
            timeFromLabel.getStyleClass().add("form-label");
            timeFromComboBox = new ComboBox<>();
            timeFromComboBox.getItems().addAll(TIME_OPTIONS);
            timeFromComboBox.setPromptText("Select start time");
            timeFromComboBox.getStyleClass().add("form-input");
            timeFromComboBox.setMaxWidth(Double.MAX_VALUE);
            timeFromBox.getChildren().addAll(timeFromLabel, timeFromComboBox);
            HBox.setHgrow(timeFromBox, Priority.ALWAYS);

            // Time To
            VBox timeToBox = new VBox(8);
            Label timeToLabel = new Label("Time To *");
            timeToLabel.getStyleClass().add("form-label");
            timeToComboBox = new ComboBox<>();
            timeToComboBox.getItems().addAll(TIME_OPTIONS);
            timeToComboBox.setPromptText("Select end time");
            timeToComboBox.getStyleClass().add("form-input");
            timeToComboBox.setMaxWidth(Double.MAX_VALUE);
            timeToBox.getChildren().addAll(timeToLabel, timeToComboBox);
            HBox.setHgrow(timeToBox, Priority.ALWAYS);

            timeBox.getChildren().addAll(timeFromBox, timeToBox);

            // Delivery Fee (changed to BDT)
            VBox feeBox = new VBox(8);
            Label feeLabel = new Label("Delivery Fee (BDT ৳) *");
            feeLabel.getStyleClass().add("form-label");
            deliveryFeeField = new TextField();
            deliveryFeeField.setPromptText("e.g., 50.00");
            deliveryFeeField.getStyleClass().add("form-input");

            // Update summary on fee change
            deliveryFeeField.textProperty().addListener((obs, old, newVal) -> updateSummary());

            feeBox.getChildren().addAll(feeLabel, deliveryFeeField);

            // Notes
            VBox notesBox = new VBox(8);
            Label notesLabel = new Label("Notes for Delivery Person (Optional)");
            notesLabel.getStyleClass().add("form-label");
            notesArea = new TextArea();
            notesArea.setPromptText("Any special instructions...");
            notesArea.setPrefRowCount(2);
            notesArea.setWrapText(true);
            notesArea.getStyleClass().add("form-input");
            notesBox.getChildren().addAll(notesLabel, notesArea);

            // Product Photo
            VBox photoBox = new VBox(8);
            Label photoLabel = new Label("Product Photo (Optional)");
            photoLabel.getStyleClass().add("form-label");
            HBox photoButtonBox = new HBox(10);
            photoButtonBox.setAlignment(Pos.CENTER_LEFT);

            Button chooseImageBtn = new Button("Choose Image");
            chooseImageBtn.getStyleClass().add("secondary-button");
            chooseImageBtn.setOnAction(e -> handleChooseImage());

            imageLabel = new Label("No image selected");
            imageLabel.getStyleClass().add("image-label");

            photoButtonBox.getChildren().addAll(chooseImageBtn, imageLabel);
            photoBox.getChildren().addAll(photoLabel, photoButtonBox);

            // Card Message Label
            cardMessageLabel = new Label();
            cardMessageLabel.getStyleClass().add("card-message-label");
            cardMessageLabel.setWrapText(true);
            cardMessageLabel.setManaged(false);
            cardMessageLabel.setVisible(false);

            // Add all to details container
            detailsContainer.getChildren().addAll(
                categoryBox,
                productBox,
                descBox,
                locationBox,
                dateBox,
                timeBox,
                feeBox,
                notesBox,
                photoBox,
                cardMessageLabel
            );

            // Add all to card
            cardView.getChildren().addAll(header, detailsContainer);
        }

        private void toggleExpand() {
            isExpanded = !isExpanded;
            detailsContainer.setVisible(isExpanded);
            detailsContainer.setManaged(isExpanded);
            expandCollapseBtn.setText(isExpanded ? "▼" : "▶");
        }

        private void updateProductDropdown() {
            String category = categoryComboBox.getValue();

            if (category.equals("Select Category")) {
                // Clear and disable when no category selected
                productNameComboBox.getItems().clear();
                productNameComboBox.setValue(null);
                productNameComboBox.setPromptText("Select a category first or type product name");
            } else {
                // Load products for selected category
                String[] products = CATEGORY_PRODUCTS.get(category);
                productNameComboBox.getItems().clear();

                if (products != null) {
                    // Add products but skip "Select Product" placeholder
                    for (int i = 1; i < products.length; i++) {
                        productNameComboBox.getItems().add(products[i]);
                    }
                }

                productNameComboBox.setValue(null);
                productNameComboBox.setPromptText("Select or type product name");
            }
        }

        private void updateDescriptionPlaceholder() {
            String category = categoryComboBox.getValue();
            String placeholder = "Describe the item to be delivered...";

            switch (category) {
                case "Groceries":
                    placeholder = "e.g., Fresh vegetables, dairy products, pantry items...";
                    break;
                case "Electronics":
                    placeholder = "e.g., Laptop - 15 inch, Brand new in box, Model: XYZ...";
                    break;
                case "Pharma":
                    placeholder = "e.g., Prescription medicines, vitamins, medical supplies...";
                    break;
                case "Food & Beverages":
                    placeholder = "e.g., Restaurant order, packaged food, beverages...";
                    break;
                case "Clothing":
                    placeholder = "e.g., Shirts, pants, footwear - Size and color details...";
                    break;
                case "Books & Stationery":
                    placeholder = "e.g., Textbooks, notebooks, office supplies...";
                    break;
                case "Home & Garden":
                    placeholder = "e.g., Furniture parts, garden tools, home decor...";
                    break;
                case "Sports & Outdoors":
                    placeholder = "e.g., Sports equipment, camping gear, outdoor accessories...";
                    break;
                default:
                    placeholder = "Describe the item to be delivered...";
            }

            descriptionArea.setPromptText(placeholder);
        }

        private void handleChooseImage() {
            try {
                FileChooser fileChooser = new FileChooser();
                fileChooser.setTitle("Select Product Image");
                fileChooser.getExtensionFilters().addAll(
                    new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg")
                );

                File selectedFile = fileChooser.showOpenDialog(cardView.getScene().getWindow());
                if (selectedFile != null) {
                    // Validate file size (max 5MB)
                    if (selectedFile.length() > 5 * 1024 * 1024) {
                        showCardError("Image file too large. Maximum size is 5MB.");
                        return;
                    }

                    // Create assets/products directory if not exists
                    Path productsDir = Paths.get("src/main/resources/com/example/quickcommercedeliverysystemdesktop/assets/products");
                    Files.createDirectories(productsDir);

                    // Copy file with unique name
                    String fileName = System.currentTimeMillis() + "_" + selectedFile.getName();
                    Path destPath = productsDir.resolve(fileName);
                    Files.copy(selectedFile.toPath(), destPath, StandardCopyOption.REPLACE_EXISTING);

                    selectedImagePath = fileName;
                    imageLabel.setText("✓ " + selectedFile.getName());
                    imageLabel.setStyle("-fx-text-fill: #27ae60; -fx-font-weight: bold;");
                    ErrorHandler.logInfo("Image uploaded: " + fileName);
                }
            } catch (Exception e) {
                ErrorHandler.handleFileException(e, "uploading image");
                showCardError("Failed to upload image.");
            }
        }

        public boolean validateCard() {
            clearCardMessage();
            clearFieldStyles();

            String category = categoryComboBox.getValue();
            String productName = productNameComboBox.getEditor().getText().trim();
            String description = descriptionArea.getText().trim();
            String location = deliveryLocationField.getText().trim();
            LocalDate deliveryDate = deliveryDatePicker.getValue();
            String timeFrom = timeFromComboBox.getValue();
            String timeTo = timeToComboBox.getValue();
            String feeStr = deliveryFeeField.getText().trim();

            // Validate category
            if (category.equals("Select Category")) {
                categoryComboBox.setStyle("-fx-border-color: #e74c3c; -fx-border-width: 2px;");
                showCardError("Please select a category");
                return false;
            }

            // Validate product name
            if (productName.isEmpty()) {
                productNameComboBox.setStyle("-fx-border-color: #e74c3c; -fx-border-width: 2px;");
                showCardError("Product name is required");
                return false;
            }

            // Validate description
            if (description.isEmpty()) {
                descriptionArea.setStyle("-fx-border-color: #e74c3c; -fx-border-width: 2px;");
                showCardError("Description is required");
                return false;
            }

            // Validate location
            if (location.isEmpty()) {
                deliveryLocationField.setStyle("-fx-border-color: #e74c3c; -fx-border-width: 2px;");
                showCardError("Delivery location is required");
                return false;
            }

            // Validate delivery date
            if (deliveryDate == null) {
                deliveryDatePicker.setStyle("-fx-border-color: #e74c3c; -fx-border-width: 2px;");
                showCardError("Delivery date is required");
                return false;
            }

            // Validate time from
            if (timeFrom == null || timeFrom.isEmpty()) {
                timeFromComboBox.setStyle("-fx-border-color: #e74c3c; -fx-border-width: 2px;");
                showCardError("Start time is required");
                return false;
            }

            // Validate time to
            if (timeTo == null || timeTo.isEmpty()) {
                timeToComboBox.setStyle("-fx-border-color: #e74c3c; -fx-border-width: 2px;");
                showCardError("End time is required");
                return false;
            }

            // Validate delivery fee
            if (!ValidationUtil.isPositiveNumber(feeStr)) {
                deliveryFeeField.setStyle("-fx-border-color: #e74c3c; -fx-border-width: 2px;");
                showCardError("Please enter a valid positive delivery fee");
                return false;
            }

            double fee = Double.parseDouble(feeStr);
            if (!ValidationUtil.isInRange(fee, 1.00, 10000.00)) {
                deliveryFeeField.setStyle("-fx-border-color: #e74c3c; -fx-border-width: 2px;");
                showCardError("Delivery fee must be between ৳1.00 and ৳10,000.00");
                return false;
            }

            return true;
        }

        public Order createOrder() {
            try {
                int userId = UserSession.getInstance().getUserId();
                String userName = UserSession.getInstance().getUserName();
                String userPhone = UserSession.getInstance().getUserPhone();

                String category = categoryComboBox.getValue();
                String productName = "[" + category + "] " + productNameComboBox.getEditor().getText().trim();
                String description = descriptionArea.getText().trim();
                String location = deliveryLocationField.getText().trim();

                // Format delivery date and time
                LocalDate deliveryDate = deliveryDatePicker.getValue();
                String timeFrom = timeFromComboBox.getValue();
                String timeTo = timeToComboBox.getValue();

                DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("MMM dd, yyyy");
                String formattedDate = deliveryDate.format(dateFormatter);
                String timeRange = formattedDate + " | " + timeFrom + " - " + timeTo;

                double fee = Double.parseDouble(deliveryFeeField.getText().trim());
                String notes = notesArea.getText().trim();

                Order order = new Order(userId, productName, description, location, timeRange,
                                       fee, notes, userName, userPhone);
                order.setProductPhoto(selectedImagePath);

                return order;
            } catch (Exception e) {
                ErrorHandler.logError(e);
                return null;
            }
        }

        private void showCardError(String message) {
            cardMessageLabel.setText("❌ " + message);
            cardMessageLabel.setStyle("-fx-text-fill: #e74c3c; -fx-background-color: #fadbd8;");
            cardMessageLabel.setManaged(true);
            cardMessageLabel.setVisible(true);
        }

        private void clearCardMessage() {
            cardMessageLabel.setText("");
            cardMessageLabel.setManaged(false);
            cardMessageLabel.setVisible(false);
        }

        private void clearFieldStyles() {
            categoryComboBox.setStyle("");
            productNameComboBox.setStyle("");
            descriptionArea.setStyle("");
            deliveryLocationField.setStyle("");
            deliveryDatePicker.setStyle("");
            timeFromComboBox.setStyle("");
            timeToComboBox.setStyle("");
            deliveryFeeField.setStyle("");
        }

        public VBox getCardView() {
            return cardView;
        }
    }
}

