package org.pancakelab.service.shared;

import org.pancakelab.common.InvalidOrderStateException;
import org.pancakelab.common.OrderNotFoundException;
import org.pancakelab.repository.OrderRepository;
import org.pancakelab.model.Order;

import java.util.UUID;
import java.util.function.Supplier;

/**
 * Shared helper for services working on stored orders: validated lookup and
 * translation of illegal transitions into {@link InvalidOrderStateException}.
 */
public final class OrderLookup {
    private final OrderRepository orders;

    public OrderLookup(OrderRepository orders) {
        this.orders = orders;
    }

    public Order find(UUID orderId) {
        requireId(orderId, "orderId");
        return orders.findById(orderId).orElseThrow(() -> new OrderNotFoundException(orderId));
    }

    public static void requireId(UUID id, String name) {
        if (id == null) {
            throw new IllegalArgumentException(name + " must not be null");
        }
    }

    public static <T> T guarded(Supplier<T> action) {
        try {
            return action.get();
        } catch (IllegalStateException e) {
            throw new InvalidOrderStateException(e.getMessage(), e);
        }
    }

    public static void guarded(Runnable action) {
        guarded(() -> { action.run(); return null; });
    }
}
