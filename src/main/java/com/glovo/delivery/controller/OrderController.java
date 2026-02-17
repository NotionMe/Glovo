package com.glovo.delivery.controller;

import com.glovo.delivery.dto.CreateOrderRequest;
import com.glovo.delivery.model.Order;
import com.glovo.delivery.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/orders")
@Tag(name = "Orders", description = "Order management endpoints")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping
    @Operation(summary = "Create a new order and trigger courier search")
    public ResponseEntity<Order> createOrder(@Valid @RequestBody CreateOrderRequest request) {
        Order order = orderService.createOrder(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(order);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get order information by ID")
    public ResponseEntity<Order> getOrder(@PathVariable UUID id) {
        Order order = orderService.getOrder(id);
        return ResponseEntity.ok(order);
    }

    @PatchMapping("/{id}/complete")
    @Operation(summary = "Complete an order and free the assigned courier")
    public ResponseEntity<Order> completeOrder(@PathVariable UUID id) {
        Order order = orderService.completeOrder(id);
        return ResponseEntity.ok(order);
    }
}
