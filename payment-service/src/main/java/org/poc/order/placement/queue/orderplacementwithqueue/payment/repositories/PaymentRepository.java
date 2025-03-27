package org.poc.order.placement.queue.orderplacementwithqueue.payment.repositories;

import org.poc.order.placement.queue.orderplacementwithqueue.payment.entities.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, UUID> {
}
