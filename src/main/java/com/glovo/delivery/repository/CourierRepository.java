package com.glovo.delivery.repository;

import com.glovo.delivery.model.Courier;
import com.glovo.delivery.model.enums.CourierStatus;
import org.springframework.stereotype.Repository;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Repository
public class CourierRepository {

    private final ConcurrentHashMap<UUID, Courier> couriers = new ConcurrentHashMap<>();

    public Courier save(Courier courier) {
        couriers.put(courier.getId(), courier);
        return courier;
    }

    public Optional<Courier> findById(UUID id) {
        return Optional.ofNullable(couriers.get(id));
    }

    public List<Courier> findAll() {
        return new ArrayList<>(couriers.values());
    }

    public List<Courier> findByStatus(CourierStatus status) {
        return couriers.values().stream()
                .filter(courier -> courier.getStatus() == status)
                .collect(Collectors.toList());
    }

    public List<Courier> findFree() {
        return findByStatus(CourierStatus.FREE);
    }

    public long countByStatus(CourierStatus status) {
        return couriers.values().stream()
                .filter(courier -> courier.getStatus() == status)
                .count();
    }

    public long count() {
        return couriers.size();
    }

    public void deleteById(UUID id) {
        couriers.remove(id);
    }

    public void clear() {
        couriers.clear();
    }
}
