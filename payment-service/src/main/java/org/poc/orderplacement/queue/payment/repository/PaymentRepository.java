package org.poc.orderplacement.queue.payment.repository;

import java.util.UUID;
import org.poc.orderplacement.queue.payment.domain.Payment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PaymentRepository extends JpaRepository<Payment, UUID> {

    boolean existsByOrderId(UUID orderId);
}
