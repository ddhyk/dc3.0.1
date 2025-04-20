package org.example.dcdemo.service;

import org.example.dcdemo.repository.OrderRepository;
import org.example.dcdemo.repository.DineInOrderRepository;
import org.example.dcdemo.model.Order;
import org.example.dcdemo.model.DineInOrder;
import org.example.dcdemo.model.MenuItem;
import org.example.dcdemo.model.OrderDetail;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import jakarta.servlet.http.HttpServletRequest;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class AdminService {
    private static final Logger logger = LoggerFactory.getLogger(AdminService.class);
    
    @Autowired
    private OrderRepository orderRepository;
    
    @Autowired
    private DineInOrderRepository dineInOrderRepository;
    
    @Autowired
    private OrderService orderService;
    
    @Autowired
    private AdminLogService adminLogService;
    
    @Autowired
    private MenuService menuService;

    public List<MenuItem> getAllMenuItems() {
        return menuService.getAllMenuItems();
    }

    public Map<String, Object> getOrdersWithFilters(
            String keyword, String status, String startDate, String endDate) {
        List<Map<String, Object>> allOrders = new ArrayList<>();
        
        // 获取订单列表
        List<Order> takeoutOrders;
        List<DineInOrder> dineInOrders;
        
        if (startDate != null && !startDate.isEmpty()) {
            LocalDateTime start = LocalDate.parse(startDate).atStartOfDay();
            LocalDateTime end = endDate != null && !endDate.isEmpty() 
                ? LocalDate.parse(endDate).atTime(23, 59, 59)
                : LocalDateTime.now();
                
            if (status != null && !status.isEmpty()) {
                takeoutOrders = orderRepository.findByStatusAndOrderTimeBetween(status, start, end);
                dineInOrders = dineInOrderRepository.findByStatusAndOrderTimeBetween(status, start, end);
            } else {
                takeoutOrders = orderRepository.findByOrderTimeBetween(start, end);
                dineInOrders = dineInOrderRepository.findByOrderTimeBetween(start, end);
            }
        } else {
            if (status != null && !status.isEmpty()) {
                takeoutOrders = orderRepository.findByStatusOrderByStatusAscOrderTimeDesc(status);
                dineInOrders = dineInOrderRepository.findByStatusOrderByOrderTimeDesc(status);
            } else {
                takeoutOrders = orderRepository.findAllByOrderByStatusAscOrderTimeDesc();
                dineInOrders = dineInOrderRepository.findAllByOrderByStatusAscOrderTimeDesc();
            }
        }
        
        // 合并订单并转换格式
        allOrders.addAll(convertTakeoutOrders(takeoutOrders, keyword));
        allOrders.addAll(convertDineInOrders(dineInOrders, keyword));
        
        // 排序
        sortOrders(allOrders);
        
        return Map.of("orders", allOrders);
    }

    private List<Map<String, Object>> convertTakeoutOrders(List<Order> orders, String keyword) {
        List<Map<String, Object>> result = new ArrayList<>();
        for (Order order : orders) {
            if (matchesKeyword(order, keyword)) {
                Map<String, Object> orderMap = new HashMap<>();
                orderMap.put("id", order.getId());
                orderMap.put("type", "TAKEOUT");
                orderMap.put("customerInfo", order.getCustomerName());
                orderMap.put("contact", order.getContactPhone());
                orderMap.put("address", order.getDeliveryAddress());
                orderMap.put("total", order.getTotal());
                orderMap.put("status", order.getStatus());
                orderMap.put("orderTime", order.getOrderTime());
                orderMap.put("remarks", order.getRemarks());
                orderMap.put("orderItems", order.getOrderItems());
                result.add(orderMap);
            }
        }
        return result;
    }

    private List<Map<String, Object>> convertDineInOrders(List<DineInOrder> orders, String keyword) {
        List<Map<String, Object>> result = new ArrayList<>();
        for (DineInOrder order : orders) {
            if (matchesKeyword(order, keyword)) {
                Map<String, Object> orderMap = new HashMap<>();
                orderMap.put("id", order.getId());
                orderMap.put("type", "DINE_IN");
                orderMap.put("customerInfo", "桌号: " + order.getTableNumber());
                orderMap.put("contact", "-");
                orderMap.put("address", order.getTableNumber());
                orderMap.put("total", order.getTotal());
                orderMap.put("status", order.getStatus());
                orderMap.put("orderTime", order.getOrderTime());
                orderMap.put("remarks", order.getRemarks());
                orderMap.put("orderItems", convertOrderDetails(order.getOrderDetails()));
                result.add(orderMap);
            }
        }
        return result;
    }

    private List<Map<String, Object>> convertOrderDetails(List<OrderDetail> details) {
        return details.stream()
            .map(detail -> {
                Map<String, Object> detailMap = new HashMap<>();
                detailMap.put("menuItemName", detail.getMenuItem().getName());
                detailMap.put("quantity", detail.getQuantity());
                detailMap.put("price", detail.getPrice());
                detailMap.put("subtotal", detail.getSubtotal());
                return detailMap;
            })
            .collect(Collectors.toList());
    }

    private void sortOrders(List<Map<String, Object>> orders) {
        orders.sort((a, b) -> {
            String statusA = (String) a.get("status");
            String statusB = (String) b.get("status");
            if (statusA.equals(statusB)) {
                LocalDateTime timeA = (LocalDateTime) a.get("orderTime");
                LocalDateTime timeB = (LocalDateTime) b.get("orderTime");
                return timeB.compareTo(timeA);
            }
            if (statusA.equals("PENDING")) return -1;
            if (statusB.equals("PENDING")) return 1;
            return statusA.compareTo(statusB);
        });
    }

    private boolean matchesKeyword(Order order, String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return true;
        }
        keyword = keyword.toLowerCase();
        return (order.getCustomerName() != null && order.getCustomerName().toLowerCase().contains(keyword)) ||
               (order.getContactPhone() != null && order.getContactPhone().contains(keyword));
    }

    private boolean matchesKeyword(DineInOrder order, String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return true;
        }
        keyword = keyword.toLowerCase();
        return order.getTableNumber().toLowerCase().contains(keyword);
    }

    public Object updateOrderStatus(Long id, String type, String newStatus, 
                                  HttpServletRequest request) {
        // 验证参数
        if (newStatus == null || newStatus.trim().isEmpty()) {
            logger.warn("状态为空: id={}, type={}", id, type);
            throw new IllegalArgumentException("状态不能为空");
        }

        if (type == null || type.trim().isEmpty()) {
            logger.warn("订单类型为空: id={}", id);
            throw new IllegalArgumentException("订单类型不能为空");
        }

        logger.info("正在更新订单状态: id={}, type={}, newStatus={}", id, type, newStatus);

        try {
            if ("TAKEOUT".equalsIgnoreCase(type)) {
                Order order = orderService.updateOrderStatus(id, newStatus);
                adminLogService.logAdminOperation(
                    "更新外卖订单状态: #" + order.getId() + " -> " + newStatus,
                    request
                );
                logger.info("外卖订单状态更新成功: id={}, newStatus={}", id, newStatus);
                return order;
            } else if ("DINE_IN".equalsIgnoreCase(type)) {
                DineInOrder order = orderService.updateDineInOrderStatus(id, newStatus);
                adminLogService.logAdminOperation(
                    "更新堂食订单状态: #" + order.getId() + " -> " + newStatus,
                    request
                );
                logger.info("堂食订单状态更新成功: id={}, newStatus={}", id, newStatus);
                return order;
            } else {
                logger.warn("无效的订单类型: id={}, type={}", id, type);
                throw new IllegalArgumentException("无效的订单类型: " + type);
            }
        } catch (Exception e) {
            logger.error("更新订单状态失败: id={}, type={}, error={}", id, type, e.getMessage(), e);
            throw e;
        }
    }

    public Map<String, Object> getOrderDetails(Long id, String type) {
        try {
            if ("takeout".equals(type)) {
                Order order = orderRepository.findById(id)
                    .orElseThrow(() -> new IllegalArgumentException("订单不存在"));
                    
                return Map.of(
                    "id", order.getId(),
                    "type", "TAKEOUT",
                    "customerName", order.getCustomerName(),
                    "contactPhone", order.getContactPhone(),
                    "deliveryAddress", order.getDeliveryAddress(),
                    "orderTime", order.getOrderTime(),
                    "status", order.getStatus(),
                    "total", order.getTotal(),
                    "remarks", order.getRemarks(),
                    "orderItems", order.getOrderItems()
                );
            } else {
                DineInOrder order = dineInOrderRepository.findById(id)
                    .orElseThrow(() -> new IllegalArgumentException("订单不存在"));
            
                // 转换订单详情为前端需要的格式
                List<Map<String, Object>> orderDetails = order.getOrderDetails().stream()
                    .map(detail -> {
                        Map<String, Object> detailMap = new HashMap<>();
                        detailMap.put("menuItemName", detail.getMenuItem().getName());
                        detailMap.put("quantity", detail.getQuantity());
                        detailMap.put("price", detail.getPrice());
                        detailMap.put("subtotal", detail.getSubtotal());
                        return detailMap;
                    })
                    .collect(Collectors.toList());
                    
                return Map.of(
                    "id", order.getId(),
                    "type", "DINE_IN", 
                    "tableNumber", order.getTableNumber(),
                    "orderTime", order.getOrderTime(),
                    "status", order.getStatus(),
                    "total", order.getTotal(),
                    "remarks", order.getRemarks(),
                    "orderDetails", orderDetails  // 使用转换后的订单详情
                );
            }
        } catch (Exception e) {
            throw new RuntimeException("获取订单信息失败：" + e.getMessage());
        }
    }
} 