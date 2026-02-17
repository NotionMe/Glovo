package com.glovo.delivery.model;

import com.glovo.delivery.model.enums.OrderStatus;

import java.util.Objects;
import java.util.UUID;

public class Order {

    private UUID id;
    private Point pickupLocation;
    private Point deliveryLocation;
    private OrderStatus status;
    private int priority;
    private double weightKg;
    private long createdAt;
    private UUID assignedCourierId;

    public Order() {
        this.id = UUID.randomUUID();
        this.status = OrderStatus.CREATED;
        this.createdAt = System.currentTimeMillis();
    }

    public Order(Point pickupLocation, Point deliveryLocation, int priority, double weightKg) {
        this();
        validatePriority(priority);
        validateWeight(weightKg);
        this.pickupLocation = pickupLocation;
        this.deliveryLocation = deliveryLocation;
        this.priority = priority;
        this.weightKg = weightKg;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public Point getPickupLocation() {
        return pickupLocation;
    }

    public void setPickupLocation(Point pickupLocation) {
        this.pickupLocation = pickupLocation;
    }

    public Point getDeliveryLocation() {
        return deliveryLocation;
    }

    public void setDeliveryLocation(Point deliveryLocation) {
        this.deliveryLocation = deliveryLocation;
    }

    public OrderStatus getStatus() {
        return status;
    }

    public void setStatus(OrderStatus status) {
        this.status = status;
    }

    public int getPriority() {
        return priority;
    }

    public void setPriority(int priority) {
        validatePriority(priority);
        this.priority = priority;
    }

    public double getWeightKg() {
        return weightKg;
    }

    public void setWeightKg(double weightKg) {
        validateWeight(weightKg);
        this.weightKg = weightKg;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }

    public UUID getAssignedCourierId() {
        return assignedCourierId;
    }

    public void setAssignedCourierId(UUID assignedCourierId) {
        this.assignedCourierId = assignedCourierId;
    }

    private static void validatePriority(int priority) {
        if (priority < 1 || priority > 10) {
            throw new IllegalArgumentException("Priority must be between 1 and 10. Got: " + priority);
        }
    }

    private static void validateWeight(double weightKg) {
        if (weightKg <= 0) {
            throw new IllegalArgumentException("Weight must be greater than 0. Got: " + weightKg);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Order order = (Order) o;
        return Objects.equals(id, order.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Order{id=" + id + ", status=" + status + ", priority=" + priority +
                ", weightKg=" + weightKg + "}";
    }
}
