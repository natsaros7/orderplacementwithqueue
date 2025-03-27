package org.poc.order.placement.queue.orderplacementwithqueue.inventory.service;

import org.poc.order.placement.queue.orderplacementwithqueue.inventory.entities.Inventory;
import org.poc.order.placement.queue.orderplacementwithqueue.inventory.repositories.InventoryRepository;
import org.poc.order.placement.queue.orderplacementwithqueue.order.entites.Order;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Service;

@Service
public class InventoryService {
    private final InventoryRepository inventoryRepository;

    public InventoryService(InventoryRepository inventoryRepository) {
        this.inventoryRepository = inventoryRepository;
    }

    @JmsListener(destination = "orders.topic", containerFactory = "myJmsContainerFactory")
    public void listenOrder(Order order) {
        // Logic to update inventory based on the order
        Inventory inventory = inventoryRepository.findById(order.getProductId())
                .orElse(new Inventory(order.getProductId(), 0));
        inventory.setQuantity(inventory.getQuantity() - order.getQuantity());
        inventoryRepository.save(inventory);

        System.out.println("Updated inventory for product: " + order.getProductId());
    }
}
