package org.poc.orderplacement.queue.payment.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.poc.orderplacement.queue.payment.domain.Payment;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;

@DataJpaTest
class PaymentRepositoryTest {

    private final PaymentRepository paymentRepository;

    @Autowired
    PaymentRepositoryTest(final PaymentRepository paymentRepository) {
        this.paymentRepository = paymentRepository;
    }

    @Test
    void should_rejectDuplicateOrderId_when_savingSecondPaymentForSameOrder() {
        final UUID orderId = UUID.randomUUID();
        paymentRepository.saveAndFlush(Payment.completed(orderId, "WIDGET", 1));

        assertThatThrownBy(() -> paymentRepository.saveAndFlush(Payment.completed(orderId, "WIDGET", 1)))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void should_reportExists_when_paymentAlreadySavedForOrder() {
        final UUID orderId = UUID.randomUUID();
        paymentRepository.saveAndFlush(Payment.completed(orderId, "WIDGET", 1));

        assertThat(paymentRepository.existsByOrderId(orderId)).isTrue();
        assertThat(paymentRepository.existsByOrderId(UUID.randomUUID())).isFalse();
    }
}
