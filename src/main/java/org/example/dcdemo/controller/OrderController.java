package org.example.dcdemo.controller;

import org.example.dcdemo.model.Order;
import org.example.dcdemo.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

import java.util.List;

@Controller
public class OrderController {
    @Autowired
    private OrderService orderService;

    @GetMapping("/orders")
    public String orderPage(Model model) {
        List<Order> orders = orderService.getAllOrders();
        model.addAttribute("orders", orders);
        return "customer"; 
    }

    @PostMapping("/orders/addOrder")
    public String addOrder(Order order) {
        orderService.addOrder(order);
        return "redirect:/orders";
    }
} 