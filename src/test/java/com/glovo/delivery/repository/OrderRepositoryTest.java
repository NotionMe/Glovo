package com.glovo.delivery.repository;

import com.glovo.delivery.model.Order;
import com.glovo.delivery.model.Point;
import com.glovo.delivery.model.enums.OrderStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class OrderRepositoryTest {

    private OrderRepository repository;

    @BeforeEach
    void setUp() {
        repository = new OrderRepository();
    }

    @Test
    void shouldSaveAndFindById() {
        Order order = new Order(new Point(10, 20), new Point(30, 40), 5, 3.0);
        repository.save(order);

        Optional<Order> found = repository.findById(order.getId());
        assertTrue(found.isPresent());
        assertEquals(order.getId(), found.get().getId());
    }

    @Test
    void shouldReturnEmptyForNonExistentId() {
        Optional<Order> found = repository.findById(UUID.randomUUID());
        assertTrue(found.isEmpty());
    }

    @Test
    void shouldFindAll() {
        Order o1 = new Order(new Point(10, 10), new Point(20, 20), 1, 2.0);
        Order o2 = new Order(new Point(30, 30), new Point(40, 40), 3, 4.0);
        repository.save(o1);
        repository.save(o2);

        List<Order> all = repository.findAll();
        assertEquals(2, all.size());
    }

    @Test
    void shouldFindByStatus() {
        Order o1 = new Order(new Point(10, 10), new Point(20, 20), 1, 2.0);
        Order o2 = new Order(new Point(30, 30), new Point(40, 40), 3, 4.0);
        o2.setStatus(OrderStatus.ASSIGNED);
        repository.save(o1);
        repository.save(o2);

        List<Order> created = repository.findByStatus(OrderStatus.CREATED);
        assertEquals(1, created.size());
        assertEquals(o1.getId(), created.get(0).getId());

        List<Order> assigned = repository.findByStatus(OrderStatus.ASSIGNED);
        assertEquals(1, assigned.size());
        assertEquals(o2.getId(), assigned.get(0).getId());
    }

    @Test
    void shouldCountByStatus() {
        Order o1 = new Order(new Point(10, 10), new Point(20, 20), 1, 2.0);
        Order o2 = new Order(new Point(30, 30), new Point(40, 40), 3, 4.0);
        repository.save(o1);
        repository.save(o2);

        assertEquals(2, repository.countByStatus(OrderStatus.CREATED));
        assertEquals(0, repository.countByStatus(OrderStatus.ASSIGNED));
    }

    @Test
    void shouldCount() {
        assertEquals(0, repository.count());
        repository.save(new Order(new Point(10, 10), new Point(20, 20), 1, 2.0));
        assertEquals(1, repository.count());
    }

    @Test
    void shouldDeleteById() {
        Order order = new Order(new Point(10, 10), new Point(20, 20), 1, 2.0);
        repository.save(order);
        assertEquals(1, repository.count());

        repository.deleteById(order.getId());
        assertEquals(0, repository.count());
        assertTrue(repository.findById(order.getId()).isEmpty());
    }

    @Test
    void shouldClear() {
        repository.save(new Order(new Point(10, 10), new Point(20, 20), 1, 2.0));
        repository.save(new Order(new Point(30, 30), new Point(40, 40), 3, 4.0));
        assertEquals(2, repository.count());

        repository.clear();
        assertEquals(0, repository.count());
    }

    @Test
    void shouldOverwriteOnSaveWithSameId() {
        Order order = new Order(new Point(10, 10), new Point(20, 20), 5, 3.0);
        repository.save(order);
        order.setStatus(OrderStatus.COMPLETED);
        repository.save(order);

        assertEquals(1, repository.count());
        assertEquals(OrderStatus.COMPLETED, repository.findById(order.getId()).get().getStatus());
    }
}
