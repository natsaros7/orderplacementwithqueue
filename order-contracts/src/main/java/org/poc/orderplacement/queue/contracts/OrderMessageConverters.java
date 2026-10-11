package org.poc.orderplacement.queue.contracts;

import java.util.Map;
import org.springframework.jms.support.converter.JacksonJsonMessageConverter;
import org.springframework.jms.support.converter.MessageType;

/**
 * Factory for the {@link JacksonJsonMessageConverter} shared by producer and consumers.
 *
 * <p>Deliberately a plain factory rather than a {@code @Configuration} class: each service still
 * declares its own {@code @Bean}, keeping Boot autoconfiguration (and per-service overrides) in the
 * service module while the wire format itself lives in one place.
 */
public final class OrderMessageConverters {

    private OrderMessageConverters() {
    }

    /**
     * Converter mapping the logical {@value OrderDestinations#ORDER_PLACED_TYPE_ID} type id to
     * {@link OrderPlaced}, so producer and consumers never need each other's fully-qualified class
     * name on the wire (and {@code packages.trust-all} can stay {@code false}).
     */
    public static JacksonJsonMessageConverter orderPlacedConverter() {
        final JacksonJsonMessageConverter converter = new JacksonJsonMessageConverter();
        converter.setTargetType(MessageType.TEXT);
        converter.setTypeIdPropertyName("_type");
        converter.setTypeIdMappings(Map.of(OrderDestinations.ORDER_PLACED_TYPE_ID, OrderPlaced.class));
        return converter;
    }
}
