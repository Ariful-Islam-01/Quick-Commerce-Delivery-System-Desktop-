package com.example.quickcommercedeliverysystemdesktop;

import com.example.quickcommercedeliverysystemdesktop.database.DatabaseInitializer;
import com.example.quickcommercedeliverysystemdesktop.database.UserDAO;
import com.example.quickcommercedeliverysystemdesktop.models.User;

import java.util.Scanner;

/**
 * Admin Account Creator Tool
 * Run this class to create an admin account
 */
public class CreateAdminAccount {

    public static void main(String[] args) {
        System.out.println("=== Quick Commerce Admin Account Creator ===\n");

        // Initialize database
        DatabaseInitializer.initialize();

        Scanner scanner = new Scanner(System.in);

        System.out.print("Enter admin name: ");
        String name = scanner.nextLine().trim();

        System.out.print("Enter admin email: ");
        String email = scanner.nextLine().trim();

        System.out.print("Enter admin phone: ");
        String phone = scanner.nextLine().trim();

        System.out.print("Enter admin password: ");
        String password = scanner.nextLine().trim();

        if (name.isEmpty() || email.isEmpty() || password.isEmpty()) {
            System.err.println("\n❌ Error: Name, email, and password are required!");
            return;
        }

        // Create the user account
        boolean created = UserDAO.register(name, email, phone, password);

        if (!created) {
            System.err.println("\n❌ Error: Failed to create account. Email may already exist.");
            return;
        }

        System.out.println("\n✅ User account created successfully!");

        // Get the user to find their ID
        User user = UserDAO.login(email, password);

        if (user == null) {
            System.err.println("❌ Error: Failed to retrieve created user.");
            return;
        }

        // Set admin status
        boolean adminSet = UserDAO.setAdminStatus(user.getUserId(), true);

        if (adminSet) {
            System.out.println("✅ Admin status granted successfully!\n");
            System.out.println("═══════════════════════════════════");
            System.out.println("Admin Account Created:");
            System.out.println("  Name: " + name);
            System.out.println("  Email: " + email);
            System.out.println("  Password: " + password);
            System.out.println("  Role: ADMIN");
            System.out.println("═══════════════════════════════════");
            System.out.println("\n🎉 You can now login with these credentials!");
        } else {
            System.err.println("❌ Error: Failed to set admin status.");
        }

        scanner.close();
    }
}

