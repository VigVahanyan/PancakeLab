package org.pancakelab.service.shared;

import org.pancakelab.common.OrderOperation;
import org.pancakelab.model.Order;

import java.util.function.Consumer;
import java.util.logging.Logger;

/**
 * Audit log of order operations: {@link #log} records an operation and the resulting order status.
 * Writes through {@link java.util.logging} (INFO) by default, or to an injected sink.
 */
public class OrderLog {
    private static final Logger LOGGER = Logger.getLogger(OrderLog.class.getName());

    private final Consumer<String> sink;

    public OrderLog() {
        this(LOGGER::info);
    }

    public OrderLog(Consumer<String> sink) {
        this.sink = sink;
    }

    /**
     * Logs an operation that succeeded, with the order's status after it.
     */
    public void log(OrderOperation operation, Order order) {
        log(operation.name(), order);
    }

    /**
     * Same, for an operation that is not a workflow transition (e.g. {@code CREATE}).
     */
    public void log(String operation, Order order) {
        sink.accept("%s: order %s is now %s (%d pancakes, building %d, room %d)".formatted(
                operation, order.getId(), order.getStatus(), order.getPancakes().size(),
                order.getBuilding(), order.getRoom()));
    }
}
