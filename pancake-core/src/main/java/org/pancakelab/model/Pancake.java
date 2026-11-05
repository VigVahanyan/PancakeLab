package org.pancakelab.model;

import org.pancakelab.api.model.Ingredient;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * A pancake is an ordered list of catalog ingredients; there are no hardcoded recipes. It can only be
 * changed through its {@link Order}, which checks that the order is still open.
 */
public class Pancake {
    private final UUID id = UUID.randomUUID();
    private final List<Ingredient> ingredients = new ArrayList<>();

    Pancake() {
    }

    public UUID getId() {
        return id;
    }

    /**
     * Read-only view of the ingredients in the order they were added.
     */
    public List<Ingredient> getIngredients() {
        return Collections.unmodifiableList(ingredients);
    }

    public String description() {
        return "Delicious pancake with %s!".formatted(
                ingredients.stream().map(Ingredient::description).collect(Collectors.joining(", ")));
    }

    void addIngredient(Ingredient ingredient) {
        ingredients.add(ingredient);
    }
}
