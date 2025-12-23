package com.example.quickcommercedeliverysystemdesktop.models;

public class User {
    private int userId;
    private String name;
    private String email;
    private String phone;
    private String defaultAddress;
    private String profileImage;
    private boolean isAdmin;
    private boolean isBanned;

    // Full constructor
    public User(int id, String name, String email, String phone, String defaultAddress,
                String profileImage, boolean isAdmin, boolean isBanned) {
        this.userId = id;
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.defaultAddress = defaultAddress;
        this.profileImage = profileImage;
        this.isAdmin = isAdmin;
        this.isBanned = isBanned;
    }

    // Constructor without admin/banned flags (for backward compatibility)
    public User(int id, String name, String email, String phone, String defaultAddress, String profileImage) {
        this(id, name, email, phone, defaultAddress, profileImage, false, false);
    }

    // Basic constructor for login
    public User(int id, String name, String email, String phone) {
        this(id, name, email, phone, "", "", false, false);
    }

    // Getters
    public int getUserId() { return userId; }
    public String getName() { return name; }
    public String getEmail() { return email; }
    public String getPhone() { return phone; }
    public String getDefaultAddress() { return defaultAddress; }
    public String getProfileImage() { return profileImage; }
    public boolean isAdmin() { return isAdmin; }
    public boolean isBanned() { return isBanned; }

    // Setters
    public void setName(String name) { this.name = name; }
    public void setEmail(String email) { this.email = email; }
    public void setPhone(String phone) { this.phone = phone; }
    public void setDefaultAddress(String defaultAddress) { this.defaultAddress = defaultAddress; }
    public void setProfileImage(String profileImage) { this.profileImage = profileImage; }
    public void setAdmin(boolean admin) { isAdmin = admin; }
    public void setBanned(boolean banned) { isBanned = banned; }
}

