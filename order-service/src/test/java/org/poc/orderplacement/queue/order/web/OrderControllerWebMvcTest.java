package org.poc.orderplacement.queue.order.web;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.poc.orderplacement.queue.order.config.OrderCatalogProperties;
import org.poc.orderplacement.queue.order.domain.Order;
import org.poc.orderplacement.queue.order.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.assertj.MockMvcTester;

@WebMvcTest(OrderController.class)
class OrderControllerWebMvcTest {

    @Autowired
    private MockMvcTester mvc;

    @MockitoBean
    private OrderService orderService;

    @TestConfiguration
    static class CatalogTestConfig {
        @Bean
        OrderCatalogProperties orderCatalogProperties() {
            return new OrderCatalogProperties(List.of("WIDGET", "GADGET"));
        }
    }

    @Test
    void should_renderForm_when_gettingNewOrderPage() {
        mvc.get().uri("/orders/new").assertThat()
                .hasStatusOk()
                .bodyText().contains("WIDGET");
        verifyNoInteractions(orderService);
    }

    @Test
    void should_rejectAndReshowForm_when_quantityMissing() {
        mvc.post().uri("/orders").param("productCode", "WIDGET").assertThat()
                .hasStatusOk()
                .bodyText().contains("Place an order");
        verifyNoInteractions(orderService);
    }

    @Test
    void should_redirectWithOrderId_when_orderIsValid() {
        final Order saved = new Order("WIDGET", 2);
        when(orderService.placeOrder(any())).thenReturn(saved);

        mvc.post().uri("/orders").param("productCode", "WIDGET").param("quantity", "2").assertThat()
                .hasStatus3xxRedirection()
                .hasRedirectedUrl("/orders/new");

        verify(orderService).placeOrder(any());
    }
}
