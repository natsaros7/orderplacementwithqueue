package org.poc.orderplacement.queue.order.config;

import jakarta.validation.constraints.NotEmpty;
import java.util.List;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

/** The product codes offered on the order form. */
@ConfigurationProperties(prefix = "order.catalog")
@Validated
public record OrderCatalogProperties(@NotEmpty List<String> products) {
}
