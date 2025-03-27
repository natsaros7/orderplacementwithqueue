package org.poc.order.placement.queue.orderplacementwithqueue.inventory.repositories;

import org.poc.order.placement.queue.orderplacementwithqueue.inventory.entities.Inventory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface InventoryRepository extends JpaRepository<Inventory, String> {
}
