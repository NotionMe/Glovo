package com.glovo.delivery.controller;

import com.glovo.delivery.dto.DispatchStatsResponse;
import com.glovo.delivery.service.DispatchService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Map;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(DispatchController.class)
class DispatchControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private DispatchService dispatchService;

    @Test
    @DisplayName("GET /api/dispatch/stats should return statistics")
    void shouldReturnStats() throws Exception {
        DispatchStatsResponse stats = new DispatchStatsResponse();
        stats.setTotalOrders(10);
        stats.setTotalCouriers(8);
        stats.setTotalAssignments(7);
        stats.setOrdersByStatus(Map.of("CREATED", 1L, "ASSIGNED", 3L, "COMPLETED", 6L));
        stats.setCouriersByStatus(Map.of("FREE", 5L, "BUSY", 3L));

        when(dispatchService.getStats()).thenReturn(stats);

        mockMvc.perform(get("/api/dispatch/stats"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalOrders").value(10))
                .andExpect(jsonPath("$.totalCouriers").value(8))
                .andExpect(jsonPath("$.totalAssignments").value(7))
                .andExpect(jsonPath("$.ordersByStatus.CREATED").value(1))
                .andExpect(jsonPath("$.couriersByStatus.FREE").value(5));
    }
}
