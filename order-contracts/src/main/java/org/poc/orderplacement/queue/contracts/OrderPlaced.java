package org.poc.orderplacement.queue.contracts;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * Wire message published by order-service on {@link OrderDestinations#ORDERS_TOPIC} once an order
 * has been committed. Consumers (inventory-service, payment-service) depend only on this record,
 * never on order-service's JPA entity or Boot application.
 */
public record OrderPlaced(UUID orderId, String productCode, int quantity, Instant placedAt) {

    public OrderPlaced {
        Objects.requireNonNull(orderId, "orderId");
        Objects.requireNonNull(productCode, "productCode");
        Objects.requireNonNull(placedAt, "placedAt");
        if (quantity < 1) {
            throw new IllegalArgumentException("quantity must be at least 1, was " + quantity);
        }
    }
}
