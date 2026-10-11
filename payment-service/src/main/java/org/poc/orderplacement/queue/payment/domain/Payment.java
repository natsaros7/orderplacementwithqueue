package org.poc.orderplacement.queue.payment.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "payment", uniqueConstraints = @UniqueConstraint(columnNames = "order_id"))
public class Payment {

    @Id
    @GeneratedValue
    private UUID id;

    @Column(name = "order_id", nullable = false)
    private UUID orderId;

    @Column(nullable = false)
    private String productCode;

    @Column(nullable = false)
    private int quantity;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentStatus status;

    @Column(nullable = false)
    private Instant processedAt;

    protected Payment() {
        // JPA
    }

    private Payment(final UUID orderId, final String productCode, final int quantity, final PaymentStatus status) {
        this.orderId = orderId;
        this.productCode = productCode;
        this.quantity = quantity;
        this.status = status;
        this.processedAt = Instant.now();
    }

    public static Payment completed(final UUID orderId, final String productCode, final int quantity) {
        return new Payment(orderId, productCode, quantity, PaymentStatus.COMPLETED);
    }

    public UUID getId() {
        return id;
    }

    public UUID getOrderId() {
        return orderId;
    }

    public String getProductCode() {
        return productCode;
    }

    public int getQuantity() {
        return quantity;
    }

    public PaymentStatus getStatus() {
        return status;
    }

    public Instant getProcessedAt() {
        return processedAt;
    }
}
