package com.example.quickcommercedeliverysystemdesktop.database;

import java.sql.Connection;
import java.sql.Statement;

public class DatabaseInitializer {
    public static void initialize() {
        try(Connection conn = DBConnection.getConnection();
            Statement stmt = conn.createStatement()) {

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
                        message TEXT NOT NULL,
                        is_read INTEGER DEFAULT 0,
                        created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
                        FOREIGN KEY (user_id) REFERENCES Users(user_id)
                    );
                    """;

            // Execute all create statements
            stmt.execute(createUsers);
            stmt.execute(createOrders);
            stmt.execute(createDeliveries);
            stmt.execute(createEarnings);
            stmt.execute(createRatings);
            stmt.execute(createNotifications);

            System.out.println("✔ Database tables created successfully (Users, Orders, Deliveries, Earnings, Ratings, Notifications).");

        } catch (Exception ex) {
            System.err.println("Error initializing database: " + ex.getMessage());
            ex.printStackTrace();
        }
    }
}
