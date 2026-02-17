package com.glovo.delivery.controller;

import com.glovo.delivery.dto.DispatchStatsResponse;
import com.glovo.delivery.service.DispatchService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/dispatch")
@Tag(name = "Dispatch", description = "Dispatch statistics endpoints")
public class DispatchController {

    private final DispatchService dispatchService;

    public DispatchController(DispatchService dispatchService) {
        this.dispatchService = dispatchService;
    }

    @GetMapping("/stats")
    @Operation(summary = "Get overall system dispatch statistics")
    public ResponseEntity<DispatchStatsResponse> getStats() {
        DispatchStatsResponse stats = dispatchService.getStats();
        return ResponseEntity.ok(stats);
    }
}
