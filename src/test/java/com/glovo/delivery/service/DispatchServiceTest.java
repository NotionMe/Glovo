package com.glovo.delivery.service;

import com.glovo.delivery.dto.DispatchStatsResponse;
import com.glovo.delivery.model.Courier;
import com.glovo.delivery.model.Order;
import com.glovo.delivery.model.Point;
import com.glovo.delivery.model.enums.CourierStatus;
import com.glovo.delivery.model.enums.CourierType;
import com.glovo.delivery.model.enums.OrderStatus;
import com.glovo.delivery.repository.CourierRepository;
import com.glovo.delivery.repository.OrderRepository;
import com.glovo.delivery.service.strategy.CourierMatchingStrategy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DispatchServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private CourierRepository courierRepository;

    @Mock
    private CourierMatchingStrategy matchingStrategy;

    @InjectMocks
    private DispatchService dispatchService;

    private Order testOrder;
    private Courier testCourier;

    @BeforeEach
    void setUp() {
        testOrder = new Order(new Point(50, 50), new Point(60, 60), 5, 3.0);
        testCourier = new Courier(new Point(55, 55), CourierType.BICYCLE);
    }

    @Nested
    @DisplayName("dispatch")
    class Dispatch {

        @Test
        @DisplayName("Should assign best courier to order")
        void shouldAssignBestCourier() {
            List<Courier> freeCouriers = List.of(testCourier);
            when(courierRepository.findFree()).thenReturn(freeCouriers);
            when(matchingStrategy.findBestCourier(eq(testOrder), eq(freeCouriers)))
                    .thenReturn(Optional.of(testCourier));
            when(orderRepository.save(any(Order.class))).thenReturn(testOrder);
            when(courierRepository.save(any(Courier.class))).thenReturn(testCourier);

            dispatchService.dispatch(testOrder);

            assertEquals(OrderStatus.ASSIGNED, testOrder.getStatus());
            assertEquals(testCourier.getId(), testOrder.getAssignedCourierId());
            assertEquals(CourierStatus.BUSY, testCourier.getStatus());
            verify(orderRepository, times(2)).save(testOrder); // SEARCHING + ASSIGNED
            verify(courierRepository).save(testCourier);
        }

        @Test
        @DisplayName("Should set order to SEARCHING before finding courier")
        void shouldSetSearchingStatus() {
            when(courierRepository.findFree()).thenReturn(List.of(testCourier));
            when(matchingStrategy.findBestCourier(any(), any()))
                    .thenReturn(Optional.of(testCourier));
            when(orderRepository.save(any(Order.class))).thenReturn(testOrder);
            when(courierRepository.save(any(Courier.class))).thenReturn(testCourier);

            dispatchService.dispatch(testOrder);

            // Verify save was called when status was SEARCHING (first call)
            verify(orderRepository, times(2)).save(testOrder);
        }

        @Test
        @DisplayName("Should enqueue order when no free couriers (instead of throwing)")
        void shouldEnqueueWhenNoCouriersAvailable() {
            when(courierRepository.findFree()).thenReturn(Collections.emptyList());
            when(matchingStrategy.findBestCourier(any(), any())).thenReturn(Optional.empty());
            when(orderRepository.save(any(Order.class))).thenReturn(testOrder);

            dispatchService.dispatch(testOrder);

            assertEquals(OrderStatus.QUEUED, testOrder.getStatus());
            assertEquals(1, dispatchService.getQueueSize());
        }

        @Test
        @DisplayName("Should enqueue order when strategy returns empty (all couriers filtered out)")
        void shouldEnqueueWhenStrategyReturnsEmpty() {
            when(courierRepository.findFree()).thenReturn(List.of(testCourier));
            when(matchingStrategy.findBestCourier(any(), any())).thenReturn(Optional.empty());
            when(orderRepository.save(any(Order.class))).thenReturn(testOrder);

            dispatchService.dispatch(testOrder);

            assertEquals(OrderStatus.QUEUED, testOrder.getStatus());
            assertEquals(1, dispatchService.getQueueSize());
        }
    }

    @Nested
    @DisplayName("completeOrder")
    class CompleteOrder {

        @Test
        @DisplayName("Should complete assigned order and free courier")
        void shouldCompleteOrderAndFreeCourier() {
            testOrder.setStatus(OrderStatus.ASSIGNED);
            testOrder.setAssignedCourierId(testCourier.getId());
            testCourier.setStatus(CourierStatus.BUSY);

            when(orderRepository.save(any(Order.class))).thenReturn(testOrder);
            when(courierRepository.findById(testCourier.getId())).thenReturn(Optional.of(testCourier));
            when(courierRepository.save(any(Courier.class))).thenReturn(testCourier);

            Order result = dispatchService.completeOrder(testOrder);

            assertEquals(OrderStatus.COMPLETED, result.getStatus());
            assertEquals(CourierStatus.FREE, testCourier.getStatus());
            verify(courierRepository).save(testCourier);
        }

        @Test
        @DisplayName("Should increment completedOrdersToday when completing order")
        void shouldIncrementCompletedOrdersToday() {
            testOrder.setStatus(OrderStatus.ASSIGNED);
            testOrder.setAssignedCourierId(testCourier.getId());
            testCourier.setStatus(CourierStatus.BUSY);
            assertEquals(0, testCourier.getCompletedOrdersToday());

            when(orderRepository.save(any(Order.class))).thenReturn(testOrder);
            when(courierRepository.findById(testCourier.getId())).thenReturn(Optional.of(testCourier));
            when(courierRepository.save(any(Courier.class))).thenReturn(testCourier);

            dispatchService.completeOrder(testOrder);

            assertEquals(1, testCourier.getCompletedOrdersToday());
        }

        @Test
        @DisplayName("Should process queue after completing order")
        void shouldProcessQueueAfterComplete() {
            // First: enqueue an order (no couriers available)
            Order queuedOrder = new Order(new Point(10, 10), new Point(20, 20), 3, 2.0);
            when(courierRepository.findFree()).thenReturn(Collections.emptyList());
            when(matchingStrategy.findBestCourier(any(), any())).thenReturn(Optional.empty());
            when(orderRepository.save(any(Order.class))).thenAnswer(inv -> inv.getArgument(0));

            dispatchService.dispatch(queuedOrder);
            assertEquals(1, dispatchService.getQueueSize());
            assertEquals(OrderStatus.QUEUED, queuedOrder.getStatus());

            // Now: complete the testOrder, which frees testCourier
            testOrder.setStatus(OrderStatus.ASSIGNED);
            testOrder.setAssignedCourierId(testCourier.getId());
            testCourier.setStatus(CourierStatus.BUSY);

            when(courierRepository.findById(testCourier.getId())).thenReturn(Optional.of(testCourier));
            when(courierRepository.save(any(Courier.class))).thenReturn(testCourier);
            // After freeing: processQueue finds free courier
            when(courierRepository.findFree()).thenReturn(List.of(testCourier));
            when(orderRepository.findById(queuedOrder.getId())).thenReturn(Optional.of(queuedOrder));
            when(matchingStrategy.findBestCourier(eq(queuedOrder), any()))
                    .thenReturn(Optional.of(testCourier));

            dispatchService.completeOrder(testOrder);

            // The queued order should now be assigned
            assertEquals(OrderStatus.ASSIGNED, queuedOrder.getStatus());
            assertEquals(testCourier.getId(), queuedOrder.getAssignedCourierId());
            assertEquals(0, dispatchService.getQueueSize());
        }

        @Test
        @DisplayName("Should throw when completing non-ASSIGNED order")
        void shouldThrowWhenCompletingCreatedOrder() {
            // Order is in CREATED status
            assertThrows(IllegalStateException.class,
                    () -> dispatchService.completeOrder(testOrder));
        }

        @Test
        @DisplayName("Should throw when completing SEARCHING order")
        void shouldThrowWhenCompletingSearchingOrder() {
            testOrder.setStatus(OrderStatus.SEARCHING);
            assertThrows(IllegalStateException.class,
                    () -> dispatchService.completeOrder(testOrder));
        }

        @Test
        @DisplayName("Should throw when completing already COMPLETED order")
        void shouldThrowWhenCompletingCompletedOrder() {
            testOrder.setStatus(OrderStatus.COMPLETED);
            assertThrows(IllegalStateException.class,
                    () -> dispatchService.completeOrder(testOrder));
        }

        @Test
        @DisplayName("Should throw when completing QUEUED order")
        void shouldThrowWhenCompletingQueuedOrder() {
            testOrder.setStatus(OrderStatus.QUEUED);
            assertThrows(IllegalStateException.class,
                    () -> dispatchService.completeOrder(testOrder));
        }

        @Test
        @DisplayName("Should handle null assignedCourierId gracefully")
        void shouldHandleNullCourierId() {
            testOrder.setStatus(OrderStatus.ASSIGNED);
            testOrder.setAssignedCourierId(null);
            when(orderRepository.save(any(Order.class))).thenReturn(testOrder);

            Order result = dispatchService.completeOrder(testOrder);
            assertEquals(OrderStatus.COMPLETED, result.getStatus());
            verify(courierRepository, never()).findById(any());
        }
    }

    @Nested
    @DisplayName("getStats")
    class GetStats {

        @Test
        @DisplayName("Should return correct statistics including queuedOrders")
        void shouldReturnStats() {
            when(orderRepository.count()).thenReturn(5L);
            when(courierRepository.count()).thenReturn(8L);
            when(orderRepository.countByStatus(any(OrderStatus.class))).thenReturn(1L);
            when(courierRepository.countByStatus(any(CourierStatus.class))).thenReturn(2L);

            DispatchStatsResponse stats = dispatchService.getStats();

            assertEquals(5, stats.getTotalOrders());
            assertEquals(8, stats.getTotalCouriers());
            assertEquals(0, stats.getTotalAssignments());
            assertEquals(0, stats.getQueuedOrders());
            assertNotNull(stats.getOrdersByStatus());
            assertNotNull(stats.getCouriersByStatus());
            assertEquals(OrderStatus.values().length, stats.getOrdersByStatus().size());
            assertEquals(CourierStatus.values().length, stats.getCouriersByStatus().size());
        }

        @Test
        @DisplayName("Should increment totalAssignments after dispatch")
        void shouldTrackTotalAssignments() {
            // Dispatch an order first
            when(courierRepository.findFree()).thenReturn(List.of(testCourier));
            when(matchingStrategy.findBestCourier(any(), any()))
                    .thenReturn(Optional.of(testCourier));
            when(orderRepository.save(any())).thenReturn(testOrder);
            when(courierRepository.save(any())).thenReturn(testCourier);

            dispatchService.dispatch(testOrder);

            // Now check stats
            when(orderRepository.count()).thenReturn(1L);
            when(courierRepository.count()).thenReturn(1L);
            when(orderRepository.countByStatus(any())).thenReturn(0L);
            when(courierRepository.countByStatus(any())).thenReturn(0L);

            DispatchStatsResponse stats = dispatchService.getStats();
            assertEquals(1, stats.getTotalAssignments());
        }
    }
}
