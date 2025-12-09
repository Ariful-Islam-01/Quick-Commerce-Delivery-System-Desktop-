package com.example.quickcommercedeliverysystemdesktop.database;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;

public class Database {
    public static Connection getConnection() {
        try {
            // Ensure folder exists
            File folder = new File("database");
            if (!folder.exists()) {
                folder.mkdirs();
            }

            String url = "jdbc:sqlite:database/quickcommerce.db";
            return DriverManager.getConnection(url);

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

}
