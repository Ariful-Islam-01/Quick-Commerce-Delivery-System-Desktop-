package com.example.quickcommercedeliverysystemdesktop.models;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Order model for peer-to-peer delivery system
 * Represents a delivery request created by a customer
 */
public class Order {
    // IDs
    private int orderId;
    private int createdByUserId;        // customer_id from DB
    private Integer acceptedByUserId;   // delivery_person_id (null if pending)

    // Product details
    private String productName;
    private String description;
    private String productPhoto;

    // Delivery details
    private String deliveryLocation;
    private String deliveryTimeRange;   // Combined from time_from and time_to
    private double deliveryFee;
    private String notesForDelivery;

    // Customer info
    private String customerName;
    private String customerPhone;

    // Status & timestamps
    private OrderStatus status;
    private LocalDateTime orderDate;
    private LocalDateTime acceptedAt;
    private LocalDateTime pickedUpAt;
    private LocalDateTime deliveryDate;
    private Integer ratingId;

    public enum OrderStatus {
        PENDING("Pending"),
        ACCEPTED("Accepted"),
        PICKED_UP("Picked Up"),
        ON_THE_WAY("On the Way"),
        DELIVERED("Delivered"),
        CANCELLED("Cancelled");

        private final String displayName;
        OrderStatus(String displayName) { this.displayName = displayName; }
        public String getDisplayName() { return displayName; }
        @Override
        public String toString() { return displayName; }
    }

    // Constructor for creating new order
    public Order(int createdByUserId, String productName, String description,
                 String deliveryLocation, String deliveryTimeRange, double deliveryFee,
                 String notesForDelivery, String customerName, String customerPhone) {
        this.createdByUserId = createdByUserId;
        this.productName = productName;
        this.description = description;
        this.deliveryLocation = deliveryLocation;
        this.deliveryTimeRange = deliveryTimeRange;
        this.deliveryFee = deliveryFee;
        this.notesForDelivery = notesForDelivery;
        this.customerName = customerName;
        this.customerPhone = customerPhone;
        this.status = OrderStatus.PENDING;
        this.orderDate = LocalDateTime.now();
    }

    // Full constructor for loading from DB
    public Order(int orderId, int createdByUserId, Integer acceptedByUserId,
                 String productName, String description, String productPhoto,
                 String deliveryLocation, String deliveryTimeRange, double deliveryFee,
                 String notesForDelivery, String customerName, String customerPhone,
                 OrderStatus status, LocalDateTime orderDate, LocalDateTime acceptedAt,
                 LocalDateTime pickedUpAt, LocalDateTime deliveryDate, Integer ratingId) {
        this.orderId = orderId;
        this.createdByUserId = createdByUserId;
        this.acceptedByUserId = acceptedByUserId;
        this.productName = productName;
        this.description = description;
        this.productPhoto = productPhoto;
        this.deliveryLocation = deliveryLocation;
        this.deliveryTimeRange = deliveryTimeRange;
        this.deliveryFee = deliveryFee;
        this.notesForDelivery = notesForDelivery;
        this.customerName = customerName;
        this.customerPhone = customerPhone;
        this.status = status;
        this.orderDate = orderDate;
        this.acceptedAt = acceptedAt;
        this.pickedUpAt = pickedUpAt;
        this.deliveryDate = deliveryDate;
        this.ratingId = ratingId;
    }

    // Getters
    public int getOrderId() { return orderId; }
    public void setOrderId(int orderId) { this.orderId = orderId; }

    public int getCreatedByUserId() { return createdByUserId; }
    public void setCreatedByUserId(int createdByUserId) { this.createdByUserId = createdByUserId; }

    public Integer getAcceptedByUserId() { return acceptedByUserId; }
    public void setAcceptedByUserId(Integer acceptedByUserId) { this.acceptedByUserId = acceptedByUserId; }

    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getProductPhoto() { return productPhoto; }
    public void setProductPhoto(String productPhoto) { this.productPhoto = productPhoto; }

    public String getDeliveryLocation() { return deliveryLocation; }
    public void setDeliveryLocation(String deliveryLocation) { this.deliveryLocation = deliveryLocation; }

    public String getDeliveryTimeRange() { return deliveryTimeRange; }
    public void setDeliveryTimeRange(String deliveryTimeRange) { this.deliveryTimeRange = deliveryTimeRange; }

    public double getDeliveryFee() { return deliveryFee; }
    public void setDeliveryFee(double deliveryFee) { this.deliveryFee = deliveryFee; }

    public String getNotesForDelivery() { return notesForDelivery; }
    public void setNotesForDelivery(String notesForDelivery) { this.notesForDelivery = notesForDelivery; }

    public String getCustomerName() { return customerName; }
    public void setCustomerName(String customerName) { this.customerName = customerName; }

    public String getCustomerPhone() { return customerPhone; }
    public void setCustomerPhone(String customerPhone) { this.customerPhone = customerPhone; }

    public OrderStatus getStatus() { return status; }
    public void setStatus(OrderStatus status) { this.status = status; }

    public LocalDateTime getOrderDate() { return orderDate; }
    public void setOrderDate(LocalDateTime orderDate) { this.orderDate = orderDate; }

    public LocalDateTime getAcceptedAt() { return acceptedAt; }
    public void setAcceptedAt(LocalDateTime acceptedAt) { this.acceptedAt = acceptedAt; }

    public LocalDateTime getPickedUpAt() { return pickedUpAt; }
    public void setPickedUpAt(LocalDateTime pickedUpAt) { this.pickedUpAt = pickedUpAt; }

    public LocalDateTime getDeliveryDate() { return deliveryDate; }
    public void setDeliveryDate(LocalDateTime deliveryDate) { this.deliveryDate = deliveryDate; }

    public Integer getRatingId() { return ratingId; }
    public void setRatingId(Integer ratingId) { this.ratingId = ratingId; }

    // Helper methods
    public boolean isAccepted() {
        return acceptedByUserId != null;
    }

    public boolean canEdit() {
        return status == OrderStatus.PENDING && !isAccepted();
    }

    public boolean canCancel() {
        return status != OrderStatus.DELIVERED && status != OrderStatus.CANCELLED;
    }

    public String getFormattedOrderDate() {
        if (orderDate == null) return "N/A";
        return orderDate.format(DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm"));
    }

    public String getFormattedDeliveryFee() {
        return String.format("$%.2f", deliveryFee);
    }

    public String getStatusStyleClass() {
        return switch (status) {
            case DELIVERED -> "status-delivered";
            case CANCELLED -> "status-cancelled";
            case ON_THE_WAY -> "status-on-the-way";
            case PICKED_UP -> "status-picked-up";
            case ACCEPTED -> "status-accepted";
            case PENDING -> "status-pending";
        };
    }

    @Override
    public String toString() {
        return "Order{" +
                "orderId=" + orderId +
                ", productName='" + productName + '\'' +
                ", deliveryFee=" + deliveryFee +
                ", status=" + status +
                '}';
    }
}

