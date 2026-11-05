package org.pancakelab.service.shared;

import org.pancakelab.api.model.Ingredient;
import org.pancakelab.model.Order;

import java.util.UUID;
import java.util.function.Consumer;
import java.util.logging.Logger;

/**
 * Audit log of order operations. Writes through {@link java.util.logging} by default, or an injected sink.
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

    public void logAddPancake(Order order, UUID pancakeId) {
        sink.accept("Added pancake %s to order %s containing %d pancakes, %s."
                            .formatted(pancakeId, order.getId(), order.getPancakes().size(), place(order)));
    }

    public void logAddIngredient(Order order, UUID pancakeId, Ingredient ingredient) {
        sink.accept("Added ingredient '%s' (id %d) to pancake %s of order %s containing %d pancakes, %s."
                            .formatted(ingredient.description(), ingredient.id(), pancakeId, order.getId(), order.getPancakes().size(), place(order)));
    }

    public void logRemovePancakes(Order order, String description, int count) {
        sink.accept("Removed %d pancake(s) with description '%s' from order %s now containing %d pancakes, %s."
                            .formatted(count, description, order.getId(), order.getPancakes().size(), place(order)));
    }

    public void logCompleteOrder(Order order) {
        sink.accept("Completed order %s with %d pancakes %s."
                            .formatted(order.getId(), order.getPancakes().size(), place(order)));
    }

    public void logPrepareOrder(Order order) {
        sink.accept("Prepared order %s with %d pancakes %s."
                            .formatted(order.getId(), order.getPancakes().size(), place(order)));
    }

    public void logCancelOrder(Order order) {
        sink.accept("Cancelled order %s with %d pancakes %s."
                            .formatted(order.getId(), order.getPancakes().size(), place(order)));
    }

    public void logDeliverOrder(Order order) {
        sink.accept("Order %s with %d pancakes %s out for delivery."
                            .formatted(order.getId(), order.getPancakes().size(), place(order)));
    }

    private static String place(Order order) {
        return "for building %d, room %d".formatted(order.getBuilding(), order.getRoom());
    }
}
