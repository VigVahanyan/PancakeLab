package org.pancakelab.api;

import org.pancakelab.api.model.DeliveredOrder;

import java.util.Set;
import java.util.UUID;

/**
 * What Delivery can do: find prepared orders and deliver them.
 */
public interface DeliveryApi {
    Set<UUID> listPreparedOrders();

    DeliveredOrder deliverOrder(UUID orderId);
}
