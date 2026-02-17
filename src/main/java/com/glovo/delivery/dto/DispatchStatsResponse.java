package com.glovo.delivery.dto;

import java.util.Map;

public class DispatchStatsResponse {

    private long totalOrders;
    private Map<String, Long> ordersByStatus;
    private long totalCouriers;
    private Map<String, Long> couriersByStatus;
    private long totalAssignments;
    private int queuedOrders;

    public DispatchStatsResponse() {
    }

    public long getTotalOrders() {
        return totalOrders;
    }

    public void setTotalOrders(long totalOrders) {
        this.totalOrders = totalOrders;
    }

    public Map<String, Long> getOrdersByStatus() {
        return ordersByStatus;
    }

    public void setOrdersByStatus(Map<String, Long> ordersByStatus) {
        this.ordersByStatus = ordersByStatus;
    }

    public long getTotalCouriers() {
        return totalCouriers;
    }

    public void setTotalCouriers(long totalCouriers) {
        this.totalCouriers = totalCouriers;
    }

    public Map<String, Long> getCouriersByStatus() {
        return couriersByStatus;
    }

    public void setCouriersByStatus(Map<String, Long> couriersByStatus) {
        this.couriersByStatus = couriersByStatus;
    }

    public long getTotalAssignments() {
        return totalAssignments;
    }

    public void setTotalAssignments(long totalAssignments) {
        this.totalAssignments = totalAssignments;
    }

    public int getQueuedOrders() {
        return queuedOrders;
    }

    public void setQueuedOrders(int queuedOrders) {
        this.queuedOrders = queuedOrders;
    }
}
