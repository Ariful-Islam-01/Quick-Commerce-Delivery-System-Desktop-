package com.example.quickcommercedeliverysystemdesktop;

import com.example.quickcommercedeliverysystemdesktop.database.NotificationDAO;
import com.example.quickcommercedeliverysystemdesktop.database.Database;

/**
 * Test class to create sample notifications
 */
public class TestNotifications {
    public static void main(String[] args) {
        System.out.println("Creating sample notifications...");

        // Create sample notifications for user ID 1
        NotificationDAO.createNotification(1,
            "Welcome!",
            "Welcome to Quick Commerce Delivery System!",
            "INFO",
            null);

        NotificationDAO.createNotification(1,
            "New Order",
            "You have received a new order #1001",
            "ORDER_UPDATE",
            1);

        NotificationDAO.createNotification(1,
            "Delivery Accepted",
            "Your delivery has been accepted by a partner",
            "DELIVERY_UPDATE",
            1);

        NotificationDAO.createNotification(1,
            "Earnings Added",
            "You earned $5.00 from order #1001",
            "EARNING",
            1);

        NotificationDAO.createNotification(1,
            "Order Completed",
            "Your order has been delivered successfully!",
            "SUCCESS",
            1);

        System.out.println("Sample notifications created!");

        // Test reading notifications
        var notifications = NotificationDAO.getNotificationsByUser(1);
        System.out.println("Found " + notifications.size() + " notifications for user 1");

        for (var notif : notifications) {
            System.out.println("- " + notif.getTitle() + ": " + notif.getMessage());
        }
    }
}

