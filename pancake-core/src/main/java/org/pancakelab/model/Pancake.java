package org.pancakelab.model;

import org.pancakelab.api.model.Ingredient;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * A pancake is an ordered list of catalog ingredients; there are no hardcoded recipes. It can only be
 * changed through its {@link Order}, which checks that the order is still open. Its own methods are
 * {@code synchronized} so readers never see a half-changed list; the order's lock is always taken first.
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
     * Read-only snapshot of the ingredients in the order they were added.
     */
    public synchronized List<Ingredient> getIngredients() {
        return List.copyOf(ingredients);
    }

    public synchronized String description() {
        return "Delicious pancake with %s!".formatted(
                ingredients.stream().map(Ingredient::description).collect(Collectors.joining(", ")));
    }

    synchronized void addIngredient(Ingredient ingredient) {
        ingredients.add(ingredient);
    }
}
