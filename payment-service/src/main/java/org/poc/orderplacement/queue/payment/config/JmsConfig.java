package org.poc.orderplacement.queue.payment.config;

import jakarta.jms.ConnectionFactory;
import org.apache.activemq.RedeliveryPolicy;
import org.poc.orderplacement.queue.contracts.OrderMessageConverters;
import org.springframework.boot.activemq.autoconfigure.ActiveMQConnectionFactoryCustomizer;
import org.springframework.boot.jms.autoconfigure.DefaultJmsListenerContainerFactoryConfigurer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jms.config.DefaultJmsListenerContainerFactory;
import org.springframework.jms.support.converter.JacksonJsonMessageConverter;

@Configuration
public class JmsConfig {

    /** Durable subscription name; paired with {@code spring.jms.client-id} it survives this consumer being offline. */
    public static final String DURABLE_SUBSCRIPTION_NAME = "payment-orders-subscription";

    /** Name of the {@link DefaultJmsListenerContainerFactory} bean declared below. */
    public static final String DURABLE_TOPIC_CONTAINER_FACTORY = "durableTopicListenerContainerFactory";

    @Bean
    public JacksonJsonMessageConverter jacksonJmsMessageConverter() {
        return OrderMessageConverters.orderPlacedConverter();
    }

    /**
     * Topic subscription is durable (not Boot's default autoconfigured factory) so orders placed
     * while this service is offline are redelivered on restart instead of being lost.
     */
    @Bean(DURABLE_TOPIC_CONTAINER_FACTORY)
    public DefaultJmsListenerContainerFactory durableTopicListenerContainerFactory(
            final DefaultJmsListenerContainerFactoryConfigurer configurer, final ConnectionFactory connectionFactory) {
        final DefaultJmsListenerContainerFactory factory = new DefaultJmsListenerContainerFactory();
        configurer.configure(factory, connectionFactory);
        factory.setSubscriptionDurable(true);
        return factory;
    }

    /** A redelivery policy keeps a few retries local before ActiveMQ dead-letters to {@code ActiveMQ.DLQ}. */
    @Bean
    public ActiveMQConnectionFactoryCustomizer redeliveryPolicyCustomizer() {
        return connectionFactory -> {
            final RedeliveryPolicy policy = new RedeliveryPolicy();
            policy.setMaximumRedeliveries(3);
            policy.setInitialRedeliveryDelay(1_000L);
            policy.setUseExponentialBackOff(true);
            policy.setBackOffMultiplier(2.0);
            connectionFactory.setRedeliveryPolicy(policy);
        };
    }
}
