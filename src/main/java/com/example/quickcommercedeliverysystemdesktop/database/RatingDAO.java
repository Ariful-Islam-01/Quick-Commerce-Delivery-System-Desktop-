package com.example.quickcommercedeliverysystemdesktop.database;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for Rating operations
 * Handles rating CRUD operations and statistics
 */
public class RatingDAO {

    /**
     * Submit a rating for a delivered order
     * @param orderId The order that was delivered
     * @param customerId The customer giving the rating
     * @param deliveryPersonId The delivery person being rated
     * @param rating Rating value (1-5)
     * @param comment Optional comment
     * @return true if rating was saved successfully
     */
    public static boolean submitRating(int orderId, int customerId, int deliveryPersonId, int rating, String comment) {
        // First check if rating already exists for this order
        if (hasRating(orderId)) {
            System.err.println("Rating already exists for order #" + orderId);
            return false;
        }

        String sql = "INSERT INTO Ratings (order_id, customer_id, delivery_person_id, rating, comment, created_at) " +
                     "VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, orderId);
            ps.setInt(2, customerId);
            ps.setInt(3, deliveryPersonId);
            ps.setInt(4, rating);
            ps.setString(5, comment);
            ps.setTimestamp(6, Timestamp.valueOf(LocalDateTime.now()));

            int rowsInserted = ps.executeUpdate();
            return rowsInserted > 0;

        } catch (SQLException e) {
            System.err.println("Error submitting rating: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Check if a rating already exists for an order
     */
    public static boolean hasRating(int orderId) {
        String sql = "SELECT COUNT(*) FROM Ratings WHERE order_id = ?";

        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, orderId);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                return rs.getInt(1) > 0;
            }

        } catch (SQLException e) {
            System.err.println("Error checking rating: " + e.getMessage());
        }

