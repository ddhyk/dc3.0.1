package org.example.dcdemo.controller;

import org.example.dcdemo.model.User;
import org.example.dcdemo.model.Operation;
import org.example.dcdemo.service.UserService;
import org.example.dcdemo.service.OperationStaffService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import jakarta.servlet.http.HttpSession;
import org.springframework.security.crypto.password.PasswordEncoder;

@Controller
public class LoginController {
    
    @Autowired
    private UserService userService;
    
    @Autowired
    private OperationStaffService operationStaffService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @GetMapping("/")
    public String home() {
        return "redirect:/login";
    }

    @GetMapping("/login")
    public String showLoginPage(@RequestParam(required = false) String error, 
                              HttpSession session, 
                              Model model) {
        if (error != null) {
            String errorMessage = (String) session.getAttribute("error");
            model.addAttribute("error", errorMessage != null ? errorMessage : "登录失败，请检查邮箱和密码");
            session.removeAttribute("error");
        }
        return "login";
    }

    @PostMapping("/doLogin")
    public String login(@RequestParam String email, 
                       @RequestParam String password,
                       HttpSession session,
                       RedirectAttributes redirectAttributes) {
        try {
            // 先尝试普通用户登录
            User user = userService.findByEmail(email);
            if (user != null && passwordEncoder.matches(password, user.getPassword())) {
                session.setAttribute("user", user);
                session.setAttribute("userType", user.getUserType());
                session.setAttribute("userEmail", user.getEmail());
                return user.getUserType().equals("admin") ? "redirect:/admin" : "redirect:/customer";
            }
            
            // 尝试运营人员登录
            Operation staff = operationStaffService.findByEmail(email);
            if (staff != null && passwordEncoder.matches(password, staff.getPassword())) {
                session.setAttribute("user", staff);
                session.setAttribute("userType", "operation");
                session.setAttribute("userEmail", staff.getEmail());
                return "redirect:/dashboard";
            }
            
            redirectAttributes.addFlashAttribute("error", "邮箱或密码错误");
            return "redirect:/login?error";
            
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "登录失败：" + e.getMessage());
            return "redirect:/login?error";
        }
    }

    @GetMapping("/register")
    public String showRegistrationForm() {
        return "register";
    }

    @PostMapping("/register")
    public String register(User user, RedirectAttributes redirectAttributes) {
        try {
            // 检查邮箱是否已存在
            if (userService.findByEmail(user.getEmail()) != null) {
                redirectAttributes.addFlashAttribute("error", "该邮箱已被注册");
                return "redirect:/register";
            }
            
            // 检查运营人员邮箱
            if (operationStaffService.findByEmail(user.getEmail()) != null) {
                redirectAttributes.addFlashAttribute("error", "该邮箱已被注册");
                return "redirect:/register";
            }
            
            user.setUserType("customer");
            // 设置默认值
            user.setAddress("未设置");
            user.setPhone("未设置");
            user.setName("用户" + user.getEmail()); 
            userService.saveUser(user);
            redirectAttributes.addFlashAttribute("message", "注册成功，请登录");
            return "redirect:/login";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "注册失败，请稍后重试");
            return "redirect:/register";
        }
    }
    
    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/login";
    }
} 