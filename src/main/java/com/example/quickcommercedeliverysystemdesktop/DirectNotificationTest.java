package com.example.quickcommercedeliverysystemdesktop;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

/**
 * Direct SQL test to create and verify notifications
 */
public class DirectNotificationTest {
    public static void main(String[] args) {
        String url = "jdbc:sqlite:database/quickcommerce.db";

        try (Connection conn = DriverManager.getConnection(url)) {
            System.out.println("Connected to database");

            // Check if Notifications table exists
            String checkTable = "SELECT name FROM sqlite_master WHERE type='table' AND name='Notifications'";
            try (PreparedStatement ps = conn.prepareStatement(checkTable)) {
                ResultSet rs = ps.executeQuery();
                if (rs.next()) {
                    System.out.println("Notifications table exists");
                } else {
                    System.out.println("ERROR: Notifications table does not exist!");
                    return;
                }
            }

            // Insert test notifications for user ID 1
            String insertSql = "INSERT INTO Notifications (user_id, title, message, type, order_id, is_read, created_at) " +
                             "VALUES (?, ?, ?, ?, ?, ?, datetime('now'))";

            // Clear existing notifications for user 1
            String deleteSql = "DELETE FROM Notifications WHERE user_id = 1";
            try (PreparedStatement ps = conn.prepareStatement(deleteSql)) {
                int deleted = ps.executeUpdate();
                System.out.println("Deleted " + deleted + " existing notifications for user 1");
            }

            // Insert sample notifications
            Object[][] notifications = {
                {1, "Welcome!", "Welcome to Quick Commerce!", "INFO", null, 0},
                {1, "New Order", "You have a new order #101", "ORDER_UPDATE", 1, 0},
                {1, "Delivery Update", "Your order is on the way", "DELIVERY_UPDATE", 1, 0},
                {1, "Earnings", "You earned $5.50", "EARNING", 1, 0},
                {1, "Order Completed", "Order delivered successfully!", "SUCCESS", 1, 1}
            };

            for (Object[] notif : notifications) {
                try (PreparedStatement ps = conn.prepareStatement(insertSql)) {
                    ps.setInt(1, (Integer) notif[0]);
                    ps.setString(2, (String) notif[1]);
                    ps.setString(3, (String) notif[2]);
                    ps.setString(4, (String) notif[3]);
                    if (notif[4] != null) {
                        ps.setInt(5, (Integer) notif[4]);
                    } else {
                        ps.setNull(5, java.sql.Types.INTEGER);
                    }
                    ps.setInt(6, (Integer) notif[5]);
                    ps.executeUpdate();
                    System.out.println("Inserted: " + notif[1]);
                }
            }

            // Verify insertions
            String countSql = "SELECT COUNT(*) as count FROM Notifications WHERE user_id = 1";
            try (PreparedStatement ps = conn.prepareStatement(countSql)) {
                ResultSet rs = ps.executeQuery();
                if (rs.next()) {
                    System.out.println("\nTotal notifications for user 1: " + rs.getInt("count"));
                }
            }

            // Show all notifications
            String selectSql = "SELECT * FROM Notifications WHERE user_id = 1 ORDER BY created_at DESC";
            try (PreparedStatement ps = conn.prepareStatement(selectSql)) {
                ResultSet rs = ps.executeQuery();
                System.out.println("\nAll notifications:");
                while (rs.next()) {
                    System.out.println("  [" + rs.getInt("notification_id") + "] " +
                                     rs.getString("title") + " - " +
                                     rs.getString("message") +
                                     " (read: " + rs.getInt("is_read") + ")");
                }
            }

            System.out.println("\nTest notifications created successfully!");

        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}

