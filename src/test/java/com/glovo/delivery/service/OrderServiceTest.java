package com.glovo.delivery.service;

import com.glovo.delivery.dto.CreateOrderRequest;
import com.glovo.delivery.exception.OrderNotFoundException;
import com.glovo.delivery.model.Order;
import com.glovo.delivery.model.Point;
import com.glovo.delivery.model.enums.OrderStatus;
import com.glovo.delivery.repository.OrderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private DispatchService dispatchService;

    @InjectMocks
    private OrderService orderService;

    private CreateOrderRequest validRequest;

    @BeforeEach
    void setUp() {
        validRequest = new CreateOrderRequest(
                new Point(10, 20),
                new Point(80, 90),
                5,
                3.0
        );
    }

    @Test
    @DisplayName("createOrder should create order and trigger dispatch")
    void shouldCreateOrderAndDispatch() {
        when(orderRepository.save(any(Order.class))).thenAnswer(inv -> inv.getArgument(0));
        doNothing().when(dispatchService).dispatch(any(Order.class));

        Order result = orderService.createOrder(validRequest);

        assertNotNull(result.getId());
        assertEquals(new Point(10, 20), result.getPickupLocation());
        assertEquals(new Point(80, 90), result.getDeliveryLocation());
        assertEquals(5, result.getPriority());
        verify(orderRepository).save(any(Order.class));
        verify(dispatchService).dispatch(any(Order.class));
    }

    @Test
    @DisplayName("getOrder should return order when found")
    void shouldReturnOrderWhenFound() {
        Order order = new Order(new Point(10, 20), new Point(30, 40), 5, 3.0);
        when(orderRepository.findById(order.getId())).thenReturn(Optional.of(order));

        Order result = orderService.getOrder(order.getId());
        assertEquals(order.getId(), result.getId());
    }

    @Test
    @DisplayName("getOrder should throw when not found")
    void shouldThrowWhenOrderNotFound() {
        UUID id = UUID.randomUUID();
        when(orderRepository.findById(id)).thenReturn(Optional.empty());

        assertThrows(OrderNotFoundException.class, () -> orderService.getOrder(id));
    }

    @Test
    @DisplayName("completeOrder should delegate to dispatchService")
    void shouldCompleteOrder() {
        Order order = new Order(new Point(10, 20), new Point(30, 40), 5, 3.0);
        order.setStatus(OrderStatus.ASSIGNED);

        when(orderRepository.findById(order.getId())).thenReturn(Optional.of(order));
        when(dispatchService.completeOrder(order)).thenReturn(order);

        Order result = orderService.completeOrder(order.getId());
        verify(dispatchService).completeOrder(order);
    }

    @Test
    @DisplayName("getAllOrders should delegate to repository")
    void shouldGetAllOrders() {
        when(orderRepository.findAll()).thenReturn(List.of(
                new Order(new Point(10, 20), new Point(30, 40), 5, 3.0)
        ));

        List<Order> result = orderService.getAllOrders();
        assertEquals(1, result.size());
        verify(orderRepository).findAll();
    }

    @Test
    @DisplayName("getOrdersByStatus should delegate to repository")
    void shouldGetOrdersByStatus() {
        when(orderRepository.findByStatus(OrderStatus.ASSIGNED)).thenReturn(List.of());

        List<Order> result = orderService.getOrdersByStatus(OrderStatus.ASSIGNED);
        assertTrue(result.isEmpty());
        verify(orderRepository).findByStatus(OrderStatus.ASSIGNED);
    }
}
