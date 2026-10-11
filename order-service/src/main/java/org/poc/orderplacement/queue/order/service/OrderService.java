package org.poc.orderplacement.queue.order.service;

import java.util.UUID;
import org.poc.orderplacement.queue.contracts.OrderPlaced;
import org.poc.orderplacement.queue.order.domain.Order;
import org.poc.orderplacement.queue.order.domain.OrderRepository;
import org.poc.orderplacement.queue.order.event.OrderPlacedEvent;
import org.poc.orderplacement.queue.order.web.OrderForm;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class OrderService {

    private static final Logger LOG = LoggerFactory.getLogger(OrderService.class);

    private final OrderRepository orderRepository;
    private final ApplicationEventPublisher eventPublisher;

    public OrderService(final OrderRepository orderRepository, final ApplicationEventPublisher eventPublisher) {
        this.orderRepository = orderRepository;
        this.eventPublisher = eventPublisher;
    }

    /**
     * Persists the order and publishes an in-process event within the same transaction. The event
     * is only forwarded to JMS after this transaction commits (see
     * {@link org.poc.orderplacement.queue.order.messaging.OrderPlacedPublisher}) — this closes the
     * window where a message could be sent for an order that then rolls back, but it does not close
     * the inverse gap (commit succeeds, process crashes before the publish); see README "Known gaps".
     */
    @Transactional
    public Order placeOrder(final OrderForm form) {
        final Order order = orderRepository.save(new Order(form.productCode(), form.quantity()));
        LOG.info("Order {} placed for product {} x{}", order.getId(), order.getProductCode(), order.getQuantity());

        eventPublisher.publishEvent(new OrderPlacedEvent(
                new OrderPlaced(order.getId(), order.getProductCode(), order.getQuantity(), order.getPlacedAt())));

        return order;
    }

    /**
     * Re-reads the order. Called after {@link #placeOrder} returns, by which point the AFTER_COMMIT
     * publish (success or failure, see {@link org.poc.orderplacement.queue.order.messaging.OrderPlacedPublisher})
     * has already run synchronously on the same thread, so this reflects the real outcome even though
     * the {@link Order} instance {@code placeOrder} returned does not (it predates that publish).
     */
    @Transactional(readOnly = true)
    public Order getOrder(final UUID orderId) {
        return orderRepository.findById(orderId).orElseThrow();
    }
}
