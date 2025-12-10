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
        String sql = "SELECT user_id, name, email, phone FROM Users WHERE email = ? AND password = ?";
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
                            rs.getString("phone")
                    );
                }
            }
        } catch (Exception ex) {
            System.err.println("Login error: " + ex.getMessage());
        }
        return null;
    }
}
