package com.glovo.delivery.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.glovo.delivery.dto.UpdateLocationRequest;
import com.glovo.delivery.exception.CourierNotFoundException;
import com.glovo.delivery.model.Courier;
import com.glovo.delivery.model.Point;
import com.glovo.delivery.model.enums.CourierType;
import com.glovo.delivery.service.CourierService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CourierController.class)
class CourierControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private CourierService courierService;

    @Nested
    @DisplayName("GET /api/couriers/free")
    class GetFreeCouriers {

        @Test
        void shouldReturnFreeCouriers() throws Exception {
            Courier courier = new Courier(new Point(10, 20), CourierType.BICYCLE);
            when(courierService.getFreeCouriers()).thenReturn(List.of(courier));

            mockMvc.perform(get("/api/couriers/free"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()").value(1))
                    .andExpect(jsonPath("$[0].type").value("BICYCLE"))
                    .andExpect(jsonPath("$[0].status").value("FREE"));
        }

        @Test
        void shouldReturnEmptyListWhenNoFreeCouriers() throws Exception {
            when(courierService.getFreeCouriers()).thenReturn(Collections.emptyList());

            mockMvc.perform(get("/api/couriers/free"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()").value(0));
        }
    }

    @Nested
    @DisplayName("PATCH /api/couriers/{id}/location")
    class UpdateLocation {

        @Test
        void shouldUpdateCourierLocation() throws Exception {
            Courier courier = new Courier(new Point(80, 90), CourierType.CAR);
            UpdateLocationRequest request = new UpdateLocationRequest(new Point(80, 90));

            when(courierService.updateLocation(eq(courier.getId()), any()))
                    .thenReturn(courier);

            mockMvc.perform(patch("/api/couriers/" + courier.getId() + "/location")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.currentLocation.x").value(80))
                    .andExpect(jsonPath("$.currentLocation.y").value(90));
        }

        @Test
        void shouldReturn404WhenCourierNotFound() throws Exception {
            UUID id = UUID.randomUUID();
            when(courierService.updateLocation(eq(id), any()))
                    .thenThrow(new CourierNotFoundException("Courier not found: " + id));

            UpdateLocationRequest request = new UpdateLocationRequest(new Point(50, 50));

            mockMvc.perform(patch("/api/couriers/" + id + "/location")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isNotFound());
        }

        @Test
        void shouldReturn400ForInvalidLocation() throws Exception {
            UUID id = UUID.randomUUID();
            String json = """
                    {
                        "location": {"x": 200, "y": 50}
                    }
                    """;

            mockMvc.perform(patch("/api/couriers/" + id + "/location")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(json))
                    .andExpect(status().isBadRequest());
        }

        @Test
        void shouldReturn400ForMissingLocation() throws Exception {
            UUID id = UUID.randomUUID();

            mockMvc.perform(patch("/api/couriers/" + id + "/location")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{}"))
                    .andExpect(status().isBadRequest());
        }
    }
}
