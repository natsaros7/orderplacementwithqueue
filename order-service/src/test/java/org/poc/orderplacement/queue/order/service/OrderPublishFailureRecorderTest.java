package org.poc.orderplacement.queue.order.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.poc.orderplacement.queue.order.domain.Order;
import org.poc.orderplacement.queue.order.domain.OrderRepository;
import org.poc.orderplacement.queue.order.domain.OrderStatus;

@ExtendWith(MockitoExtension.class)
class OrderPublishFailureRecorderTest {

    @Mock
    private OrderRepository orderRepository;

    @Test
    void should_markOrderPublishFailed_when_orderExists() {
        final Order order = new Order("WIDGET", 1);
        final UUID orderId = UUID.randomUUID();
        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));

        new OrderPublishFailureRecorder(orderRepository).markPublishFailed(orderId);

        assertThat(order.getStatus()).isEqualTo(OrderStatus.PUBLISH_FAILED);
        verify(orderRepository).save(order);
    }

    @Test
    void should_doNothing_when_orderNoLongerExists() {
        final UUID orderId = UUID.randomUUID();
        when(orderRepository.findById(orderId)).thenReturn(Optional.empty());

        new OrderPublishFailureRecorder(orderRepository).markPublishFailed(orderId);

        verify(orderRepository, never()).save(any());
    }
}
