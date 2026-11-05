package org.pancakelab.api.model;

/**
 * A row of the ingredients table: what the client sees when listing the menu, and what it refers
 * to (by {@code id}) when adding an ingredient to a pancake. Plain data; the rows are validated by
 * the {@code IngredientCatalog} that holds them.
 */
public record Ingredient(int id, String description) {
}
