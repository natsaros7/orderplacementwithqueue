package org.poc.orderplacement.queue.order.service;

import java.util.UUID;
import org.poc.orderplacement.queue.order.domain.OrderRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * Records a publish failure in its own transaction. Called from
 * {@link org.poc.orderplacement.queue.order.messaging.OrderPlacedPublisher}, which runs
 * {@code AFTER_COMMIT} on the original placing transaction, i.e. after it's too late to roll that
 * transaction back or update the order within it.
 */
@Service
public class OrderPublishFailureRecorder {

    private final OrderRepository orderRepository;

    public OrderPublishFailureRecorder(final OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void markPublishFailed(final UUID orderId) {
        orderRepository.findById(orderId).ifPresent(order -> {
            order.markPublishFailed();
            orderRepository.save(order);
        });
    }
}
