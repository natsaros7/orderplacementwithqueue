package org.poc.orderplacement.queue.inventory.web;

import static org.mockito.Mockito.when;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.poc.orderplacement.queue.inventory.domain.InventoryItem;
import org.poc.orderplacement.queue.inventory.repository.InventoryItemRepository;
import org.poc.orderplacement.queue.inventory.repository.InventoryReservationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.assertj.MockMvcTester;

@WebMvcTest(InventoryController.class)
class InventoryControllerWebMvcTest {

    private final MockMvcTester mvc;

    // @MockitoBean only supports field/type targets, not constructor parameters, so these stay fields.
    @MockitoBean
    private InventoryItemRepository inventoryItemRepository;

    @MockitoBean
    private InventoryReservationRepository reservationRepository;

    @Autowired
    InventoryControllerWebMvcTest(final MockMvcTester mvc) {
        this.mvc = mvc;
    }

    @Test
    void should_renderStock_when_gettingHomePage() {
        when(inventoryItemRepository.findAll()).thenReturn(List.of(new InventoryItem("WIDGET", 5)));
        when(reservationRepository.findAll()).thenReturn(List.of());

        mvc.get().uri("/").assertThat()
                .hasStatusOk()
                .bodyText().contains("WIDGET");
    }
}
