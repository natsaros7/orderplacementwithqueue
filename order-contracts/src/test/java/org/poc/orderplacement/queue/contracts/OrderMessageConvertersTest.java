package org.poc.orderplacement.queue.contracts;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import jakarta.jms.Message;
import jakarta.jms.Session;
import java.time.Instant;
import java.util.UUID;
import org.apache.activemq.command.ActiveMQTextMessage;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jms.support.converter.JacksonJsonMessageConverter;
import org.springframework.jms.support.converter.MessageConversionException;

/**
 * Exercises the actual {@link OrderMessageConverters#orderPlacedConverter()} (not just a plain
 * Jackson round trip): the wire format is a TEXT message carrying a {@code _type} property, and an
 * unrecognized {@code _type} must be rejected rather than silently deserialized by FQCN.
 */
@ExtendWith(MockitoExtension.class)
class OrderMessageConvertersTest {

    private static final String TYPE_ID_PROPERTY = "_type";

    @Mock
    private Session session;

    @Test
    void should_writeTypeIdPropertyAndJsonBody_when_convertingToMessage() throws Exception {
        stubCreateTextMessage();
        final OrderPlaced orderPlaced = new OrderPlaced(UUID.randomUUID(), "WIDGET", 2, Instant.now());

        final Message message = OrderMessageConverters.orderPlacedConverter().toMessage(orderPlaced, session);

        assertThat(message.getStringProperty(TYPE_ID_PROPERTY)).isEqualTo(OrderDestinations.ORDER_PLACED_TYPE_ID);
        assertThat(((ActiveMQTextMessage) message).getText())
                .contains("\"orderId\"", orderPlaced.orderId().toString(), "\"productCode\":\"WIDGET\"");
    }

    @Test
    void should_roundTrip_when_convertingBackFromMessage() throws Exception {
        stubCreateTextMessage();
        final OrderPlaced orderPlaced = new OrderPlaced(UUID.randomUUID(), "GADGET", 1, Instant.now());
        final JacksonJsonMessageConverter converter = OrderMessageConverters.orderPlacedConverter();

        final Message message = converter.toMessage(orderPlaced, session);
        final Object roundTripped = converter.fromMessage(message);

        assertThat(roundTripped).isEqualTo(orderPlaced);
    }

    private void stubCreateTextMessage() throws Exception {
        when(session.createTextMessage(anyString())).thenAnswer(invocation -> {
            final ActiveMQTextMessage message = new ActiveMQTextMessage();
            message.setText(invocation.getArgument(0));
            return message;
        });
    }

    @Test
    void should_rejectMessage_when_typeIdIsUnrecognized() throws Exception {
        final ActiveMQTextMessage message = new ActiveMQTextMessage();
        message.setStringProperty(TYPE_ID_PROPERTY, "SomethingElse");
        message.setText("{}");

        assertThatThrownBy(() -> OrderMessageConverters.orderPlacedConverter().fromMessage(message))
                .isInstanceOf(MessageConversionException.class);
    }
}
