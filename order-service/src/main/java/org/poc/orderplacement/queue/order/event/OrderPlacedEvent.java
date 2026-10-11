package org.poc.orderplacement.queue.order.event;

import org.poc.orderplacement.queue.contracts.OrderPlaced;

/**
 * In-process application event carrying the {@link OrderPlaced} payload. Published from within the
 * placing transaction; {@link org.poc.orderplacement.queue.order.messaging.OrderPlacedPublisher}
 * only forwards it to JMS after that transaction commits.
 */
public record OrderPlacedEvent(OrderPlaced orderPlaced) {
}
