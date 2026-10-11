package org.poc.orderplacement.queue.payment.web;

import org.poc.orderplacement.queue.payment.repository.PaymentRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class PaymentController {

    private final PaymentRepository paymentRepository;

    public PaymentController(final PaymentRepository paymentRepository) {
        this.paymentRepository = paymentRepository;
    }

    @GetMapping("/")
    public String showPayments(final Model model) {
        model.addAttribute("payments", paymentRepository.findAll());
        return "payments";
    }
}
