package com.glovo.delivery.model;

import com.glovo.delivery.model.enums.OrderStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class OrderTest {

    @Nested
    @DisplayName("Constructor")
    class Constructor {

        @Test
        void shouldCreateOrderWithValidData() {
            Point pickup = new Point(10, 20);
            Point delivery = new Point(80, 90);
            Order order = new Order(pickup, delivery, 5, 3.0);

            assertNotNull(order.getId());
            assertEquals(pickup, order.getPickupLocation());
            assertEquals(delivery, order.getDeliveryLocation());
            assertEquals(5, order.getPriority());
            assertEquals(3.0, order.getWeightKg());
            assertEquals(OrderStatus.CREATED, order.getStatus());
            assertTrue(order.getCreatedAt() > 0);
            assertNull(order.getAssignedCourierId());
        }

        @Test
        void shouldCreateWithMinPriority() {
            Order order = new Order(new Point(0, 0), new Point(100, 100), 1, 1.0);
            assertEquals(1, order.getPriority());
        }

        @Test
        void shouldCreateWithMaxPriority() {
            Order order = new Order(new Point(0, 0), new Point(100, 100), 10, 1.0);
            assertEquals(10, order.getPriority());
        }

        @Test
        void shouldRejectPriorityBelowRange() {
            assertThrows(IllegalArgumentException.class,
                    () -> new Order(new Point(0, 0), new Point(10, 10), 0, 1.0));
        }

        @Test
        void shouldRejectPriorityAboveRange() {
            assertThrows(IllegalArgumentException.class,
                    () -> new Order(new Point(0, 0), new Point(10, 10), 11, 1.0));
        }
    }

    @Nested
    @DisplayName("Priority setter validation")
    class PriorityValidation {

        @Test
        void shouldUpdatePriorityWithValidValue() {
            Order order = new Order(new Point(0, 0), new Point(10, 10), 5, 2.0);
            order.setPriority(8);
            assertEquals(8, order.getPriority());
        }

        @Test
        void shouldRejectInvalidPriorityInSetter() {
            Order order = new Order(new Point(0, 0), new Point(10, 10), 5, 2.0);
            assertThrows(IllegalArgumentException.class, () -> order.setPriority(0));
            assertThrows(IllegalArgumentException.class, () -> order.setPriority(11));
            assertEquals(5, order.getPriority()); // unchanged
        }
    }

    @Nested
    @DisplayName("Weight validation")
    class WeightValidation {

        @Test
        void shouldCreateOrderWithValidWeight() {
            Order order = new Order(new Point(0, 0), new Point(10, 10), 5, 25.5);
            assertEquals(25.5, order.getWeightKg());
        }

        @Test
        void shouldRejectZeroWeight() {
            assertThrows(IllegalArgumentException.class,
                    () -> new Order(new Point(0, 0), new Point(10, 10), 5, 0.0));
        }

        @Test
        void shouldRejectNegativeWeight() {
            assertThrows(IllegalArgumentException.class,
                    () -> new Order(new Point(0, 0), new Point(10, 10), 5, -1.0));
        }

        @Test
        void shouldRejectInvalidWeightInSetter() {
            Order order = new Order(new Point(0, 0), new Point(10, 10), 5, 3.0);
            assertThrows(IllegalArgumentException.class, () -> order.setWeightKg(0));
            assertThrows(IllegalArgumentException.class, () -> order.setWeightKg(-5));
            assertEquals(3.0, order.getWeightKg()); // unchanged
        }

        @Test
        void shouldUpdateWeightWithValidValue() {
            Order order = new Order(new Point(0, 0), new Point(10, 10), 5, 3.0);
            order.setWeightKg(10.0);
            assertEquals(10.0, order.getWeightKg());
        }
    }

    @Nested
    @DisplayName("Default constructor")
    class DefaultConstructor {

        @Test
        void shouldSetDefaultValues() {
            Order order = new Order();
            assertNotNull(order.getId());
            assertEquals(OrderStatus.CREATED, order.getStatus());
            assertTrue(order.getCreatedAt() > 0);
        }
    }

    @Nested
    @DisplayName("equals and hashCode")
    class EqualsAndHashCode {

        @Test
        void shouldBeEqualByIdOnly() {
            Order a = new Order(new Point(10, 20), new Point(30, 40), 5, 2.0);
            Order b = new Order(new Point(50, 60), new Point(70, 80), 3, 4.0);
            assertNotEquals(a, b); // different IDs

            // Same ID should be equal
            b.setId(a.getId());
            assertEquals(a, b);
            assertEquals(a.hashCode(), b.hashCode());
        }

        @Test
        void shouldEqualSelf() {
            Order order = new Order(new Point(10, 20), new Point(30, 40), 5, 2.0);
            assertEquals(order, order);
        }

        @Test
        void shouldNotEqualNull() {
            Order order = new Order(new Point(10, 20), new Point(30, 40), 5, 2.0);
            assertNotEquals(null, order);
        }
    }

    @Test
    void toStringShouldContainIdStatusAndWeight() {
        Order order = new Order(new Point(0, 0), new Point(10, 10), 5, 3.5);
        String str = order.toString();
        assertTrue(str.contains(order.getId().toString()));
        assertTrue(str.contains("CREATED"));
        assertTrue(str.contains("5"));
        assertTrue(str.contains("3.5"));
    }
}
