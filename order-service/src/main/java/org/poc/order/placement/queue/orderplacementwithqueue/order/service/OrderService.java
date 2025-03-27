package org.poc.order.placement.queue.orderplacementwithqueue.order.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.poc.order.placement.queue.orderplacementwithqueue.order.entites.Order;
import org.poc.order.placement.queue.orderplacementwithqueue.order.repositories.OrderRepository;
import org.springframework.stereotype.Service;

@Service
public class OrderService {
    private final OrderRepository orderRepository;
    private final MessagePublisherService messagePublisherService;

    public OrderService(OrderRepository orderRepository, MessagePublisherService messagePublisherService) {
        this.orderRepository = orderRepository;
        this.messagePublisherService = messagePublisherService;
    }

    public Order placeOrder(Order order) throws JsonProcessingException {
        // Persist the order
        Order savedOrder = orderRepository.save(order);
        messagePublisherService.publishMessage(savedOrder);
        return savedOrder;
    }
}
