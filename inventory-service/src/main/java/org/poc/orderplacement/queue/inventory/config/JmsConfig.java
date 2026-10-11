package org.poc.orderplacement.queue.inventory.config;

import org.apache.activemq.RedeliveryPolicy;
import org.poc.orderplacement.queue.contracts.OrderMessageConverters;
import org.springframework.boot.activemq.autoconfigure.ActiveMQConnectionFactoryCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jms.support.converter.JacksonJsonMessageConverter;

@Configuration
public class JmsConfig {

    /** Durable subscription name; paired with {@code spring.jms.client-id} it survives this consumer being offline. */
    public static final String DURABLE_SUBSCRIPTION_NAME = "inventory-orders-subscription";

    @Bean
    public JacksonJsonMessageConverter jacksonJmsMessageConverter() {
        return OrderMessageConverters.orderPlacedConverter();
    }

    /** A redelivery policy keeps a few retries local before ActiveMQ dead-letters to {@code ActiveMQ.DLQ}. */
    @Bean
    public ActiveMQConnectionFactoryCustomizer redeliveryPolicyCustomizer(final RedeliveryProperties properties) {
        return connectionFactory -> {
            final RedeliveryPolicy policy = new RedeliveryPolicy();
            policy.setMaximumRedeliveries(properties.maxRedeliveries());
            policy.setInitialRedeliveryDelay(properties.initialDelay().toMillis());
            policy.setUseExponentialBackOff(true);
            policy.setBackOffMultiplier(properties.backOffMultiplier());
            connectionFactory.setRedeliveryPolicy(policy);
        };
    }
}
