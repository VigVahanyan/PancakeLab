package org.pancakelab.model.pancakes;

import org.pancakelab.api.model.Ingredient;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Plain data holder: a pancake is just an ordered list of catalog ingredients; there are no hardcoded recipes.
 */
public class Pancake {
    private final UUID         id          = UUID.randomUUID();
    private final List<Ingredient> ingredients = new ArrayList<>();

    public UUID getId() {
        return id;
    }

    /** The live, mutable list of ingredients in the order they were added. */
    public List<Ingredient> getIngredients() {
        return ingredients;
    }
}
