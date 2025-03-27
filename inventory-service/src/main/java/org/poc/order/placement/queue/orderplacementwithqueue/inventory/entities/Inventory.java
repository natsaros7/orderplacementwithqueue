package org.poc.order.placement.queue.orderplacementwithqueue.inventory.entities;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;

@Entity
public class Inventory {
    @Id
    private String productId; // Assume a simple product identifier
    private int quantity;

    public Inventory() {
    }

    public Inventory(String productId, int quantity) {
        this.productId = productId;
        this.quantity = quantity;
    }

    public String getProductId() {
        return productId;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    // Constructors, getters, and setters
}
