package org.poc.orderplacement.queue.order.domain;

public enum OrderStatus {

    /** Saved and (as far as we know) published to {@code orders.topic}. */
    PLACED,

    /**
     * Saved, but the post-commit publish to {@code orders.topic} failed (e.g. broker unavailable).
     * The order exists and will never reach inventory/payment unless republished out of band; see
     * README "Known gaps" for the outbox fix.
     */
    PUBLISH_FAILED
}
