package org.pancakelab.service.role;

import org.pancakelab.service.order.OrderService;
import org.pancakelab.api.DeliveryApi;
import org.pancakelab.api.model.DeliveredOrder;

import java.util.Set;
import java.util.UUID;

/** Delivery use-cases, delegating to the order service. */
public class DeliveryApiImpl implements DeliveryApi {
    private final OrderService orders;

    public DeliveryApiImpl(OrderService orders) {
        this.orders = orders;
    }

    @Override
    public Set<UUID> listPreparedOrders() {
        return orders.listPreparedOrders();
    }

    @Override
    public DeliveredOrder deliverOrder(UUID orderId) {
        return orders.deliverOrder(orderId);
    }
}
