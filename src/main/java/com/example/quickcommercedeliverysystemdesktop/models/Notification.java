package com.example.quickcommercedeliverysystemdesktop.models;

import java.time.LocalDateTime;

/**
 * Notification model for in-app notifications
 */
public class Notification {

    private int notificationId;
    private int userId;
    private String title;
    private String message;
    private NotificationType type;
    private Integer orderId; // nullable
    private boolean isRead;
    private LocalDateTime createdAt;

    /**
     * Notification types
     */
    public enum NotificationType {
        INFO("Info"),
        SUCCESS("Success"),
        WARNING("Warning"),
        ORDER_UPDATE("Order Update"),
        DELIVERY_UPDATE("Delivery Update"),
        EARNING("Earning");

        private final String displayName;

        NotificationType(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    // Full constructor
    public Notification(int notificationId, int userId, String title, String message,
                       NotificationType type, Integer orderId, boolean isRead, LocalDateTime createdAt) {
        this.notificationId = notificationId;
        this.userId = userId;
        this.title = title;
        this.message = message;
        this.type = type;
        this.orderId = orderId;
        this.isRead = isRead;
        this.createdAt = createdAt;
    }

    // Constructor without ID (for new notifications)
    public Notification(int userId, String title, String message, NotificationType type, Integer orderId) {
        this.userId = userId;
        this.title = title;
        this.message = message;
        this.type = type;
        this.orderId = orderId;
        this.isRead = false;
        this.createdAt = LocalDateTime.now();
    }

    // Getters and Setters
    public int getNotificationId() {
        return notificationId;
    }

    public void setNotificationId(int notificationId) {
        this.notificationId = notificationId;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public NotificationType getType() {
        return type;
    }

    public void setType(NotificationType type) {
        this.type = type;
    }

    public Integer getOrderId() {
        return orderId;
    }

    public void setOrderId(Integer orderId) {
        this.orderId = orderId;
    }

    public boolean isRead() {
        return isRead;
    }

    public void setRead(boolean read) {
        isRead = read;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    // Helper methods
    public String getFormattedTime() {
        return createdAt.toLocalTime().toString().substring(0, 5);
    }

    public String getFormattedDate() {
        return createdAt.toLocalDate().toString();
    }

    public String getTimeAgo() {
        LocalDateTime now = LocalDateTime.now();
        long minutes = java.time.Duration.between(createdAt, now).toMinutes();

        if (minutes < 1) return "Just now";
        if (minutes < 60) return minutes + " min ago";

        long hours = minutes / 60;
        if (hours < 24) return hours + " hour" + (hours > 1 ? "s" : "") + " ago";

        long days = hours / 24;
        if (days < 7) return days + " day" + (days > 1 ? "s" : "") + " ago";

        return getFormattedDate();
    }

    @Override
    public String toString() {
        return "Notification{" +
                "id=" + notificationId +
                ", title='" + title + '\'' +
                ", type=" + type +
                ", isRead=" + isRead +
                '}';
    }
}

