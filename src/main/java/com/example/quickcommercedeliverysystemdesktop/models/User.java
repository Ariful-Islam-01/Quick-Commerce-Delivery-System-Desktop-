package com.example.quickcommercedeliverysystemdesktop.models;

public class User {
    private int userId;
    private String name;
    private String email;
    private String phone;
    private String defaultAddress;
    private String profileImage;

    // Full constructor
    public User(int id, String name, String email, String phone, String defaultAddress, String profileImage) {
        this.userId = id;
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.defaultAddress = defaultAddress;
        this.profileImage = profileImage;
    }

    // Basic constructor for login
    public User(int id, String name, String email, String phone) {
        this(id, name, email, phone, "", "");
    }

    // Getters
    public int getUserId() { return userId; }
    public String getName() { return name; }
    public String getEmail() { return email; }
    public String getPhone() { return phone; }
    public String getDefaultAddress() { return defaultAddress; }
    public String getProfileImage() { return profileImage; }

    // Setters
    public void setName(String name) { this.name = name; }
    public void setEmail(String email) { this.email = email; }
    public void setPhone(String phone) { this.phone = phone; }
    public void setDefaultAddress(String defaultAddress) { this.defaultAddress = defaultAddress; }
    public void setProfileImage(String profileImage) { this.profileImage = profileImage; }
}
