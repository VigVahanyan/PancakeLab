package org.pancakelab.service.role;

import org.pancakelab.api.ChefApi;
import org.pancakelab.service.order.OrderService;

import java.util.Set;
import java.util.UUID;

/**
 * Chef use-cases, delegating to the order service.
 */
public class ChefApiImpl implements ChefApi {
    private final OrderService orders;

    public ChefApiImpl(OrderService orders) {
        this.orders = orders;
    }

    @Override
    public Set<UUID> listCompletedOrders() {
        return orders.listCompletedOrders();
    }

    @Override
    public void prepareOrder(UUID orderId) {
        orders.prepareOrder(orderId);
    }
}
