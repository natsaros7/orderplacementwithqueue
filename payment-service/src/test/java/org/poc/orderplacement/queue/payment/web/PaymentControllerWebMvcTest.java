package org.poc.orderplacement.queue.payment.web;

import static org.mockito.Mockito.when;

import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.poc.orderplacement.queue.payment.domain.Payment;
import org.poc.orderplacement.queue.payment.repository.PaymentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.assertj.MockMvcTester;

@WebMvcTest(PaymentController.class)
class PaymentControllerWebMvcTest {

    @Autowired
    private MockMvcTester mvc;

    @MockitoBean
    private PaymentRepository paymentRepository;

    @Test
    void should_renderPayments_when_gettingHomePage() {
        when(paymentRepository.findAll())
                .thenReturn(List.of(Payment.completed(UUID.randomUUID(), "WIDGET", 2)));

        mvc.get().uri("/").assertThat()
                .hasStatusOk()
                .bodyText().contains("WIDGET");
    }
}
