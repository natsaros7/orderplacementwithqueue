package org.poc.orderplacement.queue.payment.service;

import org.poc.orderplacement.queue.contracts.OrderDestinations;
import org.poc.orderplacement.queue.contracts.OrderPlaced;
import org.poc.orderplacement.queue.payment.config.JmsConfig;
import org.poc.orderplacement.queue.payment.domain.Payment;
import org.poc.orderplacement.queue.payment.repository.PaymentRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PaymentProcessingService {

    private static final Logger LOG = LoggerFactory.getLogger(PaymentProcessingService.class);

    private final PaymentRepository paymentRepository;

    public PaymentProcessingService(final PaymentRepository paymentRepository) {
        this.paymentRepository = paymentRepository;
    }

    @Transactional
    @JmsListener(destination = OrderDestinations.ORDERS_TOPIC, containerFactory = JmsConfig.DURABLE_TOPIC_CONTAINER_FACTORY,
            subscription = JmsConfig.DURABLE_SUBSCRIPTION_NAME)
    public void onOrderPlaced(final OrderPlaced orderPlaced) {
        if (paymentRepository.existsByOrderId(orderPlaced.orderId())) {
            LOG.info("Order {} already paid, skipping duplicate delivery", orderPlaced.orderId());
            return;
        }

        paymentRepository.save(Payment.completed(orderPlaced.orderId(), orderPlaced.productCode(),
                orderPlaced.quantity()));
        LOG.info("Recorded payment for order {}", orderPlaced.orderId());
    }
}
