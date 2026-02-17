package com.glovo.delivery.dto;

import com.glovo.delivery.model.Point;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public class CreateOrderRequest {

    @NotNull(message = "Pickup location is required")
    @Valid
    private Point pickupLocation;

    @NotNull(message = "Delivery location is required")
    @Valid
    private Point deliveryLocation;

    @Min(value = 1, message = "Priority must be at least 1")
    @Max(value = 10, message = "Priority must be at most 10")
    private int priority;

    public CreateOrderRequest() {
    }

    public CreateOrderRequest(Point pickupLocation, Point deliveryLocation, int priority) {
        this.pickupLocation = pickupLocation;
        this.deliveryLocation = deliveryLocation;
        this.priority = priority;
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

    public int getPriority() {
        return priority;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }
}
