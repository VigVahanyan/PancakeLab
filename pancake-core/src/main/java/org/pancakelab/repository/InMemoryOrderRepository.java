package org.pancakelab.repository;

import org.pancakelab.common.OrderStatus;
import org.pancakelab.model.Order;

import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class InMemoryOrderRepository implements OrderRepository {
    private final Map<UUID, Order> orders = new ConcurrentHashMap<>();

    @Override
    public void save(Order order) {
        orders.put(order.getId(), order);
    }

    @Override
    public Optional<Order> findById(UUID orderId) {
        return Optional.ofNullable(orders.get(orderId));
    }

    @Override
    public void remove(UUID orderId) {
        orders.remove(orderId);
    }

    @Override
    public Set<UUID> findIdsByStatus(OrderStatus status) {
        return orders.values().stream()
                .filter(order -> order.getStatus() == status)
                .map(Order::getId)
                .collect(Collectors.toUnmodifiableSet());
    }
}
