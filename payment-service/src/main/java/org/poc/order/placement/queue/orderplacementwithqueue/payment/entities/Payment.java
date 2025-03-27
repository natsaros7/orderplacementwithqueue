package org.poc.order.placement.queue.orderplacementwithqueue.payment.entities;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;

import java.util.UUID;

@Entity
public class Payment {
    @Id
    @GeneratedValue
    private UUID id;
    private String orderId;
    private String status; // For simplicity, let's assume "COMPLETED", "FAILED"

    public Payment() {
    }

    public Payment(UUID id, String orderId, String status) {
        this.id = id;
        this.orderId = orderId;
        this.status = status;
    }

    public UUID getId() {
        return id;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public String getOrderId() {
        return orderId;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getStatus() {
        return status;
    }
}
