package org.pancakelab.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.pancakelab.common.InvalidOrderStateException;
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

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.pancakelab.service.TestIngredients.DARK_CHOCOLATE;

public class OrderLogTest {
    private List<String> messages;
    private OrderService orderService;
    private PancakeService pancakeService;

    @BeforeEach
    public void setUp() {
        messages = new ArrayList<>();
        OrderRepository repository = new InMemoryOrderRepository();
        OrderLog log = new OrderLog(messages::add);
        orderService = new OrderServiceImpl(repository, BuildingRegistry.defaultRegistry(), log);
        pancakeService = new PancakeServiceImpl(repository, IngredientCatalog.defaultCatalog(), log);
    }

    @Test
    public void GivenValidBuildingAndRoom_WhenCreatingOrder_ThenCreationIsLogged_Test() {
        // exercise
        UUID orderId = orderService.createOrder(10, 20);

        // verify
        assertEquals(1, messages.size());
        assertTrue(messages.get(0).startsWith("CREATE: order " + orderId + " is now CREATED"), messages.get(0));
        assertTrue(messages.get(0).contains("building 10, room 20"), messages.get(0));
    }

    @Test
    public void GivenOrderWorkflow_WhenRunningEachOperation_ThenOperationAndStatusAreLogged_Test() {
        // exercise
        UUID orderId = orderService.createOrder(10, 20);
        UUID pancakeId = pancakeService.addPancake(orderId);
        pancakeService.addIngredientToPancake(orderId, pancakeId, DARK_CHOCOLATE);
        orderService.completeOrder(orderId);
        orderService.prepareOrder(orderId);
        orderService.deliverOrder(orderId);

        // verify
        List<String> expected = List.of("CREATE", "ADD_PANCAKE", "ADD_INGREDIENT", "COMPLETE", "PREPARE", "DELIVER");
        List<String> statuses = List.of("CREATED", "CREATED", "CREATED", "COMPLETED", "PREPARED", "DELIVERED");
        assertEquals(expected.size(), messages.size());
        for (int i = 0; i < expected.size(); i++) {
            assertTrue(messages.get(i).startsWith(expected.get(i) + ": order " + orderId + " is now " + statuses.get(i)),
                    messages.get(i));
        }
    }
}
