package org.example.dcdemo.controller;

import org.example.dcdemo.model.*;
import org.example.dcdemo.service.OrderService;
import org.example.dcdemo.service.AdminService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpSession;

import java.util.List;
import java.util.Map;
import org.springframework.http.HttpStatus;

@Controller
@RequestMapping("/customer")
public class CustomerController {

    
    @Autowired
    private OrderService orderService;

    @Autowired
    private AdminService adminService;


    @GetMapping("")
    public String customerPage(HttpSession session, Model model) {
        List<MenuItem> menuItems = adminService.getAllMenuItems();
        model.addAttribute("menuItems", menuItems);
        model.addAttribute("message", "欢迎使用御膳房点餐系统！");
        return "customer/customer";
    }

    @GetMapping("/profile")
    public String showProfile(HttpSession session, Model model) {
        User user = (User) session.getAttribute("user");
        model.addAttribute("user", user);
        return "customer/profile";
    }

    @GetMapping("/cart")
    public String showCart(HttpSession session, Model model) {
        User user = (User) session.getAttribute("user");
        Map<String, Object> cartDetails = orderService.getCartDetails(user);
        model.addAttribute("cartItems", cartDetails.get("items"));
        model.addAttribute("total", cartDetails.get("total"));
        return "customer/cart";
    }

    @PostMapping("/cart/add")
    @ResponseBody
    public ResponseEntity<?> addToCart(HttpSession session,
                                     @RequestParam Long menuItemId,
                                     @RequestParam Integer quantity) {
        try {
            User user = (User) session.getAttribute("user");
            return ResponseEntity.ok(orderService.addToCart(user, menuItemId, quantity));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    @DeleteMapping("/cart/{itemId}")
    @ResponseBody
    public Map<String, Object> removeFromCart(HttpSession session, @PathVariable Long itemId) {
        try {
            User user = (User) session.getAttribute("user");
            orderService.removeCartItem(itemId, user);
            return Map.of("success", true, "message", "商品已从购物车移除");
        } catch (Exception e) {
            return Map.of("success", false, "message", "移除商品失败，请重试");
        }
    }

    @PostMapping("/cart/submit")
    @ResponseBody
    public ResponseEntity<?> submitOrder(@RequestBody Map<String, String> orderData, 
                                       HttpSession session) {
        try {
            User user = (User) session.getAttribute("user");
            return ResponseEntity.ok(orderService.submitOrder(user, orderData));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("success", false, "message", "订单提交失败，请稍后重试"));
        }
    }

    @GetMapping("/orders")
    public String showOrders(HttpSession session, Model model) {
        User user = (User) session.getAttribute("user");
        List<Order> orders = orderService.getCustomerOrders(user);
        model.addAttribute("orders", orders);
        return "customer/orders";
    }

    @GetMapping("/cart/count")
    @ResponseBody
    public Map<String, Object> getCartCount(HttpSession session) {
        User user = (User) session.getAttribute("user");
        long count = orderService.getCartItemCount(user);
        return Map.of("count", count);
    }

    @GetMapping("/lucky-draw")
    public String showLuckyDraw(HttpSession session, Model model) {
        User user = (User) session.getAttribute("user");
        model.addAttribute("user", user);
        return "customer/lucky-draw";
    }

    @PostMapping("/cart/clear")
    @ResponseBody
    public Map<String, Object> clearCart(HttpSession session) {
        try {
            User user = (User) session.getAttribute("user");
            orderService.clearUserCart(user);
            return Map.of("success", true, "message", "购物车已清空");
        } catch (Exception e) {
            return Map.of("success", false, "message", "清空购物车失败，请重试");
        }
    }

    @GetMapping("/cart/details")
    @ResponseBody
    public Map<String, Object> getCartDetails(HttpSession session) {
        User user = (User) session.getAttribute("user");
        return orderService.getCartDetails(user);
    }

} 