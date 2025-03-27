package org.poc.order.placement.queue.orderplacementwithqueue.inventory.controller;

import org.poc.order.placement.queue.orderplacementwithqueue.inventory.repositories.InventoryRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class InventoryController {

    private final InventoryRepository inventoryRepository;

    public InventoryController(InventoryRepository inventoryRepository) {
        this.inventoryRepository = inventoryRepository;
    }

    @GetMapping("/")
    public String showInventoryDashboard(Model model) {
        model.addAttribute("inventory", inventoryRepository.findAll());
        return "inventory";
    }
}