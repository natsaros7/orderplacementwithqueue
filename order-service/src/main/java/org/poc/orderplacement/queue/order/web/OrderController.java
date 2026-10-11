package org.poc.orderplacement.queue.order.web;

import jakarta.validation.Valid;
import org.poc.orderplacement.queue.order.config.OrderCatalogProperties;
import org.poc.orderplacement.queue.order.domain.Order;
import org.poc.orderplacement.queue.order.service.OrderService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class OrderController {

    private final OrderService orderService;
    private final OrderCatalogProperties catalog;

    public OrderController(final OrderService orderService, final OrderCatalogProperties catalog) {
        this.orderService = orderService;
        this.catalog = catalog;
    }

    @GetMapping("/orders/new")
    public String showOrderForm(final Model model) {
        if (!model.containsAttribute("orderForm")) {
            model.addAttribute("orderForm", new OrderForm(null, null));
        }
        model.addAttribute("products", catalog.products());
        return "orders/new";
    }

    @PostMapping("/orders")
    public String placeOrder(@Valid @ModelAttribute("orderForm") final OrderForm orderForm,
            final BindingResult bindingResult, final Model model, final RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("products", catalog.products());
            return "orders/new";
        }

        final Order placed = orderService.placeOrder(orderForm);
        redirectAttributes.addFlashAttribute("message", "Order placed successfully! Order id: " + placed.getId());
        return "redirect:/orders/new";
    }
}
