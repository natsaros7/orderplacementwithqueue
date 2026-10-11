package org.poc.orderplacement.queue.inventory.domain;

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
@Table(name = "inventory_reservation", uniqueConstraints = @UniqueConstraint(columnNames = "order_id"))
public class InventoryReservation {

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
    private ReservationStatus status;

    @Column(nullable = false)
    private Instant decidedAt;

    protected InventoryReservation() {
        // JPA
    }

    private InventoryReservation(final UUID orderId, final String productCode, final int quantity,
            final ReservationStatus status) {
        this.orderId = orderId;
        this.productCode = productCode;
        this.quantity = quantity;
        this.status = status;
        this.decidedAt = Instant.now();
    }

    public static InventoryReservation reserved(final UUID orderId, final String productCode, final int quantity) {
        return new InventoryReservation(orderId, productCode, quantity, ReservationStatus.RESERVED);
    }

    public static InventoryReservation rejected(final UUID orderId, final String productCode, final int quantity) {
        return new InventoryReservation(orderId, productCode, quantity, ReservationStatus.REJECTED);
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

    public ReservationStatus getStatus() {
        return status;
    }

    public Instant getDecidedAt() {
        return decidedAt;
    }
}
