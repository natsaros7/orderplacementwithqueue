package org.poc.orderplacement.queue.inventory.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.poc.orderplacement.queue.inventory.domain.InventoryReservation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataIntegrityViolationException;
import org.testcontainers.postgresql.PostgreSQLContainer;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(InventoryReservationRepositoryTest.PostgresTestcontainersConfiguration.class)
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

    @TestConfiguration(proxyBeanMethods = false)
    static class PostgresTestcontainersConfiguration {
        @Bean
        @ServiceConnection
        PostgreSQLContainer postgresContainer() {
            return new PostgreSQLContainer("postgres:18");
        }
    }
}
