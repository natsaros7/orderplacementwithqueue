package org.poc.order.placement.queue.orderplacementwithqueue.order.controller;

import org.poc.order.placement.queue.orderplacementwithqueue.order.entites.Order;
import org.poc.order.placement.queue.orderplacementwithqueue.order.service.OrderService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class OrderController {
    private static final Logger logger = LoggerFactory.getLogger(OrderController.class);
    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @GetMapping("/placeOrder")
    public String showOrderForm(Order order) {
        logger.info("Showing order form");
        return "placeOrder";
    }

    @PostMapping("/orders")
    public String placeOrder(Order order, RedirectAttributes redirectAttributes) throws Exception {
        orderService.placeOrder(order);
        redirectAttributes.addFlashAttribute("message", "Order placed successfully!");
        return "redirect:/placeOrder"; // Redirect back to the order form page
    }
}
