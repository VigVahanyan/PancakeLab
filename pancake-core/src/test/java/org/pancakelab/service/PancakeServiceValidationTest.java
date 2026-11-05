package org.pancakelab.service;

import org.pancakelab.service.order.OrderService;
import org.pancakelab.service.order.OrderServiceImpl;
import org.pancakelab.service.pancake.PancakeService;
import org.pancakelab.service.pancake.PancakeServiceImpl;
import org.pancakelab.service.shared.OrderLog;
import org.pancakelab.common.OrderNotFoundException;
import org.pancakelab.repository.InMemoryOrderRepository;
import org.pancakelab.repository.OrderRepository;
import org.pancakelab.model.BuildingRegistry;
import org.pancakelab.model.pancakes.IngredientCatalog;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.pancakelab.service.TestIngredients.*;

public class PancakeServiceValidationTest {
    private OrderService orderService;
    private PancakeService pancakeService;
    private UUID orderId;
    private UUID pancakeId;

    @BeforeEach
    public void setUp() {
        OrderRepository repository = new InMemoryOrderRepository();
        OrderLog log = new OrderLog();
        orderService = new OrderServiceImpl(repository, BuildingRegistry.defaultRegistry(), log);
        pancakeService = new PancakeServiceImpl(repository, IngredientCatalog.defaultCatalog(), log);
        orderId = orderService.createOrder(10, 20);
        pancakeId = pancakeService.addPancake(orderId);
    }

    @ParameterizedTest
    @CsvSource({"0, 1", "-1, 1", "10, 0", "10, -5", "999, 1", "10, 9999"})
    public void GivenInvalidBuildingOrRoom_WhenCreatingOrder_ThenRejected_Test(int building, int room) {
        assertThrows(IllegalArgumentException.class, () -> orderService.createOrder(building, room));
    }

    @ParameterizedTest
    @ValueSource(ints = {0, -1, 5, 999})
    public void GivenUnknownIngredientId_WhenAddingIngredient_ThenRejectedAndPancakeUnchanged_Test(int ingredientId) {
        assertThrows(IllegalArgumentException.class,
                () -> pancakeService.addIngredientToPancake(orderId, pancakeId, ingredientId));

        pancakeService.addIngredientToPancake(orderId, pancakeId, DARK_CHOCOLATE);
        assertEquals(List.of("Delicious pancake with dark chocolate!"), orderService.viewOrder(orderId));
    }

    @Test
    public void GivenUnknownOrder_WhenUsingAnyOperation_ThenOrderNotFound_Test() {
        UUID unknown = UUID.randomUUID();

        assertThrows(OrderNotFoundException.class, () -> pancakeService.addPancake(unknown));
        assertThrows(OrderNotFoundException.class, () -> pancakeService.addIngredientToPancake(unknown, pancakeId, DARK_CHOCOLATE));
        assertThrows(OrderNotFoundException.class, () -> orderService.viewOrder(unknown));
        assertThrows(OrderNotFoundException.class, () -> pancakeService.removePancakes("any", unknown, 1));
        assertThrows(OrderNotFoundException.class, () -> orderService.completeOrder(unknown));
        assertThrows(OrderNotFoundException.class, () -> orderService.prepareOrder(unknown));
        assertThrows(OrderNotFoundException.class, () -> orderService.deliverOrder(unknown));
        assertThrows(OrderNotFoundException.class, () -> orderService.cancelOrder(unknown));
    }

    @Test
    public void GivenNullOrderId_WhenUsingAnyOperation_ThenRejected_Test() {
        assertThrows(IllegalArgumentException.class, () -> pancakeService.addPancake(null));
        assertThrows(IllegalArgumentException.class, () -> pancakeService.addIngredientToPancake(null, pancakeId, DARK_CHOCOLATE));
        assertThrows(IllegalArgumentException.class, () -> pancakeService.addIngredientToPancake(orderId, null, DARK_CHOCOLATE));
        assertThrows(IllegalArgumentException.class, () -> orderService.viewOrder(null));
        assertThrows(IllegalArgumentException.class, () -> pancakeService.removePancakes("any", null, 1));
        assertThrows(IllegalArgumentException.class, () -> orderService.completeOrder(null));
        assertThrows(IllegalArgumentException.class, () -> orderService.prepareOrder(null));
        assertThrows(IllegalArgumentException.class, () -> orderService.deliverOrder(null));
        assertThrows(IllegalArgumentException.class, () -> orderService.cancelOrder(null));
    }

    @Test
    public void GivenUnknownPancake_WhenAddingIngredient_ThenRejected_Test() {
        assertThrows(IllegalArgumentException.class,
                () -> pancakeService.addIngredientToPancake(orderId, UUID.randomUUID(), DARK_CHOCOLATE));
    }

    @ParameterizedTest
    @ValueSource(ints = {0, -1})
    public void GivenNonPositiveCount_WhenRemovingPancakes_ThenRejected_Test(int count) {
        pancakeService.addIngredientToPancake(orderId, pancakeId, DARK_CHOCOLATE);

        assertThrows(IllegalArgumentException.class,
                () -> pancakeService.removePancakes("Delicious pancake with dark chocolate!", orderId, count));
    }

    @Test
    public void GivenFewerPancakesThanRequested_WhenRemovingPancakes_ThenRejectedAndNothingRemoved_Test() {
        pancakeService.addIngredientToPancake(orderId, pancakeId, DARK_CHOCOLATE);

        assertThrows(IllegalArgumentException.class,
                () -> pancakeService.removePancakes("Delicious pancake with dark chocolate!", orderId, 2));
        assertEquals(1, orderService.viewOrder(orderId).size());
    }

    @ParameterizedTest
    @NullAndEmptySource
    public void GivenBlankDescription_WhenRemovingPancakes_ThenRejected_Test(String description) {
        assertThrows(IllegalArgumentException.class,
                () -> pancakeService.removePancakes(description, orderId, 1));
    }
}
