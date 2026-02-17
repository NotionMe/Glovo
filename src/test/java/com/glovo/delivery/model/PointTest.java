package com.glovo.delivery.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PointTest {

    @Nested
    @DisplayName("Constructor validation")
    class ConstructorValidation {

        @Test
        void shouldCreatePointWithValidCoordinates() {
            Point point = new Point(50, 75);
            assertEquals(50, point.getX());
            assertEquals(75, point.getY());
        }

        @Test
        void shouldCreatePointWithBoundaryValues() {
            Point zero = new Point(0, 0);
            assertEquals(0, zero.getX());
            assertEquals(0, zero.getY());

            Point max = new Point(100, 100);
            assertEquals(100, max.getX());
            assertEquals(100, max.getY());
        }

        @Test
        void shouldRejectNegativeX() {
            IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                    () -> new Point(-1, 50));
            assertTrue(ex.getMessage().contains("X coordinate"));
        }

        @Test
        void shouldRejectNegativeY() {
            IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                    () -> new Point(50, -1));
            assertTrue(ex.getMessage().contains("Y coordinate"));
        }

        @Test
        void shouldRejectXAbove100() {
            IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                    () -> new Point(101, 50));
            assertTrue(ex.getMessage().contains("X coordinate"));
        }

        @Test
        void shouldRejectYAbove100() {
            IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                    () -> new Point(50, 101));
            assertTrue(ex.getMessage().contains("Y coordinate"));
        }
    }

    @Nested
    @DisplayName("Setter validation")
    class SetterValidation {

        @Test
        void shouldUpdateXWithValidValue() {
            Point point = new Point(10, 20);
            point.setX(55);
            assertEquals(55, point.getX());
        }

        @Test
        void shouldUpdateYWithValidValue() {
            Point point = new Point(10, 20);
            point.setY(88);
            assertEquals(88, point.getY());
        }

        @Test
        void shouldRejectInvalidXInSetter() {
            Point point = new Point(10, 20);
            assertThrows(IllegalArgumentException.class, () -> point.setX(-5));
            assertEquals(10, point.getX()); // unchanged
        }

        @Test
        void shouldRejectInvalidYInSetter() {
            Point point = new Point(10, 20);
            assertThrows(IllegalArgumentException.class, () -> point.setY(200));
            assertEquals(20, point.getY()); // unchanged
        }
    }

    @Nested
    @DisplayName("distanceTo")
    class DistanceTo {

        @Test
        void shouldCalculateDistanceBetweenTwoPoints() {
            Point a = new Point(0, 0);
            Point b = new Point(3, 4);
            assertEquals(5.0, a.distanceTo(b), 0.001);
        }

        @Test
        void shouldReturnZeroForSamePoint() {
            Point p = new Point(42, 73);
            assertEquals(0.0, p.distanceTo(p), 0.001);
        }

        @Test
        void shouldBeSymmetric() {
            Point a = new Point(10, 20);
            Point b = new Point(70, 80);
            assertEquals(a.distanceTo(b), b.distanceTo(a), 0.001);
        }

        @Test
        void shouldCalculateDiagonalDistance() {
            Point a = new Point(0, 0);
            Point b = new Point(100, 100);
            assertEquals(Math.sqrt(20000), a.distanceTo(b), 0.001);
        }
    }

    @Nested
    @DisplayName("equals and hashCode")
    class EqualsAndHashCode {

        @Test
        void shouldBeEqualForSameCoordinates() {
            Point a = new Point(10, 20);
            Point b = new Point(10, 20);
            assertEquals(a, b);
            assertEquals(a.hashCode(), b.hashCode());
        }

        @Test
        void shouldNotBeEqualForDifferentCoordinates() {
            Point a = new Point(10, 20);
            Point b = new Point(20, 10);
            assertNotEquals(a, b);
        }

        @Test
        void shouldNotEqualNull() {
            Point a = new Point(10, 20);
            assertNotEquals(null, a);
        }

        @Test
        void shouldEqualSelf() {
            Point a = new Point(10, 20);
            assertEquals(a, a);
        }
    }

    @Test
    void toStringShouldContainCoordinates() {
        Point p = new Point(42, 73);
        String str = p.toString();
        assertTrue(str.contains("42"));
        assertTrue(str.contains("73"));
    }
}
