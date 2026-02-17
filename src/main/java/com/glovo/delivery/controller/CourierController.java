package com.glovo.delivery.controller;

import com.glovo.delivery.dto.UpdateLocationRequest;
import com.glovo.delivery.model.Courier;
import com.glovo.delivery.service.CourierService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/couriers")
@Tag(name = "Couriers", description = "Courier management endpoints")
public class CourierController {

    private final CourierService courierService;

    public CourierController(CourierService courierService) {
        this.courierService = courierService;
    }

    @GetMapping("/free")
    @Operation(summary = "Get list of all free couriers")
    public ResponseEntity<List<Courier>> getFreeCouriers() {
        List<Courier> freeCouriers = courierService.getFreeCouriers();
        return ResponseEntity.ok(freeCouriers);
    }

    @PatchMapping("/{id}/location")
    @Operation(summary = "Update courier location (simulate movement)")
    public ResponseEntity<Courier> updateLocation(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateLocationRequest request) {
        Courier courier = courierService.updateLocation(id, request);
        return ResponseEntity.ok(courier);
    }
}
