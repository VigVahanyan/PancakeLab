package org.pancakelab.service.role;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.pancakelab.api.model.Ingredient;
import org.pancakelab.common.InvalidOrderStateException;
import org.pancakelab.common.OrderNotFoundException;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.pancakelab.service.TestIngredients.*;

public class DiscipleApiImplTest {
    private static final String DARK_CHOCOLATE_PANCAKE = "Delicious pancake with dark chocolate!";

    private TestShop shop;

    @BeforeEach
    public void setUp() {
        shop = new TestShop();
    }

    @Test
    public void GivenCatalog_WhenListingIngredients_ThenTheMenuIsReturned_Test() {
        // exercise
        List<Ingredient> menu = shop.disciple.listIngredients();

        // verify
        assertEquals(List.of(new Ingredient(DARK_CHOCOLATE, "dark chocolate"),
                new Ingredient(MILK_CHOCOLATE, "milk chocolate"),
                new Ingredient(WHIPPED_CREAM, "whipped cream"),
                new Ingredient(HAZELNUTS, "hazelnuts")), menu);
    }

    @Test
    public void GivenNewOrder_WhenAddingPancakeIngredientByIngredient_ThenOrderShowsThePancake_Test() {
        // setup
        UUID orderId = shop.disciple.createOrder(10, 20);
        UUID pancakeId = shop.disciple.addPancake(orderId);

        // exercise
        shop.disciple.addIngredientToPancake(orderId, pancakeId, MILK_CHOCOLATE);
        shop.disciple.addIngredientToPancake(orderId, pancakeId, HAZELNUTS);

        // verify
        assertEquals(List.of("Delicious pancake with milk chocolate, hazelnuts!"),
                shop.disciple.viewOrder(orderId));

        // tear down
    }

    @Test
    public void GivenOrderWithPancakes_WhenRemovingByDescription_ThenOnlyRequestedCountRemoved_Test() {
        // setup
        UUID orderId = shop.orderWithPancake();
        UUID second = shop.disciple.addPancake(orderId);
        shop.disciple.addIngredientToPancake(orderId, second, DARK_CHOCOLATE);

        // exercise
        shop.disciple.removePancakes(DARK_CHOCOLATE_PANCAKE, orderId, 1);

        // verify
        assertEquals(List.of(DARK_CHOCOLATE_PANCAKE), shop.disciple.viewOrder(orderId));

        // tear down
    }

    @Test
    public void GivenOrderWithPancake_WhenCompleting_ThenChefCanSeeIt_Test() {
        // setup
        UUID orderId = shop.orderWithPancake();

        // exercise
        shop.disciple.completeOrder(orderId);

        // verify
        assertTrue(shop.chef.listCompletedOrders().contains(orderId));

        // tear down
    }

    @Test
    public void GivenOrder_WhenCancelling_ThenOrderIsRemoved_Test() {
        // setup
        UUID orderId = shop.orderWithPancake();

        // exercise
        shop.disciple.cancelOrder(orderId);

        // verify
        assertThrows(OrderNotFoundException.class, () -> shop.disciple.viewOrder(orderId));

        // tear down
    }

    @Test
    public void GivenUnknownBuildingOrRoom_WhenCreatingOrder_ThenRejected_Test() {
        assertThrows(IllegalArgumentException.class, () -> shop.disciple.createOrder(999, 20));
        assertThrows(IllegalArgumentException.class, () -> shop.disciple.createOrder(10, 999));
    }

    @Test
    public void GivenUnknownIngredient_WhenAddingToPancake_ThenRejected_Test() {
        // setup
        UUID orderId = shop.disciple.createOrder(10, 20);
        UUID pancakeId = shop.disciple.addPancake(orderId);

        // exercise and verify
        assertThrows(IllegalArgumentException.class,
                () -> shop.disciple.addIngredientToPancake(orderId, pancakeId, 999));
        shop.disciple.addIngredientToPancake(orderId, pancakeId, DARK_CHOCOLATE);
        assertEquals(List.of(DARK_CHOCOLATE_PANCAKE), shop.disciple.viewOrder(orderId));

        // tear down
    }

    @Test
    public void GivenEmptyOrder_WhenCompleting_ThenInvalidOrderState_Test() {
        UUID orderId = shop.disciple.createOrder(10, 20);

        assertThrows(InvalidOrderStateException.class, () -> shop.disciple.completeOrder(orderId));
    }

    @Test
    public void GivenCompletedOrder_WhenEditingOrCancelling_ThenInvalidOrderState_Test() {
        // setup
        UUID orderId = shop.completedOrder();

        // exercise and verify
        assertThrows(InvalidOrderStateException.class, () -> shop.disciple.addPancake(orderId));
        assertThrows(InvalidOrderStateException.class, () -> shop.disciple.cancelOrder(orderId));
        assertEquals(List.of(DARK_CHOCOLATE_PANCAKE), shop.disciple.viewOrder(orderId));

        // tear down
    }

    @Test
    public void GivenUnknownOrder_WhenUsingAnyOperation_ThenOrderNotFound_Test() {
        UUID unknown = UUID.randomUUID();

        assertThrows(OrderNotFoundException.class, () -> shop.disciple.viewOrder(unknown));
        assertThrows(OrderNotFoundException.class, () -> shop.disciple.addPancake(unknown));
        assertThrows(OrderNotFoundException.class, () -> shop.disciple.completeOrder(unknown));
        assertThrows(OrderNotFoundException.class, () -> shop.disciple.cancelOrder(unknown));
    }
}
