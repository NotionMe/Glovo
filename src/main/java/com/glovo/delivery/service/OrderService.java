package com.glovo.delivery.service;

import com.glovo.delivery.dto.CreateOrderRequest;
import com.glovo.delivery.exception.OrderNotFoundException;
import com.glovo.delivery.model.Order;
import com.glovo.delivery.model.enums.OrderStatus;
import com.glovo.delivery.repository.OrderRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class OrderService {

    private static final Logger log = LoggerFactory.getLogger(OrderService.class);

    private final OrderRepository orderRepository;
    private final DispatchService dispatchService;

    public OrderService(OrderRepository orderRepository, DispatchService dispatchService) {
        this.orderRepository = orderRepository;
        this.dispatchService = dispatchService;
    }

    public Order createOrder(CreateOrderRequest request) {
        Order order = new Order(
                request.getPickupLocation(),
                request.getDeliveryLocation(),
                request.getPriority()
        );

        order = orderRepository.save(order);
        log.info("Order created: {} with priority {}", order.getId(), order.getPriority());

        // Trigger dispatch (search for courier)
        dispatchService.dispatch(order);

        return order;
    }

    public Order getOrder(UUID id) {
        return orderRepository.findById(id)
                .orElseThrow(() -> new OrderNotFoundException("Order not found: " + id));
    }

    public Order completeOrder(UUID id) {
        Order order = getOrder(id);
        return dispatchService.completeOrder(order);
    }

    public List<Order> getAllOrders() {
        return orderRepository.findAll();
    }

    public List<Order> getOrdersByStatus(OrderStatus status) {
        return orderRepository.findByStatus(status);
    }
}
