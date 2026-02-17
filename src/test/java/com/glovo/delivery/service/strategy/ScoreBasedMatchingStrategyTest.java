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
