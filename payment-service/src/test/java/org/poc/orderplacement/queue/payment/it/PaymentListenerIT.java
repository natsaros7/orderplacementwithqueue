package org.poc.orderplacement.queue.payment.it;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

import jakarta.jms.Connection;
import jakarta.jms.ConnectionFactory;
import jakarta.jms.Message;
import jakarta.jms.MessageConsumer;
import jakarta.jms.Queue;
import jakarta.jms.Session;
import java.time.Duration;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.poc.orderplacement.queue.contracts.OrderDestinations;
import org.poc.orderplacement.queue.contracts.OrderPlaced;
import org.poc.orderplacement.queue.payment.repository.PaymentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jms.config.JmsListenerEndpointRegistry;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.listener.DefaultMessageListenerContainer;
import org.springframework.test.context.ActiveProfiles;

/** Proves the durable, idempotent consumer actually behaves that way against a real broker. */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@ActiveProfiles("test")
class PaymentListenerIT {

    private static final String LISTENER_ID = "paymentOrdersListener";

    private final JmsTemplate jmsTemplate;
    private final ConnectionFactory connectionFactory;
    private final PaymentRepository paymentRepository;
    private final JmsListenerEndpointRegistry listenerRegistry;

    @Autowired
    PaymentListenerIT(final JmsTemplate jmsTemplate, final ConnectionFactory connectionFactory,
            final PaymentRepository paymentRepository, final JmsListenerEndpointRegistry listenerRegistry) {
        this.jmsTemplate = jmsTemplate;
        this.connectionFactory = connectionFactory;
        this.paymentRepository = paymentRepository;
        this.listenerRegistry = listenerRegistry;
    }

    @Test
    void should_recordPayment_when_orderPlacedMessageArrives() {
        awaitListenerRegistered();
        final OrderPlaced orderPlaced = new OrderPlaced(UUID.randomUUID(), "WIDGET", 1, Instant.now());

        jmsTemplate.convertAndSend(OrderDestinations.ORDERS_TOPIC, orderPlaced);

        await().atMost(Duration.ofSeconds(10))
                .untilAsserted(() -> assertThat(paymentRepository.existsByOrderId(orderPlaced.orderId())).isTrue());
    }

    @Test
    void should_recordPaymentOnlyOnce_when_sameOrderIsRedelivered() {
        awaitListenerRegistered();
        final OrderPlaced orderPlaced = new OrderPlaced(UUID.randomUUID(), "GADGET", 1, Instant.now());

        jmsTemplate.convertAndSend(OrderDestinations.ORDERS_TOPIC, orderPlaced);
        await().atMost(Duration.ofSeconds(10)).until(() -> paymentRepository.existsByOrderId(orderPlaced.orderId()));

        // Simulates broker redelivery of a message the consumer already processed (e.g. after a
        // crash before the JMS ack but after the DB commit).
        jmsTemplate.convertAndSend(OrderDestinations.ORDERS_TOPIC, orderPlaced);
        await().atMost(Duration.ofSeconds(5))
                .until(() -> paymentRepository.findAll().stream()
                        .filter(p -> p.getOrderId().equals(orderPlaced.orderId())).count() >= 1);

        assertThat(paymentRepository.findAll().stream()
                .filter(p -> p.getOrderId().equals(orderPlaced.orderId())).count()).isEqualTo(1);
    }

    @Test
    void should_consumeAfterRestart_when_listenerWasStoppedWhileOrderWasPublished() {
        final DefaultMessageListenerContainer container =
                (DefaultMessageListenerContainer) listenerRegistry.getListenerContainer(LISTENER_ID);
        final OrderPlaced orderPlaced = new OrderPlaced(UUID.randomUUID(), "GIZMO", 1, Instant.now());
        try {
            awaitListenerRegistered();
            container.stop();

            jmsTemplate.convertAndSend(OrderDestinations.ORDERS_TOPIC, orderPlaced);
            assertThat(paymentRepository.existsByOrderId(orderPlaced.orderId())).isFalse();

            container.start();

            await().atMost(Duration.ofSeconds(10))
                    .untilAsserted(() -> assertThat(paymentRepository.existsByOrderId(orderPlaced.orderId())).isTrue());
        } finally {
            if (!container.isRunning()) {
                container.start();
            }
        }
    }

    @Test
    void should_sendToDeadLetterQueue_when_messageCannotBeDeserialized() throws Exception {
        awaitListenerRegistered();

        try (Connection connection = connectionFactory.createConnection()) {
            connection.start();
            try (Session session = connection.createSession(true, Session.SESSION_TRANSACTED)) {
                final var topic = session.createTopic(OrderDestinations.ORDERS_TOPIC);
                final var producer = session.createProducer(topic);
                final var poison = session.createTextMessage("not valid json");
                poison.setStringProperty("_type", OrderDestinations.ORDER_PLACED_TYPE_ID);
                producer.send(poison);
                session.commit();
            }

            try (Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE)) {
                final Queue dlq = session.createQueue("ActiveMQ.DLQ");
                try (MessageConsumer consumer = session.createConsumer(dlq)) {
                    final Message onDlq = consumer.receive(15_000);
                    assertThat(onDlq).as("poison message should be dead-lettered after exhausting redeliveries")
                            .isNotNull();
                }
            }
        }
    }

    /**
     * The durable consumer is created asynchronously when the Spring context refreshes; sending
     * before it registers with the broker would (rarely, under slow-CI load) drop the message.
     */
    private void awaitListenerRegistered() {
        final DefaultMessageListenerContainer container =
                (DefaultMessageListenerContainer) listenerRegistry.getListenerContainer(LISTENER_ID);
        await().atMost(Duration.ofSeconds(10)).until(container::isRegisteredWithDestination);
    }
}
