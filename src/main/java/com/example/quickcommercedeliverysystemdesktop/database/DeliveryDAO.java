package com.example.quickcommercedeliverysystemdesktop.database;

import com.example.quickcommercedeliverysystemdesktop.models.Order;
import com.example.quickcommercedeliverysystemdesktop.models.Order.OrderStatus;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for Delivery operations
 */
public class DeliveryDAO {

    /**
     * Get all available orders (PENDING status) that can be accepted by delivery partners
     */
    public static List<Order> getAvailableOrders() {
        List<Order> orders = new ArrayList<>();
        String sql = "SELECT * FROM Orders WHERE status = 'PENDING' ORDER BY created_at DESC";

        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                orders.add(mapResultSetToOrder(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error fetching available orders: " + e.getMessage());
            e.printStackTrace();
        }

        return orders;
    }

    /**
     * Accept an order by a delivery partner
     */
    public static boolean acceptOrder(int orderId, int deliveryPersonId) {
        String updateOrderSql = "UPDATE Orders SET status = 'ACCEPTED' WHERE order_id = ? AND status = 'PENDING'";
        String insertDeliverySql = "INSERT INTO Deliveries (order_id, delivery_person_id, status, created_at) VALUES (?, ?, 'Accepted', ?)";

        try (Connection conn = Database.getConnection()) {
            conn.setAutoCommit(false);

            try (PreparedStatement psUpdate = conn.prepareStatement(updateOrderSql);
                 PreparedStatement psInsert = conn.prepareStatement(insertDeliverySql)) {

                // Update order status
                psUpdate.setInt(1, orderId);
                int updated = psUpdate.executeUpdate();

                if (updated > 0) {
                    // Insert delivery record
                    psInsert.setInt(1, orderId);
                    psInsert.setInt(2, deliveryPersonId);
                    psInsert.setTimestamp(3, Timestamp.valueOf(LocalDateTime.now()));
                    psInsert.executeUpdate();

                    conn.commit();
                    return true;
                }

                conn.rollback();
                return false;

            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }

        } catch (SQLException e) {
            System.err.println("Error accepting order: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Get all deliveries for a specific delivery partner
     */
    public static List<Order> getDeliveriesByPartner(int deliveryPersonId) {
        List<Order> orders = new ArrayList<>();
        String sql = """
                SELECT o.* FROM Orders o
                INNER JOIN Deliveries d ON o.order_id = d.order_id
                WHERE d.delivery_person_id = ?
                ORDER BY o.created_at DESC
                """;

        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, deliveryPersonId);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                orders.add(mapResultSetToOrder(rs));
            }

        } catch (SQLException e) {
            System.err.println("Error fetching deliveries for partner: " + e.getMessage());
            e.printStackTrace();
        }

        return orders;
    }

    /**
     * Update delivery status to PICKED_UP
     */
    public static boolean markAsPickedUp(int orderId, int deliveryPersonId) {
        String updateOrderSql = "UPDATE Orders SET status = 'PICKED_UP' WHERE order_id = ? AND status = 'ACCEPTED'";
        String updateDeliverySql = "UPDATE Deliveries SET status = 'Picked Up', pickup_time = ? WHERE order_id = ? AND delivery_person_id = ?";

        try (Connection conn = Database.getConnection()) {
            conn.setAutoCommit(false);

            try (PreparedStatement psOrder = conn.prepareStatement(updateOrderSql);
                 PreparedStatement psDelivery = conn.prepareStatement(updateDeliverySql)) {

                // Update order status
                psOrder.setInt(1, orderId);
                int updated = psOrder.executeUpdate();

                if (updated > 0) {
                    // Update delivery record
                    psDelivery.setTimestamp(1, Timestamp.valueOf(LocalDateTime.now()));
                    psDelivery.setInt(2, orderId);
                    psDelivery.setInt(3, deliveryPersonId);
                    psDelivery.executeUpdate();

                    conn.commit();
                    return true;
                }

                conn.rollback();
                return false;

            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }

        } catch (SQLException e) {
            System.err.println("Error marking order as picked up: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Update delivery status to ON_THE_WAY
     */
    public static boolean markAsOnTheWay(int orderId) {
        String sql = "UPDATE Orders SET status = 'ON_THE_WAY' WHERE order_id = ? AND status = 'PICKED_UP'";

        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, orderId);
            int updated = ps.executeUpdate();
            return updated > 0;

        } catch (SQLException e) {
            System.err.println("Error marking order as on the way: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Complete delivery and mark as DELIVERED
     */
    public static boolean completeDelivery(int orderId, int deliveryPersonId, double deliveryFee) {
        String updateOrderSql = "UPDATE Orders SET status = 'DELIVERED' WHERE order_id = ? AND (status = 'ON_THE_WAY' OR status = 'PICKED_UP')";
        String updateDeliverySql = "UPDATE Deliveries SET status = 'Delivered', delivered_time = ? WHERE order_id = ? AND delivery_person_id = ?";
        String insertEarningSql = "INSERT INTO Earnings (delivery_person_id, order_id, amount, created_at) VALUES (?, ?, ?, ?)";

        try (Connection conn = Database.getConnection()) {
            conn.setAutoCommit(false);

            try (PreparedStatement psOrder = conn.prepareStatement(updateOrderSql);
                 PreparedStatement psDelivery = conn.prepareStatement(updateDeliverySql);
                 PreparedStatement psEarning = conn.prepareStatement(insertEarningSql)) {

                LocalDateTime now = LocalDateTime.now();

                // Update order status
                psOrder.setInt(1, orderId);
                int updated = psOrder.executeUpdate();

                if (updated > 0) {
                    // Update delivery record
                    psDelivery.setTimestamp(1, Timestamp.valueOf(now));
                    psDelivery.setInt(2, orderId);
                    psDelivery.setInt(3, deliveryPersonId);
                    psDelivery.executeUpdate();

                    // Record earning
                    psEarning.setInt(1, deliveryPersonId);
                    psEarning.setInt(2, orderId);
                    psEarning.setDouble(3, deliveryFee);
                    psEarning.setTimestamp(4, Timestamp.valueOf(now));
                    psEarning.executeUpdate();

                    conn.commit();
                    return true;
                }

                conn.rollback();
                return false;

            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }

        } catch (SQLException e) {
            System.err.println("Error completing delivery: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Get delivery statistics for a partner
     */
    public static DeliveryStats getDeliveryStats(int deliveryPersonId) {
        String sql = """
                SELECT 
                    COUNT(*) as total_deliveries,
                    SUM(CASE WHEN o.status = 'DELIVERED' THEN 1 ELSE 0 END) as completed,
                    SUM(CASE WHEN o.status IN ('ACCEPTED', 'PICKED_UP', 'ON_THE_WAY') THEN 1 ELSE 0 END) as in_progress,
                    COALESCE(SUM(e.amount), 0) as total_earnings
                FROM Deliveries d
                INNER JOIN Orders o ON d.order_id = o.order_id
                LEFT JOIN Earnings e ON e.order_id = o.order_id AND e.delivery_person_id = d.delivery_person_id
                WHERE d.delivery_person_id = ?
                """;

        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, deliveryPersonId);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                return new DeliveryStats(
                        rs.getInt("total_deliveries"),
                        rs.getInt("completed"),
                        rs.getInt("in_progress"),
                        rs.getDouble("total_earnings")
                );
            }

        } catch (SQLException e) {
            System.err.println("Error fetching delivery stats: " + e.getMessage());
            e.printStackTrace();
        }

        return new DeliveryStats(0, 0, 0, 0.0);
    }

    /**
     * Helper method to map ResultSet to Order object
     */
    private static Order mapResultSetToOrder(ResultSet rs) throws SQLException {
        int orderId = rs.getInt("order_id");
        int customerId = rs.getInt("customer_id");
        String productName = rs.getString("product_name");
        String description = rs.getString("description");
        String photo = rs.getString("photo");
        String deliveryLocation = rs.getString("delivery_location");
        String timeFrom = rs.getString("time_from");
        String timeTo = rs.getString("time_to");
        String timeRange = timeFrom + " - " + timeTo;
        double fee = rs.getDouble("fee");
        String statusStr = rs.getString("status");
        OrderStatus status = OrderStatus.valueOf(statusStr);
        Timestamp createdAt = rs.getTimestamp("created_at");
        LocalDateTime orderDate = createdAt.toLocalDateTime();

        // Get customer info from Users table
        String customerName = "";
        String customerPhone = "";
        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement("SELECT name, phone FROM Users WHERE user_id = ?")) {
            ps.setInt(1, customerId);
            ResultSet rsUser = ps.executeQuery();
            if (rsUser.next()) {
                customerName = rsUser.getString("name");
                customerPhone = rsUser.getString("phone");
            }
        }

        Order order = new Order(customerId, productName, description, deliveryLocation,
                timeRange, fee, "", customerName, customerPhone);
        order.setOrderId(orderId);
        order.setProductPhoto(photo);
        order.setStatus(status);
        order.setOrderDate(orderDate);

        return order;
    }

    /**
     * Inner class to hold delivery statistics
     */
    public static class DeliveryStats {
        private final int totalDeliveries;
        private final int completedDeliveries;
        private final int inProgressDeliveries;
        private final double totalEarnings;

        public DeliveryStats(int totalDeliveries, int completedDeliveries, int inProgressDeliveries, double totalEarnings) {
            this.totalDeliveries = totalDeliveries;
            this.completedDeliveries = completedDeliveries;
            this.inProgressDeliveries = inProgressDeliveries;
            this.totalEarnings = totalEarnings;
        }

        public int getTotalDeliveries() { return totalDeliveries; }
        public int getCompletedDeliveries() { return completedDeliveries; }
        public int getInProgressDeliveries() { return inProgressDeliveries; }
        public double getTotalEarnings() { return totalEarnings; }
    }
}

