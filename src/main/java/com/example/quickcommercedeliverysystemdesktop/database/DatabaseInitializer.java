package com.example.quickcommercedeliverysystemdesktop.database;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

public class DatabaseInitializer {
    public static void initialize() {
        try(Connection conn = Database.getConnection();
            Statement stmt = conn.createStatement()) {

            // First, check and fix Notifications table structure
            verifyAndFixNotificationsTable(conn);

            // Check and fix Users table structure
            verifyAndFixUsersTable(conn);

            // USERS table
            String createUsers = """
                    CREATE TABLE IF NOT EXISTS users(
                        user_id INTEGER PRIMARY KEY AUTOINCREMENT,
                        name TEXT NOT NULL,
                        email TEXT UNIQUE NOT NULL,
                        phone TEXT,
                        password TEXT NOT NULL,
                        default_address TEXT,
                        profile_image TEXT,
                        is_admin INTEGER DEFAULT 0,
                        is_banned INTEGER DEFAULT 0,
                        created_at DATETIME DEFAULT CURRENT_TIMESTAMP
                    );
            """;

            // ORDERS table
            String createOrders = """
                    CREATE TABLE IF NOT EXISTS orders(
                        order_id INTEGER PRIMARY KEY AUTOINCREMENT,
                        customer_id INTEGER NOT NULL,
                        product_name TEXT NOT NULL,
                        description TEXT,
                        photo TEXT,
                        delivery_location TEXT NOT NULL,
                        time_from TEXT,
                        time_to TEXT,
                        fee REAL NOT NULL DEFAULT 0.0,
                        status TEXT DEFAULT 'Pending',
                        created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
                        FOREIGN KEY(customer_id) REFERENCES Users(user_id)
                    );
            """;

            // DELIVERIES table
            String createDeliveries = """
                    CREATE TABLE IF NOT EXISTS Deliveries (
                        delivery_id INTEGER PRIMARY KEY AUTOINCREMENT,
                        order_id INTEGER NOT NULL,
                        delivery_person_id INTEGER NOT NULL,
                        status TEXT DEFAULT 'Accepted',
                        pickup_time DATETIME,
                        delivered_time DATETIME,
                        created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
                        FOREIGN KEY (order_id) REFERENCES Orders(order_id),
                        FOREIGN KEY (delivery_person_id) REFERENCES Users(user_id)
                    );
                    """;

            // EARNINGS table
            String createEarnings = """
                    CREATE TABLE IF NOT EXISTS Earnings (
                        earning_id INTEGER PRIMARY KEY AUTOINCREMENT,
                        delivery_person_id INTEGER NOT NULL,
                        order_id INTEGER NOT NULL,
                        amount REAL NOT NULL,
                        created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
                        FOREIGN KEY (delivery_person_id) REFERENCES Users(user_id),
                        FOREIGN KEY (order_id) REFERENCES Orders(order_id)
                    );
                    """;

            // RATINGS table
            String createRatings = """
                    CREATE TABLE IF NOT EXISTS Ratings (
                        rating_id INTEGER PRIMARY KEY AUTOINCREMENT,
                        order_id INTEGER NOT NULL,
                        customer_id INTEGER NOT NULL,
                        delivery_person_id INTEGER NOT NULL,
                        rating INTEGER CHECK (rating >= 1 AND rating <= 5),
                        comment TEXT,
                        created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
                        FOREIGN KEY (order_id) REFERENCES Orders(order_id),
                        FOREIGN KEY (customer_id) REFERENCES Users(user_id),
                        FOREIGN KEY (delivery_person_id) REFERENCES Users(user_id)
                    );
                    """;

            // NOTIFICATIONS table
            String createNotifications = """
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
                    );
                    """;

            // ORDER HISTORY table
            String createOrderHistory = """
                    CREATE TABLE IF NOT EXISTS OrderHistory (
                        history_id INTEGER PRIMARY KEY AUTOINCREMENT,
                        order_id INTEGER NOT NULL,
                        status TEXT NOT NULL,
                        changed_at DATETIME DEFAULT CURRENT_TIMESTAMP,
                        changed_by INTEGER,
                        notes TEXT,
                        FOREIGN KEY (order_id) REFERENCES Orders(order_id),
                        FOREIGN KEY (changed_by) REFERENCES Users(user_id)
                    );
                    """;

            // Execute all create statements
            stmt.execute(createUsers);
            stmt.execute(createOrders);
            stmt.execute(createDeliveries);
            stmt.execute(createEarnings);
            stmt.execute(createRatings);
            stmt.execute(createNotifications);
            stmt.execute(createOrderHistory);

            System.out.println("✔ Database tables created successfully (Users, Orders, Deliveries, Earnings, Ratings, Notifications, OrderHistory).");

        } catch (Exception ex) {
            System.err.println("Error initializing database: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    /**
     * Verify and fix Notifications table structure
     */
    private static void verifyAndFixNotificationsTable(Connection conn) {
        try {
            // Check if Notifications table exists and has correct structure
            String checkColumns = "PRAGMA table_info(Notifications)";
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery(checkColumns)) {

                boolean hasTitle = false;
                boolean hasType = false;
                boolean hasOrderId = false;

                while (rs.next()) {
                    String columnName = rs.getString("name");
                    if ("title".equals(columnName)) hasTitle = true;
                    if ("type".equals(columnName)) hasType = true;
                    if ("order_id".equals(columnName)) hasOrderId = true;
                }

                // If missing required columns, drop and recreate
                if (!hasTitle || !hasType || !hasOrderId) {
                    System.out.println("⚠ Notifications table has incorrect structure - fixing...");

                    // Drop old table
                    stmt.execute("DROP TABLE IF EXISTS Notifications");
                    System.out.println("✓ Dropped old Notifications table");

                    // Table will be recreated below in the normal flow
                    System.out.println("✓ Notifications table will be recreated with correct structure");
                }
            }
        } catch (Exception e) {
            // Table doesn't exist yet, which is fine - it will be created below
            System.out.println("ℹ Notifications table will be created");
        }
    }

    /**
     * Verify and fix Users table structure by adding missing columns
     */
    private static void verifyAndFixUsersTable(Connection conn) {
        try {
            // Check if users table exists (case-insensitive search)
            String checkTable = "SELECT name FROM sqlite_master WHERE type='table' AND (name='Users' OR name='users')";
            String actualTableName = null;

            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery(checkTable)) {

                if (!rs.next()) {
                    // Table doesn't exist yet, it will be created
                    System.out.println("ℹ Users table will be created");
                    return;
                }
                actualTableName = rs.getString("name");
            }

            // Check existing columns using the actual table name
            String checkColumns = "PRAGMA table_info(" + actualTableName + ")";
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery(checkColumns)) {

                boolean hasIsAdmin = false;
                boolean hasIsBanned = false;

                while (rs.next()) {
                    String columnName = rs.getString("name");
                    if ("is_admin".equals(columnName)) hasIsAdmin = true;
                    if ("is_banned".equals(columnName)) hasIsBanned = true;
                }

                // Add missing columns
                if (!hasIsAdmin) {
                    System.out.println("⚠ Adding missing 'is_admin' column to " + actualTableName + " table...");
                    stmt.execute("ALTER TABLE " + actualTableName + " ADD COLUMN is_admin INTEGER DEFAULT 0");
                    System.out.println("✓ Added 'is_admin' column");
                }

                if (!hasIsBanned) {
                    System.out.println("⚠ Adding missing 'is_banned' column to " + actualTableName + " table...");
                    stmt.execute("ALTER TABLE " + actualTableName + " ADD COLUMN is_banned INTEGER DEFAULT 0");
                    System.out.println("✓ Added 'is_banned' column");
                }
            }
        } catch (Exception e) {
            System.err.println("Error verifying Users table: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
