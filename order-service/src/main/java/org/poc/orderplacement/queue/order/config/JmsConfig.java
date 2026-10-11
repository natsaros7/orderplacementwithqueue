package org.poc.orderplacement.queue.order.config;

import org.poc.orderplacement.queue.contracts.OrderMessageConverters;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jms.support.converter.JacksonJsonMessageConverter;

/**
 * Boot autoconfigures the {@code ConnectionFactory} and {@code JmsTemplate} from
 * {@code spring.activemq.*} / {@code spring.jms.*} properties; the only thing this service adds is
 * the shared message converter so {@code JmsTemplate.convertAndSend} writes JSON keyed by the
 * logical type id rather than a Java FQCN.
 */
@Configuration
public class JmsConfig {

    @Bean
    public JacksonJsonMessageConverter jacksonJmsMessageConverter() {
        return OrderMessageConverters.orderPlacedConverter();
    }
}
