package com.example.quickcommercedeliverysystemdesktop;

import com.example.quickcommercedeliverysystemdesktop.database.DBConnection;
import com.example.quickcommercedeliverysystemdesktop.database.DatabaseInitializer;

public class TestDB {
    public static void main(String[] args) {
        if(DBConnection.getConnection() != null){
            System.out.println("SQLite Connection Successful");
        }

        System.out.println("Starting DB initialization...");
        DatabaseInitializer.initialize();
        System.out.println("DB initialization finished. Check project-root/database/quickcommerce.db for file.");
    }
}