        return false;
    }

    /**
     * Get rating for a specific order
     */
    public static Rating getRatingForOrder(int orderId) {
        String sql = "SELECT * FROM Ratings WHERE order_id = ?";

        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, orderId);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                return new Rating(
                    rs.getInt("rating_id"),
                    rs.getInt("order_id"),
                    rs.getInt("customer_id"),
                    rs.getInt("delivery_person_id"),
                    rs.getInt("rating"),
                    rs.getString("comment"),
                    rs.getTimestamp("created_at").toLocalDateTime()
                );
            }

        } catch (SQLException e) {
            System.err.println("Error fetching rating: " + e.getMessage());
        }

        return null;
    }

    /**
     * Get all ratings for a delivery person
     */
    public static List<Rating> getRatingsForDeliveryPerson(int deliveryPersonId) {
        List<Rating> ratings = new ArrayList<>();
        String sql = "SELECT * FROM Ratings WHERE delivery_person_id = ? ORDER BY created_at DESC";

        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, deliveryPersonId);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                ratings.add(new Rating(
                    rs.getInt("rating_id"),
                    rs.getInt("order_id"),
                    rs.getInt("customer_id"),
                    rs.getInt("delivery_person_id"),
                    rs.getInt("rating"),
                    rs.getString("comment"),
                    rs.getTimestamp("created_at").toLocalDateTime()
                ));
            }

        } catch (SQLException e) {
            System.err.println("Error fetching ratings: " + e.getMessage());
        }

        return ratings;
    }

    /**
     * Get average rating for a delivery person
     */
    public static double getAverageRating(int deliveryPersonId) {
        String sql = "SELECT AVG(rating) as avg_rating FROM Ratings WHERE delivery_person_id = ?";

        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, deliveryPersonId);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                return rs.getDouble("avg_rating");
            }

        } catch (SQLException e) {
            System.err.println("Error calculating average rating: " + e.getMessage());
        }

        return 0.0;
    }

    /**
     * Get total rating count for a delivery person
     */
    public static int getRatingCount(int deliveryPersonId) {
        String sql = "SELECT COUNT(*) FROM Ratings WHERE delivery_person_id = ?";

        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, deliveryPersonId);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                return rs.getInt(1);
            }

        } catch (SQLException e) {
            System.err.println("Error counting ratings: " + e.getMessage());
        }

        return 0;
    }

    /**
     * Get rating statistics for a delivery person
     */
    public static RatingStats getRatingStats(int deliveryPersonId) {
        String sql = """
                SELECT 
                    COUNT(*) as total_ratings,
                    AVG(rating) as avg_rating,
                    SUM(CASE WHEN rating = 5 THEN 1 ELSE 0 END) as five_star,
                    SUM(CASE WHEN rating = 4 THEN 1 ELSE 0 END) as four_star,
                    SUM(CASE WHEN rating = 3 THEN 1 ELSE 0 END) as three_star,
                    SUM(CASE WHEN rating = 2 THEN 1 ELSE 0 END) as two_star,
                    SUM(CASE WHEN rating = 1 THEN 1 ELSE 0 END) as one_star
                FROM Ratings 
                WHERE delivery_person_id = ?
                """;

        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, deliveryPersonId);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                return new RatingStats(
                    rs.getInt("total_ratings"),
                    rs.getDouble("avg_rating"),
                    rs.getInt("five_star"),
                    rs.getInt("four_star"),
                    rs.getInt("three_star"),
                    rs.getInt("two_star"),
                    rs.getInt("one_star")
                );
            }

        } catch (SQLException e) {
            System.err.println("Error fetching rating stats: " + e.getMessage());
        }

        return new RatingStats(0, 0.0, 0, 0, 0, 0, 0);
    }

    /**
     * Inner class to hold rating data
     */
    public static class Rating {
        private int ratingId;
        private int orderId;
        private int customerId;
        private int deliveryPersonId;
        private int rating;
        private String comment;
        private LocalDateTime createdAt;

        public Rating(int ratingId, int orderId, int customerId, int deliveryPersonId,
                     int rating, String comment, LocalDateTime createdAt) {
            this.ratingId = ratingId;
            this.orderId = orderId;
            this.customerId = customerId;
            this.deliveryPersonId = deliveryPersonId;
            this.rating = rating;
            this.comment = comment;
            this.createdAt = createdAt;
        }

        public int getRatingId() { return ratingId; }
        public int getOrderId() { return orderId; }
        public int getCustomerId() { return customerId; }
        public int getDeliveryPersonId() { return deliveryPersonId; }
        public int getRating() { return rating; }
        public String getComment() { return comment; }
        public LocalDateTime getCreatedAt() { return createdAt; }
    }

    /**
     * Inner class to hold rating statistics
     */
    public static class RatingStats {
        private int totalRatings;
        private double averageRating;
        private int fiveStarCount;
        private int fourStarCount;
        private int threeStarCount;
        private int twoStarCount;
        private int oneStarCount;

        public RatingStats(int totalRatings, double averageRating, int fiveStarCount,
                          int fourStarCount, int threeStarCount, int twoStarCount, int oneStarCount) {
            this.totalRatings = totalRatings;
            this.averageRating = averageRating;
            this.fiveStarCount = fiveStarCount;
            this.fourStarCount = fourStarCount;
            this.threeStarCount = threeStarCount;
            this.twoStarCount = twoStarCount;
            this.oneStarCount = oneStarCount;
        }

        public int getTotalRatings() { return totalRatings; }
        public double getAverageRating() { return averageRating; }
        public int getFiveStarCount() { return fiveStarCount; }
        public int getFourStarCount() { return fourStarCount; }
        public int getThreeStarCount() { return threeStarCount; }
        public int getTwoStarCount() { return twoStarCount; }
        public int getOneStarCount() { return oneStarCount; }

        public String getFormattedAverage() {
            return String.format("%.1f", averageRating);
        }

        public String getStarDisplay() {
            int fullStars = (int) Math.round(averageRating);
            StringBuilder stars = new StringBuilder();
            for (int i = 0; i < 5; i++) {
                stars.append(i < fullStars ? "⭐" : "☆");
            }
            return stars.toString();
        }
    }
}

