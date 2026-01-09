package com.example.quickcommercedeliverysystemdesktop.controllers.dialogs;

import com.example.quickcommercedeliverysystemdesktop.database.DeliveryDAO;
import com.example.quickcommercedeliverysystemdesktop.database.NotificationDAO;
import com.example.quickcommercedeliverysystemdesktop.database.OrderDAO;
import com.example.quickcommercedeliverysystemdesktop.database.RatingDAO;
import com.example.quickcommercedeliverysystemdesktop.models.Order;
import com.example.quickcommercedeliverysystemdesktop.models.Order.OrderStatus;
import com.example.quickcommercedeliverysystemdesktop.utils.UserSession;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.InputStream;

/**
 * Controller for Order Details Dialog
 */
public class OrderDetailsController {

    @FXML private Label orderIdLabel;
    @FXML private Label orderDateLabel;
    @FXML private Label statusBadge;

    @FXML private ImageView productImageView;
    @FXML private Label photoLabel;
    @FXML private Label productNameLabel;
    @FXML private TextArea descriptionArea;

    @FXML private Label deliveryLocationLabel;
    @FXML private Label timeRangeLabel;
    @FXML private Label deliveryFeeLabel;
    @FXML private Label notesLabelTitle;
    @FXML private TextArea notesArea;

    @FXML private VBox customerInfoCard;
    @FXML private Label customerNameLabel;
    @FXML private Label customerPhoneLabel;

    @FXML private VBox partnerInfoCard;
    @FXML private Label partnerNameLabel;
    @FXML private Label partnerPhoneLabel;
    @FXML private Label deliveryStatusLabel;

    @FXML private VBox timelineContainer;

    // Action Buttons
    @FXML private Button cancelOrderBtn;
    @FXML private Button contactPartnerBtn;
    @FXML private Button rateDeliveryBtn;
    @FXML private Button acceptOrderBtn;
    @FXML private Button pickUpBtn;
    @FXML private Button onTheWayBtn;
    @FXML private Button completeBtn;
    @FXML private Button contactCustomerBtn;

    private Order order;
    private String userRole; // "CUSTOMER" or "DELIVERY_PARTNER"
    private int currentUserId;

    @FXML
    public void initialize() {
        currentUserId = UserSession.getInstance().getUserId();
    }

    /**
     * Set the order to display
     */
    public void setOrder(Order order) {
        this.order = order;
        loadOrderData();
    }

    /**
     * Set user role to show appropriate UI elements
     */
    public void setUserRole(String role) {
        this.userRole = role;
        configureUIForRole();
    }

    /**
     * Load order data into UI
     */
    private void loadOrderData() {
        if (order == null) return;

        // Header
        orderIdLabel.setText("Order #" + order.getOrderId());
        orderDateLabel.setText("Created on: " + order.getOrderDate().toLocalDate() + " at " +
                              order.getOrderDate().toLocalTime().toString().substring(0, 5));

        // Status Badge
        updateStatusBadge(order.getStatus());

        // Product Info
        productNameLabel.setText(order.getProductName());
        descriptionArea.setText(order.getDescription() != null ? order.getDescription() : "No description provided");

        // Load product photo
        loadProductPhoto();

        // Delivery Details
        deliveryLocationLabel.setText(order.getDeliveryLocation());
        timeRangeLabel.setText(order.getDeliveryTimeRange());
        deliveryFeeLabel.setText(order.getFormattedDeliveryFee());

        // Special Notes
        if (order.getNotesForDelivery() != null && !order.getNotesForDelivery().isEmpty()) {
            notesLabelTitle.setVisible(true);
            notesLabelTitle.setManaged(true);
            notesArea.setVisible(true);
            notesArea.setManaged(true);
            notesArea.setText(order.getNotesForDelivery());
        }

        // Customer Info (for delivery partners)
        if (order.getCustomerName() != null) {
            customerNameLabel.setText(order.getCustomerName());
            customerPhoneLabel.setText(order.getCustomerPhone() != null ? order.getCustomerPhone() : "Not available");
        }

        // Delivery Partner Info (for customers)
        if (order.getAcceptedByUserId() != null) {
            loadDeliveryPartnerInfo(order.getAcceptedByUserId());
        }

        // Build Timeline
        buildStatusTimeline();
    }

