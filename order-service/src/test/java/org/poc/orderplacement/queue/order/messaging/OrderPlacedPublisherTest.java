package org.poc.orderplacement.queue.order.messaging;

import static org.mockito.Mockito.verify;

import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.poc.orderplacement.queue.contracts.OrderDestinations;
import org.poc.orderplacement.queue.contracts.OrderPlaced;
import org.poc.orderplacement.queue.order.event.OrderPlacedEvent;
import org.springframework.jms.core.JmsTemplate;

@ExtendWith(MockitoExtension.class)
class OrderPlacedPublisherTest {

    @Mock
    private JmsTemplate jmsTemplate;

    @Test
    void should_sendToOrdersTopic_when_orderPlacedEventReceived() {
        final OrderPlaced orderPlaced = new OrderPlaced(UUID.randomUUID(), "WIDGET", 1, Instant.now());
        final OrderPlacedPublisher publisher = new OrderPlacedPublisher(jmsTemplate);

        publisher.onOrderPlaced(new OrderPlacedEvent(orderPlaced));

        verify(jmsTemplate).convertAndSend(OrderDestinations.ORDERS_TOPIC, orderPlaced);
    }
}
