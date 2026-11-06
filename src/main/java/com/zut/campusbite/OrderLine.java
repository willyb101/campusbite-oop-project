package com.zut.campusbite;

public class OrderLine {
    private final int foodItemId;
    private final String itemName;
    private final int quantity;
    private final double unitPrice;

    public OrderLine(int foodItemId, String itemName, int quantity, double unitPrice) {
        this.foodItemId = foodItemId;
        this.itemName = itemName;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
    }
    public int getFoodItemId() { return foodItemId; }
    public String getItemName() { return itemName; }
    public int getQuantity() { return quantity; }
    public double getUnitPrice() { return unitPrice; }
    public double getLineTotal() { return quantity * unitPrice; }
}
