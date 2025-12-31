package com.example.quickcommercedeliverysystemdesktop.database;

import com.example.quickcommercedeliverysystemdesktop.models.User;
import com.example.quickcommercedeliverysystemdesktop.utils.PasswordUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class UserDAO {

    // Register user (returns true if success)
    public static boolean register(String name, String email, String phone, String passwordPlain) {
        String sql = "INSERT INTO Users (name, email, phone, password, default_address, profile_image) VALUES (?, ?, ?, ?, ?, ?)";
        String hashed = PasswordUtil.hash(passwordPlain);
        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, name);
            ps.setString(2, email);
            ps.setString(3, phone);
            ps.setString(4, hashed);
            ps.setString(5, ""); // default address empty
            ps.setString(6, ""); // profile image path empty

            ps.executeUpdate();
            return true;
        } catch (Exception ex) {
            System.err.println("Register error: " + ex.getMessage());
            return false;
        }
    }

    // Login (returns User if success)
    public static User login(String email, String passwordPlain) {
        String sql = "SELECT user_id, name, email, phone, default_address, profile_image FROM Users WHERE email = ? AND password = ?";
        String hashed = PasswordUtil.hash(passwordPlain);
        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, email);
            ps.setString(2, hashed);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new User(
                            rs.getInt("user_id"),
                            rs.getString("name"),
                            rs.getString("email"),
                            rs.getString("phone"),
                            rs.getString("default_address"),
                            rs.getString("profile_image")
                    );
                }
            }
        } catch (Exception ex) {
            System.err.println("Login error: " + ex.getMessage());
        }
        return null;
    }

    // Get user by ID (returns full User object)
    public static User getUserById(int userId) {
        String sql = "SELECT user_id, name, email, phone, default_address, profile_image FROM Users WHERE user_id = ?";
        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, userId);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new User(
                            rs.getInt("user_id"),
                            rs.getString("name"),
                            rs.getString("email"),
                            rs.getString("phone"),
                            rs.getString("default_address"),
                            rs.getString("profile_image")
                    );
                }
            }
        } catch (Exception ex) {
            System.err.println("Get user error: " + ex.getMessage());
        }
        return null;
    }

    // Update user profile (name, email, phone)
    public static boolean updateProfile(int userId, String name, String email, String phone) {
        String sql = "UPDATE Users SET name = ?, email = ?, phone = ? WHERE user_id = ?";
        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, name);
            ps.setString(2, email);
            ps.setString(3, phone);
            ps.setInt(4, userId);

            int rowsAffected = ps.executeUpdate();
            return rowsAffected > 0;
        } catch (Exception ex) {
            System.err.println("Update profile error: " + ex.getMessage());
            return false;
        }
    }

    // Update default address
    public static boolean updateAddress(int userId, String address) {
        String sql = "UPDATE Users SET default_address = ? WHERE user_id = ?";
        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, address);
            ps.setInt(2, userId);

            int rowsAffected = ps.executeUpdate();
            return rowsAffected > 0;
        } catch (Exception ex) {
            System.err.println("Update address error: " + ex.getMessage());
            return false;
        }
    }

    // Update profile image path
    public static boolean updateProfileImage(int userId, String imagePath) {
        String sql = "UPDATE Users SET profile_image = ? WHERE user_id = ?";
        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, imagePath);
            ps.setInt(2, userId);

            int rowsAffected = ps.executeUpdate();
            return rowsAffected > 0;
        } catch (Exception ex) {
            System.err.println("Update profile image error: " + ex.getMessage());
            return false;
        }
    }

    // Update password
    public static boolean updatePassword(int userId, String currentPassword, String newPassword) {
        // First verify current password
        String checkSql = "SELECT user_id FROM Users WHERE user_id = ? AND password = ?";
        String hashedCurrent = PasswordUtil.hash(currentPassword);

        try (Connection conn = Database.getConnection();
             PreparedStatement checkPs = conn.prepareStatement(checkSql)) {

            checkPs.setInt(1, userId);
            checkPs.setString(2, hashedCurrent);

            try (ResultSet rs = checkPs.executeQuery()) {
                if (!rs.next()) {
                    return false; // Current password incorrect
                }
            }

            // Update to new password
            String updateSql = "UPDATE Users SET password = ? WHERE user_id = ?";
            String hashedNew = PasswordUtil.hash(newPassword);

            try (PreparedStatement updatePs = conn.prepareStatement(updateSql)) {
                updatePs.setString(1, hashedNew);
                updatePs.setInt(2, userId);

                int rowsAffected = updatePs.executeUpdate();
                return rowsAffected > 0;
            }
        } catch (Exception ex) {
            System.err.println("Update password error: " + ex.getMessage());
            return false;
        }
    }

    // ===== ADMIN METHODS =====

    /**
     * Get all users (for admin dashboard)
     */
    public static java.util.List<User> getAllUsers() {
        java.util.List<User> users = new java.util.ArrayList<>();
        String sql = "SELECT user_id, name, email, phone, default_address, profile_image, " +
                    "COALESCE(is_admin, 0) as is_admin, COALESCE(is_banned, 0) as is_banned " +
                    "FROM Users ORDER BY created_at DESC";

        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                users.add(new User(
                    rs.getInt("user_id"),
                    rs.getString("name"),
                    rs.getString("email"),
                    rs.getString("phone"),
                    rs.getString("default_address"),
                    rs.getString("profile_image"),
                    rs.getInt("is_admin") == 1,
                    rs.getInt("is_banned") == 1
                ));
            }
        } catch (Exception ex) {
            System.err.println("Get all users error: " + ex.getMessage());
        }
        return users;
    }

    /**
     * Get total user count
     */
    public static int getTotalUserCount() {
        String sql = "SELECT COUNT(*) as count FROM Users";
        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            if (rs.next()) {
                return rs.getInt("count");
            }
        } catch (Exception ex) {
            System.err.println("Get user count error: " + ex.getMessage());
        }
        return 0;
    }

    /**
     * Get users registered today
     */
    public static int getTodayUserCount() {
        String sql = "SELECT COUNT(*) as count FROM Users WHERE DATE(created_at) = DATE('now')";
        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            if (rs.next()) {
                return rs.getInt("count");
            }
        } catch (Exception ex) {
            System.err.println("Get today user count error: " + ex.getMessage());
        }
        return 0;
    }

    /**
     * Ban/Unban a user (Admin only)
     */
    public static boolean setBanStatus(int userId, boolean banned) {
        String sql = "UPDATE Users SET is_banned = ? WHERE user_id = ?";
        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, banned ? 1 : 0);
            ps.setInt(2, userId);

            int rowsAffected = ps.executeUpdate();
            return rowsAffected > 0;
        } catch (Exception ex) {
            System.err.println("Set ban status error: " + ex.getMessage());
            return false;
        }
    }

    /**
     * Set admin status for a user (Admin only)
     */
    public static boolean setAdminStatus(int userId, boolean isAdmin) {
        String sql = "UPDATE Users SET is_admin = ? WHERE user_id = ?";
        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, isAdmin ? 1 : 0);
            ps.setInt(2, userId);

            int rowsAffected = ps.executeUpdate();
            return rowsAffected > 0;
        } catch (Exception ex) {
            System.err.println("Set admin status error: " + ex.getMessage());
            return false;
        }
    }

    /**
     * Delete a user (Admin only)
     */
    public static boolean deleteUser(int userId) {
        String sql = "DELETE FROM Users WHERE user_id = ?";
        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, userId);

            int rowsAffected = ps.executeUpdate();
            return rowsAffected > 0;
        } catch (Exception ex) {
            System.err.println("Delete user error: " + ex.getMessage());
            return false;
        }
    }

    /**
     * Admin update user info
     */
    public static boolean adminUpdateUser(int userId, String name, String email, String phone, String address) {
        String sql = "UPDATE Users SET name = ?, email = ?, phone = ?, default_address = ? WHERE user_id = ?";
        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, name);
            ps.setString(2, email);
            ps.setString(3, phone);
            ps.setString(4, address);
            ps.setInt(5, userId);

            int rowsAffected = ps.executeUpdate();
            return rowsAffected > 0;
        } catch (Exception ex) {
            System.err.println("Admin update user error: " + ex.getMessage());
            return false;
        }
    }
}
