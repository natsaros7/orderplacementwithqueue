package org.poc.orderplacement.queue.inventory.it;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

import java.time.Duration;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.poc.orderplacement.queue.contracts.OrderDestinations;
import org.poc.orderplacement.queue.contracts.OrderPlaced;
import org.poc.orderplacement.queue.inventory.repository.InventoryReservationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jms.core.JmsTemplate;

/** Proves the durable-subscription consumer reacts to a message sent through the (embedded) broker. */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
class InventoryListenerIT {

    @Autowired
    private JmsTemplate jmsTemplate;

    @Autowired
    private InventoryReservationRepository reservationRepository;

    @Test
    void should_reserveStock_when_orderPlacedMessageArrives() {
        final OrderPlaced orderPlaced = new OrderPlaced(UUID.randomUUID(), "WIDGET", 2, Instant.now());

        jmsTemplate.convertAndSend(OrderDestinations.ORDERS_TOPIC, orderPlaced);

        await().atMost(Duration.ofSeconds(10))
                .untilAsserted(() -> assertThat(reservationRepository.existsByOrderId(orderPlaced.orderId())).isTrue());
    }
}
