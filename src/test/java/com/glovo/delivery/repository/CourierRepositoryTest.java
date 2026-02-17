package com.glovo.delivery.repository;

import com.glovo.delivery.model.Courier;
import com.glovo.delivery.model.Point;
import com.glovo.delivery.model.enums.CourierStatus;
import com.glovo.delivery.model.enums.CourierType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class CourierRepositoryTest {

    private CourierRepository repository;

    @BeforeEach
    void setUp() {
        repository = new CourierRepository();
    }

    @Test
    void shouldSaveAndFindById() {
        Courier courier = new Courier(new Point(10, 20), CourierType.CAR);
        repository.save(courier);

        Optional<Courier> found = repository.findById(courier.getId());
        assertTrue(found.isPresent());
        assertEquals(courier.getId(), found.get().getId());
    }

    @Test
    void shouldReturnEmptyForNonExistentId() {
        assertTrue(repository.findById(UUID.randomUUID()).isEmpty());
    }

    @Test
    void shouldFindAll() {
        repository.save(new Courier(new Point(10, 10), CourierType.CAR));
        repository.save(new Courier(new Point(20, 20), CourierType.BICYCLE));
        assertEquals(2, repository.findAll().size());
    }

    @Test
    void shouldFindByStatus() {
        Courier free = new Courier(new Point(10, 10), CourierType.CAR);
        Courier busy = new Courier(new Point(20, 20), CourierType.BICYCLE);
        busy.setStatus(CourierStatus.BUSY);

        repository.save(free);
        repository.save(busy);

        List<Courier> freeList = repository.findByStatus(CourierStatus.FREE);
        assertEquals(1, freeList.size());
        assertEquals(free.getId(), freeList.get(0).getId());
    }

    @Test
    void shouldFindFree() {
        Courier free1 = new Courier(new Point(10, 10), CourierType.CAR);
        Courier free2 = new Courier(new Point(20, 20), CourierType.BICYCLE);
        Courier busy = new Courier(new Point(30, 30), CourierType.PEDESTRIAN);
        busy.setStatus(CourierStatus.BUSY);

        repository.save(free1);
        repository.save(free2);
        repository.save(busy);

        List<Courier> freeList = repository.findFree();
        assertEquals(2, freeList.size());
    }

    @Test
    void shouldCountByStatus() {
        Courier free = new Courier(new Point(10, 10), CourierType.CAR);
        Courier busy = new Courier(new Point(20, 20), CourierType.BICYCLE);
        busy.setStatus(CourierStatus.BUSY);

        repository.save(free);
        repository.save(busy);

        assertEquals(1, repository.countByStatus(CourierStatus.FREE));
        assertEquals(1, repository.countByStatus(CourierStatus.BUSY));
        assertEquals(0, repository.countByStatus(CourierStatus.OFFLINE));
    }

    @Test
    void shouldCount() {
        assertEquals(0, repository.count());
        repository.save(new Courier(new Point(10, 10), CourierType.CAR));
        assertEquals(1, repository.count());
    }

    @Test
    void shouldDeleteById() {
        Courier courier = new Courier(new Point(10, 10), CourierType.CAR);
        repository.save(courier);
        assertEquals(1, repository.count());

        repository.deleteById(courier.getId());
        assertEquals(0, repository.count());
    }

    @Test
    void shouldClear() {
        repository.save(new Courier(new Point(10, 10), CourierType.CAR));
        repository.save(new Courier(new Point(20, 20), CourierType.BICYCLE));

        repository.clear();
        assertEquals(0, repository.count());
    }
}
