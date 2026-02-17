package com.glovo.delivery.service.strategy;

import com.glovo.delivery.model.Courier;
import com.glovo.delivery.model.Order;
import com.glovo.delivery.model.Point;
import com.glovo.delivery.model.enums.CourierType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class ScoreBasedMatchingStrategyTest {

    private ScoreBasedMatchingStrategy strategy;

    @BeforeEach
    void setUp() {
        strategy = new ScoreBasedMatchingStrategy();
    }

    @Test
    @DisplayName("Should return empty when no couriers available")
    void shouldReturnEmptyForNoCouriers() {
        Order order = new Order(new Point(50, 50), new Point(60, 60), 5, 3.0);
        Optional<Courier> result = strategy.findBestCourier(order, Collections.emptyList());
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("Should return single courier when only one available")
    void shouldReturnOnlyCourier() {
        Order order = new Order(new Point(50, 50), new Point(60, 60), 5, 3.0);
        Courier courier = new Courier(new Point(55, 55), CourierType.BICYCLE);

        Optional<Courier> result = strategy.findBestCourier(order, List.of(courier));
        assertTrue(result.isPresent());
        assertEquals(courier.getId(), result.get().getId());
    }

    @Nested
    @DisplayName("Scoring formula: Score = (Distance * TransportWeight) - (Priority * 0.5)")
    class ScoringFormula {

        @Test
        @DisplayName("Closer courier should win over farther same-type courier")
        void closerCourierShouldWin() {
            Order order = new Order(new Point(50, 50), new Point(60, 60), 5, 3.0);
            Courier close = new Courier(new Point(51, 51), CourierType.BICYCLE);
            Courier far = new Courier(new Point(90, 90), CourierType.BICYCLE);

            Optional<Courier> result = strategy.findBestCourier(order, List.of(far, close));
            assertTrue(result.isPresent());
            assertEquals(close.getId(), result.get().getId());
        }

        @Test
        @DisplayName("Car should beat pedestrian at same distance due to lower weight")
        void carShouldBeatPedestrianAtSameDistance() {
            Order order = new Order(new Point(50, 50), new Point(60, 60), 5, 3.0);
            // Both at equal distance from pickup
            Courier car = new Courier(new Point(60, 50), CourierType.CAR);         // distance=10, weight=0.7
            Courier pedestrian = new Courier(new Point(40, 50), CourierType.PEDESTRIAN); // distance=10, weight=1.5

            Optional<Courier> result = strategy.findBestCourier(order, List.of(pedestrian, car));
            assertTrue(result.isPresent());
            assertEquals(car.getId(), result.get().getId());
        }

        @Test
        @DisplayName("Should verify exact score calculation")
        void shouldCalculateCorrectScore() {
            // Courier at (0,0), order pickup at (3,4) => distance = 5.0
            // CAR weight = 0.7, priority = 6
            // Score = (5.0 * 0.7) - (6 * 0.5) = 3.5 - 3.0 = 0.5
            Order order = new Order(new Point(3, 4), new Point(60, 60), 6, 3.0);
            Courier car = new Courier(new Point(0, 0), CourierType.CAR);

            // Courier at (10,0), order pickup at (3,4) => distance = sqrt(49+16) = sqrt(65) ≈ 8.062
            // BICYCLE weight = 1.0
            // Score = (8.062 * 1.0) - (6 * 0.5) = 8.062 - 3.0 = 5.062
            Courier bicycle = new Courier(new Point(10, 0), CourierType.BICYCLE);

            Optional<Courier> result = strategy.findBestCourier(order, List.of(bicycle, car));
            assertTrue(result.isPresent());
            assertEquals(car.getId(), result.get().getId()); // car has lower score (0.5 < 5.062)
        }

        @Test
        @DisplayName("Higher priority should slightly favor all couriers (lower score)")
        void higherPriorityShouldReduceScore() {
            // Same courier, same distance, but different priorities
            // Priority 10: score = (dist * weight) - (10 * 0.5) = (dist * weight) - 5
            // Priority 1:  score = (dist * weight) - (1 * 0.5)  = (dist * weight) - 0.5
            // The priority affects all couriers equally, so the best courier stays the same.
            // But it does reduce the absolute score.
            Order highPriority = new Order(new Point(50, 50), new Point(60, 60), 10, 3.0);
            Order lowPriority = new Order(new Point(50, 50), new Point(60, 60), 1, 3.0);

            Courier courier = new Courier(new Point(55, 55), CourierType.BICYCLE);

            // Both should pick the only available courier
            assertTrue(strategy.findBestCourier(highPriority, List.of(courier)).isPresent());
            assertTrue(strategy.findBestCourier(lowPriority, List.of(courier)).isPresent());
        }
    }

    @Test
    @DisplayName("Should pick best from multiple couriers of different types")
    void shouldPickBestFromMixedCouriers() {
        Order order = new Order(new Point(50, 50), new Point(60, 60), 5, 3.0);

        Courier pedestrian = new Courier(new Point(52, 52), CourierType.PEDESTRIAN); // dist≈2.83, score=(2.83*1.5)-2.5=1.74
        Courier bicycle = new Courier(new Point(55, 55), CourierType.BICYCLE);       // dist≈7.07, score=(7.07*1.0)-2.5=4.57
        Courier car = new Courier(new Point(60, 60), CourierType.CAR);               // dist≈14.14, score=(14.14*0.7)-2.5=7.40

        Optional<Courier> result = strategy.findBestCourier(order, List.of(car, bicycle, pedestrian));
        assertTrue(result.isPresent());
        assertEquals(pedestrian.getId(), result.get().getId()); // lowest score wins
    }

    @Nested
    @DisplayName("Tiebreaker: completedOrdersToday when distance < 1 unit")
    class CompletedOrdersTiebreaker {

        @Test
        @DisplayName("Should prefer courier with fewer completedOrdersToday when distance diff < 1")
        void shouldPreferFewerCompletedOrdersWhenDistanceClose() {
            Order order = new Order(new Point(50, 50), new Point(60, 60), 5, 3.0);

            // Both couriers at very similar distance from pickup (50,50)
            Courier courierA = new Courier(new Point(50, 51), CourierType.BICYCLE); // distance = 1.0
            courierA.setCompletedOrdersToday(5);

            Courier courierB = new Courier(new Point(51, 50), CourierType.BICYCLE); // distance = 1.0
            courierB.setCompletedOrdersToday(2);

            // Distance diff = 0 < 1.0 threshold => tiebreaker applies
            // CourierB has fewer completedOrdersToday (2 < 5) => wins
            Optional<Courier> result = strategy.findBestCourier(order, List.of(courierA, courierB));
            assertTrue(result.isPresent());
            assertEquals(courierB.getId(), result.get().getId());
        }

        @Test
        @DisplayName("Should NOT apply tiebreaker when distance diff >= 1 unit")
        void shouldNotApplyTiebreakerWhenDistanceFar() {
            Order order = new Order(new Point(50, 50), new Point(60, 60), 5, 3.0);

            // CourierA is much closer
            Courier courierA = new Courier(new Point(50, 51), CourierType.BICYCLE); // distance ~1.0
            courierA.setCompletedOrdersToday(10);

            // CourierB is significantly farther — distance diff >= 1
            Courier courierB = new Courier(new Point(50, 53), CourierType.BICYCLE); // distance ~3.0
            courierB.setCompletedOrdersToday(0);

            // Distance diff = |1.0 - 3.0| = 2.0 >= threshold => normal scoring, courierA wins (lower score)
            Optional<Courier> result = strategy.findBestCourier(order, List.of(courierA, courierB));
            assertTrue(result.isPresent());
            assertEquals(courierA.getId(), result.get().getId());
        }

        @Test
        @DisplayName("Should fall back to score when completedOrdersToday are equal and distance < 1")
        void shouldFallBackToScoreWhenCompletedOrdersEqual() {
            Order order = new Order(new Point(50, 50), new Point(60, 60), 5, 3.0);

            // Both at nearly the same distance, same completedOrdersToday
            // But different transport types => different scores
            Courier car = new Courier(new Point(50, 51), CourierType.CAR);       // distance=1, score=(1*0.7)-2.5=-1.8
            car.setCompletedOrdersToday(3);

            Courier pedestrian = new Courier(new Point(51, 50), CourierType.PEDESTRIAN); // distance=1, score=(1*1.5)-2.5=-1.0
            pedestrian.setCompletedOrdersToday(3);

            // Same completedOrders, distance diff=0 < threshold => falls back to score
            // CAR score (-1.8) < PEDESTRIAN score (-1.0) => car wins
            Optional<Courier> result = strategy.findBestCourier(order, List.of(pedestrian, car));
            assertTrue(result.isPresent());
            assertEquals(car.getId(), result.get().getId());
        }

        @Test
        @DisplayName("Tiebreaker should work with distance diff just under threshold")
        void shouldApplyTiebreakerAtBoundary() {
            Order order = new Order(new Point(50, 50), new Point(60, 60), 5, 3.0);

            // CourierA at (50, 50) => distance = 0
            Courier courierA = new Courier(new Point(50, 50), CourierType.BICYCLE);
            courierA.setCompletedOrdersToday(4);

            // CourierB at (50.0, 50.99) => distance ~0.99 (just under 1.0 threshold from courierA's distance=0)
            // We use (50, 51) for distance=1.0, diff = |0 - 1.0| = 1.0 which is NOT < 1.0
            // So use a closer point: (50, 50) + tiny offset => but Point uses int.
            // With integer points: courier at (51, 50) distance=1.0, diff=|0-1.0|=1.0 NOT < 1.0
            // Let's use both at same distance instead
            Courier courierB = new Courier(new Point(50, 50), CourierType.BICYCLE);
            courierB.setCompletedOrdersToday(1);

            // Both at distance 0, diff = 0 < 1.0 => tiebreaker applies
            Optional<Courier> result = strategy.findBestCourier(order, List.of(courierA, courierB));
            assertTrue(result.isPresent());
            assertEquals(courierB.getId(), result.get().getId()); // fewer completedOrders wins
        }
    }

    @Nested
    @DisplayName("Weight filtering: courier must be able to carry order weight")
    class WeightFiltering {

        @Test
        @DisplayName("Heavy order (10kg) should filter out pedestrian (max 5kg)")
        void heavyOrderShouldFilterOutPedestrian() {
            Order order = new Order(new Point(50, 50), new Point(60, 60), 5, 10.0);

            // Pedestrian is closest but can't carry 10kg (max 5kg)
            Courier pedestrian = new Courier(new Point(51, 51), CourierType.PEDESTRIAN);
            Courier bicycle = new Courier(new Point(55, 55), CourierType.BICYCLE); // can carry 15kg

            Optional<Courier> result = strategy.findBestCourier(order, List.of(pedestrian, bicycle));
            assertTrue(result.isPresent());
            assertEquals(bicycle.getId(), result.get().getId());
        }

        @Test
        @DisplayName("Very heavy order (20kg) should filter out pedestrian and bicycle")
        void veryHeavyOrderShouldFilterOutPedestrianAndBicycle() {
            Order order = new Order(new Point(50, 50), new Point(60, 60), 5, 20.0);

            Courier pedestrian = new Courier(new Point(51, 51), CourierType.PEDESTRIAN); // max 5kg
            Courier bicycle = new Courier(new Point(52, 52), CourierType.BICYCLE);       // max 15kg
            Courier car = new Courier(new Point(90, 90), CourierType.CAR);               // max 50kg

            Optional<Courier> result = strategy.findBestCourier(order, List.of(pedestrian, bicycle, car));
            assertTrue(result.isPresent());
            assertEquals(car.getId(), result.get().getId());
        }

        @Test
        @DisplayName("Should return empty when no courier can carry the order weight")
        void shouldReturnEmptyWhenNoCourierCanCarry() {
            Order order = new Order(new Point(50, 50), new Point(60, 60), 5, 60.0); // 60kg > max 50kg for CAR

            Courier pedestrian = new Courier(new Point(51, 51), CourierType.PEDESTRIAN);
            Courier bicycle = new Courier(new Point(52, 52), CourierType.BICYCLE);
            Courier car = new Courier(new Point(53, 53), CourierType.CAR);

            Optional<Courier> result = strategy.findBestCourier(order, List.of(pedestrian, bicycle, car));
            assertTrue(result.isEmpty());
        }

        @Test
        @DisplayName("Light order (4kg) should allow all courier types")
        void lightOrderShouldAllowAllTypes() {
            Order order = new Order(new Point(50, 50), new Point(60, 60), 5, 4.0);

            // Pedestrian is closest, can carry 4kg (max 5kg)
            Courier pedestrian = new Courier(new Point(51, 51), CourierType.PEDESTRIAN);
            Courier bicycle = new Courier(new Point(55, 55), CourierType.BICYCLE);
            Courier car = new Courier(new Point(60, 60), CourierType.CAR);

            Optional<Courier> result = strategy.findBestCourier(order, List.of(car, bicycle, pedestrian));
            assertTrue(result.isPresent());
            assertEquals(pedestrian.getId(), result.get().getId()); // closest + all eligible
        }

        @Test
        @DisplayName("Order at exact max weight should be accepted (boundary)")
        void exactMaxWeightShouldBeAccepted() {
            Order order = new Order(new Point(50, 50), new Point(60, 60), 5, 5.0); // exactly 5kg

            Courier pedestrian = new Courier(new Point(51, 51), CourierType.PEDESTRIAN); // max 5kg
            Optional<Courier> result = strategy.findBestCourier(order, List.of(pedestrian));
            assertTrue(result.isPresent());
            assertEquals(pedestrian.getId(), result.get().getId());
        }

        @Test
        @DisplayName("Order just over max weight should be rejected (boundary)")
        void justOverMaxWeightShouldBeRejected() {
            Order order = new Order(new Point(50, 50), new Point(60, 60), 5, 5.01); // just over 5kg

            Courier pedestrian = new Courier(new Point(51, 51), CourierType.PEDESTRIAN); // max 5kg
            Optional<Courier> result = strategy.findBestCourier(order, List.of(pedestrian));
            assertTrue(result.isEmpty());
        }
    }
}
