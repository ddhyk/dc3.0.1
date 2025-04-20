package org.example.dcdemo.controller;

import org.example.dcdemo.service.DashboardService;
import org.example.dcdemo.dto.DashboardData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import jakarta.servlet.http.HttpSession;

@Controller
public class DashboardController {
    @Autowired
    private DashboardService dashboardService;
    
    
    @GetMapping("/dashboard")
    public String showDashboard(Model model, HttpSession session) {
        // 检查登录状态
        Object user = session.getAttribute("user");
        String userType = (String) session.getAttribute("userType");
        
        if (user == null || !"operation".equals(userType)) {
            return "redirect:/login";
        }
        
        dashboardService.populateDashboardModel(model);
        return "dashboard";
    }
    
    @GetMapping("/api/dashboard/data")
    @ResponseBody
    public DashboardData getDashboardData(HttpSession session) {
        String userType = (String) session.getAttribute("userType");
        if (session.getAttribute("user") == null || !"operation".equals(userType)) {
            return null;
        }
        return dashboardService.getDashboardData();
    }
} 