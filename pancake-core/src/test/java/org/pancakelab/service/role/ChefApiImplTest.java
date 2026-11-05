package org.pancakelab.service.role;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.pancakelab.common.InvalidOrderStateException;
import org.pancakelab.common.OrderNotFoundException;

import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

public class ChefApiImplTest {
    private TestShop shop;

    @BeforeEach
    public void setUp() {
        shop = new TestShop();
    }

    @Test
    public void GivenNoOrders_WhenListingCompleted_ThenEmpty_Test() {
        assertEquals(Set.of(), shop.chef.listCompletedOrders());
    }

    @Test
    public void GivenCreatedAndCompletedOrders_WhenListingCompleted_ThenOnlyCompletedReturned_Test() {
        // setup
        UUID created = shop.orderWithPancake();
        UUID completed = shop.completedOrder();

        // exercise
        Set<UUID> orders = shop.chef.listCompletedOrders();

        // verify
        assertEquals(Set.of(completed), orders);
        assertFalse(orders.contains(created));

        // tear down
    }

    @Test
    public void GivenCompletedOrder_WhenPreparing_ThenItMovesFromCompletedToPrepared_Test() {
        // setup
        UUID orderId = shop.completedOrder();

        // exercise
        shop.chef.prepareOrder(orderId);

        // verify
        assertEquals(Set.of(), shop.chef.listCompletedOrders());
        assertEquals(Set.of(orderId), shop.delivery.listPreparedOrders());

        // tear down
    }

    @Test
    public void GivenOrderNotCompleted_WhenPreparing_ThenInvalidOrderState_Test() {
        UUID orderId = shop.orderWithPancake();

        assertThrows(InvalidOrderStateException.class, () -> shop.chef.prepareOrder(orderId));
    }

    @Test
    public void GivenOrderAlreadyPrepared_WhenPreparingAgain_ThenInvalidOrderState_Test() {
        UUID orderId = shop.preparedOrder();

        assertThrows(InvalidOrderStateException.class, () -> shop.chef.prepareOrder(orderId));
    }

    @Test
    public void GivenUnknownOrder_WhenPreparing_ThenOrderNotFound_Test() {
        assertThrows(OrderNotFoundException.class, () -> shop.chef.prepareOrder(UUID.randomUUID()));
    }
}
