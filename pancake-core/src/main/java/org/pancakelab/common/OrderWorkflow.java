package org.pancakelab.common;

import java.util.EnumMap;
import java.util.Map;
import java.util.Set;

import static org.pancakelab.common.OrderOperation.*;
import static org.pancakelab.common.OrderStatus.*;

/**
 * The order lifecycle as a table: for each status, which operations are available and which
 * status each one leads to. Business preconditions (e.g. a non-empty order) are not part of it.
 */
public final class OrderWorkflow {
    private static final Map<OrderStatus, Map<OrderOperation, OrderStatus>> TRANSITIONS =
            new EnumMap<>(OrderStatus.class);

    static {
        TRANSITIONS.put(CREATED, new EnumMap<>(Map.of(
                ADD_PANCAKE, CREATED,
                ADD_INGREDIENT, CREATED,
                REMOVE_PANCAKES, CREATED,
                COMPLETE, COMPLETED,
                CANCEL, CANCELLED)));
        TRANSITIONS.put(COMPLETED, new EnumMap<>(Map.of(PREPARE, PREPARED)));
        TRANSITIONS.put(PREPARED, new EnumMap<>(Map.of(DELIVER, DELIVERED)));
        TRANSITIONS.put(DELIVERED, new EnumMap<>(OrderOperation.class));
        TRANSITIONS.put(CANCELLED, new EnumMap<>(OrderOperation.class));
    }

    private OrderWorkflow() {
    }

    /**
     * @return the status the operation leads to
     * @throws IllegalStateException if the operation is not available in the given status
     */
    public static OrderStatus next(OrderStatus status, OrderOperation operation) {
        OrderStatus next = TRANSITIONS.get(status).get(operation);
        if (next == null) {
            throw new IllegalStateException("Cannot %s: order is %s, allowed operations: %s"
                    .formatted(operation, status, allowedOperations(status)));
        }
        return next;
    }

    public static boolean allows(OrderStatus status, OrderOperation operation) {
        return TRANSITIONS.get(status).containsKey(operation);
    }

    public static Set<OrderOperation> allowedOperations(OrderStatus status) {
        return Set.copyOf(TRANSITIONS.get(status).keySet());
    }
}
