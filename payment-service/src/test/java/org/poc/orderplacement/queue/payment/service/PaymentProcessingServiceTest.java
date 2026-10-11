package org.poc.orderplacement.queue.payment.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.poc.orderplacement.queue.contracts.OrderPlaced;
import org.poc.orderplacement.queue.payment.domain.Payment;
import org.poc.orderplacement.queue.payment.enums.PaymentStatus;
import org.poc.orderplacement.queue.payment.repository.PaymentRepository;

@ExtendWith(MockitoExtension.class)
class PaymentProcessingServiceTest {

    @Mock
    private PaymentRepository paymentRepository;

    @Test
    void should_recordCompletedPayment_when_orderPlacedMessageArrives() {
        final PaymentProcessingService service = new PaymentProcessingService(paymentRepository);
        final OrderPlaced orderPlaced = new OrderPlaced(UUID.randomUUID(), "WIDGET", 2, Instant.now());
        when(paymentRepository.existsByOrderId(orderPlaced.orderId())).thenReturn(false);

        service.onOrderPlaced(orderPlaced);

        final ArgumentCaptor<Payment> captor = ArgumentCaptor.forClass(Payment.class);
        verify(paymentRepository).save(captor.capture());
        assertThat(captor.getValue().getStatus()).isEqualTo(PaymentStatus.COMPLETED);
        assertThat(captor.getValue().getOrderId()).isEqualTo(orderPlaced.orderId());
    }

    @Test
    void should_skipProcessing_when_orderAlreadyPaid() {
        final PaymentProcessingService service = new PaymentProcessingService(paymentRepository);
        final OrderPlaced orderPlaced = new OrderPlaced(UUID.randomUUID(), "WIDGET", 1, Instant.now());
        when(paymentRepository.existsByOrderId(orderPlaced.orderId())).thenReturn(true);

        service.onOrderPlaced(orderPlaced);

        verify(paymentRepository, never()).save(any());
    }
}
