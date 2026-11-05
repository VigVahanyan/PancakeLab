package org.pancakelab.api;

import java.util.Set;
import java.util.UUID;

/**
 * What the Chef can do: find completed orders and prepare them.
 */
public interface ChefApi {
    Set<UUID> listCompletedOrders();

    void prepareOrder(UUID orderId);
}
