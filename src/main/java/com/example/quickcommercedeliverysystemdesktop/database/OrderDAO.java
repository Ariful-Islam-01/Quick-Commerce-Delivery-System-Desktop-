package com.example.quickcommercedeliverysystemdesktop.database;

import com.example.quickcommercedeliverysystemdesktop.models.Order;
import com.example.quickcommercedeliverysystemdesktop.models.Order.OrderStatus;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class OrderDAO {

    // Create new order
    public static boolean createOrder(Order order) {
        String sql = "INSERT INTO Orders (customer_id, product_name, description, photo, " +
                     "delivery_location, time_from, time_to, fee, status, created_at) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            // Split time range into from and to
            String[] times = order.getDeliveryTimeRange().split(" - ");
            String timeFrom = times.length > 0 ? times[0].trim() : "";
            String timeTo = times.length > 1 ? times[1].trim() : "";

            ps.setInt(1, order.getCreatedByUserId());
            ps.setString(2, order.getProductName());
            ps.setString(3, order.getDescription());
            ps.setString(4, order.getProductPhoto());
            ps.setString(5, order.getDeliveryLocation());
            ps.setString(6, timeFrom);
            ps.setString(7, timeTo);
            ps.setDouble(8, order.getDeliveryFee());
            ps.setString(9, order.getStatus().name());
            ps.setString(10, Timestamp.valueOf(order.getOrderDate()).toString());

            ps.executeUpdate();

            // Get generated order ID
            ResultSet generatedKeys = ps.getGeneratedKeys();
            if (generatedKeys.next()) {
                int orderId = generatedKeys.getInt(1);

                // Create notification for the user
                NotificationDAO.createNotification(
                    order.getCreatedByUserId(),
                    "Order Created",
                    "Your order for '" + order.getProductName() + "' has been created and is waiting for a delivery partner.",
                    "ORDER_UPDATE",
                    orderId
                );
            }

            return true;
        } catch (Exception ex) {
            System.err.println("Error creating order: " + ex.getMessage());
            ex.printStackTrace();
            return false;
        }
    }

    // Get orders created by a specific user (My Orders)
    public static List<Order> getOrdersByUser(int userId) {
        List<Order> orders = new ArrayList<>();
        String sql = "SELECT * FROM Orders WHERE customer_id = ? ORDER BY created_at DESC";

        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                orders.add(mapResultSetToOrder(rs));
            }
        } catch (Exception ex) {
            System.err.println("Error fetching user orders: " + ex.getMessage());
        }

        return orders;
    }

    // Get all orders (for admin)
    public static List<Order> getAllOrders() {
        List<Order> orders = new ArrayList<>();
        String sql = "SELECT * FROM Orders ORDER BY created_at DESC";

        try (Connection conn = Database.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                orders.add(mapResultSetToOrder(rs));
            }
        } catch (Exception ex) {
            System.err.println("Error fetching all orders: " + ex.getMessage());
        }

        return orders;
    }

    // Update order
    public static boolean updateOrder(Order order) {
        String sql = "UPDATE Orders SET product_name=?, description=?, delivery_location=?, " +
                     "time_from=?, time_to=?, fee=?, status=? WHERE order_id=?";

        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            String[] times = order.getDeliveryTimeRange().split(" - ");
            String timeFrom = times.length > 0 ? times[0].trim() : "";
            String timeTo = times.length > 1 ? times[1].trim() : "";

            ps.setString(1, order.getProductName());
            ps.setString(2, order.getDescription());
            ps.setString(3, order.getDeliveryLocation());
            ps.setString(4, timeFrom);
            ps.setString(5, timeTo);
            ps.setDouble(6, order.getDeliveryFee());
            ps.setString(7, order.getStatus().name());
            ps.setInt(8, order.getOrderId());

            ps.executeUpdate();
            return true;
        } catch (Exception ex) {
            System.err.println("Error updating order: " + ex.getMessage());
            return false;
        }
    }

    // Cancel order
    public static boolean cancelOrder(int orderId) {
        String sql = "UPDATE Orders SET status='CANCELLED' WHERE order_id=?";

        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, orderId);
            ps.executeUpdate();
            return true;
        } catch (Exception ex) {
            System.err.println("Error cancelling order: " + ex.getMessage());
            return false;
        }
    }

    // Get order by ID
    public static Order getOrderById(int orderId) {
        String sql = "SELECT * FROM Orders WHERE order_id = ?";

        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, orderId);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                return mapResultSetToOrder(rs);
            }
        } catch (Exception ex) {
            System.err.println("Error fetching order: " + ex.getMessage());
        }

        return null;
    }

    // Helper method to map ResultSet to Order object
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
            ResultSet userRs = ps.executeQuery();
            if (userRs.next()) {
                customerName = userRs.getString("name");
                customerPhone = userRs.getString("phone");
            }
        } catch (Exception e) {
            // Ignore
        }

        Order order = new Order(orderId, customerId, null, productName, description, photo,
                               deliveryLocation, timeRange, fee, null, customerName, customerPhone,
                               status, orderDate, null, null, null, null);

        return order;
    }

    // ===== ADMIN METHODS =====

    /**
     * Get total order count
     */
    public static int getTotalOrderCount() {
        String sql = "SELECT COUNT(*) as count FROM Orders";
        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            if (rs.next()) {
                return rs.getInt("count");
            }
        } catch (Exception ex) {
            System.err.println("Get order count error: " + ex.getMessage());
        }
        return 0;
    }

    /**
     * Get orders created today
     */
    public static int getTodayOrderCount() {
        String sql = "SELECT COUNT(*) as count FROM Orders WHERE DATE(created_at) = DATE('now')";
        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            if (rs.next()) {
                return rs.getInt("count");
            }
        } catch (Exception ex) {
            System.err.println("Get today order count error: " + ex.getMessage());
        }
        return 0;
    }

    /**
     * Get order count by status
     */
    public static int getOrderCountByStatus(String status) {
        String sql = "SELECT COUNT(*) as count FROM Orders WHERE status = ?";
        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, status);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                return rs.getInt("count");
            }
        } catch (Exception ex) {
            System.err.println("Get order count by status error: " + ex.getMessage());
        }
        return 0;
    }

    /**
     * Get orders by status (admin filter)
     */
    public static List<Order> getOrdersByStatus(String status) {
        List<Order> orders = new ArrayList<>();
        String sql = "SELECT * FROM Orders WHERE status = ? ORDER BY created_at DESC";

        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, status);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                orders.add(mapResultSetToOrder(rs));
            }
        } catch (Exception ex) {
            System.err.println("Error fetching orders by status: " + ex.getMessage());
        }

        return orders;
    }

    /**
     * Get customer name for order
     */
    public static String getCustomerName(int customerId) {
        String sql = "SELECT name FROM Users WHERE user_id = ?";
        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, customerId);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                return rs.getString("name");
            }
        } catch (Exception ex) {
            System.err.println("Get customer name error: " + ex.getMessage());
        }
        return "Unknown";
    }

    /**
     * Delete order and related data (cascade) - ADMIN ONLY
     */
    public static boolean adminDeleteOrder(int orderId) {
        try (Connection conn = Database.getConnection()) {
            conn.setAutoCommit(false);

            try {
                // Delete from Deliveries
                String deleteDeliveries = "DELETE FROM Deliveries WHERE order_id = ?";
                try (PreparedStatement ps = conn.prepareStatement(deleteDeliveries)) {
                    ps.setInt(1, orderId);
                    ps.executeUpdate();
                }

                // Delete from Earnings
                String deleteEarnings = "DELETE FROM Earnings WHERE order_id = ?";
                try (PreparedStatement ps = conn.prepareStatement(deleteEarnings)) {
                    ps.setInt(1, orderId);
                    ps.executeUpdate();
                }

                // Delete from OrderHistory
                String deleteHistory = "DELETE FROM OrderHistory WHERE order_id = ?";
                try (PreparedStatement ps = conn.prepareStatement(deleteHistory)) {
                    ps.setInt(1, orderId);
                    ps.executeUpdate();
                }

                // Delete from Notifications
                String deleteNotifications = "DELETE FROM Notifications WHERE order_id = ?";
                try (PreparedStatement ps = conn.prepareStatement(deleteNotifications)) {
                    ps.setInt(1, orderId);
                    ps.executeUpdate();
                }

                // Delete from Ratings
                String deleteRatings = "DELETE FROM Ratings WHERE order_id = ?";
                try (PreparedStatement ps = conn.prepareStatement(deleteRatings)) {
                    ps.setInt(1, orderId);
                    ps.executeUpdate();
                }

                // Finally, delete the order itself
                String deleteOrder = "DELETE FROM Orders WHERE order_id = ?";
                try (PreparedStatement ps = conn.prepareStatement(deleteOrder)) {
                    ps.setInt(1, orderId);
                    ps.executeUpdate();
                }

                conn.commit();
                return true;

            } catch (Exception ex) {
                conn.rollback();
                System.err.println("Error deleting order (rolled back): " + ex.getMessage());
                ex.printStackTrace();
                return false;
            }
        } catch (Exception ex) {
            System.err.println("Database connection error: " + ex.getMessage());
            return false;
        }
    }
}

