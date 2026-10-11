package org.poc.orderplacement.queue.contracts;

/**
 * JMS destination name and message-type identifiers shared by producer and consumers, so neither
 * side has to hard-code the other's values.
 */
public final class OrderDestinations {

    /** Topic (pub-sub) that order-service publishes {@link OrderPlaced} events to. */
    public static final String ORDERS_TOPIC = "orders.topic";

    /** Logical type id carried in the JMS message, decoupling producer/consumer from each other's FQCN. */
    public static final String ORDER_PLACED_TYPE_ID = "OrderPlaced";

    private OrderDestinations() {
    }
}
