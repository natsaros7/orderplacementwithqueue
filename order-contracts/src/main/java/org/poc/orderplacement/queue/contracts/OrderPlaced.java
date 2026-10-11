package org.poc.orderplacement.queue.contracts;

import java.time.Instant;
import java.util.UUID;

/**
 * Wire message published by order-service on {@link OrderDestinations#ORDERS_TOPIC} once an order
 * has been committed. Consumers (inventory-service, payment-service) depend only on this record,
 * never on order-service's JPA entity or Boot application.
 */
public record OrderPlaced(UUID orderId, String productCode, int quantity, Instant placedAt) {
}
