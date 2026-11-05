package org.pancakelab.api.model;

import java.util.List;
import java.util.UUID;

/**
 * What the delivery service receives: plain data, no domain objects.
 */
public record DeliveredOrder(UUID orderId, int building, int room, List<String> pancakes) {
}
