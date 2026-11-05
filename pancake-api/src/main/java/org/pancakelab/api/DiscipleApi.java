package org.pancakelab.api;

import org.pancakelab.api.model.Ingredient;

import java.util.List;
import java.util.UUID;

/**
 * What a Disciple can do: create an order for a building and room, build pancakes ingredient by
 * ingredient, view the order, then complete or cancel it. Only ids and strings cross the API.
 */
public interface DiscipleApi {
    UUID createOrder(int building, int room);

    /** Starts a new, empty pancake; add its ingredients one by one with {@link #addIngredientToPancake}. */
    UUID addPancake(UUID orderId);

    /** The ingredients table: every ingredient with its id and description. */
    List<Ingredient> listIngredients();

    /** Adds the ingredient with the given id (see {@link #listIngredients}) to the pancake. */
    void addIngredientToPancake(UUID orderId, UUID pancakeId, int ingredientId);

    void removePancakes(String description, UUID orderId, int count);

    List<String> viewOrder(UUID orderId);

    void completeOrder(UUID orderId);

    void cancelOrder(UUID orderId);
}
