package org.poc.orderplacement.queue.order.messaging;

import org.poc.orderplacement.queue.contracts.OrderDestinations;
import org.poc.orderplacement.queue.order.event.OrderPlacedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * Forwards an {@link OrderPlacedEvent} to the orders topic strictly after the placing transaction
 * commits. This is a dual write (DB commit, then a separate JMS send) — if the process dies between
 * the two, the order exists but is never published. A transactional outbox (persist the event in the
 * same transaction as the order, relay it with a poller) removes that gap; see README "Known gaps".
 */
@Component
public class OrderPlacedPublisher {

    private static final Logger LOG = LoggerFactory.getLogger(OrderPlacedPublisher.class);

    private final JmsTemplate jmsTemplate;

    public OrderPlacedPublisher(final JmsTemplate jmsTemplate) {
        this.jmsTemplate = jmsTemplate;
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onOrderPlaced(final OrderPlacedEvent event) {
        LOG.info("Publishing order {} to {}", event.orderPlaced().orderId(), OrderDestinations.ORDERS_TOPIC);
        jmsTemplate.convertAndSend(OrderDestinations.ORDERS_TOPIC, event.orderPlaced());
    }
}
