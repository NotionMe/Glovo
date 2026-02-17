package com.glovo.delivery.service;

import com.glovo.delivery.dto.UpdateLocationRequest;
import com.glovo.delivery.exception.CourierNotFoundException;
import com.glovo.delivery.model.Courier;
import com.glovo.delivery.model.enums.CourierStatus;
import com.glovo.delivery.repository.CourierRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class CourierService {

    private static final Logger log = LoggerFactory.getLogger(CourierService.class);

    private final CourierRepository courierRepository;

    public CourierService(CourierRepository courierRepository) {
        this.courierRepository = courierRepository;
    }

    public Courier getCourier(UUID id) {
        return courierRepository.findById(id)
                .orElseThrow(() -> new CourierNotFoundException("Courier not found: " + id));
    }

    public List<Courier> getFreeCouriers() {
        return courierRepository.findFree();
    }

    public List<Courier> getAllCouriers() {
        return courierRepository.findAll();
    }

    public Courier updateLocation(UUID id, UpdateLocationRequest request) {
        Courier courier = getCourier(id);
        courier.setCurrentLocation(request.getLocation());
        courierRepository.save(courier);
        log.info("Courier {} location updated to {}", id, request.getLocation());
        return courier;
    }

    public Courier registerCourier(Courier courier) {
        courier = courierRepository.save(courier);
        log.info("Courier registered: {} [{}] at {}", courier.getId(), courier.getType(), courier.getCurrentLocation());
        return courier;
    }
}
