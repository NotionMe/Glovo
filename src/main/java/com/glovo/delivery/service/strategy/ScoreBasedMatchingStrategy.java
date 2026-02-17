package com.glovo.delivery.service.strategy;

import com.glovo.delivery.model.Courier;
import com.glovo.delivery.model.Order;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

/**
 * Default scoring strategy based on the formula:
 * Score = (Distance * TransportWeight) - (OrderPriority * 0.5)
 *
 * Lower score = better candidate.
 *
 * Tiebreaker: when two couriers are within 1 distance unit of each other,
 * the courier with fewer completed orders today wins.
 */
@Component
public class ScoreBasedMatchingStrategy implements CourierMatchingStrategy {

    private static final Logger log = LoggerFactory.getLogger(ScoreBasedMatchingStrategy.class);
    private static final double PRIORITY_COEFFICIENT = 0.5;
    private static final double DISTANCE_TIEBREAK_THRESHOLD = 1.0;

    @Override
    public Optional<Courier> findBestCourier(Order order, List<Courier> availableCouriers) {
        if (availableCouriers.isEmpty()) {
            log.warn("No available couriers for order {}", order.getId());
            return Optional.empty();
        }

        // Filter couriers that can carry the order weight
        List<Courier> eligibleCouriers = availableCouriers.stream()
                .filter(c -> c.getType().canCarry(order.getWeightKg()))
                .toList();

        if (eligibleCouriers.isEmpty()) {
            log.warn("No couriers can carry {}kg for order {} (available: {})",
                    order.getWeightKg(), order.getId(), availableCouriers.size());
            return Optional.empty();
        }

        log.info("=== Scoring couriers for order {} (priority={}, weight={}kg, pickup={}) ===",
                order.getId(), order.getPriority(), order.getWeightKg(), order.getPickupLocation());
        log.info("  Eligible couriers: {}/{} (filtered by max weight capacity)",
                eligibleCouriers.size(), availableCouriers.size());

        Courier bestCourier = null;
        double bestScore = Double.MAX_VALUE;
        double bestDistance = Double.MAX_VALUE;

        for (Courier courier : eligibleCouriers) {
            double distance = courier.getCurrentLocation().distanceTo(order.getPickupLocation());
            double transportWeight = courier.getType().getTransportWeight();
            double score = (distance * transportWeight) - (order.getPriority() * PRIORITY_COEFFICIENT);

            log.info("  Courier {} [{}] at {} -> distance={}, weight={}, score={}, completedToday={}",
                    courier.getId(),
                    courier.getType(),
                    courier.getCurrentLocation(),
                    String.format("%.2f", distance),
                    transportWeight,
                    String.format("%.2f", score),
                    courier.getCompletedOrdersToday());

            boolean isBetter = false;

            if (bestCourier == null) {
                isBetter = true;
            } else if (Math.abs(distance - bestDistance) < DISTANCE_TIEBREAK_THRESHOLD) {
                // Tiebreaker: when distances are close (< 1 unit), prefer fewer completed orders
                if (courier.getCompletedOrdersToday() < bestCourier.getCompletedOrdersToday()) {
                    isBetter = true;
                    log.info("    -> Tiebreak: {} completed vs {} completed (distance diff={}) — wins by fewer orders",
                            courier.getCompletedOrdersToday(),
                            bestCourier.getCompletedOrdersToday(),
                            String.format("%.2f", Math.abs(distance - bestDistance)));
                } else if (courier.getCompletedOrdersToday() == bestCourier.getCompletedOrdersToday()
                        && score < bestScore) {
                    isBetter = true;
                }
            } else if (score < bestScore) {
                isBetter = true;
            }

            if (isBetter) {
                bestScore = score;
                bestDistance = distance;
                bestCourier = courier;
            }
        }

        if (bestCourier != null) {
            log.info(">>> Best courier: {} [{}] with score {} (completedToday={})",
                    bestCourier.getId(), bestCourier.getType(),
                    String.format("%.2f", bestScore),
                    bestCourier.getCompletedOrdersToday());
        }

        return Optional.ofNullable(bestCourier);
    }
}
