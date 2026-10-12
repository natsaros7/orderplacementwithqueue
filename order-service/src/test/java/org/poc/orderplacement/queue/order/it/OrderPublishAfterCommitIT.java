package org.poc.orderplacement.queue.order.it;

import static org.assertj.core.api.Assertions.assertThat;

import jakarta.jms.Connection;
import jakarta.jms.ConnectionFactory;
import jakarta.jms.Message;
import jakarta.jms.MessageConsumer;
import jakarta.jms.Session;
import jakarta.jms.Topic;
import org.junit.jupiter.api.Test;
import org.poc.orderplacement.queue.contracts.OrderDestinations;
import org.poc.orderplacement.queue.contracts.OrderPlaced;
import org.poc.orderplacement.queue.order.TestcontainersConfiguration;
import org.poc.orderplacement.queue.order.domain.OrderRepository;
import org.poc.orderplacement.queue.order.service.OrderService;
import org.poc.orderplacement.queue.order.web.OrderForm;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.jms.support.converter.MessageConverter;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

/**
 * Proves the dual-write boundary described in the README: a message only reaches the topic once the
 * placing transaction has committed, never when it rolls back. Runs against real Postgres and
 * ActiveMQ Testcontainers (see {@link TestcontainersConfiguration}), not the embedded vm:// broker.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@Import(TestcontainersConfiguration.class)
class OrderPublishAfterCommitIT {

    private final OrderService orderService;
    private final OrderRepository orderRepository;
    private final PlatformTransactionManager transactionManager;
    private final ConnectionFactory connectionFactory;
    private final MessageConverter messageConverter;

    @Autowired
    OrderPublishAfterCommitIT(final OrderService orderService, final OrderRepository orderRepository,
            final PlatformTransactionManager transactionManager, final ConnectionFactory connectionFactory,
            final MessageConverter messageConverter) {
        this.orderService = orderService;
        this.orderRepository = orderRepository;
        this.transactionManager = transactionManager;
        this.connectionFactory = connectionFactory;
        this.messageConverter = messageConverter;
    }

    @Test
    void should_publishOrderPlaced_when_transactionCommits() throws Exception {
        final TransactionTemplate tx = new TransactionTemplate(transactionManager);
        final OrderForm form = new OrderForm("WIDGET", 2);

        try (Connection connection = connectionFactory.createConnection()) {
            connection.start();
            try (Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE)) {
                final Topic topic = session.createTopic(OrderDestinations.ORDERS_TOPIC);
                try (MessageConsumer consumer = session.createConsumer(topic)) {

                    tx.executeWithoutResult(status -> orderService.placeOrder(form));

                    final Message message = consumer.receive(5_000);
                    assertThat(message).isNotNull();
                    final OrderPlaced orderPlaced = (OrderPlaced) messageConverter.fromMessage(message);
                    assertThat(orderPlaced.productCode()).isEqualTo("WIDGET");
                    assertThat(orderPlaced.quantity()).isEqualTo(2);
                }
            }
        }
    }

    @Test
    void should_notPublishOrderPlaced_when_transactionRollsBack() throws Exception {
        final TransactionTemplate tx = new TransactionTemplate(transactionManager);
        final OrderForm rolledBackForm = new OrderForm("GADGET", 1);
        final OrderForm controlForm = new OrderForm("WIDGET", 1);
        final long countBeforeRollback = orderRepository.count();

        try (Connection connection = connectionFactory.createConnection()) {
            connection.start();
            try (Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE)) {
                final Topic topic = session.createTopic(OrderDestinations.ORDERS_TOPIC);
                try (MessageConsumer consumer = session.createConsumer(topic)) {

                    tx.executeWithoutResult(status -> {
                        orderService.placeOrder(rolledBackForm);
                        status.setRollbackOnly();
                    });

                    // Negative wait alone would pass vacuously on a slow broker; prove the broker is
                    // actually live and not just slow by committing a second order and checking that
                    // *it* (not the rolled-back one) is the next message to arrive.
                    final var control = tx.execute(status -> orderService.placeOrder(controlForm));

                    final Message message = consumer.receive(5_000);
                    assertThat(message).isNotNull();
                    final OrderPlaced orderPlaced = (OrderPlaced) messageConverter.fromMessage(message);
                    assertThat(orderPlaced.orderId()).isEqualTo(control.getId());
                    assertThat(orderPlaced.productCode()).isEqualTo("WIDGET");

                    assertThat(orderRepository.count()).isEqualTo(countBeforeRollback + 1);
                }
            }
        }
    }
}
