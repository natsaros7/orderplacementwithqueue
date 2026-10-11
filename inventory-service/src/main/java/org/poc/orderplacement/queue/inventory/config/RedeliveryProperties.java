package org.poc.orderplacement.queue.inventory.config;

import jakarta.validation.constraints.Min;
import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

/** Tunable so tests can use short delays instead of waiting out production backoff timings. */
@ConfigurationProperties(prefix = "app.jms.redelivery")
@Validated
public record RedeliveryProperties(@Min(0) int maxRedeliveries, Duration initialDelay, double backOffMultiplier) {
}
