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
 */
@Component
public class ScoreBasedMatchingStrategy implements CourierMatchingStrategy {

    private static final Logger log = LoggerFactory.getLogger(ScoreBasedMatchingStrategy.class);
    private static final double PRIORITY_COEFFICIENT = 0.5;

    @Override
    public Optional<Courier> findBestCourier(Order order, List<Courier> availableCouriers) {
        if (availableCouriers.isEmpty()) {
            log.warn("No available couriers for order {}", order.getId());
            return Optional.empty();
        }

        log.info("=== Scoring couriers for order {} (priority={}, pickup={}) ===",
                order.getId(), order.getPriority(), order.getPickupLocation());

        Courier bestCourier = null;
        double bestScore = Double.MAX_VALUE;

        for (Courier courier : availableCouriers) {
            double distance = courier.getCurrentLocation().distanceTo(order.getPickupLocation());
            double transportWeight = courier.getType().getTransportWeight();
            double score = (distance * transportWeight) - (order.getPriority() * PRIORITY_COEFFICIENT);

            log.info("  Courier {} [{}] at {} -> distance={}, weight={}, score={}",
                    courier.getId(),
                    courier.getType(),
                    courier.getCurrentLocation(),
                    String.format("%.2f", distance),
                    transportWeight,
                    String.format("%.2f", score));

            if (score < bestScore) {
                bestScore = score;
                bestCourier = courier;
            }
        }

        if (bestCourier != null) {
            log.info(">>> Best courier: {} [{}] with score {}",
                    bestCourier.getId(), bestCourier.getType(), String.format("%.2f", bestScore));
        }

        return Optional.ofNullable(bestCourier);
    }
}
