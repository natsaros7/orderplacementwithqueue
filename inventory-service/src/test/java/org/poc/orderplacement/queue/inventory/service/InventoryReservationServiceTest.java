package org.poc.orderplacement.queue.inventory.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.poc.orderplacement.queue.contracts.OrderPlaced;
import org.poc.orderplacement.queue.inventory.domain.InventoryItem;
import org.poc.orderplacement.queue.inventory.domain.InventoryReservation;
import org.poc.orderplacement.queue.inventory.enums.ReservationStatus;
import org.poc.orderplacement.queue.inventory.repository.InventoryItemRepository;
import org.poc.orderplacement.queue.inventory.repository.InventoryReservationRepository;

@ExtendWith(MockitoExtension.class)
class InventoryReservationServiceTest {

    @Mock
    private InventoryItemRepository inventoryItemRepository;

    @Mock
    private InventoryReservationRepository reservationRepository;

    private InventoryReservationService service;

    @Test
    void should_reserveStock_when_sufficientStockAvailable() {
        service = new InventoryReservationService(inventoryItemRepository, reservationRepository);
        final OrderPlaced orderPlaced = new OrderPlaced(UUID.randomUUID(), "WIDGET", 2, Instant.now());
        when(reservationRepository.existsByOrderId(orderPlaced.orderId())).thenReturn(false);
        when(inventoryItemRepository.findById("WIDGET")).thenReturn(Optional.of(new InventoryItem("WIDGET", 5)));

        service.onOrderPlaced(orderPlaced);

        final ArgumentCaptor<InventoryItem> itemCaptor = ArgumentCaptor.forClass(InventoryItem.class);
        verify(inventoryItemRepository).save(itemCaptor.capture());
        assertThat(itemCaptor.getValue().getStock()).isEqualTo(3);

        final ArgumentCaptor<InventoryReservation> reservationCaptor = ArgumentCaptor.forClass(InventoryReservation.class);
        verify(reservationRepository).save(reservationCaptor.capture());
        assertThat(reservationCaptor.getValue().getStatus()).isEqualTo(ReservationStatus.RESERVED);
    }

    @Test
    void should_rejectReservation_when_insufficientStock() {
        service = new InventoryReservationService(inventoryItemRepository, reservationRepository);
        final OrderPlaced orderPlaced = new OrderPlaced(UUID.randomUUID(), "GIZMO", 10, Instant.now());
        when(reservationRepository.existsByOrderId(orderPlaced.orderId())).thenReturn(false);
        when(inventoryItemRepository.findById("GIZMO")).thenReturn(Optional.of(new InventoryItem("GIZMO", 1)));

        service.onOrderPlaced(orderPlaced);

        verify(inventoryItemRepository, never()).save(any());
        final ArgumentCaptor<InventoryReservation> reservationCaptor = ArgumentCaptor.forClass(InventoryReservation.class);
        verify(reservationRepository).save(reservationCaptor.capture());
        assertThat(reservationCaptor.getValue().getStatus()).isEqualTo(ReservationStatus.REJECTED);
    }

    @Test
    void should_skipProcessing_when_orderAlreadyReserved() {
        service = new InventoryReservationService(inventoryItemRepository, reservationRepository);
        final OrderPlaced orderPlaced = new OrderPlaced(UUID.randomUUID(), "WIDGET", 1, Instant.now());
        when(reservationRepository.existsByOrderId(orderPlaced.orderId())).thenReturn(true);

        service.onOrderPlaced(orderPlaced);

        verify(inventoryItemRepository, never()).findById(any());
        verify(reservationRepository, never()).save(any());
    }
}
