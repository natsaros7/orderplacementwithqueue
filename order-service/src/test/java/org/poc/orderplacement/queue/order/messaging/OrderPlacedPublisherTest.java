package org.poc.orderplacement.queue.order.messaging;

import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.poc.orderplacement.queue.contracts.OrderDestinations;
import org.poc.orderplacement.queue.contracts.OrderPlaced;
import org.poc.orderplacement.queue.order.event.OrderPlacedEvent;
import org.poc.orderplacement.queue.order.service.OrderPublishFailureRecorder;
import org.springframework.jms.JmsException;
import org.springframework.jms.core.JmsTemplate;

@ExtendWith(MockitoExtension.class)
class OrderPlacedPublisherTest {

    @Mock
    private JmsTemplate jmsTemplate;

    @Mock
    private OrderPublishFailureRecorder failureRecorder;

    @Test
    void should_sendToOrdersTopic_when_orderPlacedEventReceived() {
        final OrderPlaced orderPlaced = new OrderPlaced(UUID.randomUUID(), "WIDGET", 1, Instant.now());
        final OrderPlacedPublisher publisher = new OrderPlacedPublisher(jmsTemplate, failureRecorder);

        publisher.onOrderPlaced(new OrderPlacedEvent(orderPlaced));

        verify(jmsTemplate).convertAndSend(OrderDestinations.ORDERS_TOPIC, orderPlaced);
        verifyNoInteractions(failureRecorder);
    }

    @Test
    void should_logAndNotPropagate_when_jmsSendFails() {
        final OrderPlaced orderPlaced = new OrderPlaced(UUID.randomUUID(), "WIDGET", 1, Instant.now());
        final OrderPlacedPublisher publisher = new OrderPlacedPublisher(jmsTemplate, failureRecorder);
        doThrow(new JmsException("broker unavailable") {}).when(jmsTemplate)
                .convertAndSend(OrderDestinations.ORDERS_TOPIC, orderPlaced);

        publisher.onOrderPlaced(new OrderPlacedEvent(orderPlaced));

        verify(failureRecorder).markPublishFailed(orderPlaced.orderId());
    }
}
