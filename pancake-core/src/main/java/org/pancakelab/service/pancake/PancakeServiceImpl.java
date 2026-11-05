package org.pancakelab.service.pancake;

import org.pancakelab.service.order.OrderService;
import org.pancakelab.service.shared.OrderLog;
import org.pancakelab.service.shared.OrderLookup;
import org.pancakelab.service.shared.OrderRules;
import org.pancakelab.repository.OrderRepository;
import org.pancakelab.api.model.Ingredient;
import org.pancakelab.common.OrderOperation;
import org.pancakelab.model.Order;
import org.pancakelab.model.pancakes.Pancake;
import org.pancakelab.model.pancakes.IngredientCatalog;

import java.util.List;
import java.util.UUID;

import static org.pancakelab.service.shared.OrderLookup.guarded;
import static org.pancakelab.service.shared.OrderLookup.requireId;

/**
 * Pancake operations on an existing order: start a pancake, add ingredients one by one, remove
 * pancakes. Order creation and lifecycle belong to {@link OrderService}. Only ids and strings
 * cross the API.
 */
public class PancakeServiceImpl implements PancakeService {
    private final OrderLookup       lookup;
    private final IngredientCatalog ingredients;
    private final OrderLog          log;

    public PancakeServiceImpl(OrderRepository orders, IngredientCatalog ingredients, OrderLog log) {
        this.lookup      = new OrderLookup(orders);
        this.ingredients = ingredients;
        this.log         = log;
    }

    /** Starts a new, empty pancake; add its ingredients one by one with {@link #addIngredientToPancake}. */
    @Override
    public UUID addPancake(UUID orderId) {
        Order   order = lookup.find(orderId);
        Pancake pancake = guarded(() -> {
            OrderRules.transition(order, OrderOperation.ADD_PANCAKE);
            Pancake created = new Pancake();
            order.getPancakes().add(created);
            return created;
        });
        log.logAddPancake(order, pancake.getId());
        return pancake.getId();
    }

    @Override
    public List<Ingredient> listIngredients() {
        return ingredients.all();
    }

    @Override
    public void addIngredientToPancake(UUID orderId, UUID pancakeId, int ingredientId) {
        requireId(pancakeId, "pancakeId");
        Order      order      = lookup.find(orderId);
        Ingredient ingredient = ingredients.require(ingredientId);
        guarded(() -> {
            OrderRules.transition(order, OrderOperation.ADD_INGREDIENT);
            Pancake pancake = order.getPancakes().stream()
                    .filter(p -> p.getId().equals(pancakeId)).findFirst()
                    .orElseThrow(() -> new IllegalArgumentException(
                            "Pancake %s is not in order %s".formatted(pancakeId, order.getId())));
            pancake.getIngredients().add(ingredient);
        });
        log.logAddIngredient(order, pancakeId, ingredient);
    }

    @Override
    public void removePancakes(String description, UUID orderId, int count) {
        if (description == null || description.isBlank()) {
            throw new IllegalArgumentException("Description must not be blank");
        }
        if (count < 1) {
            throw new IllegalArgumentException("Count must be at least 1: " + count);
        }
        Order order = lookup.find(orderId);
        guarded(() -> {
            OrderRules.transition(order, OrderOperation.REMOVE_PANCAKES);
            List<Pancake> matching = order.getPancakes().stream()
                    .filter(p -> OrderRules.describe(p).equals(description)).toList();
            if (matching.size() < count) {
                throw new IllegalArgumentException("Cannot remove %d pancake(s) '%s': order contains only %d"
                        .formatted(count, description, matching.size()));
            }
            order.getPancakes().removeAll(matching.subList(0, count));
        });
        log.logRemovePancakes(order, description, count);
    }
}
