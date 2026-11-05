package org.pancakelab.service.pancake;

import org.pancakelab.service.order.OrderService;
import org.pancakelab.api.model.Ingredient;

import java.util.List;
import java.util.UUID;

/**
 * Pancake operations on an existing order: start a pancake, add ingredients one by one, remove
 * pancakes. Order creation and lifecycle belong to {@link OrderService}. Only ids and strings
 * cross its boundary, never domain objects.
 */
public interface PancakeService {
    /** Starts a new, empty pancake; add its ingredients one by one with {@link #addIngredientToPancake}. */
    UUID addPancake(UUID orderId);

    /** The ingredients table: every ingredient with its id and description. */
    List<Ingredient> listIngredients();

    /** Adds the ingredient with the given id (see {@link #listIngredients}) to the pancake. */
    void addIngredientToPancake(UUID orderId, UUID pancakeId, int ingredientId);

    void removePancakes(String description, UUID orderId, int count);
}
