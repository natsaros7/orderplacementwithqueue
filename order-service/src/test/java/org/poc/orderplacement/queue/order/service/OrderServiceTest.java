package org.poc.orderplacement.queue.order.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.poc.orderplacement.queue.order.domain.Order;
import org.poc.orderplacement.queue.order.domain.OrderRepository;
import org.poc.orderplacement.queue.order.domain.OrderStatus;
import org.poc.orderplacement.queue.order.event.OrderPlacedEvent;
import org.poc.orderplacement.queue.order.web.OrderForm;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @Test
    void should_saveOrderAndPublishEvent_when_placingOrder() {
        final OrderForm form = new OrderForm("WIDGET", 3);
        // @GeneratedValue only assigns an id once a real EntityManager persists the row; stand in
        // for that here so the published OrderPlaced (which requires a non-null orderId) has one.
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> {
            final Order order = invocation.getArgument(0);
            ReflectionTestUtils.setField(order, "id", UUID.randomUUID());
            return order;
        });

        final OrderService orderService = new OrderService(orderRepository, eventPublisher);
        final Order placed = orderService.placeOrder(form);

        assertThat(placed.getProductCode()).isEqualTo("WIDGET");
        assertThat(placed.getQuantity()).isEqualTo(3);
        assertThat(placed.getStatus()).isEqualTo(OrderStatus.PLACED);

        final ArgumentCaptor<OrderPlacedEvent> captor = ArgumentCaptor.forClass(OrderPlacedEvent.class);
        verify(eventPublisher).publishEvent(captor.capture());
        assertThat(captor.getValue().orderPlaced().orderId()).isEqualTo(placed.getId());
        assertThat(captor.getValue().orderPlaced().productCode()).isEqualTo("WIDGET");
        assertThat(captor.getValue().orderPlaced().quantity()).isEqualTo(3);
    }

    @Test
    void should_returnCurrentOrder_when_gettingById() {
        final Order order = new Order("WIDGET", 1);
        when(orderRepository.findById(any())).thenReturn(Optional.of(order));

        final Order found = new OrderService(orderRepository, eventPublisher).getOrder(UUID.randomUUID());

        assertThat(found).isSameAs(order);
    }
}
