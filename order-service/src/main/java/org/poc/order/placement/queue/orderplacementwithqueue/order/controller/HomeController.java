package org.poc.order.placement.queue.orderplacementwithqueue.order.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {
    @GetMapping("/")
    public String redirectToPlaceOrder() {
        return "redirect:/placeOrder";
    }
}
