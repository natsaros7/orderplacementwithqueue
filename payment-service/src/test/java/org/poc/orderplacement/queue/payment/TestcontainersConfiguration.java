package org.poc.orderplacement.queue.payment;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.testcontainers.activemq.ActiveMQContainer;
import org.testcontainers.postgresql.PostgreSQLContainer;

/**
 * Containers are declared as singleton beans rather than JUnit {@code @Container} fields, so
 * Spring's test context cache starts them once and reuses them across every test class that shares
 * this configuration (import it, don't instantiate containers per test).
 */
@TestConfiguration(proxyBeanMethods = false)
public class TestcontainersConfiguration {

    @Bean
    @ServiceConnection
    PostgreSQLContainer postgresContainer() {
        return new PostgreSQLContainer("postgres:18");
    }

    @Bean
    @ServiceConnection
    ActiveMQContainer activeMQContainer() {
        return new ActiveMQContainer("apache/activemq-classic:6.2.0");
    }
}
