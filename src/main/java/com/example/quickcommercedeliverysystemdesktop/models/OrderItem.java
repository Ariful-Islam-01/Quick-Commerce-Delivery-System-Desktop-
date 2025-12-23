package com.example.quickcommercedeliverysystemdesktop.models;

public class OrderItem {
    private int itemId;
    private String productName;
    private int quantity;
    private double price;
    private double subtotal;

    // Full constructor
    public OrderItem(int itemId, String productName, int quantity, double price) {
        this.itemId = itemId;
        this.productName = productName;
        this.quantity = quantity;
        this.price = price;
        this.subtotal = quantity * price;
    }

    // Constructor without itemId (for new items)
    public OrderItem(String productName, int quantity, double price) {
        this(0, productName, quantity, price);
    }

    // Getters
    public int getItemId() { return itemId; }
    public String getProductName() { return productName; }
    public int getQuantity() { return quantity; }
    public double getPrice() { return price; }
    public double getSubtotal() { return subtotal; }

    // Setters
    public void setItemId(int itemId) { this.itemId = itemId; }
    public void setProductName(String productName) { this.productName = productName; }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
        calculateSubtotal();
    }

    public void setPrice(double price) {
        this.price = price;
        calculateSubtotal();
    }

    // Helper method
    private void calculateSubtotal() {
        this.subtotal = this.quantity * this.price;
    }

    public String getFormattedPrice() {
        return String.format("$%.2f", price);
    }

    public String getFormattedSubtotal() {
        return String.format("$%.2f", subtotal);
    }

    @Override
    public String toString() {
        return "OrderItem{" +
                "productName='" + productName + '\'' +
                ", quantity=" + quantity +
                ", price=" + price +
                ", subtotal=" + subtotal +
                '}';
    }
}

