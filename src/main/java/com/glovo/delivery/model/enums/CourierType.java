package com.glovo.delivery.model.enums;

public enum CourierType {

    PEDESTRIAN(1.5, 5.0),
    BICYCLE(1.0, 15.0),
    CAR(0.7, 50.0);

    private final double transportWeight;
    private final double maxWeightKg;

    CourierType(double transportWeight, double maxWeightKg) {
        this.transportWeight = transportWeight;
        this.maxWeightKg = maxWeightKg;
    }

    public double getTransportWeight() {
        return transportWeight;
    }

    public double getMaxWeightKg() {
        return maxWeightKg;
    }

    public boolean canCarry(double weightKg) {
        return weightKg <= maxWeightKg;
    }
}
