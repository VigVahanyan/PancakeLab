package org.pancakelab.common;

import java.util.UUID;

public class OrderNotFoundException extends IllegalArgumentException {
    public OrderNotFoundException(UUID orderId) {
        super("Order not found: " + orderId);
    }
}
