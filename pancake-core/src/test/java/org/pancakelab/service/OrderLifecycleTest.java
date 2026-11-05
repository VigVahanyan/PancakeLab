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

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;

import static org.junit.jupiter.api.Assertions.*;
import static org.pancakelab.service.TestIngredients.DARK_CHOCOLATE;
import static org.pancakelab.service.TestIngredients.HAZELNUTS;

public class OrderLifecycleTest {
    private OrderService orderService;
    private PancakeService pancakeService;
    private UUID orderId;

    @BeforeEach
    public void setUp() {
        OrderRepository repository = new InMemoryOrderRepository();
        OrderLog log = new OrderLog();
        orderService = new OrderServiceImpl(repository, BuildingRegistry.defaultRegistry(), log);
        pancakeService = new PancakeServiceImpl(repository, IngredientCatalog.defaultCatalog(), log);
        orderId = orderService.createOrder(10, 20);
    }

    @Test
    public void GivenEmptyOrder_WhenCompleting_ThenRejected_Test() {
        assertThrows(InvalidOrderStateException.class, () -> orderService.completeOrder(orderId));
        assertFalse(orderService.listCompletedOrders().contains(orderId));
    }

    @Test
    public void GivenPancakeWithoutIngredients_WhenCompleting_ThenRejected_Test() {
        pancakeService.addPancake(orderId);

        assertThrows(InvalidOrderStateException.class, () -> orderService.completeOrder(orderId));
    }

    @Test
    public void GivenCompletedOrder_WhenModifyingOrCompletingAgain_ThenRejected_Test() {
        UUID pancakeId = addPancake();
        orderService.completeOrder(orderId);

        assertThrows(InvalidOrderStateException.class, () -> pancakeService.addPancake(orderId));
        assertThrows(InvalidOrderStateException.class,
                () -> pancakeService.addIngredientToPancake(orderId, pancakeId, HAZELNUTS));
        assertThrows(InvalidOrderStateException.class,
                () -> pancakeService.removePancakes("Delicious pancake with dark chocolate!", orderId, 1));
        assertThrows(InvalidOrderStateException.class, () -> orderService.completeOrder(orderId));
        assertThrows(InvalidOrderStateException.class, () -> orderService.cancelOrder(orderId));
    }

    @Test
    public void GivenNotCompletedOrder_WhenPreparing_ThenRejected_Test() {
        addPancake();

        assertThrows(InvalidOrderStateException.class, () -> orderService.prepareOrder(orderId));
    }

    @Test
    public void GivenNotPreparedOrder_WhenDelivering_ThenRejectedAndOrderKept_Test() {
        addPancake();
        assertThrows(InvalidOrderStateException.class, () -> orderService.deliverOrder(orderId));

        orderService.completeOrder(orderId);
        assertThrows(InvalidOrderStateException.class, () -> orderService.deliverOrder(orderId));

        assertEquals(1, orderService.viewOrder(orderId).size());
        assertTrue(orderService.listCompletedOrders().contains(orderId));
    }

    @Test
    public void GivenDeliveredOrder_WhenDeliveringAgain_ThenOrderNotFound_Test() {
        addPancake();
        orderService.completeOrder(orderId);
        orderService.prepareOrder(orderId);
        orderService.deliverOrder(orderId);

        assertThrows(OrderNotFoundException.class, () -> orderService.deliverOrder(orderId));
    }

    @Test
    public void GivenCancelledOrder_WhenUsingIt_ThenOrderNotFound_Test() {
        addPancake();
        orderService.cancelOrder(orderId);

        assertThrows(OrderNotFoundException.class, () -> orderService.completeOrder(orderId));
        assertThrows(OrderNotFoundException.class, () -> orderService.cancelOrder(orderId));
    }

    @Test
    public void GivenOrder_WhenCancelling_ThenCancellationIsLoggedExactlyOnce_Test() {
        List<String> messages = new CopyOnWriteArrayList<>();
        OrderService service = new OrderServiceImpl(new InMemoryOrderRepository(),
                BuildingRegistry.defaultRegistry(),
                new OrderLog(messages::add));
        UUID id = service.createOrder(10, 20);

        service.cancelOrder(id);

        assertEquals(1, messages.stream().filter(m -> m.startsWith("CANCEL: order " + id + " is now CANCELLED")).count());
    }

    private UUID addPancake() {
        UUID pancakeId = pancakeService.addPancake(orderId);
        pancakeService.addIngredientToPancake(orderId, pancakeId, DARK_CHOCOLATE);
        return pancakeId;
    }
}
