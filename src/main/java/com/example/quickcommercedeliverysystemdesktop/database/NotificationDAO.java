package com.example.quickcommercedeliverysystemdesktop.database;

import com.example.quickcommercedeliverysystemdesktop.models.Notification;
import com.example.quickcommercedeliverysystemdesktop.models.Notification.NotificationType;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for Notification operations
 */
public class NotificationDAO {

    /**
     * Create a new notification
     */
    public static boolean createNotification(int userId, String title, String message,
                                           String type, Integer orderId) {
        String sql = "INSERT INTO Notifications (user_id, title, message, type, order_id, created_at) " +
                     "VALUES (?, ?, ?, ?, ?, ?)";

        System.out.println("=== Creating Notification ===");
        System.out.println("User ID: " + userId);
        System.out.println("Title: " + title);
        System.out.println("Message: " + message);
        System.out.println("Type: " + type);
        System.out.println("Order ID: " + orderId);

        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, userId);
            ps.setString(2, title);
            ps.setString(3, message);
            ps.setString(4, type);
            if (orderId != null) {
                ps.setInt(5, orderId);
            } else {
                ps.setNull(5, Types.INTEGER);
            }
            ps.setTimestamp(6, Timestamp.valueOf(LocalDateTime.now()));

            int rowsAffected = ps.executeUpdate();
            System.out.println("✓ Notification created successfully! Rows affected: " + rowsAffected);
            return true;

        } catch (SQLException e) {
            System.err.println("✗ Error creating notification: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Get all notifications for a user
     */
    public static List<Notification> getNotificationsByUser(int userId) {
        List<Notification> notifications = new ArrayList<>();
        String sql = "SELECT * FROM Notifications WHERE user_id = ? ORDER BY created_at DESC";

        System.out.println("=== Fetching Notifications for User ID: " + userId + " ===");

        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                notifications.add(mapResultSetToNotification(rs));
            }

            System.out.println("✓ Found " + notifications.size() + " notifications for user " + userId);

        } catch (SQLException e) {
            System.err.println("✗ Error fetching notifications: " + e.getMessage());
            e.printStackTrace();
        }

        return notifications;
    }

    /**
     * Get recent notifications (limited)
     */
    public static List<Notification> getRecentNotifications(int userId, int limit) {
        List<Notification> notifications = new ArrayList<>();
        String sql = "SELECT * FROM Notifications WHERE user_id = ? ORDER BY created_at DESC LIMIT ?";

        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, userId);
            ps.setInt(2, limit);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                notifications.add(mapResultSetToNotification(rs));
            }

        } catch (SQLException e) {
            System.err.println("Error fetching recent notifications: " + e.getMessage());
            e.printStackTrace();
        }

        return notifications;
    }

    /**
     * Get unread notification count
     */
    public static int getUnreadCount(int userId) {
        String sql = "SELECT COUNT(*) as count FROM Notifications WHERE user_id = ? AND is_read = 0";

        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                return rs.getInt("count");
            }

        } catch (SQLException e) {
            System.err.println("Error getting unread count: " + e.getMessage());
            e.printStackTrace();
        }

        return 0;
    }

    /**
     * Mark notification as read
     */
    public static boolean markAsRead(int notificationId) {
        String sql = "UPDATE Notifications SET is_read = 1 WHERE notification_id = ?";

        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, notificationId);
            int updated = ps.executeUpdate();
            return updated > 0;

        } catch (SQLException e) {
            System.err.println("Error marking notification as read: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Mark all notifications as read for a user
     */
    public static boolean markAllAsRead(int userId) {
        String sql = "UPDATE Notifications SET is_read = 1 WHERE user_id = ? AND is_read = 0";

        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, userId);
            ps.executeUpdate();
            return true;

        } catch (SQLException e) {
            System.err.println("Error marking all as read: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Delete a notification
     */
    public static boolean deleteNotification(int notificationId) {
        String sql = "DELETE FROM Notifications WHERE notification_id = ?";

        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, notificationId);
            int deleted = ps.executeUpdate();
            return deleted > 0;

        } catch (SQLException e) {
            System.err.println("Error deleting notification: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Helper method to map ResultSet to Notification object
     */
    private static Notification mapResultSetToNotification(ResultSet rs) throws SQLException {
        int notificationId = rs.getInt("notification_id");
        int userId = rs.getInt("user_id");
        String title = rs.getString("title");
        String message = rs.getString("message");
        String typeStr = rs.getString("type");
        Integer orderId = rs.getObject("order_id", Integer.class);
        boolean isRead = rs.getInt("is_read") == 1;
        Timestamp timestamp = rs.getTimestamp("created_at");
        LocalDateTime createdAt = timestamp != null ? timestamp.toLocalDateTime() : LocalDateTime.now();

        NotificationType type;
        try {
            type = NotificationType.valueOf(typeStr);
        } catch (IllegalArgumentException e) {
            type = NotificationType.INFO;
        }

        return new Notification(notificationId, userId, title, message, type, orderId, isRead, createdAt);
    }
}