    /**
     * Load product photo
     */
    private void loadProductPhoto() {
        try {
            if (order.getProductPhoto() != null && !order.getProductPhoto().isEmpty()) {
                String photoPath = "/com/example/quickcommercedeliverysystemdesktop/assets/products/" +
                                  order.getProductPhoto();
                InputStream imageStream = getClass().getResourceAsStream(photoPath);

                if (imageStream != null) {
                    Image image = new Image(imageStream);
                    productImageView.setImage(image);
                    photoLabel.setText("");
                } else {
                    loadDefaultPhoto();
                }
            } else {
                loadDefaultPhoto();
            }
        } catch (Exception e) {
            loadDefaultPhoto();
        }
    }

    /**
     * Load default placeholder photo
     */
    private void loadDefaultPhoto() {
        try {
            InputStream defaultStream = getClass().getResourceAsStream(
                "/com/example/quickcommercedeliverysystemdesktop/assets/default-avatar.png");
            if (defaultStream != null) {
                Image defaultImage = new Image(defaultStream);
                productImageView.setImage(defaultImage);
            }
            photoLabel.setText("No photo available");
        } catch (Exception e) {
            photoLabel.setText("No photo");
        }
    }

    /**
     * Load delivery partner information from database
     */
    private void loadDeliveryPartnerInfo(int deliveryPartnerId) {
        try {
            com.example.quickcommercedeliverysystemdesktop.models.User deliveryPartner =
                com.example.quickcommercedeliverysystemdesktop.database.UserDAO.getUserById(deliveryPartnerId);

            if (deliveryPartner != null) {
                partnerNameLabel.setText(deliveryPartner.getName());
                partnerPhoneLabel.setText(deliveryPartner.getPhone() != null ? deliveryPartner.getPhone() : "Not available");

                // Set delivery status based on order status
                String statusText = switch (order.getStatus()) {
                    case ACCEPTED -> "Accepted - Preparing for pickup";
                    case PICKED_UP -> "Picked up - On the way to you";
                    case ON_THE_WAY -> "On the way - Delivery in progress";
                    case DELIVERED -> "Delivered - Order completed";
                    case CANCELLED -> "Cancelled";
                    default -> "Processing";
                };

                deliveryStatusLabel.setText(statusText);
            } else {
                partnerNameLabel.setText("Information not available");
                partnerPhoneLabel.setText("Not available");
                deliveryStatusLabel.setText("Unknown");
            }
        } catch (Exception e) {
            System.err.println("Error loading delivery partner info: " + e.getMessage());
            partnerNameLabel.setText("Error loading info");
            partnerPhoneLabel.setText("Not available");
            deliveryStatusLabel.setText("Unknown");
        }
    }

    /**
     * Update status badge styling
     */
    private void updateStatusBadge(OrderStatus status) {
        statusBadge.setText(status.getDisplayName());

        String style = "-fx-text-fill: white; -fx-padding: 8 16; -fx-background-radius: 20; " +
                      "-fx-font-weight: bold; -fx-font-size: 12px; -fx-background-color: ";

        switch (status) {
            case PENDING -> statusBadge.setStyle(style + "#ffc107;");
            case ACCEPTED -> statusBadge.setStyle(style + "#3498db;");
            case PICKED_UP -> statusBadge.setStyle(style + "#17a2b8;");
            case ON_THE_WAY -> statusBadge.setStyle(style + "#9b59b6;");
            case DELIVERED -> statusBadge.setStyle(style + "#27ae60;");
            case CANCELLED -> statusBadge.setStyle(style + "#e74c3c;");
        }
    }

    /**
     * Build status timeline
     */
    private void buildStatusTimeline() {
        timelineContainer.getChildren().clear();

        addTimelineItem("Order Created", order.getOrderDate().toString(), true);

        if (order.getStatus() != OrderStatus.PENDING && order.getStatus() != OrderStatus.CANCELLED) {
            addTimelineItem("Accepted", "Accepted by delivery partner", true);
        }

        if (order.getStatus() == OrderStatus.PICKED_UP || order.getStatus() == OrderStatus.ON_THE_WAY ||
            order.getStatus() == OrderStatus.DELIVERED) {
            addTimelineItem("Picked Up", "Order picked up", true);
        }

        if (order.getStatus() == OrderStatus.ON_THE_WAY || order.getStatus() == OrderStatus.DELIVERED) {
            addTimelineItem("On The Way", "Delivery in progress", true);
        }

        if (order.getStatus() == OrderStatus.DELIVERED) {
            addTimelineItem("Delivered", "Order delivered successfully", true);
        }

        if (order.getStatus() == OrderStatus.CANCELLED) {
            addTimelineItem("Cancelled", "Order was cancelled", false);
        }
    }

