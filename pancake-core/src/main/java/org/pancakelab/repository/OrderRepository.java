package org.pancakelab.repository;

import org.pancakelab.common.OrderStatus;
import org.pancakelab.model.Order;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;

/**
 * Storage of orders.
 */
public interface OrderRepository {
    void save(Order order);

    Optional<Order> findById(UUID orderId);

    void remove(UUID orderId);

    /**
     * @return an immutable snapshot of the ids of the orders currently in the given status
     */
    Set<UUID> findIdsByStatus(OrderStatus status);
}
