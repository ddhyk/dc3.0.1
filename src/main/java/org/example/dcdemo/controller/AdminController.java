package org.example.dcdemo.controller;

import org.example.dcdemo.model.MenuItem;
import org.example.dcdemo.model.Order;
import org.example.dcdemo.model.User;
import org.example.dcdemo.service.CustomerService;
import org.example.dcdemo.service.FileService;
import org.example.dcdemo.service.MenuService;
import org.example.dcdemo.service.OrderService;
import org.example.dcdemo.service.StatisticsService;
import org.example.dcdemo.dto.AdminCreateRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.example.dcdemo.service.AdminLogService;
import jakarta.servlet.http.HttpServletRequest;
import org.example.dcdemo.service.AdminService;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

@Controller
@RequestMapping("/admin")
public class AdminController {
    
    @Autowired
    private CustomerService customerService;
    
    @Autowired
    private MenuService menuService;
    
    @Autowired
    private FileService fileService;
    
    @Autowired
    private OrderService orderService;
    
    @Autowired
    private StatisticsService statisticsService;
    
    @Autowired
    private AdminLogService adminLogService;
    
    @Autowired
    private AdminService adminService;

    
    @GetMapping("")
    public String adminPage(Model model) {
        // 更新历史订单总金额
        orderService.updateOrderTotals();
        
        List<User> customers = customerService.getAllCustomers();
        List<MenuItem> menuItems = menuService.getAllMenuItems();
        List<Order> orders = orderService.getAllOrders();
        
        model.addAttribute("customers", customers);
        model.addAttribute("menuItems", menuItems);
        model.addAttribute("orders", orders);
        return "admin/admin";
    }
    
    // 客户管理API
    @GetMapping("/api/customers")
    @ResponseBody
    public List<User> getCustomers(@RequestParam(required = false) String keyword) {
        if (keyword != null && !keyword.trim().isEmpty()) {
            return customerService.searchCustomers(keyword);
        }
        return customerService.getAllCustomers();
    }
    
    @GetMapping("/api/customers/{id}")
    @ResponseBody
    public ResponseEntity<User> getCustomer(@PathVariable Long id) {
        User customer = customerService.getCustomerById(id);
        if (customer != null) {
            return ResponseEntity.ok(customer);
        }
        return ResponseEntity.notFound().build();
    }
    
    @PostMapping("/api/customers")
    @ResponseBody
    public ResponseEntity<?> createCustomer(@RequestBody User customer, HttpServletRequest request) {
        try {
            if (customer.getPassword() == null || customer.getPassword().trim().isEmpty()) {
                return ResponseEntity.badRequest()
                    .body(Map.of("error", "密码不能为空"));
            }
            
            if (customerService.existsByEmail(customer.getEmail())) {
                return ResponseEntity.badRequest()
                    .body(Map.of("error", "该邮箱已被注册"));
            }
            
            customer.setUserType("customer");
            User savedCustomer = customerService.saveCustomer(customer);
            
            // 记录操作日志
            adminLogService.logAdminOperation(
                "创建新客户: " + savedCustomer.getEmail(),
                request
            );
            
            return ResponseEntity.ok(savedCustomer);
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(Map.of("error", "创建用户失败：" + e.getMessage()));
        }
    }
    
    @PutMapping("/api/customers/{id}")
    @ResponseBody
    public ResponseEntity<?> updateCustomer(@PathVariable Long id, @RequestBody User customer, HttpServletRequest request) {
        try {
            User existingUser = customerService.getCustomerById(id);
            if (existingUser == null) {
                return ResponseEntity.notFound().build();
            }
            
            if (!existingUser.getEmail().equals(customer.getEmail()) 
                && customerService.existsByEmail(customer.getEmail())) {
                return ResponseEntity.badRequest()
                    .body(Map.of("error", "该邮箱已被其他用户使用"));
            }
            
            User updatedCustomer = customerService.updateCustomer(id, customer);
            
            // 记录操作日志
            adminLogService.logAdminOperation(
                "更新客户信息: " + updatedCustomer.getEmail(),
                request
            );
            
            return ResponseEntity.ok(updatedCustomer);
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(Map.of("error", "更新失败：" + e.getMessage()));
        }
    }
    
    @DeleteMapping("/api/customers/{id}")
    @ResponseBody
    public ResponseEntity<?> deleteCustomer(@PathVariable Long id, HttpServletRequest request) {
        User customer = customerService.getCustomerById(id);
        if (customer == null) {
            return ResponseEntity.notFound().build();
        }
        
        if ("admin".equals(customer.getUserType())) {
            return ResponseEntity.badRequest()
                .body(Map.of("error", "不能删除管理员账号"));
        }
        
        // 记录操作日志
        adminLogService.logAdminOperation(
            "删除客户: " + customer.getEmail(),
            request
        );
        
        customerService.deleteCustomer(id);
        return ResponseEntity.ok(Map.of("message", "客户删除成功"));
    }

    @GetMapping("/api/menu-items")
    @ResponseBody
    public List<MenuItem> getMenuItems(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String category) {
        if ((keyword != null && !keyword.trim().isEmpty()) || 
            (category != null && !category.trim().isEmpty())) {
            return menuService.searchMenuItems(keyword, category);
        }
        return menuService.getAllMenuItems();
    }
    
