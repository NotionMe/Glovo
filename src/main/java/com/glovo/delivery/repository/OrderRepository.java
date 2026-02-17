package com.glovo.delivery.repository;

import com.glovo.delivery.model.Order;
import com.glovo.delivery.model.enums.OrderStatus;
import org.springframework.stereotype.Repository;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Repository
public class OrderRepository {

    private final ConcurrentHashMap<UUID, Order> orders = new ConcurrentHashMap<>();

    public Order save(Order order) {
        orders.put(order.getId(), order);
        return order;
    }

    public Optional<Order> findById(UUID id) {
        return Optional.ofNullable(orders.get(id));
    }

    public List<Order> findAll() {
        return new ArrayList<>(orders.values());
    }

    public List<Order> findByStatus(OrderStatus status) {
        return orders.values().stream()
                .filter(order -> order.getStatus() == status)
                .collect(Collectors.toList());
    }

    public long countByStatus(OrderStatus status) {
        return orders.values().stream()
                .filter(order -> order.getStatus() == status)
                .count();
    }

    public long count() {
        return orders.size();
    }

    public void deleteById(UUID id) {
        orders.remove(id);
    }

    public void clear() {
        orders.clear();
    }
}
