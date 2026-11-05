package org.pancakelab.service.role;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.pancakelab.api.model.DeliveredOrder;
import org.pancakelab.common.InvalidOrderStateException;
import org.pancakelab.common.OrderNotFoundException;

import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class DeliveryApiImplTest {
    private TestShop shop;

    @BeforeEach
    public void setUp() {
        shop = new TestShop();
    }

    @Test
    public void GivenOrdersInDifferentStates_WhenListingPrepared_ThenOnlyPreparedReturned_Test() {
        // setup
        shop.orderWithPancake();
        shop.completedOrder();
        UUID prepared = shop.preparedOrder();

        // exercise
        Set<UUID> orders = shop.delivery.listPreparedOrders();

        // verify
        assertEquals(Set.of(prepared), orders);

        // tear down
    }

    @Test
    public void GivenPreparedOrder_WhenDelivering_ThenDeliveryDataReturnedAndOrderRemoved_Test() {
        // setup
        UUID orderId = shop.preparedOrder();

        // exercise
        DeliveredOrder delivered = shop.delivery.deliverOrder(orderId);

        // verify
        assertEquals(new DeliveredOrder(orderId, 10, 20, List.of("Delicious pancake with dark chocolate!")),
                delivered);
        assertEquals(Set.of(), shop.delivery.listPreparedOrders());
        assertThrows(OrderNotFoundException.class, () -> shop.delivery.deliverOrder(orderId));
        assertThrows(OrderNotFoundException.class, () -> shop.disciple.viewOrder(orderId));

        // tear down
    }

    @Test
    public void GivenOrderNotPrepared_WhenDelivering_ThenInvalidOrderStateAndOrderKept_Test() {
        // setup
        UUID orderId = shop.completedOrder();

        // exercise and verify
        assertThrows(InvalidOrderStateException.class, () -> shop.delivery.deliverOrder(orderId));
        assertEquals(Set.of(orderId), shop.chef.listCompletedOrders());

        // tear down
    }

    @Test
    public void GivenUnknownOrder_WhenDelivering_ThenOrderNotFound_Test() {
        assertThrows(OrderNotFoundException.class, () -> shop.delivery.deliverOrder(UUID.randomUUID()));
    }
}
