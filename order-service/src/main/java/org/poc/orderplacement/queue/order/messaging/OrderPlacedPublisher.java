package org.poc.orderplacement.queue.order.messaging;

import org.poc.orderplacement.queue.contracts.OrderDestinations;
import org.poc.orderplacement.queue.order.event.OrderPlacedEvent;
import org.poc.orderplacement.queue.order.service.OrderPublishFailureRecorder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jms.JmsException;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * Forwards an {@link OrderPlacedEvent} to the orders topic strictly after the placing transaction
 * commits. This is a dual write (DB commit, then a separate JMS send): two failure modes follow.
 * <ul>
 *     <li>The process crashes between commit and send: the order exists, nothing is ever published,
 *     and nothing here can detect or record that (no code runs). Only a transactional outbox (persist
 *     the event in the same transaction as the order, relay it with a poller) closes this gap.</li>
 *     <li>The send itself fails (e.g. broker unavailable) while this JVM is still running: caught
 *     below, logged, and recorded on the order via {@link OrderPublishFailureRecorder} so the UI
 *     reflects it instead of silently claiming success.</li>
 * </ul>
 * See README "Known gaps" for both.
 */
@Component
public class OrderPlacedPublisher {

    private static final Logger LOG = LoggerFactory.getLogger(OrderPlacedPublisher.class);

    private final JmsTemplate jmsTemplate;
    private final OrderPublishFailureRecorder failureRecorder;

    public OrderPlacedPublisher(final JmsTemplate jmsTemplate, final OrderPublishFailureRecorder failureRecorder) {
        this.jmsTemplate = jmsTemplate;
        this.failureRecorder = failureRecorder;
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onOrderPlaced(final OrderPlacedEvent event) {
        final var orderId = event.orderPlaced().orderId();
        try {
            LOG.info("Publishing order {} to {}", orderId, OrderDestinations.ORDERS_TOPIC);
            jmsTemplate.convertAndSend(OrderDestinations.ORDERS_TOPIC, event.orderPlaced());
        } catch (final JmsException ex) {
            LOG.error("Order {} committed but publish to {} failed", orderId, OrderDestinations.ORDERS_TOPIC, ex);
            failureRecorder.markPublishFailed(orderId);
        }
    }
}
