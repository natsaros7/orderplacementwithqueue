package org.poc.order.placement.queue.orderplacementwithqueue.payment.controller;

import org.poc.order.placement.queue.orderplacementwithqueue.payment.repositories.PaymentRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class PaymentController {
    private final PaymentRepository paymentRepository;

    public PaymentController(PaymentRepository paymentRepository) {
        this.paymentRepository = paymentRepository;
    }

    @GetMapping("/")
    public String showPayments(Model model) {
        model.addAttribute("payments", paymentRepository.findAll());
        return "payments";
    }
}
