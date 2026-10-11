package org.poc.orderplacement.queue.order.web;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/** Command object bound from the order form; Spring MVC binds request params to this record's constructor. */
public record OrderForm(@NotBlank String productCode, @NotNull @Min(1) Integer quantity) {
}