    /**
     * Add timeline item
     */
    private void addTimelineItem(String title, String subtitle, boolean completed) {
        HBox timelineItem = new HBox(12);
        timelineItem.setAlignment(Pos.CENTER_LEFT);

        // Status dot
        Label dot = new Label("●");
        dot.setStyle("-fx-font-size: 16px; -fx-text-fill: " + (completed ? "#27ae60" : "#ccc") + ";");

        // Text
        VBox textBox = new VBox(2);
        Label titleLabel = new Label(title);
        titleLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 13px;");
        Label subtitleLabel = new Label(subtitle);
        subtitleLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #666;");
        textBox.getChildren().addAll(titleLabel, subtitleLabel);

        timelineItem.getChildren().addAll(dot, textBox);
        timelineContainer.getChildren().add(timelineItem);
    }

    /**
     * Configure UI based on user role
     */
    private void configureUIForRole() {
        if ("CUSTOMER".equals(userRole)) {
            // Customer view
            partnerInfoCard.setVisible(order.getStatus() != OrderStatus.PENDING);
            partnerInfoCard.setManaged(order.getStatus() != OrderStatus.PENDING);

            // Show cancel button only for pending orders
            cancelOrderBtn.setVisible(order.getStatus() == OrderStatus.PENDING);
            cancelOrderBtn.setManaged(order.getStatus() == OrderStatus.PENDING);

            // Show contact partner button if order is accepted
            contactPartnerBtn.setVisible(order.getStatus() != OrderStatus.PENDING &&
                                        order.getStatus() != OrderStatus.CANCELLED);
            contactPartnerBtn.setManaged(order.getStatus() != OrderStatus.PENDING &&
                                        order.getStatus() != OrderStatus.CANCELLED);

            // Show rate button if delivered
            rateDeliveryBtn.setVisible(order.getStatus() == OrderStatus.DELIVERED);
            rateDeliveryBtn.setManaged(order.getStatus() == OrderStatus.DELIVERED);

        } else if ("DELIVERY_PARTNER".equals(userRole)) {
            // Delivery partner view
            customerInfoCard.setVisible(true);
            customerInfoCard.setManaged(true);

            // Show accept button only for pending orders
            acceptOrderBtn.setVisible(order.getStatus() == OrderStatus.PENDING);
            acceptOrderBtn.setManaged(order.getStatus() == OrderStatus.PENDING);

            // Show status update buttons based on current status
            pickUpBtn.setVisible(order.getStatus() == OrderStatus.ACCEPTED);
            pickUpBtn.setManaged(order.getStatus() == OrderStatus.ACCEPTED);

            onTheWayBtn.setVisible(order.getStatus() == OrderStatus.PICKED_UP);
            onTheWayBtn.setManaged(order.getStatus() == OrderStatus.PICKED_UP);

            completeBtn.setVisible(order.getStatus() == OrderStatus.ON_THE_WAY ||
                                  order.getStatus() == OrderStatus.PICKED_UP);
            completeBtn.setManaged(order.getStatus() == OrderStatus.ON_THE_WAY ||
                                   order.getStatus() == OrderStatus.PICKED_UP);

            contactCustomerBtn.setVisible(order.getStatus() != OrderStatus.PENDING);
            contactCustomerBtn.setManaged(order.getStatus() != OrderStatus.PENDING);
        }
    }

    // Action Handlers

