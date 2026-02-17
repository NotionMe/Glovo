package com.glovo.delivery.model;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;

import java.util.Objects;

public class Point {

    @DecimalMin(value = "0", message = "X coordinate must be >= 0")
    @DecimalMax(value = "100", message = "X coordinate must be <= 100")
    private double x;

    @DecimalMin(value = "0", message = "Y coordinate must be >= 0")
    @DecimalMax(value = "100", message = "Y coordinate must be <= 100")
    private double y;

    public Point() {
    }

    public Point(double x, double y) {
        validateCoordinate(x, "X");
        validateCoordinate(y, "Y");
        this.x = x;
        this.y = y;
    }

    public double distanceTo(Point other) {
        double dx = this.x - other.x;
        double dy = this.y - other.y;
        return Math.sqrt(dx * dx + dy * dy);
    }

    public double getX() {
        return x;
    }

    public void setX(double x) {
        validateCoordinate(x, "X");
        this.x = x;
    }

    public double getY() {
        return y;
    }

    public void setY(double y) {
        validateCoordinate(y, "Y");
        this.y = y;
    }

    private static void validateCoordinate(double value, String name) {
        if (value < 0 || value > 100) {
            throw new IllegalArgumentException(
                    name + " coordinate must be in range [0, 100]. Got: " + value);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Point point = (Point) o;
        return Double.compare(point.x, x) == 0 && Double.compare(point.y, y) == 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(x, y);
    }

    @Override
    public String toString() {
        return "(" + x + ", " + y + ")";
    }
}
