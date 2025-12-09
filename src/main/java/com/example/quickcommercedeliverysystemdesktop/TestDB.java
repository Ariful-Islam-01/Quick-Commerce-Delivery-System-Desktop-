package com.example.quickcommercedeliverysystemdesktop;

import com.example.quickcommercedeliverysystemdesktop.database.Database;

public class TestDB {
    public static void main(String[] args) {
        if(Database.getConnection() != null){
            System.out.println("SQLite Connection Successful");
        }
    }
}
