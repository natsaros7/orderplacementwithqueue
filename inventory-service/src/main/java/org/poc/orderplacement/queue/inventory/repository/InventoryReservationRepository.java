package org.poc.orderplacement.queue.inventory.repository;

import java.util.UUID;
import org.poc.orderplacement.queue.inventory.domain.InventoryReservation;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InventoryReservationRepository extends JpaRepository<InventoryReservation, UUID> {

    boolean existsByOrderId(UUID orderId);
}
