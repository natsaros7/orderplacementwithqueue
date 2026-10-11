package org.poc.orderplacement.queue.order.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.poc.orderplacement.queue.order.domain.Order;
import org.poc.orderplacement.queue.order.domain.OrderRepository;
import org.poc.orderplacement.queue.order.event.OrderPlacedEvent;
import org.poc.orderplacement.queue.order.web.OrderForm;
import org.springframework.context.ApplicationEventPublisher;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @Test
    void should_saveOrderAndPublishEvent_when_placingOrder() {
        final OrderForm form = new OrderForm("WIDGET", 3);
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));

        final OrderService orderService = new OrderService(orderRepository, eventPublisher);
        final Order placed = orderService.placeOrder(form);

        assertThat(placed.getProductCode()).isEqualTo("WIDGET");
        assertThat(placed.getQuantity()).isEqualTo(3);
        verify(eventPublisher).publishEvent(any(OrderPlacedEvent.class));
    }
}
