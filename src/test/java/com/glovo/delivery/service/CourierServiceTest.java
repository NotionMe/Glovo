package com.glovo.delivery.service;

import com.glovo.delivery.dto.UpdateLocationRequest;
import com.glovo.delivery.exception.CourierNotFoundException;
import com.glovo.delivery.model.Courier;
import com.glovo.delivery.model.Point;
import com.glovo.delivery.model.enums.CourierStatus;
import com.glovo.delivery.model.enums.CourierType;
import com.glovo.delivery.repository.CourierRepository;
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
class CourierServiceTest {

    @Mock
    private CourierRepository courierRepository;

    @InjectMocks
    private CourierService courierService;

    private Courier testCourier;

    @BeforeEach
    void setUp() {
        testCourier = new Courier(new Point(10, 20), CourierType.BICYCLE);
    }

    @Test
    @DisplayName("getCourier should return courier when found")
    void shouldReturnCourierWhenFound() {
        when(courierRepository.findById(testCourier.getId())).thenReturn(Optional.of(testCourier));

        Courier result = courierService.getCourier(testCourier.getId());
        assertEquals(testCourier.getId(), result.getId());
    }

    @Test
    @DisplayName("getCourier should throw when not found")
    void shouldThrowWhenCourierNotFound() {
        UUID id = UUID.randomUUID();
        when(courierRepository.findById(id)).thenReturn(Optional.empty());

        assertThrows(CourierNotFoundException.class, () -> courierService.getCourier(id));
    }

    @Test
    @DisplayName("getFreeCouriers should delegate to repository")
    void shouldGetFreeCouriers() {
        List<Courier> freeCouriers = List.of(testCourier);
        when(courierRepository.findFree()).thenReturn(freeCouriers);

        List<Courier> result = courierService.getFreeCouriers();
        assertEquals(1, result.size());
        verify(courierRepository).findFree();
    }

    @Test
    @DisplayName("getAllCouriers should delegate to repository")
    void shouldGetAllCouriers() {
        when(courierRepository.findAll()).thenReturn(List.of(testCourier));

        List<Courier> result = courierService.getAllCouriers();
        assertEquals(1, result.size());
        verify(courierRepository).findAll();
    }

    @Test
    @DisplayName("updateLocation should update and save courier")
    void shouldUpdateLocation() {
        Point newLocation = new Point(80, 90);
        UpdateLocationRequest request = new UpdateLocationRequest(newLocation);

        when(courierRepository.findById(testCourier.getId())).thenReturn(Optional.of(testCourier));
        when(courierRepository.save(any(Courier.class))).thenReturn(testCourier);

        Courier result = courierService.updateLocation(testCourier.getId(), request);
        assertEquals(newLocation, result.getCurrentLocation());
        verify(courierRepository).save(testCourier);
    }

    @Test
    @DisplayName("updateLocation should throw when courier not found")
    void shouldThrowWhenUpdatingNonExistentCourier() {
        UUID id = UUID.randomUUID();
        when(courierRepository.findById(id)).thenReturn(Optional.empty());

        assertThrows(CourierNotFoundException.class,
                () -> courierService.updateLocation(id, new UpdateLocationRequest(new Point(50, 50))));
    }

    @Test
    @DisplayName("registerCourier should save and return courier")
    void shouldRegisterCourier() {
        when(courierRepository.save(testCourier)).thenReturn(testCourier);

        Courier result = courierService.registerCourier(testCourier);
        assertEquals(testCourier.getId(), result.getId());
        verify(courierRepository).save(testCourier);
    }
}
