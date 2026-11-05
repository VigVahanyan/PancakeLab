package org.pancakelab.model.pancakes;

import org.pancakelab.api.model.Ingredient;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 * The ingredients table: each ingredient has a unique positive id and a non-blank description, and
 * a pancake may only contain ingredients from it. Validates its rows on creation. Immutable.
 */
public final class IngredientCatalog {
    private static final String DEFAULT_RESOURCE = "/ingredients.properties";

    private final Map<Integer, Ingredient> byId;

    public IngredientCatalog(Collection<Ingredient> ingredients) {
        Map<Integer, Ingredient> table = new LinkedHashMap<>();
        ingredients.stream().sorted(Comparator.comparingInt(Ingredient::id)).forEach(ingredient -> {
            validate(ingredient);
            if (table.put(ingredient.id(), ingredient) != null) {
                throw new IllegalArgumentException("Duplicate ingredient id " + ingredient.id());
            }
        });
        this.byId = Map.copyOf(table);
    }

    private static void validate(Ingredient ingredient) {
        if (ingredient.id() <= 0) {
            throw new IllegalArgumentException("Ingredient id must be positive: " + ingredient.id());
        }
        if (ingredient.description() == null || ingredient.description().isBlank()) {
            throw new IllegalArgumentException(
                    "Ingredient %d must have a description".formatted(ingredient.id()));
        }
    }

    /** Loads {@code ingredients.properties}: one {@code <id>=<description>} entry per ingredient. */
    public static IngredientCatalog defaultCatalog() {
        try (InputStream in = IngredientCatalog.class.getResourceAsStream(DEFAULT_RESOURCE)) {
            if (in == null) {
                throw new IllegalStateException("Missing resource " + DEFAULT_RESOURCE);
            }
            Properties properties = new Properties();
            properties.load(in);
            return new IngredientCatalog(properties.stringPropertyNames().stream()
                    .map(key -> new Ingredient(Integer.parseInt(key.trim()), properties.getProperty(key).trim()))
                    .toList());
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    /** @return all ingredients, ordered by id */
    public List<Ingredient> all() {
        return byId.values().stream().sorted(Comparator.comparingInt(Ingredient::id)).toList();
    }

    /**
     * @throws IllegalArgumentException if there is no ingredient with the given id
     */
    public Ingredient require(int id) {
        Ingredient ingredient = byId.get(id);
        if (ingredient == null) {
            throw new IllegalArgumentException("Unknown ingredient id %d. Available: %s".formatted(id, all()));
        }
        return ingredient;
    }
}
