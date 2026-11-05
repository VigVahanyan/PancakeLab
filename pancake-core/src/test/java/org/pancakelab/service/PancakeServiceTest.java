package org.pancakelab.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.pancakelab.api.model.DeliveredOrder;
import org.pancakelab.api.model.Ingredient;
import org.pancakelab.common.OrderNotFoundException;
import org.pancakelab.model.BuildingRegistry;
import org.pancakelab.model.pancakes.IngredientCatalog;
import org.pancakelab.repository.InMemoryOrderRepository;
import org.pancakelab.repository.OrderRepository;
import org.pancakelab.service.order.OrderService;
import org.pancakelab.service.order.OrderServiceImpl;
import org.pancakelab.service.pancake.PancakeService;
import org.pancakelab.service.pancake.PancakeServiceImpl;
import org.pancakelab.service.shared.OrderLog;

import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.pancakelab.service.TestIngredients.*;

public class PancakeServiceTest {
    private OrderService orderService;
    private PancakeService pancakeService;
    private UUID orderId;

    private final static String DARK_CHOCOLATE_PANCAKE_DESCRIPTION = "Delicious pancake with dark chocolate!";
    private final static String MILK_CHOCOLATE_PANCAKE_DESCRIPTION = "Delicious pancake with milk chocolate!";
    private final static String MILK_CHOCOLATE_HAZELNUTS_PANCAKE_DESCRIPTION = "Delicious pancake with milk chocolate, hazelnuts!";

    @BeforeEach
    public void setUp() {
        OrderRepository repository = new InMemoryOrderRepository();
        OrderLog log = new OrderLog();
        orderService = new OrderServiceImpl(repository, BuildingRegistry.defaultRegistry(), log);
        pancakeService = new PancakeServiceImpl(repository, IngredientCatalog.defaultCatalog(), log);
        orderId = orderService.createOrder(10, 20);
    }

    @Test
    public void GivenOrderDoesNotExist_WhenCreatingOrder_ThenOrderCreatedAndEmpty_Test() {
        // setup

        // exercise
        UUID newOrderId = orderService.createOrder(10, 20);

        // verify
        assertNotNull(newOrderId);
        assertNotEquals(orderId, newOrderId);
        assertEquals(List.of(), orderService.viewOrder(newOrderId));

        // tear down
    }

    @Test
    public void GivenCatalog_WhenListingIngredients_ThenEveryIngredientHasIdAndDescription_Test() {
        // exercise
        List<Ingredient> ingredients = pancakeService.listIngredients();

        // verify
        assertEquals(List.of(new Ingredient(DARK_CHOCOLATE, "dark chocolate"),
                new Ingredient(MILK_CHOCOLATE, "milk chocolate"),
                new Ingredient(WHIPPED_CREAM, "whipped cream"),
                new Ingredient(HAZELNUTS, "hazelnuts")), ingredients);
    }

    @Test
    public void GivenOrderExists_WhenAddingIngredientsOneByOne_ThenPancakeDescribedByItsIngredients_Test() {
        // setup
        UUID pancakeId = pancakeService.addPancake(orderId);

        // exercise
        pancakeService.addIngredientToPancake(orderId, pancakeId, MILK_CHOCOLATE);
        pancakeService.addIngredientToPancake(orderId, pancakeId, HAZELNUTS);

        // verify
        assertEquals(List.of(MILK_CHOCOLATE_HAZELNUTS_PANCAKE_DESCRIPTION), orderService.viewOrder(orderId));

        // tear down
    }

    @Test
    public void GivenOrderExists_WhenAddingPancakes_ThenCorrectNumberOfPancakesAdded_Test() {
        // setup

        // exercise
        addPancakes();

        // verify
        assertEquals(List.of(DARK_CHOCOLATE_PANCAKE_DESCRIPTION,
                DARK_CHOCOLATE_PANCAKE_DESCRIPTION,
                DARK_CHOCOLATE_PANCAKE_DESCRIPTION,
                MILK_CHOCOLATE_PANCAKE_DESCRIPTION,
                MILK_CHOCOLATE_PANCAKE_DESCRIPTION,
                MILK_CHOCOLATE_PANCAKE_DESCRIPTION,
                MILK_CHOCOLATE_HAZELNUTS_PANCAKE_DESCRIPTION,
                MILK_CHOCOLATE_HAZELNUTS_PANCAKE_DESCRIPTION,
                MILK_CHOCOLATE_HAZELNUTS_PANCAKE_DESCRIPTION), orderService.viewOrder(orderId));

        // tear down
    }

