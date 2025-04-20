package org.example.dcdemo.controller;

import org.example.dcdemo.model.User;
import org.example.dcdemo.service.DrawService;
import org.springframework.http.ResponseEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpSession;
import java.util.Map;

@RestController
@RequestMapping("/api/draw")
public class DrawController {
    @Autowired
    private DrawService drawService;

    @PostMapping("/generate")
    public ResponseEntity<?> generateDrawCode(@RequestParam Long orderId, HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "请先登录"));
        }

        try {
            return ResponseEntity.ok(drawService.generateDrawCodeForOrder(user, orderId));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/verify")
    public ResponseEntity<?> verifyDrawCode(@RequestBody Map<String, String> request, HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "请先登录"));
        }

        try {
            return ResponseEntity.ok(drawService.verifyDrawCode(user, request.get("code")));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
} 