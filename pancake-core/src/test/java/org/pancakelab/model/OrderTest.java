package org.pancakelab.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.pancakelab.api.model.Ingredient;
import org.pancakelab.common.OrderStatus;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

public class OrderTest {
    private static final Ingredient DARK = new Ingredient(1, "dark chocolate");
    private static final Ingredient HAZELNUTS = new Ingredient(4, "hazelnuts");

    private Order order;

    @BeforeEach
    public void setUp() {
        order = new Order(10, 20);
    }

    private UUID pancakeWith(Ingredient... ingredients) {
        UUID pancakeId = order.addPancake();
        for (Ingredient ingredient : ingredients) {
            order.addIngredient(pancakeId, ingredient);
        }
        return pancakeId;
    }

    @Test
    public void GivenNewOrder_WhenInspecting_ThenCreatedAndEmpty_Test() {
        assertEquals(OrderStatus.CREATED, order.getStatus());
        assertEquals(10, order.getBuilding());
        assertEquals(20, order.getRoom());
        assertTrue(order.getPancakes().isEmpty());
    }

    @Test
    public void GivenOrder_WhenAddingPancakeAndIngredients_ThenPancakeIsDescribed_Test() {
        UUID pancakeId = pancakeWith(DARK, HAZELNUTS);

        assertEquals(1, order.getPancakes().size());
        assertEquals(pancakeId, order.getPancakes().get(0).getId());
        assertEquals(List.of("Delicious pancake with dark chocolate, hazelnuts!"), order.descriptions());
    }

    @Test
    public void GivenOrder_WhenAddingIngredientToUnknownPancake_ThenRejected_Test() {
        pancakeWith(DARK);

        assertThrows(IllegalArgumentException.class, () -> order.addIngredient(UUID.randomUUID(), HAZELNUTS));
    }

    @Test
    public void GivenEmptyOrder_WhenCompleting_ThenRejectedAndStatusUnchanged_Test() {
        assertThrows(IllegalStateException.class, () -> order.complete());

        assertEquals(OrderStatus.CREATED, order.getStatus());
    }

    @Test
    public void GivenPancakeWithoutIngredients_WhenCompleting_ThenRejected_Test() {
        order.addPancake();

        assertThrows(IllegalStateException.class, () -> order.complete());
        assertEquals(OrderStatus.CREATED, order.getStatus());
    }

    @Test
    public void GivenValidOrder_WhenMovingThroughLifecycle_ThenStatusFollowsWorkflow_Test() {
        pancakeWith(DARK);

        order.complete();
        assertEquals(OrderStatus.COMPLETED, order.getStatus());
        order.prepare();
        assertEquals(OrderStatus.PREPARED, order.getStatus());
        order.deliver();
        assertEquals(OrderStatus.DELIVERED, order.getStatus());
    }

    @Test
    public void GivenCreatedOrder_WhenCancelling_ThenCancelled_Test() {
        order.cancel();

        assertEquals(OrderStatus.CANCELLED, order.getStatus());
    }

    @Test
    public void GivenCompletedOrder_WhenModifyingOrCancelling_ThenRejected_Test() {
        UUID pancakeId = pancakeWith(DARK);
        order.complete();

        assertThrows(IllegalStateException.class, () -> order.addPancake());
        assertThrows(IllegalStateException.class, () -> order.addIngredient(pancakeId, HAZELNUTS));
        assertThrows(IllegalStateException.class,
                () -> order.removePancakes("Delicious pancake with dark chocolate!", 1));
        assertThrows(IllegalStateException.class, () -> order.cancel());
        assertThrows(IllegalStateException.class, () -> order.complete());
        assertThrows(IllegalStateException.class, () -> order.deliver());
        assertEquals(OrderStatus.COMPLETED, order.getStatus());
    }

    @Test
    public void GivenCreatedOrder_WhenPreparingOrDelivering_ThenRejected_Test() {
        pancakeWith(DARK);

        assertThrows(IllegalStateException.class, () -> order.prepare());
        assertThrows(IllegalStateException.class, () -> order.deliver());
    }

    @Test
    public void GivenMatchingPancakes_WhenRemoving_ThenOnlyRequestedCountRemoved_Test() {
        pancakeWith(DARK);
        pancakeWith(DARK);
        pancakeWith(HAZELNUTS);

        order.removePancakes("Delicious pancake with dark chocolate!", 1);

        assertEquals(List.of("Delicious pancake with dark chocolate!", "Delicious pancake with hazelnuts!"),
                order.descriptions());
    }

    @Test
    public void GivenFewerMatchingPancakes_WhenRemovingMore_ThenRejectedAndNothingRemoved_Test() {
        pancakeWith(DARK);

        assertThrows(IllegalArgumentException.class,
                () -> order.removePancakes("Delicious pancake with dark chocolate!", 2));

        assertEquals(1, order.getPancakes().size());
    }

    @Test
    public void GivenOrder_WhenChangingExposedCollections_ThenNotAllowed_Test() {
        pancakeWith(DARK);

        assertThrows(UnsupportedOperationException.class, () -> order.getPancakes().clear());
        assertThrows(UnsupportedOperationException.class,
                () -> order.getPancakes().get(0).getIngredients().add(HAZELNUTS));
    }
}