    @Test
    public void GivenPancakesExists_WhenRemovingPancakes_ThenCorrectNumberOfPancakesRemoved_Test() {
        // setup
        addPancakes();

        // exercise
        pancakeService.removePancakes(DARK_CHOCOLATE_PANCAKE_DESCRIPTION, orderId, 2);
        pancakeService.removePancakes(MILK_CHOCOLATE_PANCAKE_DESCRIPTION, orderId, 3);
        pancakeService.removePancakes(MILK_CHOCOLATE_HAZELNUTS_PANCAKE_DESCRIPTION, orderId, 1);

        // verify
        assertEquals(List.of(DARK_CHOCOLATE_PANCAKE_DESCRIPTION,
                MILK_CHOCOLATE_HAZELNUTS_PANCAKE_DESCRIPTION,
                MILK_CHOCOLATE_HAZELNUTS_PANCAKE_DESCRIPTION), orderService.viewOrder(orderId));

        // tear down
    }

    @Test
    public void GivenOrderExists_WhenCompletingOrder_ThenOrderCompleted_Test() {
        // setup
        addPancakes();

        // exercise
        orderService.completeOrder(orderId);

        // verify
        assertTrue(orderService.listCompletedOrders().contains(orderId));

        // tear down
    }

    @Test
    public void GivenOrderCompleted_WhenPreparingOrder_ThenOrderPrepared_Test() {
        // setup
        addPancakes();
        orderService.completeOrder(orderId);

        // exercise
        orderService.prepareOrder(orderId);

        // verify
        assertFalse(orderService.listCompletedOrders().contains(orderId));
        assertTrue(orderService.listPreparedOrders().contains(orderId));

        // tear down
    }

    @Test
    public void GivenOrderPrepared_WhenDeliveringOrder_ThenDeliveryDataReturnedAndOrderRemovedFromTheDatabase_Test() {
        // setup
        addPancakes();
        orderService.completeOrder(orderId);
        orderService.prepareOrder(orderId);
        List<String> pancakesToDeliver = orderService.viewOrder(orderId);

        // exercise
        DeliveredOrder deliveredOrder = orderService.deliverOrder(orderId);

        // verify
        assertEquals(orderId, deliveredOrder.orderId());
        assertEquals(10, deliveredOrder.building());
        assertEquals(20, deliveredOrder.room());
        assertEquals(pancakesToDeliver, deliveredOrder.pancakes());

        assertFalse(orderService.listCompletedOrders().contains(orderId));
        assertFalse(orderService.listPreparedOrders().contains(orderId));
        assertThrows(OrderNotFoundException.class, () -> orderService.viewOrder(orderId));

        // tear down
    }

    @Test
    public void GivenOrderExists_WhenCancellingOrder_ThenOrderAndPancakesRemoved_Test() {
        // setup
        addPancakes();

        // exercise
        orderService.cancelOrder(orderId);

        // verify
        assertFalse(orderService.listCompletedOrders().contains(orderId));
        assertFalse(orderService.listPreparedOrders().contains(orderId));
        assertThrows(OrderNotFoundException.class, () -> orderService.viewOrder(orderId));

        // tear down
    }

    @Test
    public void GivenCompletedOrders_WhenModifyingReturnedSet_ThenInternalStateIsNotAffected_Test() {
        // setup
        addPancakes();
        orderService.completeOrder(orderId);

        // exercise
        Set<UUID> completedOrders = orderService.listCompletedOrders();

        // verify
        assertThrows(UnsupportedOperationException.class, () -> completedOrders.add(UUID.randomUUID()));
        assertThrows(UnsupportedOperationException.class, () -> completedOrders.remove(orderId));
        assertTrue(orderService.listCompletedOrders().contains(orderId));

        // tear down
    }

    private void addPancakes() {
        addPancakes(3, DARK_CHOCOLATE);
        addPancakes(3, MILK_CHOCOLATE);
        addPancakes(3, MILK_CHOCOLATE, HAZELNUTS);
    }

    private void addPancakes(int count, int... ingredients) {
        for (int i = 0; i < count; ++i) {
            UUID pancakeId = pancakeService.addPancake(orderId);
            for (int ingredient : ingredients) {
                pancakeService.addIngredientToPancake(orderId, pancakeId, ingredient);
            }
        }
    }
}
