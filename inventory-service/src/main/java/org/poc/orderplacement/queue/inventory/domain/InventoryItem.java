package org.poc.orderplacement.queue.inventory.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;

@Entity
public class InventoryItem {

    @Id
    private String productCode;

    @Column(nullable = false)
    private int stock;

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
        return stock >= quantity;
    }

    public void reserve(final int quantity) {
        if (!hasEnoughStock(quantity)) {
            throw new IllegalStateException("Insufficient stock for product " + productCode);
        }
        stock -= quantity;
    }
}
