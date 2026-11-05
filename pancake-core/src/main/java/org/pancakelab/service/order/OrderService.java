package org.pancakelab.service.order;

import org.pancakelab.api.model.DeliveredOrder;

import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * Order lifecycle: create, view, complete, prepare, cancel, deliver and list. Only ids,
 * strings and {@link DeliveredOrder} cross its boundary, never domain objects.
 */
public interface OrderService {
    UUID createOrder(int building, int room);

    List<String> viewOrder(UUID orderId);

    void completeOrder(UUID orderId);

    void prepareOrder(UUID orderId);

    void cancelOrder(UUID orderId);

    DeliveredOrder deliverOrder(UUID orderId);

    Set<UUID> listCompletedOrders();

    Set<UUID> listPreparedOrders();
}