    @FXML
    private void handleCancelOrder() {
        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle("Cancel Order");
        confirmation.setHeaderText("Cancel Order #" + order.getOrderId());
        confirmation.setContentText("Are you sure you want to cancel this order?");

        confirmation.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                boolean success = OrderDAO.cancelOrder(order.getOrderId());
                if (success) {
                    // Create notification
                    NotificationDAO.createNotification(
                        order.getCreatedByUserId(),
                        "Order Cancelled",
                        "Your order #" + order.getOrderId() + " has been cancelled.",
                        "ORDER_UPDATE",
                        order.getOrderId()
                    );

                    showAlert("Order cancelled successfully", Alert.AlertType.INFORMATION);
                    closeDialog();
                } else {
                    showAlert("Failed to cancel order", Alert.AlertType.ERROR);
                }
            }
        });
    }

    @FXML
    private void handleAcceptOrder() {
        boolean success = DeliveryDAO.acceptOrder(order.getOrderId(), currentUserId);
        if (success) {
            // Notify customer
            NotificationDAO.createNotification(
                order.getCreatedByUserId(),
                "Order Accepted",
                "Your order #" + order.getOrderId() + " has been accepted by a delivery partner!",
                "ORDER_UPDATE",
                order.getOrderId()
            );

            showAlert("Order accepted successfully!", Alert.AlertType.INFORMATION);
            closeDialog();
        } else {
            showAlert("Failed to accept order", Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void handlePickUp() {
        boolean success = DeliveryDAO.markAsPickedUp(order.getOrderId(), currentUserId);
        if (success) {
            // Notify customer
            NotificationDAO.createNotification(
                order.getCreatedByUserId(),
                "Order Picked Up",
                "Your order #" + order.getOrderId() + " has been picked up!",
                "DELIVERY_UPDATE",
                order.getOrderId()
            );

            showAlert("Order marked as picked up!", Alert.AlertType.INFORMATION);
            closeDialog();
        } else {
            showAlert("Failed to update status", Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void handleOnTheWay() {
        boolean success = DeliveryDAO.markAsOnTheWay(order.getOrderId());
        if (success) {
            // Notify customer
            NotificationDAO.createNotification(
                order.getCreatedByUserId(),
                "Delivery In Progress",
                "Your order #" + order.getOrderId() + " is on the way!",
                "DELIVERY_UPDATE",
                order.getOrderId()
            );

            showAlert("Status updated to 'On The Way'", Alert.AlertType.INFORMATION);
            closeDialog();
        } else {
            showAlert("Failed to update status", Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void handleComplete() {
        boolean success = DeliveryDAO.completeDelivery(order.getOrderId(), currentUserId, order.getDeliveryFee());
        if (success) {
            // Notify customer
            NotificationDAO.createNotification(
                order.getCreatedByUserId(),
                "Order Delivered",
                "Your order #" + order.getOrderId() + " has been delivered successfully!",
                "ORDER_UPDATE",
                order.getOrderId()
            );

            // Notify delivery partner about earnings
            NotificationDAO.createNotification(
                currentUserId,
                "Earnings Added",
                "You earned " + order.getFormattedDeliveryFee() + " from order #" + order.getOrderId(),
                "EARNING",
                order.getOrderId()
            );

            showAlert("Delivery completed! Earnings recorded.", Alert.AlertType.INFORMATION);
            closeDialog();
        } else {
            showAlert("Failed to complete delivery", Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void handleContactPartner() {
        showAlert("Contact feature coming soon!\nPhone: " +
                 (order.getCustomerPhone() != null ? order.getCustomerPhone() : "Not available"),
                 Alert.AlertType.INFORMATION);
    }

    @FXML
    private void handleContactCustomer() {
        showAlert("Contact feature coming soon!\nPhone: " +
                 (order.getCustomerPhone() != null ? order.getCustomerPhone() : "Not available"),
                 Alert.AlertType.INFORMATION);
    }

    @FXML
    private void handleRateDelivery() {
        // Validate order status
        if (order == null) {
            showAlert("Order not found!", Alert.AlertType.ERROR);
            return;
        }

        // Check if order is delivered
        if (order.getStatus() != OrderStatus.DELIVERED) {
            showAlert("You can only rate a delivery after it has been completed.", Alert.AlertType.WARNING);
            return;
        }

        // Check if there's a delivery partner
        if (order.getAcceptedByUserId() == null) {
            showAlert("No delivery partner assigned to this order!", Alert.AlertType.WARNING);
            return;
        }

        // Check if already rated
        if (RatingDAO.hasRating(order.getOrderId())) {
            RatingDAO.Rating existingRating = RatingDAO.getRatingForOrder(order.getOrderId());
            if (existingRating != null) {
                showAlert("You already rated this delivery!\n\n" +
                         "Rating: " + existingRating.getRating() + " ⭐\n" +
                         "Comment: " + (existingRating.getComment() != null && !existingRating.getComment().isEmpty()
                             ? existingRating.getComment() : "None"),
                         Alert.AlertType.INFORMATION);
            } else {
                showAlert("This order has already been rated.", Alert.AlertType.INFORMATION);
            }
            return;
        }

        // Create rating dialog
        showRatingDialog();
    }

    /**
     * Show rating dialog with star selection
     */
    private void showRatingDialog() {
        Stage ratingStage = new Stage();
        ratingStage.initModality(Modality.APPLICATION_MODAL);
        ratingStage.setTitle("Rate Delivery - Order #" + order.getOrderId());
        ratingStage.setResizable(false);

        VBox container = new VBox(20);
        container.setPadding(new Insets(30));
        container.setAlignment(Pos.CENTER);
        container.setStyle("-fx-background-color: white;");

        // Title
        Label titleLabel = new Label("How was your delivery experience?");
        titleLabel.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");

        // Subtitle
        Label subtitleLabel = new Label("Rate the service provided by your delivery partner");
        subtitleLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #7f8c8d;");

        // Star Rating
        HBox starBox = new HBox(15);
        starBox.setAlignment(Pos.CENTER);
        starBox.setStyle("-fx-padding: 10 0;");

        final int[] selectedRating = {0};
        Button[] starButtons = new Button[5];

        // Rating description label
        Label ratingDescLabel = new Label("Tap a star to rate");
        ratingDescLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #95a5a6; -fx-font-style: italic;");

        for (int i = 0; i < 5; i++) {
            final int rating = i + 1;
            Button starBtn = new Button("☆");
            starBtn.setStyle("-fx-font-size: 36px; -fx-background-color: transparent; " +
                           "-fx-text-fill: #bdc3c7; -fx-cursor: hand; -fx-padding: 5;");

            // Add hover effect
            starBtn.setOnMouseEntered(e -> {
                if (selectedRating[0] == 0) {
                    updateStarsPreview(starButtons, rating);
                }
            });

            starBtn.setOnMouseExited(e -> {
                if (selectedRating[0] == 0) {
                    updateStars(starButtons, 0);
                } else {
                    updateStars(starButtons, selectedRating[0]);
                }
            });

            starBtn.setOnAction(e -> {
                selectedRating[0] = rating;
                updateStars(starButtons, rating);
                // Update description based on rating
                String[] descriptions = {
                    "Poor - Not satisfied",
                    "Fair - Below expectations",
                    "Good - Meets expectations",
                    "Very Good - Above expectations",
                    "Excellent - Outstanding service!"
                };
                ratingDescLabel.setText(descriptions[rating - 1]);
                ratingDescLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #f39c12; -fx-font-weight: bold;");
            });

            starButtons[i] = starBtn;
            starBox.getChildren().add(starBtn);
        }

        // Comment
        Label commentLabel = new Label("Share your feedback (Optional):");
        commentLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #34495e; -fx-font-weight: 600;");

        TextArea commentArea = new TextArea();
        commentArea.setPromptText("Tell us about your experience with the delivery partner...");
        commentArea.setPrefRowCount(4);
        commentArea.setMaxWidth(450);
        commentArea.setWrapText(true);
        commentArea.setStyle("-fx-border-color: #dfe6e9; -fx-border-width: 2px; -fx-border-radius: 8px; " +
                           "-fx-background-radius: 8px; -fx-padding: 10; -fx-font-size: 13px;");

        // Focus effect for comment area
        commentArea.setOnMouseEntered(e ->
            commentArea.setStyle(commentArea.getStyle().replace("#dfe6e9", "#3498db")));
        commentArea.setOnMouseExited(e ->
            commentArea.setStyle(commentArea.getStyle().replace("#3498db", "#dfe6e9")));

        // Buttons
        HBox buttonBox = new HBox(15);
        buttonBox.setAlignment(Pos.CENTER);
        buttonBox.setStyle("-fx-padding: 10 0 0 0;");

        Button submitBtn = new Button("Submit Rating");
        submitBtn.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; " +
                         "-fx-font-size: 15px; -fx-font-weight: bold; -fx-padding: 12 35; " +
                         "-fx-cursor: hand; -fx-background-radius: 8px; " +
                         "-fx-effect: dropshadow(gaussian, rgba(52, 152, 219, 0.4), 10, 0, 0, 2);");

        submitBtn.setOnMouseEntered(e ->
            submitBtn.setStyle(submitBtn.getStyle() + "-fx-background-color: #2980b9;"));
        submitBtn.setOnMouseExited(e ->
            submitBtn.setStyle(submitBtn.getStyle().replace("-fx-background-color: #2980b9;", "-fx-background-color: #3498db;")));

        Button cancelBtn = new Button("Cancel");
        cancelBtn.setStyle("-fx-background-color: #95a5a6; -fx-text-fill: white; " +
                         "-fx-font-size: 15px; -fx-font-weight: bold; -fx-padding: 12 35; " +
                         "-fx-cursor: hand; -fx-background-radius: 8px;");

        cancelBtn.setOnMouseEntered(e ->
            cancelBtn.setStyle(cancelBtn.getStyle() + "-fx-background-color: #7f8c8d;"));
        cancelBtn.setOnMouseExited(e ->
            cancelBtn.setStyle(cancelBtn.getStyle().replace("-fx-background-color: #7f8c8d;", "-fx-background-color: #95a5a6;")));

        submitBtn.setOnAction(e -> {
            if (selectedRating[0] == 0) {
                showAlert("Please select a rating before submitting!", Alert.AlertType.WARNING);
                return;
            }

            String comment = commentArea.getText().trim();

            boolean success = RatingDAO.submitRating(
                order.getOrderId(),
                order.getCreatedByUserId(),
                order.getAcceptedByUserId(),
                selectedRating[0],
                comment.isEmpty() ? null : comment
            );

            if (success) {
                // Notify delivery partner
                NotificationDAO.createNotification(
                    order.getAcceptedByUserId(),
                    "New Rating Received",
                    "You received a " + selectedRating[0] + " ⭐ rating for order #" + order.getOrderId() +
                    (comment.isEmpty() ? "" : "\nComment: " + comment),
                    "INFO",
                    order.getOrderId()
                );

                showAlert("Thank you for your feedback!\nYour rating has been submitted successfully.",
                         Alert.AlertType.INFORMATION);
                ratingStage.close();

                // Disable and update rate button after rating
                if (rateDeliveryBtn != null) {
                    rateDeliveryBtn.setDisable(true);
                    rateDeliveryBtn.setText("Rated ✓");
                    rateDeliveryBtn.setStyle("-fx-background-color: #27ae60; -fx-text-fill: white; " +
                                            "-fx-padding: 10 20; -fx-background-radius: 5; -fx-opacity: 0.7;");
                }
            } else {
                showAlert("Failed to submit rating. Please try again later.", Alert.AlertType.ERROR);
            }
        });

        cancelBtn.setOnAction(e -> ratingStage.close());

        buttonBox.getChildren().addAll(submitBtn, cancelBtn);

        // Add separator line
        javafx.scene.shape.Line separator = new javafx.scene.shape.Line();
        separator.setEndX(400);
        separator.setStroke(javafx.scene.paint.Color.web("#ecf0f1"));
        separator.setStrokeWidth(2);

        container.getChildren().addAll(
            titleLabel,
            subtitleLabel,
            new javafx.scene.layout.Region() {{ setPrefHeight(5); }},
            starBox,
            ratingDescLabel,
            separator,
            commentLabel,
            commentArea,
            buttonBox
        );

        Scene scene = new Scene(container, 550, 520);
        ratingStage.setScene(scene);
        ratingStage.showAndWait();
    }

    /**
     * Update star button display based on rating
     */
    private void updateStars(Button[] starButtons, int rating) {
        for (int i = 0; i < starButtons.length; i++) {
            if (i < rating) {
                starButtons[i].setText("⭐");
                starButtons[i].setStyle("-fx-font-size: 36px; -fx-background-color: transparent; " +
                                      "-fx-text-fill: #f39c12; -fx-cursor: hand; -fx-padding: 5;");
            } else {
                starButtons[i].setText("☆");
                starButtons[i].setStyle("-fx-font-size: 36px; -fx-background-color: transparent; " +
                                      "-fx-text-fill: #bdc3c7; -fx-cursor: hand; -fx-padding: 5;");
            }
        }
    }

    /**
     * Preview star rating on hover
     */
    private void updateStarsPreview(Button[] starButtons, int rating) {
        for (int i = 0; i < starButtons.length; i++) {
            if (i < rating) {
                starButtons[i].setText("⭐");
                starButtons[i].setStyle("-fx-font-size: 36px; -fx-background-color: transparent; " +
                                      "-fx-text-fill: #f39c12; -fx-cursor: hand; -fx-padding: 5; -fx-opacity: 0.7;");
            } else {
                starButtons[i].setText("☆");
                starButtons[i].setStyle("-fx-font-size: 36px; -fx-background-color: transparent; " +
                                      "-fx-text-fill: #bdc3c7; -fx-cursor: hand; -fx-padding: 5;");
            }
        }
    }

    @FXML
    private void handleClose() {
        closeDialog();
    }

    /**
     * Close the dialog
     */
    private void closeDialog() {
        Stage stage = (Stage) orderIdLabel.getScene().getWindow();
        stage.close();
    }

    /**
     * Show alert dialog
     */
    private void showAlert(String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setContentText(message);
        alert.showAndWait();
    }
}

