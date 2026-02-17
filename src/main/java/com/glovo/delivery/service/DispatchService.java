package com.glovo.delivery.service;

import com.glovo.delivery.dto.DispatchStatsResponse;
import com.glovo.delivery.model.Courier;
import com.glovo.delivery.model.Order;
import com.glovo.delivery.model.enums.CourierStatus;
import com.glovo.delivery.model.enums.OrderStatus;
import com.glovo.delivery.repository.CourierRepository;
import com.glovo.delivery.repository.OrderRepository;
import com.glovo.delivery.service.strategy.CourierMatchingStrategy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.ReentrantLock;

@Service
public class DispatchService {

    private static final Logger log = LoggerFactory.getLogger(DispatchService.class);

    private final OrderRepository orderRepository;
    private final CourierRepository courierRepository;
    private final CourierMatchingStrategy matchingStrategy;
    private final AtomicLong totalAssignments = new AtomicLong(0);

    /**
     * Queue for orders that could not be assigned immediately.
     * FIFO — first order in the queue gets assigned first when a courier becomes free.
     */
    private final ConcurrentLinkedQueue<Order> orderQueue = new ConcurrentLinkedQueue<>();

    /**
     * Lock to ensure dispatch is atomic: read free couriers + pick best + assign
     * must not be interleaved between concurrent requests.
     */
    private final ReentrantLock dispatchLock = new ReentrantLock();

    public DispatchService(OrderRepository orderRepository,
                           CourierRepository courierRepository,
                           CourierMatchingStrategy matchingStrategy) {
        this.orderRepository = orderRepository;
        this.courierRepository = courierRepository;
        this.matchingStrategy = matchingStrategy;
    }

    /**
     * Dispatch an order: find the best available courier and assign them.
     * If no suitable courier is available, the order is placed in the queue
     * with status QUEUED instead of throwing an exception.
     * Thread-safe: uses a lock to prevent the same courier from being assigned
     * to multiple orders concurrently (TOCTOU race condition).
     */
    public void dispatch(Order order) {
        order.setStatus(OrderStatus.SEARCHING);
        orderRepository.save(order);
        log.info("Searching for courier for order {}", order.getId());

        dispatchLock.lock();
        try {
            List<Courier> freeCouriers = courierRepository.findFree();

            Optional<Courier> bestCourier = matchingStrategy.findBestCourier(order, freeCouriers);

            if (bestCourier.isPresent()) {
                Courier courier = bestCourier.get();
                assignCourier(order, courier);
            } else {
                enqueueOrder(order);
            }
        } finally {
            dispatchLock.unlock();
        }
    }

    private void assignCourier(Order order, Courier courier) {
        order.setStatus(OrderStatus.ASSIGNED);
        order.setAssignedCourierId(courier.getId());
        orderRepository.save(order);

        courier.setStatus(CourierStatus.BUSY);
        courierRepository.save(courier);

        totalAssignments.incrementAndGet();

        log.info("Order {} assigned to courier {} [{}]",
                order.getId(), courier.getId(), courier.getType());
    }

    /**
     * Place an order in the queue when no suitable courier is available.
     */
    private void enqueueOrder(Order order) {
        order.setStatus(OrderStatus.QUEUED);
        orderRepository.save(order);
        orderQueue.add(order);
        log.info("Order {} placed in queue (no suitable courier available). Queue size: {}",
                order.getId(), orderQueue.size());
    }

    /**
     * Complete an order and free the assigned courier.
     * After freeing the courier, automatically tries to assign queued orders.
     */
    public Order completeOrder(Order order) {
        if (order.getStatus() != OrderStatus.ASSIGNED) {
            throw new IllegalStateException(
                    "Only ASSIGNED orders can be completed. Current status: " + order.getStatus());
        }

        order.setStatus(OrderStatus.COMPLETED);
        orderRepository.save(order);

        // Free the courier and increment completedOrdersToday
        if (order.getAssignedCourierId() != null) {
            courierRepository.findById(order.getAssignedCourierId()).ifPresent(courier -> {
                courier.setStatus(CourierStatus.FREE);
                courier.incrementCompletedOrdersToday();
                courierRepository.save(courier);
                log.info("Courier {} [{}] is now FREE after completing order {} (completedToday={})",
                        courier.getId(), courier.getType(), order.getId(),
                        courier.getCompletedOrdersToday());
            });
        }

        log.info("Order {} completed", order.getId());

        // Try to assign queued orders now that a courier is free
        processQueue();

        return order;
    }

    /**
     * Process the order queue: try to assign the first queued order to a free courier.
     * Called automatically after a courier is freed.
     */
    private void processQueue() {
        dispatchLock.lock();
        try {
            while (!orderQueue.isEmpty()) {
                Order queuedOrder = orderQueue.peek();

                // Verify order is still in QUEUED status (might have been cancelled/modified)
                Optional<Order> freshOrder = orderRepository.findById(queuedOrder.getId());
                if (freshOrder.isEmpty() || freshOrder.get().getStatus() != OrderStatus.QUEUED) {
                    orderQueue.poll(); // Remove stale entry
                    continue;
                }

                List<Courier> freeCouriers = courierRepository.findFree();
                Optional<Courier> bestCourier = matchingStrategy.findBestCourier(freshOrder.get(), freeCouriers);

                if (bestCourier.isPresent()) {
                    orderQueue.poll(); // Remove from queue
                    Order orderToAssign = freshOrder.get();
                    assignCourier(orderToAssign, bestCourier.get());
                    log.info("Queued order {} assigned from queue. Remaining queue size: {}",
                            orderToAssign.getId(), orderQueue.size());
                } else {
                    // No suitable courier for the first queued order — stop processing
                    log.debug("No suitable courier for queued order {}. Stopping queue processing.",
                            queuedOrder.getId());
                    break;
                }
            }
        } finally {
            dispatchLock.unlock();
        }
    }

    /**
     * Get the current queue size.
     */
    public int getQueueSize() {
        return orderQueue.size();
    }

    /**
     * Get system dispatch statistics.
     */
    public DispatchStatsResponse getStats() {
        DispatchStatsResponse stats = new DispatchStatsResponse();

        stats.setTotalOrders(orderRepository.count());
        stats.setTotalCouriers(courierRepository.count());
        stats.setTotalAssignments(totalAssignments.get());
        stats.setQueuedOrders(orderQueue.size());

        // Orders by status
        Map<String, Long> ordersByStatus = new LinkedHashMap<>();
        for (OrderStatus status : OrderStatus.values()) {
            ordersByStatus.put(status.name(), orderRepository.countByStatus(status));
        }
        stats.setOrdersByStatus(ordersByStatus);

        // Couriers by status
        Map<String, Long> couriersByStatus = new LinkedHashMap<>();
        for (CourierStatus status : CourierStatus.values()) {
            couriersByStatus.put(status.name(), courierRepository.countByStatus(status));
        }
        stats.setCouriersByStatus(couriersByStatus);

        return stats;
    }
}
