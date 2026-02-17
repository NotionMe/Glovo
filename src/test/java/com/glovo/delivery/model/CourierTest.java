package com.glovo.delivery.model;

import com.glovo.delivery.model.enums.CourierStatus;
import com.glovo.delivery.model.enums.CourierType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class CourierTest {

    @Nested
    @DisplayName("Constructor")
    class Constructor {

        @Test
        void shouldCreateCourierWithLocationAndType() {
            Point location = new Point(25, 50);
            Courier courier = new Courier(location, CourierType.BICYCLE);

            assertNotNull(courier.getId());
            assertEquals(location, courier.getCurrentLocation());
            assertEquals(CourierType.BICYCLE, courier.getType());
            assertEquals(CourierStatus.FREE, courier.getStatus());
        }

        @Test
        void defaultConstructorShouldSetFreeStatusAndGenerateId() {
            Courier courier = new Courier();
            assertNotNull(courier.getId());
            assertEquals(CourierStatus.FREE, courier.getStatus());
        }
    }

    @Nested
    @DisplayName("Transport weights")
    class TransportWeights {

        @Test
        void pedestrianWeightShouldBe1_5() {
            assertEquals(1.5, CourierType.PEDESTRIAN.getTransportWeight());
        }

        @Test
        void bicycleWeightShouldBe1_0() {
            assertEquals(1.0, CourierType.BICYCLE.getTransportWeight());
        }

        @Test
        void carWeightShouldBe0_7() {
            assertEquals(0.7, CourierType.CAR.getTransportWeight());
        }
    }

    @Nested
    @DisplayName("Status transitions")
    class StatusTransitions {

        @Test
        void shouldTransitionFromFreeToBusy() {
            Courier courier = new Courier(new Point(0, 0), CourierType.CAR);
            courier.setStatus(CourierStatus.BUSY);
            assertEquals(CourierStatus.BUSY, courier.getStatus());
        }

        @Test
        void shouldTransitionToOffline() {
            Courier courier = new Courier(new Point(0, 0), CourierType.CAR);
            courier.setStatus(CourierStatus.OFFLINE);
            assertEquals(CourierStatus.OFFLINE, courier.getStatus());
        }
    }

    @Nested
    @DisplayName("equals and hashCode")
    class EqualsAndHashCode {

        @Test
        void shouldBeEqualById() {
            Courier a = new Courier(new Point(10, 10), CourierType.PEDESTRIAN);
            Courier b = new Courier(new Point(50, 50), CourierType.CAR);
            assertNotEquals(a, b);

            // Same ID should be equal
            b.setId(a.getId());
            assertEquals(a, b);
            assertEquals(a.hashCode(), b.hashCode());
        }

        @Test
        void shouldEqualSelf() {
            Courier courier = new Courier(new Point(10, 10), CourierType.BICYCLE);
            assertEquals(courier, courier);
        }

        @Test
        void shouldNotEqualNull() {
            Courier courier = new Courier(new Point(10, 10), CourierType.BICYCLE);
            assertNotEquals(null, courier);
        }

        @Test
        void shouldNotEqualDifferentType() {
            Courier courier = new Courier(new Point(10, 10), CourierType.BICYCLE);
            assertNotEquals("not a courier", courier);
        }
    }

    @Test
    void toStringShouldContainTypeAndStatus() {
        Courier courier = new Courier(new Point(25, 50), CourierType.BICYCLE);
        String str = courier.toString();
        assertTrue(str.contains("BICYCLE"));
        assertTrue(str.contains("FREE"));
    }

    @Test
    void shouldUpdateLocation() {
        Courier courier = new Courier(new Point(10, 20), CourierType.CAR);
        Point newLocation = new Point(80, 90);
        courier.setCurrentLocation(newLocation);
        assertEquals(newLocation, courier.getCurrentLocation());
    }
}
