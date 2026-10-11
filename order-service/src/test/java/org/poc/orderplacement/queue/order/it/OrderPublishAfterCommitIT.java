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
import org.poc.orderplacement.queue.order.service.OrderService;
import org.poc.orderplacement.queue.order.web.OrderForm;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jms.support.converter.MessageConverter;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

/**
 * Proves the dual-write boundary described in the README: a message only reaches the topic once the
 * placing transaction has committed, never when it rolls back.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
class OrderPublishAfterCommitIT {

    @Autowired
    private OrderService orderService;

    @Autowired
    private PlatformTransactionManager transactionManager;

    @Autowired
    private ConnectionFactory connectionFactory;

    @Autowired
    private MessageConverter messageConverter;

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
        final OrderForm form = new OrderForm("GADGET", 1);

        try (Connection connection = connectionFactory.createConnection()) {
            connection.start();
            try (Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE)) {
                final Topic topic = session.createTopic(OrderDestinations.ORDERS_TOPIC);
                try (MessageConsumer consumer = session.createConsumer(topic)) {

                    tx.executeWithoutResult(status -> {
                        orderService.placeOrder(form);
                        status.setRollbackOnly();
                    });

                    final Message message = consumer.receive(1_000);
                    assertThat(message).isNull();
                }
            }
        }
    }
}
