package com.example.quickcommercedeliverysystemdesktop;

import com.example.quickcommercedeliverysystemdesktop.database.Database;
import com.example.quickcommercedeliverysystemdesktop.database.DatabaseInitializer;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

public class VerifyDatabaseSchema {
    public static void main(String[] args) {
        System.out.println("Initializing database...");
        DatabaseInitializer.initialize();

        System.out.println("\nListing all tables...");

        try (Connection conn = Database.getConnection();
             Statement stmt = conn.createStatement()) {

            // List all tables
            String listTables = "SELECT name FROM sqlite_master WHERE type='table' ORDER BY name";
            ResultSet tablesRs = stmt.executeQuery(listTables);
            System.out.println("Tables in database:");
            while (tablesRs.next()) {
                System.out.println("  - " + tablesRs.getString("name"));
            }
            tablesRs.close();

            System.out.println("\nVerifying Users table schema...");

            // Check Users table columns (try lowercase first)
            String checkColumns = "PRAGMA table_info(users)";
            ResultSet rs = stmt.executeQuery(checkColumns);

            boolean hasIsAdmin = false;
            boolean hasIsBanned = false;

            System.out.println("\nUsers table columns:");
            while (rs.next()) {
                String columnName = rs.getString("name");
                String columnType = rs.getString("type");
                String defValue = rs.getString("dflt_value");
                System.out.println("  - " + columnName + " (" + columnType + ")" +
                                   (defValue != null ? " DEFAULT " + defValue : ""));

                if ("is_admin".equals(columnName)) hasIsAdmin = true;
                if ("is_banned".equals(columnName)) hasIsBanned = true;
            }
            rs.close();

            System.out.println("\n" + "=".repeat(50));
            System.out.println("Column check results:");
            System.out.println("  has is_admin: " + (hasIsAdmin ? "✓ YES" : "✗ NO"));
            System.out.println("  has is_banned: " + (hasIsBanned ? "✓ YES" : "✗ NO"));
            System.out.println("=".repeat(50));

            if (hasIsAdmin && hasIsBanned) {
                System.out.println("\n✓✓✓ Database schema is CORRECT! ✓✓✓");
                System.out.println("The 'Get all users error' should now be fixed.");
            } else {
                System.out.println("\n✗✗✗ Database schema is INCORRECT! ✗✗✗");
                System.out.println("Please delete the database file and run again.");
            }

        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}

