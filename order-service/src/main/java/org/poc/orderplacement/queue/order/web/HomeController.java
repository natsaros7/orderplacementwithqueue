package org.poc.orderplacement.queue.order.web;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    @GetMapping("/")
    public String redirectToOrderForm() {
        return "redirect:/orders/new";
    }
}
