package com.example.quickcommercedeliverysystemdesktop;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;

/**
 * Fix and test notification system
 */
public class FixNotificationSystem {
    public static void main(String[] args) {
        String url = "jdbc:sqlite:database/quickcommerce.db";

        try (Connection conn = DriverManager.getConnection(url)) {
            System.out.println("✓ Connected to database");

            // Step 1: Check if Notifications table exists
            String checkTable = "SELECT name FROM sqlite_master WHERE type='table' AND name='Notifications'";
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery(checkTable)) {
                if (rs.next()) {
                    System.out.println("✓ Notifications table exists");
                } else {
                    System.out.println("✗ Notifications table does not exist - creating it...");
                    createNotificationsTable(conn);
                }
            }

            // Step 2: Check table structure
            System.out.println("\n--- Checking table structure ---");
            String pragma = "PRAGMA table_info(Notifications)";
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery(pragma)) {
                while (rs.next()) {
                    System.out.println("Column: " + rs.getString("name") +
                                     " | Type: " + rs.getString("type") +
                                     " | NotNull: " + rs.getInt("notnull") +
                                     " | Default: " + rs.getString("dflt_value"));
                }
            }

            // Step 3: Verify required columns exist
            boolean hasAllColumns = checkRequiredColumns(conn);
            if (!hasAllColumns) {
                System.out.println("\n✗ Table structure is incorrect - recreating table...");
                recreateNotificationsTable(conn);
            }

            // Step 4: Insert test notifications
            System.out.println("\n--- Inserting test notifications ---");
            insertTestNotifications(conn);

            // Step 5: Verify notifications were inserted
            System.out.println("\n--- Verifying notifications ---");
            verifyNotifications(conn);

            System.out.println("\n✓✓✓ Notification system fixed successfully! ✓✓✓");

        } catch (Exception e) {
            System.err.println("ERROR: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void createNotificationsTable(Connection conn) throws Exception {
        String sql = """
            CREATE TABLE IF NOT EXISTS Notifications (
                notification_id INTEGER PRIMARY KEY AUTOINCREMENT,
                user_id INTEGER NOT NULL,
                title TEXT NOT NULL,
                message TEXT NOT NULL,
                type TEXT DEFAULT 'INFO',
                order_id INTEGER,
                is_read INTEGER DEFAULT 0,
                created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
                FOREIGN KEY (user_id) REFERENCES Users(user_id),
                FOREIGN KEY (order_id) REFERENCES Orders(order_id)
            )
        """;
        try (Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
            System.out.println("✓ Notifications table created");
        }
    }

    private static boolean checkRequiredColumns(Connection conn) throws Exception {
        String[] requiredColumns = {"notification_id", "user_id", "title", "message", "type", "order_id", "is_read", "created_at"};
        int foundColumns = 0;

        String pragma = "PRAGMA table_info(Notifications)";
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(pragma)) {
            while (rs.next()) {
                String columnName = rs.getString("name");
                for (String required : requiredColumns) {
                    if (required.equals(columnName)) {
                        foundColumns++;
                        break;
                    }
                }
            }
        }

        System.out.println("Found " + foundColumns + " out of " + requiredColumns.length + " required columns");
        return foundColumns == requiredColumns.length;
    }

    private static void recreateNotificationsTable(Connection conn) throws Exception {
        // Drop existing table
        try (Statement stmt = conn.createStatement()) {
            stmt.execute("DROP TABLE IF EXISTS Notifications");
            System.out.println("✓ Dropped old Notifications table");
        }

        // Create new table with correct structure
        createNotificationsTable(conn);
    }

    private static void insertTestNotifications(Connection conn) throws Exception {
        // Get first user ID
        int userId = 0;
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT user_id FROM Users LIMIT 1")) {
            if (rs.next()) {
                userId = rs.getInt("user_id");
                System.out.println("Using user_id: " + userId);
            } else {
                System.out.println("No users found - cannot create test notifications");
                return;
            }
        }

        // Clear existing notifications for this user
        String deleteSql = "DELETE FROM Notifications WHERE user_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(deleteSql)) {
            ps.setInt(1, userId);
            int deleted = ps.executeUpdate();
            System.out.println("Deleted " + deleted + " existing notifications");
        }

        // Insert test notifications
        String insertSql = "INSERT INTO Notifications (user_id, title, message, type, order_id, is_read, created_at) " +
                         "VALUES (?, ?, ?, ?, ?, ?, datetime('now'))";

        Object[][] notifications = {
            {userId, "Welcome!", "Welcome to Quick Commerce Delivery System!", "INFO", null, 0},
            {userId, "New Order Available", "A new order is available for delivery in your area", "ORDER_UPDATE", null, 0},
            {userId, "Delivery Update", "Your delivery is on the way", "DELIVERY_UPDATE", null, 0},
            {userId, "Earnings Added", "You earned $5.50 from your last delivery", "EARNING", null, 0},
            {userId, "Order Completed", "Order delivered successfully! Great job!", "SUCCESS", null, 1}
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
                System.out.println("✓ Inserted: " + notif[1]);
            }
        }
    }

    private static void verifyNotifications(Connection conn) throws Exception {
        String sql = "SELECT notification_id, user_id, title, message, type, is_read, created_at FROM Notifications ORDER BY created_at DESC";

        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            int count = 0;
            while (rs.next()) {
                count++;
                System.out.println(count + ". ID: " + rs.getInt("notification_id") +
                                 " | User: " + rs.getInt("user_id") +
                                 " | Title: " + rs.getString("title") +
                                 " | Type: " + rs.getString("type") +
                                 " | Read: " + (rs.getInt("is_read") == 1 ? "Yes" : "No"));
            }

            System.out.println("\nTotal notifications: " + count);
        }
    }
}

