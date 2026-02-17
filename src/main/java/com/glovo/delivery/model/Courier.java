package com.glovo.delivery.model;

import com.glovo.delivery.model.enums.CourierStatus;
import com.glovo.delivery.model.enums.CourierType;

import java.util.Objects;
import java.util.UUID;

public class Courier {

    private UUID id;
    private Point currentLocation;
    private CourierType type;
    private CourierStatus status;

    public Courier() {
        this.id = UUID.randomUUID();
        this.status = CourierStatus.FREE;
    }

    public Courier(Point currentLocation, CourierType type) {
        this();
        this.currentLocation = currentLocation;
        this.type = type;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public Point getCurrentLocation() {
        return currentLocation;
    }

    public void setCurrentLocation(Point currentLocation) {
        this.currentLocation = currentLocation;
    }

    public CourierType getType() {
        return type;
    }

    public void setType(CourierType type) {
        this.type = type;
    }

    public CourierStatus getStatus() {
        return status;
    }

    public void setStatus(CourierStatus status) {
        this.status = status;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Courier courier = (Courier) o;
        return Objects.equals(id, courier.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Courier{id=" + id + ", type=" + type + ", status=" + status +
                ", location=" + currentLocation + "}";
    }
}
