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

                    // Get customer ID for notification
                    int customerId = getCustomerIdForOrder(orderId);
                    if (customerId > 0) {
                        // Notify customer
                        NotificationDAO.createNotification(
                            customerId,
                            "Order Accepted",
                            "A delivery partner has accepted your order #" + orderId,
                            "ORDER_UPDATE",
                            orderId
                        );
                    }

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

                    // Get customer ID for notification
                    int customerId = getCustomerIdForOrder(orderId);
                    if (customerId > 0) {
                        // Notify customer
                        NotificationDAO.createNotification(
                            customerId,
                            "Order Picked Up",
                            "Your order #" + orderId + " has been picked up by the delivery partner",
                            "DELIVERY_UPDATE",
                            orderId
                        );
                    }

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

            if (updated > 0) {
                // Get customer ID for notification
                int customerId = getCustomerIdForOrder(orderId);
                if (customerId > 0) {
                    // Notify customer
                    NotificationDAO.createNotification(
                        customerId,
                        "Order On The Way",
                        "Your order #" + orderId + " is on the way to your location!",
                        "DELIVERY_UPDATE",
                        orderId
                    );
                }
                return true;
            }

            return false;

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

                    // Get customer ID for notification
                    int customerId = getCustomerIdForOrder(orderId);
                    if (customerId > 0) {
                        // Notify customer
                        NotificationDAO.createNotification(
                            customerId,
                            "Order Delivered",
                            "Your order #" + orderId + " has been successfully delivered!",
                            "SUCCESS",
                            orderId
                        );
                    }

                    // Notify delivery partner about earnings
                    NotificationDAO.createNotification(
                        deliveryPersonId,
                        "Delivery Completed",
                        String.format("You earned $%.2f from order #%d", deliveryFee, orderId),
                        "EARNING",
                        orderId
                    );

                    // Notify all admins about successful delivery
                    java.util.List<Integer> adminIds = UserDAO.getAdminUserIds();
                    String deliveryPersonName = getUserNameById(deliveryPersonId);
                    for (Integer adminId : adminIds) {
                        NotificationDAO.createNotification(
                            adminId,
                            "Order Delivered Successfully",
                            "Order #" + orderId + " delivered by " + deliveryPersonName,
                            "SUCCESS",
                            orderId
                        );
                    }

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
     * Get earnings history for a delivery partner
     */
    public static List<EarningRecord> getEarningsHistory(int deliveryPersonId) {
        List<EarningRecord> earnings = new ArrayList<>();
        String sql = """
                SELECT e.earning_id, e.order_id, e.amount, e.created_at,
                       o.product_name, o.delivery_location
                FROM Earnings e
                INNER JOIN Orders o ON e.order_id = o.order_id
                WHERE e.delivery_person_id = ?
                ORDER BY e.created_at DESC
                """;

        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, deliveryPersonId);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                EarningRecord record = new EarningRecord(
                        rs.getInt("earning_id"),
                        rs.getInt("order_id"),
                        deliveryPersonId,
                        rs.getDouble("amount"),
                        rs.getTimestamp("created_at").toLocalDateTime(),
                        rs.getString("product_name"),
                        rs.getString("delivery_location")
                );
                earnings.add(record);
            }

        } catch (SQLException e) {
            System.err.println("Error fetching earnings history: " + e.getMessage());
            e.printStackTrace();
        }

        return earnings;
    }

    /**
     * Get earnings for a specific time period
     */
    public static double getEarningsForPeriod(int deliveryPersonId, String period) {
        String dateFilter = switch (period) {
            case "TODAY" -> "DATE(e.created_at, 'localtime') = DATE('now', 'localtime')";
            case "WEEK" -> "DATE(e.created_at, 'localtime') >= DATE('now', 'localtime', '-6 days')";
            case "MONTH" -> "DATE(e.created_at, 'localtime') >= DATE('now', 'localtime', '-29 days')";
            default -> "1=1"; // All time
        };

        String sql = String.format("""
                SELECT COALESCE(SUM(e.amount), 0) as total
                FROM Earnings e
                WHERE e.delivery_person_id = ? AND %s
                """, dateFilter);

        System.out.println("Getting earnings for period: " + period);
        System.out.println("SQL: " + sql);
        System.out.println("Delivery Person ID: " + deliveryPersonId);

        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, deliveryPersonId);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                double total = rs.getDouble("total");
                System.out.println("Result for " + period + ": " + total);
                return total;
            }

        } catch (SQLException e) {
            System.err.println("Error fetching period earnings: " + e.getMessage());
            e.printStackTrace();
        }

        return 0.0;
    }

    /**
     * Get daily earnings breakdown for the last N days
     */
    public static List<DailyEarning> getDailyEarnings(int deliveryPersonId, int days) {
        List<DailyEarning> dailyEarnings = new ArrayList<>();

        // Calculate the number of days to go back (days - 1 to include today)
        int daysBack = days - 1;

        String sql = """
                SELECT DATE(e.created_at, 'localtime') as earning_date,
                       COALESCE(SUM(e.amount), 0) as daily_total,
                       COUNT(*) as delivery_count
                FROM Earnings e
                WHERE e.delivery_person_id = ?
                  AND DATE(e.created_at, 'localtime') >= DATE('now', 'localtime', '-' || ? || ' days')
                  AND DATE(e.created_at, 'localtime') <= DATE('now', 'localtime')
                GROUP BY DATE(e.created_at, 'localtime')
                ORDER BY earning_date ASC
                """;

        System.out.println("Getting daily earnings for " + days + " days (daysBack=" + daysBack + ")");
        System.out.println("Delivery Person ID: " + deliveryPersonId);

        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, deliveryPersonId);
            ps.setInt(2, daysBack);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                String date = rs.getString("earning_date");
                double amount = rs.getDouble("daily_total");
                int count = rs.getInt("delivery_count");

                System.out.println("  Daily: " + date + " = ৳" + amount + " (" + count + " deliveries)");

                DailyEarning daily = new DailyEarning(date, amount, count);
                dailyEarnings.add(daily);
            }

            System.out.println("Total daily records: " + dailyEarnings.size());

        } catch (SQLException e) {
            System.err.println("Error fetching daily earnings: " + e.getMessage());
            e.printStackTrace();
        }

        return dailyEarnings;
    }

    /**
     * Helper method to get customer ID for an order
     */
    private static int getCustomerIdForOrder(int orderId) {
        String sql = "SELECT customer_id FROM Orders WHERE order_id = ?";

        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, orderId);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                return rs.getInt("customer_id");
            }

        } catch (SQLException e) {
            System.err.println("Error getting customer ID for order: " + e.getMessage());
            e.printStackTrace();
        }

        return -1;
    }

    /**
     * Helper method to get user name by ID
     */
    private static String getUserNameById(int userId) {
        String sql = "SELECT name FROM Users WHERE user_id = ?";

        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                return rs.getString("name");
            }

        } catch (SQLException e) {
            System.err.println("Error getting user name: " + e.getMessage());
        }

        return "Unknown User";
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

    /**
     * Inner class to hold earning record details
     */
    public static class EarningRecord {
        private final int earningId;
        private final int orderId;
        private final int deliveryPersonId;
        private final double amount;
        private final LocalDateTime earnedAt;
        private final String productName;
        private final String location;

        public EarningRecord(int earningId, int orderId, int deliveryPersonId, double amount,
                           LocalDateTime earnedAt, String productName, String location) {
            this.earningId = earningId;
            this.orderId = orderId;
            this.deliveryPersonId = deliveryPersonId;
            this.amount = amount;
            this.earnedAt = earnedAt;
            this.productName = productName;
            this.location = location;
        }

        public int getEarningId() { return earningId; }
        public int getOrderId() { return orderId; }
        public int getDeliveryPersonId() { return deliveryPersonId; }
        public double getAmount() { return amount; }
        public LocalDateTime getEarnedAt() { return earnedAt; }
        public String getProductName() { return productName; }
        public String getLocation() { return location; }

        public String getFormattedAmount() {
            return String.format("$%.2f", amount);
        }

        public String getFormattedDate() {
            return earnedAt.toLocalDate().toString();
        }

        public String getFormattedTime() {
            return earnedAt.toLocalTime().toString().substring(0, 5);
        }
    }

    /**
     * Inner class to hold daily earning summary
     */
    public static class DailyEarning {
        private final String date;
        private final double totalAmount;
        private final int deliveryCount;

        public DailyEarning(String date, double totalAmount, int deliveryCount) {
            this.date = date;
            this.totalAmount = totalAmount;
            this.deliveryCount = deliveryCount;
        }

        public String getDate() { return date; }
        public double getTotalAmount() { return totalAmount; }
        public int getDeliveryCount() { return deliveryCount; }

        public String getFormattedAmount() {
            return String.format("$%.2f", totalAmount);
        }
    }

    // ===== ADMIN METHODS =====

    /**
     * Get total delivery count across all users
     */
    public static int getTotalDeliveryCount() {
        String sql = "SELECT COUNT(*) as count FROM Deliveries";
        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            if (rs.next()) {
                return rs.getInt("count");
            }
        } catch (Exception ex) {
            System.err.println("Get total delivery count error: " + ex.getMessage());
        }
        return 0;
    }

    /**
     * Get deliveries completed today across all users
     */
    public static int getTodayDeliveryCount() {
        String sql = "SELECT COUNT(*) as count FROM Deliveries WHERE DATE(delivered_time) = DATE('now')";
        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            if (rs.next()) {
                return rs.getInt("count");
            }
        } catch (Exception ex) {
            System.err.println("Get today delivery count error: " + ex.getMessage());
        }
        return 0;
    }

    /**
     * Get total earnings across all delivery partners
     */
    public static double getTotalEarnings() {
        String sql = "SELECT COALESCE(SUM(amount), 0) as total FROM Earnings";
        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            if (rs.next()) {
                return rs.getDouble("total");
            }
        } catch (Exception ex) {
            System.err.println("Get total earnings error: " + ex.getMessage());
        }
        return 0.0;
    }

    /**
     * Get today's earnings across all delivery partners
     */
    public static double getTodayEarnings() {
        String sql = "SELECT COALESCE(SUM(amount), 0) as total FROM Earnings WHERE DATE(created_at) = DATE('now')";
        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            if (rs.next()) {
                return rs.getDouble("total");
            }
        } catch (Exception ex) {
            System.err.println("Get today earnings error: " + ex.getMessage());
        }
        return 0.0;
    }

    /**
     * Get all earnings with delivery person details (ADMIN)
     */
    public static List<AdminEarningRecord> getAllEarningsWithDetails() {
        List<AdminEarningRecord> earnings = new ArrayList<>();
        String sql = """
                SELECT e.earning_id, e.order_id, e.delivery_person_id, e.amount, e.created_at,
                       u.name as delivery_person_name,
                       o.product_name, o.customer_id,
                       c.name as customer_name
                FROM Earnings e
                INNER JOIN Users u ON e.delivery_person_id = u.user_id
                INNER JOIN Orders o ON e.order_id = o.order_id
                INNER JOIN Users c ON o.customer_id = c.user_id
                ORDER BY e.created_at DESC
                """;

        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                AdminEarningRecord record = new AdminEarningRecord(
                        rs.getInt("earning_id"),
                        rs.getInt("order_id"),
                        rs.getInt("delivery_person_id"),
                        rs.getString("delivery_person_name"),
                        rs.getInt("customer_id"),
                        rs.getString("customer_name"),
                        rs.getString("product_name"),
                        rs.getDouble("amount"),
                        rs.getTimestamp("created_at").toLocalDateTime()
                );
                earnings.add(record);
            }

        } catch (SQLException e) {
            System.err.println("Error fetching all earnings: " + e.getMessage());
            e.printStackTrace();
        }

        return earnings;
    }

    /**
     * Get earnings filtered by delivery person (ADMIN)
     */
    public static List<AdminEarningRecord> getEarningsByDeliveryPerson(int deliveryPersonId) {
        List<AdminEarningRecord> earnings = new ArrayList<>();
        String sql = """
                SELECT e.earning_id, e.order_id, e.delivery_person_id, e.amount, e.created_at,
                       u.name as delivery_person_name,
                       o.product_name, o.customer_id,
                       c.name as customer_name
                FROM Earnings e
                INNER JOIN Users u ON e.delivery_person_id = u.user_id
                INNER JOIN Orders o ON e.order_id = o.order_id
                INNER JOIN Users c ON o.customer_id = c.user_id
                WHERE e.delivery_person_id = ?
                ORDER BY e.created_at DESC
                """;

        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, deliveryPersonId);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                AdminEarningRecord record = new AdminEarningRecord(
                        rs.getInt("earning_id"),
                        rs.getInt("order_id"),
                        rs.getInt("delivery_person_id"),
                        rs.getString("delivery_person_name"),
                        rs.getInt("customer_id"),
                        rs.getString("customer_name"),
                        rs.getString("product_name"),
                        rs.getDouble("amount"),
                        rs.getTimestamp("created_at").toLocalDateTime()
                );
                earnings.add(record);
            }

        } catch (SQLException e) {
            System.err.println("Error fetching earnings by delivery person: " + e.getMessage());
            e.printStackTrace();
        }

        return earnings;
    }

    /**
     * Get earnings filtered by date range (ADMIN)
     */
    public static List<AdminEarningRecord> getEarningsByDateRange(String fromDate, String toDate) {
        List<AdminEarningRecord> earnings = new ArrayList<>();
        String sql = """
                SELECT e.earning_id, e.order_id, e.delivery_person_id, e.amount, e.created_at,
                       u.name as delivery_person_name,
                       o.product_name, o.customer_id,
                       c.name as customer_name
                FROM Earnings e
                INNER JOIN Users u ON e.delivery_person_id = u.user_id
                INNER JOIN Orders o ON e.order_id = o.order_id
                INNER JOIN Users c ON o.customer_id = c.user_id
                WHERE DATE(e.created_at) BETWEEN ? AND ?
                ORDER BY e.created_at DESC
                """;

        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, fromDate);
            ps.setString(2, toDate);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                AdminEarningRecord record = new AdminEarningRecord(
                        rs.getInt("earning_id"),
                        rs.getInt("order_id"),
                        rs.getInt("delivery_person_id"),
                        rs.getString("delivery_person_name"),
                        rs.getInt("customer_id"),
                        rs.getString("customer_name"),
                        rs.getString("product_name"),
                        rs.getDouble("amount"),
                        rs.getTimestamp("created_at").toLocalDateTime()
                );
                earnings.add(record);
            }

        } catch (SQLException e) {
            System.err.println("Error fetching earnings by date range: " + e.getMessage());
            e.printStackTrace();
        }

        return earnings;
    }

    /**
     * Get all delivery persons who have earnings (ADMIN)
     */
    public static List<DeliveryPersonSummary> getDeliveryPersonsWithEarnings() {
        List<DeliveryPersonSummary> persons = new ArrayList<>();
        String sql = """
                SELECT u.user_id, u.name,
                       COUNT(e.earning_id) as delivery_count,
                       COALESCE(SUM(e.amount), 0) as total_earned
                FROM Users u
                INNER JOIN Earnings e ON u.user_id = e.delivery_person_id
                GROUP BY u.user_id, u.name
                ORDER BY total_earned DESC
                """;

        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                DeliveryPersonSummary person = new DeliveryPersonSummary(
                        rs.getInt("user_id"),
                        rs.getString("name"),
                        rs.getInt("delivery_count"),
                        rs.getDouble("total_earned")
                );
                persons.add(person);
            }

        } catch (SQLException e) {
            System.err.println("Error fetching delivery persons with earnings: " + e.getMessage());
            e.printStackTrace();
        }

        return persons;
    }

    /**
     * Inner class for admin earning records with full details
     */
    public static class AdminEarningRecord {
        private final int earningId;
        private final int orderId;
        private final int deliveryPersonId;
        private final String deliveryPersonName;
        private final int customerId;
        private final String customerName;
        private final String productName;
        private final double amount;
        private final LocalDateTime earnedAt;

        public AdminEarningRecord(int earningId, int orderId, int deliveryPersonId, String deliveryPersonName,
                                int customerId, String customerName, String productName, double amount,
                                LocalDateTime earnedAt) {
            this.earningId = earningId;
            this.orderId = orderId;
            this.deliveryPersonId = deliveryPersonId;
            this.deliveryPersonName = deliveryPersonName;
            this.customerId = customerId;
            this.customerName = customerName;
            this.productName = productName;
            this.amount = amount;
            this.earnedAt = earnedAt;
        }

        public int getEarningId() { return earningId; }
        public int getOrderId() { return orderId; }
        public int getDeliveryPersonId() { return deliveryPersonId; }
        public String getDeliveryPersonName() { return deliveryPersonName; }
        public int getCustomerId() { return customerId; }
        public String getCustomerName() { return customerName; }
        public String getProductName() { return productName; }
        public double getAmount() { return amount; }
        public LocalDateTime getEarnedAt() { return earnedAt; }

        public String getFormattedAmount() {
            return String.format("$%.2f", amount);
        }

        public String getFormattedDate() {
            return earnedAt.toLocalDate().toString();
        }

        public String getFormattedDateTime() {
            return earnedAt.toString().replace("T", " ").substring(0, 16);
        }
    }

    /**
     * Inner class for delivery person summary
     */
    public static class DeliveryPersonSummary {
        private final int userId;
        private final String name;
        private final int deliveryCount;
        private final double totalEarned;

        public DeliveryPersonSummary(int userId, String name, int deliveryCount, double totalEarned) {
            this.userId = userId;
            this.name = name;
            this.deliveryCount = deliveryCount;
            this.totalEarned = totalEarned;
        }

        public int getUserId() { return userId; }
        public String getName() { return name; }
        public int getDeliveryCount() { return deliveryCount; }
        public double getTotalEarned() { return totalEarned; }

        public String getFormattedEarned() {
            return String.format("$%.2f", totalEarned);
        }

        @Override
        public String toString() {
            return name + " (" + deliveryCount + " deliveries, " + getFormattedEarned() + ")";
        }
    }
}

