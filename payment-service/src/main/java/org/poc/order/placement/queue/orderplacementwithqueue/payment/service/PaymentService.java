package org.poc.order.placement.queue.orderplacementwithqueue.payment.service;

import org.poc.order.placement.queue.orderplacementwithqueue.order.entites.Order;
import org.poc.order.placement.queue.orderplacementwithqueue.payment.entities.Payment;
import org.poc.order.placement.queue.orderplacementwithqueue.payment.repositories.PaymentRepository;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Service;

@Service
public class PaymentService {
    private final PaymentRepository paymentRepository;

    public PaymentService(PaymentRepository paymentRepository) {
        this.paymentRepository = paymentRepository;
    }

    @JmsListener(destination = "orders.topic", containerFactory = "myJmsContainerFactory")
    public void processPayment(Order order) {
        // Simulate payment processing
        Payment payment = new Payment();
        payment.setOrderId(order.getId().toString());
        payment.setStatus("COMPLETED"); // Assume payment is always successful for simplicity
        paymentRepository.save(payment);

        System.out.println("Processed payment for order: " + order.getId());
    }
}
