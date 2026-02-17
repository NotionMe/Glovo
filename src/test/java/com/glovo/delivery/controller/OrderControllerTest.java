package com.glovo.delivery.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.glovo.delivery.dto.CreateOrderRequest;
import com.glovo.delivery.exception.NoCouriersAvailableException;
import com.glovo.delivery.exception.OrderNotFoundException;
import com.glovo.delivery.model.Order;
import com.glovo.delivery.model.Point;
import com.glovo.delivery.model.enums.OrderStatus;
import com.glovo.delivery.service.OrderService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(OrderController.class)
class OrderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private OrderService orderService;

    @Nested
    @DisplayName("POST /api/orders")
    class CreateOrder {

        @Test
        void shouldCreateOrderAndReturn201() throws Exception {
            CreateOrderRequest request = new CreateOrderRequest(
                    new Point(10, 20), new Point(80, 90), 5, 3.0);

            Order order = new Order(new Point(10, 20), new Point(80, 90), 5, 3.0);
            order.setStatus(OrderStatus.ASSIGNED);
            when(orderService.createOrder(any())).thenReturn(order);

            mockMvc.perform(post("/api/orders")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.id").exists())
                    .andExpect(jsonPath("$.status").value("ASSIGNED"))
                    .andExpect(jsonPath("$.priority").value(5));
        }

        @Test
        void shouldReturn400ForMissingPickupLocation() throws Exception {
            String json = """
                    {
                        "deliveryLocation": {"x": 80, "y": 90},
                        "priority": 5,
                        "weightKg": 3.0
                    }
                    """;

            mockMvc.perform(post("/api/orders")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(json))
                    .andExpect(status().isBadRequest());
        }

        @Test
        void shouldReturn400ForInvalidPriority() throws Exception {
            String json = """
                    {
                        "pickupLocation": {"x": 10, "y": 20},
                        "deliveryLocation": {"x": 80, "y": 90},
                        "priority": 0,
                        "weightKg": 3.0
                    }
                    """;

            mockMvc.perform(post("/api/orders")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(json))
                    .andExpect(status().isBadRequest());
        }

        @Test
        void shouldReturn400ForInvalidCoordinates() throws Exception {
            String json = """
                    {
                        "pickupLocation": {"x": 150, "y": 20},
                        "deliveryLocation": {"x": 80, "y": 90},
                        "priority": 5,
                        "weightKg": 3.0
                    }
                    """;

            mockMvc.perform(post("/api/orders")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(json))
                    .andExpect(status().isBadRequest());
        }

        @Test
        void shouldReturn503WhenNoCouriersAvailable() throws Exception {
            when(orderService.createOrder(any()))
                    .thenThrow(new NoCouriersAvailableException("No couriers available"));

            CreateOrderRequest request = new CreateOrderRequest(
                    new Point(10, 20), new Point(80, 90), 5, 3.0);

            mockMvc.perform(post("/api/orders")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isServiceUnavailable())
                    .andExpect(jsonPath("$.message").value("No couriers available"));
        }

        @Test
        void shouldReturn400ForMalformedJson() throws Exception {
            mockMvc.perform(post("/api/orders")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{invalid json"))
                    .andExpect(status().isBadRequest());
        }

        @Test
        void shouldReturn400ForMissingWeight() throws Exception {
            String json = """
                    {
                        "pickupLocation": {"x": 10, "y": 20},
                        "deliveryLocation": {"x": 80, "y": 90},
                        "priority": 5
                    }
                    """;

            mockMvc.perform(post("/api/orders")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(json))
                    .andExpect(status().isBadRequest());
        }

        @Test
        void shouldReturn400ForZeroWeight() throws Exception {
            String json = """
                    {
                        "pickupLocation": {"x": 10, "y": 20},
                        "deliveryLocation": {"x": 80, "y": 90},
                        "priority": 5,
                        "weightKg": 0
                    }
                    """;

            mockMvc.perform(post("/api/orders")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(json))
                    .andExpect(status().isBadRequest());
        }

        @Test
        void shouldReturn400ForNegativeWeight() throws Exception {
            String json = """
                    {
                        "pickupLocation": {"x": 10, "y": 20},
                        "deliveryLocation": {"x": 80, "y": 90},
                        "priority": 5,
                        "weightKg": -1.0
                    }
                    """;

            mockMvc.perform(post("/api/orders")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(json))
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("GET /api/orders/{id}")
    class GetOrder {

        @Test
        void shouldReturnOrderWhenFound() throws Exception {
            Order order = new Order(new Point(10, 20), new Point(30, 40), 5, 3.0);
            when(orderService.getOrder(order.getId())).thenReturn(order);

            mockMvc.perform(get("/api/orders/" + order.getId()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(order.getId().toString()))
                    .andExpect(jsonPath("$.priority").value(5));
        }

        @Test
        void shouldReturn404WhenNotFound() throws Exception {
            UUID id = UUID.randomUUID();
            when(orderService.getOrder(id))
                    .thenThrow(new OrderNotFoundException("Order not found: " + id));

            mockMvc.perform(get("/api/orders/" + id))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.status").value(404));
        }
    }

    @Nested
    @DisplayName("PATCH /api/orders/{id}/complete")
    class CompleteOrder {

        @Test
        void shouldCompleteOrder() throws Exception {
            Order order = new Order(new Point(10, 20), new Point(30, 40), 5, 3.0);
            order.setStatus(OrderStatus.COMPLETED);
            when(orderService.completeOrder(order.getId())).thenReturn(order);

            mockMvc.perform(patch("/api/orders/" + order.getId() + "/complete"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value("COMPLETED"));
        }

        @Test
        void shouldReturn409WhenOrderNotAssigned() throws Exception {
            UUID id = UUID.randomUUID();
            when(orderService.completeOrder(id))
                    .thenThrow(new IllegalStateException("Only ASSIGNED orders can be completed"));

            mockMvc.perform(patch("/api/orders/" + id + "/complete"))
                    .andExpect(status().isConflict());
        }
    }
}
