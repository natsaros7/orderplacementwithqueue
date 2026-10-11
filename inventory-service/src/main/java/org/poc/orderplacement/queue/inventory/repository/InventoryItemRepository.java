package org.poc.orderplacement.queue.inventory.repository;

import org.poc.orderplacement.queue.inventory.domain.InventoryItem;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InventoryItemRepository extends JpaRepository<InventoryItem, String> {
}
