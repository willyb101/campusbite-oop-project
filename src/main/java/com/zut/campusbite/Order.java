package com.zut.campusbite;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public class Order {
    private int orderId;
    private final int studentId;
    private final String studentName;
    private final List<OrderLine> items;
    private final double totalCost;
    private final String deliveryLocation;
    private final String notes;
    private OrderStatus status;
    private final String createdAt;
    private final String preparingAt;
    private final String inTransitAt;
    private final String deliveredAt;

    public Order(int orderId, int studentId, String studentName, List<OrderLine> items,
                 double totalCost, String deliveryLocation, String notes, OrderStatus status, String createdAt) {
        this(orderId, studentId, studentName, items, totalCost, deliveryLocation, notes, status, createdAt, null, null, null);
    }

    public Order(int orderId, int studentId, String studentName, List<OrderLine> items,
                 double totalCost, String deliveryLocation, String notes, OrderStatus status, String createdAt,
                 String preparingAt, String inTransitAt, String deliveredAt) {
        this.orderId = orderId;
        this.studentId = studentId;
        this.studentName = studentName;
        this.items = new ArrayList<>(items == null ? List.of() : items);
        this.totalCost = totalCost;
        this.deliveryLocation = deliveryLocation;
        this.notes = notes == null ? "" : notes;
        this.status = status == null ? OrderStatus.PENDING : status;
        this.createdAt = createdAt == null ? Instant.now().toString() : createdAt;
        this.preparingAt = preparingAt;
        this.inTransitAt = inTransitAt;
        this.deliveredAt = deliveredAt;
    }
    public int getOrderId() { return orderId; }
    public int getStudentId() { return studentId; }
    public String getStudentName() { return studentName; }
    public List<OrderLine> getItems() { return List.copyOf(items); }
    public double getTotalCost() { return totalCost; }
    public String getDeliveryLocation() { return deliveryLocation; }
    public String getNotes() { return notes; }
    public OrderStatus getStatus() { return status; }
    public String getCreatedAt() { return createdAt; }
    public String getPreparingAt() { return preparingAt; }
    public String getInTransitAt() { return inTransitAt; }
    public String getDeliveredAt() { return deliveredAt; }
    public void setOrderId(int orderId) { this.orderId = orderId; }
    public void setStatus(OrderStatus status) { this.status = status; }
}
