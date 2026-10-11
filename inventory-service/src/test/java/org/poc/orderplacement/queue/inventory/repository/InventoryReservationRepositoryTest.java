package org.poc.orderplacement.queue.inventory.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.poc.orderplacement.queue.inventory.domain.InventoryReservation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;

@DataJpaTest
class InventoryReservationRepositoryTest {

    private final InventoryReservationRepository reservationRepository;

    @Autowired
    InventoryReservationRepositoryTest(final InventoryReservationRepository reservationRepository) {
        this.reservationRepository = reservationRepository;
    }

    @Test
    void should_rejectDuplicateOrderId_when_savingSecondReservationForSameOrder() {
        final UUID orderId = UUID.randomUUID();
        reservationRepository.saveAndFlush(InventoryReservation.reserved(orderId, "WIDGET", 1));

        assertThatThrownBy(() -> reservationRepository
                .saveAndFlush(InventoryReservation.reserved(orderId, "WIDGET", 1)))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void should_reportExists_when_reservationAlreadySavedForOrder() {
        final UUID orderId = UUID.randomUUID();
        reservationRepository.saveAndFlush(InventoryReservation.reserved(orderId, "WIDGET", 1));

        assertThat(reservationRepository.existsByOrderId(orderId)).isTrue();
        assertThat(reservationRepository.existsByOrderId(UUID.randomUUID())).isFalse();
    }
}
