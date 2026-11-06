package com.zut.campusbite;

public class FoodItem {
    private int itemId;
    private String itemName;
    private double price;
    private boolean isAvailable;
    private String imageData;

    public FoodItem(int itemId, String itemName, double price, boolean isAvailable) {
        this(itemId, itemName, price, isAvailable, null);
    }

    public FoodItem(int itemId, String itemName, double price, boolean isAvailable, String imageData) {
        this.itemId = itemId;
        this.itemName = validateItemName(itemName);
        this.price = validatePrice(price);
        this.isAvailable = isAvailable;
        this.imageData = validateImageData(imageData);
    }

    private static String validateImageData(String imageData) {
        if (imageData == null || imageData.isBlank()) return null;
        if (!imageData.startsWith("data:image/")) throw new IllegalArgumentException("Food image must be a valid image file");
        if (imageData.length() > 3_000_000) throw new IllegalArgumentException("Food image is too large. Choose an image under 2 MB.");
        return imageData;
    }

    public int getItemId() { return itemId; }
    public void setItemId(int itemId) { this.itemId = itemId; }
    public String getItemName() { return itemName; }
    public void setItemName(String itemName) { this.itemName = validateItemName(itemName); }
    private static String validateItemName(String itemName) {
        if (itemName == null || itemName.isBlank()) throw new IllegalArgumentException("itemName is required");
        return itemName.trim();
    }
    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = validatePrice(price); }
    private static double validatePrice(double price) {
        if (!Double.isFinite(price) || price < 0) throw new IllegalArgumentException("price must be zero or more");
        return price;
    }
    public boolean isAvailable() { return isAvailable; }
    public String getImageData() { return imageData; }
    public void setImageData(String imageData) { this.imageData = validateImageData(imageData); }
    public void setAvailable(boolean available) { isAvailable = available; }
}
