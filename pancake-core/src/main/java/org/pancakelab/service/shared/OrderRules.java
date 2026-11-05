package org.pancakelab.service.shared;

import org.pancakelab.common.OrderOperation;
import org.pancakelab.common.OrderWorkflow;
import org.pancakelab.api.model.Ingredient;
import org.pancakelab.model.Order;
import org.pancakelab.model.pancakes.Pancake;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Rules shared by the services: moving an order through the order workflow and
 * describing pancakes.
 */
public final class OrderRules {
    private OrderRules() {
    }

    /**
     * Moves the order to the status the operation leads to, or throws {@link IllegalStateException}.
     */
    public static void transition(Order order, OrderOperation operation) {
        order.setStatus(OrderWorkflow.next(order.getStatus(), operation));
    }

    public static String describe(Pancake pancake) {
        return "Delicious pancake with %s!".formatted(pancake.getIngredients().stream().map(Ingredient::description).collect(Collectors.joining(", ")));
    }

    public static List<String> descriptions(Order order) {
        return order.getPancakes().stream().map(OrderRules::describe).toList();
    }
}
