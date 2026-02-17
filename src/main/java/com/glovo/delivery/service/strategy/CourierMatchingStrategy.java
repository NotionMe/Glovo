package com.glovo.delivery.service.strategy;

import com.glovo.delivery.model.Courier;
import com.glovo.delivery.model.Order;

import java.util.List;
import java.util.Optional;

/**
 * Strategy interface for courier matching algorithms.
 * Allows swapping the scoring/matching logic without rewriting the dispatch service.
 */
public interface CourierMatchingStrategy {

    /**
     * Find the best courier for the given order from the list of available couriers.
     *
     * @param order            the order to assign
     * @param availableCouriers list of free couriers
     * @return the best matching courier, or empty if none found
     */
    Optional<Courier> findBestCourier(Order order, List<Courier> availableCouriers);
}
