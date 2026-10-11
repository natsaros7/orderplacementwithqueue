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

    @Autowired
    private MockMvcTester mvc;

    @MockitoBean
    private InventoryItemRepository inventoryItemRepository;

    @MockitoBean
    private InventoryReservationRepository reservationRepository;

    @Test
    void should_renderStock_when_gettingHomePage() {
        when(inventoryItemRepository.findAll()).thenReturn(List.of(new InventoryItem("WIDGET", 5)));
        when(reservationRepository.findAll()).thenReturn(List.of());

        mvc.get().uri("/").assertThat()
                .hasStatusOk()
                .bodyText().contains("WIDGET");
    }
}
