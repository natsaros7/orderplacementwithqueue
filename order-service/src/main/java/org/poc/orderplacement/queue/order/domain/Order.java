package org.poc.orderplacement.queue.order.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "orders")
public class Order {

    @Id
    @GeneratedValue
    private UUID id;

    @Column(nullable = false)
    private String productCode;

    @Column(nullable = false)
    private int quantity;

    @Column(nullable = false)
    private Instant placedAt;

    protected Order() {
        // JPA
    }

    public Order(final String productCode, final int quantity) {
        this.productCode = productCode;
        this.quantity = quantity;
        this.placedAt = Instant.now();
    }

    public UUID getId() {
        return id;
    }

    public String getProductCode() {
        return productCode;
    }

    public int getQuantity() {
        return quantity;
    }

    public Instant getPlacedAt() {
        return placedAt;
    }
}
