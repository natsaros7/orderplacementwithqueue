package org.poc.orderplacement.queue.inventory.web;

import org.poc.orderplacement.queue.inventory.repository.InventoryItemRepository;
import org.poc.orderplacement.queue.inventory.repository.InventoryReservationRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class InventoryController {

    private final InventoryItemRepository inventoryItemRepository;
    private final InventoryReservationRepository reservationRepository;

    public InventoryController(final InventoryItemRepository inventoryItemRepository,
            final InventoryReservationRepository reservationRepository) {
        this.inventoryItemRepository = inventoryItemRepository;
        this.reservationRepository = reservationRepository;
    }

    @GetMapping("/")
    public String showInventory(final Model model) {
        model.addAttribute("stock", inventoryItemRepository.findAll());
        model.addAttribute("reservations", reservationRepository.findAll());
        return "inventory";
    }
}
