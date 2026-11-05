package org.pancakelab.model.pancakes;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.pancakelab.api.model.Ingredient;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class IngredientCatalogTest {

    @ParameterizedTest
    @ValueSource(ints = {0, -1, -100})
    public void GivenNonPositiveId_WhenCreatingCatalog_ThenRejected_Test(int id) {
        List<Ingredient> ingredients = List.of(new Ingredient(id, "hazelnuts"));

        assertThrows(IllegalArgumentException.class, () -> new IngredientCatalog(ingredients));
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "   "})
    public void GivenBlankDescription_WhenCreatingCatalog_ThenRejected_Test(String description) {
        List<Ingredient> ingredients = List.of(new Ingredient(1, description));

        assertThrows(IllegalArgumentException.class, () -> new IngredientCatalog(ingredients));
    }

    @Test
    public void GivenNullDescription_WhenCreatingCatalog_ThenRejected_Test() {
        List<Ingredient> ingredients = List.of(new Ingredient(1, null));

        assertThrows(IllegalArgumentException.class, () -> new IngredientCatalog(ingredients));
    }

    @Test
    public void GivenDuplicateId_WhenCreatingCatalog_ThenRejected_Test() {
        List<Ingredient> ingredients = List.of(new Ingredient(1, "hazelnuts"), new Ingredient(1, "whipped cream"));

        assertThrows(IllegalArgumentException.class, () -> new IngredientCatalog(ingredients));
    }

    @Test
    public void GivenValidIngredients_WhenListingAndRequiring_ThenOrderedByIdAndFound_Test() {
        IngredientCatalog catalog = new IngredientCatalog(
                List.of(new Ingredient(2, "whipped cream"), new Ingredient(1, "hazelnuts")));

        assertEquals(List.of(new Ingredient(1, "hazelnuts"), new Ingredient(2, "whipped cream")), catalog.all());
        assertEquals(new Ingredient(2, "whipped cream"), catalog.require(2));
        assertThrows(IllegalArgumentException.class, () -> catalog.require(3));
    }

    @Test
    public void GivenDefaultCatalog_WhenLoading_ThenAllIngredientsPresent_Test() {
        assertEquals(4, IngredientCatalog.defaultCatalog().all().size());
    }
}
