package org.poc.order.placement.queue.orderplacementwithqueue.order.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.poc.order.placement.queue.orderplacementwithqueue.order.entites.Order;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Service;

@Service
public class MessagePublisherService {
    private final JmsTemplate pubSubJmsTemplate; // Autowired JmsTemplate configured for pub-sub

    public MessagePublisherService(JmsTemplate pubSubJmsTemplate) {
        this.pubSubJmsTemplate = pubSubJmsTemplate;
    }

    public void publishMessage(Order order) throws JsonProcessingException {
        pubSubJmsTemplate.convertAndSend("orders.topic", order);
    }
}