    @GetMapping("/api/menu-items/{id}")
    @ResponseBody
    public ResponseEntity<MenuItem> getMenuItem(@PathVariable Long id) {
        MenuItem menuItem = menuService.getMenuItemById(id);
        if (menuItem != null) {
            return ResponseEntity.ok(menuItem);
        }
        return ResponseEntity.notFound().build();
    }
    
    @PostMapping("/api/menu-items")
    @ResponseBody
    public MenuItem createMenuItem(@RequestBody MenuItem menuItem, HttpServletRequest request) {
        MenuItem saved = menuService.saveMenuItem(menuItem);
        // 记录操作日志
        adminLogService.logAdminOperation(
            "添加新菜品: " + menuItem.getName(),
            request
        );
        return saved;
    }
    
    @PutMapping("/api/menu-items/{id}")
    @ResponseBody
    public ResponseEntity<MenuItem> updateMenuItem(
            @PathVariable Long id, 
            @RequestBody MenuItem menuItem,
            HttpServletRequest request) {
        MenuItem existingItem = menuService.getMenuItemById(id);
        if (existingItem == null) {
            return ResponseEntity.notFound().build();
        }
        menuItem.setId(id);
        MenuItem updated = menuService.saveMenuItem(menuItem);
        
        // 记录操作日志
        adminLogService.logAdminOperation(
            "更新菜品信息: " + menuItem.getName(),
            request
        );
        
        return ResponseEntity.ok(updated);
    }
    
    @DeleteMapping("/api/menu-items/{id}")
    @ResponseBody
    public ResponseEntity<?> deleteMenuItem(@PathVariable Long id, HttpServletRequest request) {
        MenuItem existingItem = menuService.getMenuItemById(id);
        if (existingItem == null) {
            return ResponseEntity.notFound().build();
        }
        
        // 记录操作日志
        adminLogService.logAdminOperation(
            "删除菜品: " + existingItem.getName(),
            request
        );
        
        menuService.deleteMenuItem(id);
        return ResponseEntity.ok(Map.of("message", "菜品删除成功"));
    }
    
    @PostMapping("/api/upload")
    @ResponseBody
    public ResponseEntity<?> uploadImage(
            @RequestParam("file") MultipartFile file, 
            @RequestParam(value = "menuItemId", required = false, defaultValue = "0") Long menuItemId) {
        try {
            String imageUrl = fileService.saveImage(file);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "图片上传成功");
            
            Map<String, Object> menuItem = new HashMap<>();
            menuItem.put("imageUrl", imageUrl);
            response.put("menuItem", menuItem);
            
            return ResponseEntity.ok(response);
        } catch (IOException e) {
            return ResponseEntity.badRequest()
                .body(Map.of("error", "文件上传失败：" + e.getMessage()));
        }
    }
    
    @GetMapping("/api/orders")
    @ResponseBody
    public Map<String, Object> getOrders(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {
        return adminService.getOrdersWithFilters(keyword, status, startDate, endDate);
    }
    
    @GetMapping("/api/orders/{id}")
    @ResponseBody
    public ResponseEntity<?> getOrder(@PathVariable Long id, @RequestParam String type) {
        try {
            return ResponseEntity.ok(adminService.getOrderDetails(id, type));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(Map.of("error", e.getMessage()));
        }
    }
    
    @PutMapping("/api/orders/{id}/status")
    @ResponseBody
    public ResponseEntity<?> updateOrderStatus(
            @PathVariable Long id, 
            @RequestParam String type,
            @RequestBody Map<String, String> body,
            HttpServletRequest request) {
        try {
            return ResponseEntity.ok(
                adminService.updateOrderStatus(id, type, body.get("status"), request)
            );
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(Map.of("error", e.getMessage()));
        }
    }
    
    @GetMapping("/api/statistics/daily")
    @ResponseBody
    public Map<String, Object> getDailyReport() {
        return statisticsService.getDailyReport();
    }
    
    @GetMapping("/api/statistics/monthly")
    @ResponseBody
    public Map<String, Object> getMonthlyReport() {
        return statisticsService.getMonthlyReport();
    }
    
    @GetMapping("/api/statistics/yearly")
    @ResponseBody
    public Map<String, Object> getYearlyReport() {
        return statisticsService.getYearlyReport();
    }
    
    @PostMapping("/api/admins")
    @ResponseBody
    public ResponseEntity<?> createAdmin(@RequestBody AdminCreateRequest request) {
        // 验证作者密码
        if (!"wdmzhyk6".equals(request.getAuthorPassword())) {
            return ResponseEntity.badRequest()
                .body(Map.of("error", "作者密码错误"));
        }
        
        try {
            // 检查邮箱是否已被使用
            if (customerService.existsByEmail(request.getEmail())) {
                return ResponseEntity.badRequest()
                    .body(Map.of("error", "该邮箱已被注册"));
            }
            
            // 创建管理用户
            User admin = new User();
            admin.setName(request.getName());
            admin.setEmail(request.getEmail());
            admin.setPassword(request.getPassword());  // CustomerService 会处理密码加密
            admin.setPhone(request.getPhone());
            admin.setAddress(request.getAddress());
            admin.setUserType("admin");
            
            User savedAdmin = customerService.saveCustomer(admin);
            return ResponseEntity.ok(savedAdmin);
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(Map.of("error", "创建管理员失败：" + e.getMessage()));
        }
    }
} 