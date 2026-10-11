package org.poc.orderplacement.queue.inventory.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Version;

/**
 * Optimistic locking ({@link Version}) is a safety net, not a load-bearing guarantee here: the
 * durable topic subscription enforces exactly one live consumer instance per {@code client-id}
 * (see README trade-offs), so there is normally only a single writer thread. Scaling out to
 * multiple instances needs shared durable subscriptions or virtual topics first.
 */
@Entity
public class InventoryItem {

    @Id
    private String productCode;

    @Column(nullable = false)
    private int stock;

    @Version
    private long version;

    protected InventoryItem() {
        // JPA
    }

    public InventoryItem(final String productCode, final int stock) {
        this.productCode = productCode;
        this.stock = stock;
    }

    public String getProductCode() {
        return productCode;
    }

    public int getStock() {
        return stock;
    }

    public boolean hasEnoughStock(final int quantity) {
        requirePositive(quantity);
        return stock >= quantity;
    }

    public void reserve(final int quantity) {
        requirePositive(quantity);
        if (!hasEnoughStock(quantity)) {
            throw new IllegalStateException("Insufficient stock for product " + productCode);
        }
        stock -= quantity;
    }

    private static void requirePositive(final int quantity) {
        if (quantity < 1) {
            throw new IllegalArgumentException("quantity must be at least 1, was " + quantity);
        }
    }
}
