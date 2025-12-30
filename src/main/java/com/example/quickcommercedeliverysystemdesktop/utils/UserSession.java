package com.example.quickcommercedeliverysystemdesktop.utils;

import com.example.quickcommercedeliverysystemdesktop.models.User;

/**
 * Singleton class to maintain the current logged-in user session
 */
public class UserSession {
    private static UserSession instance;
    private User currentUser;

    private UserSession() {
        // Private constructor for singleton
    }

    public static UserSession getInstance() {
        if (instance == null) {
            instance = new UserSession();
        }
        return instance;
    }

    public void setCurrentUser(User user) {
        this.currentUser = user;
    }

    public User getCurrentUser() {
        return currentUser;
    }

    public boolean isLoggedIn() {
        return currentUser != null;
    }

    public void clearSession() {
        currentUser = null;
    }

    // Convenience methods
    public int getUserId() {
        return currentUser != null ? currentUser.getUserId() : -1;
    }

    public String getUserName() {
        return currentUser != null ? currentUser.getName() : "";
    }

    public String getUserEmail() {
        return currentUser != null ? currentUser.getEmail() : "";
    }

    public String getUserPhone() {
        return currentUser != null ? currentUser.getPhone() : "";
    }

    public String getRole() {
        // In this system, all users can be both customers and delivery partners
        // Return "CUSTOMER" as default - can be enhanced later to track role preference
        return currentUser != null ? "CUSTOMER" : "";
    }
}
