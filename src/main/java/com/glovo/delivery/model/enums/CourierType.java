package com.glovo.delivery.model.enums;

public enum CourierType {

    PEDESTRIAN(1.5),
    BICYCLE(1.0),
    CAR(0.7);

    private final double transportWeight;

    CourierType(double transportWeight) {
        this.transportWeight = transportWeight;
    }

    public double getTransportWeight() {
        return transportWeight;
    }
}
