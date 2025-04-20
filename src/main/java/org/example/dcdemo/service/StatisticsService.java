package org.example.dcdemo.service;

import org.example.dcdemo.model.Order;
import org.example.dcdemo.model.DineInOrder;
import org.example.dcdemo.repository.OrderRepository;
import org.example.dcdemo.repository.DineInOrderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.Map;
import java.util.HashMap;
import java.util.List;

@Service
public class StatisticsService {
    
    @Autowired
    private OrderRepository orderRepository;
    
    @Autowired
    private DineInOrderRepository dineInOrderRepository;
    
    public Map<String, Object> getDailyReport() {
        try {
            // 今天的开始和结束时间
            LocalDateTime startOfDay = LocalDate.now().atStartOfDay();
            LocalDateTime endOfDay = startOfDay.plusDays(1);
            
            // 所有订单
            List<Order> takeoutOrders = orderRepository.findByOrderTimeBetween(startOfDay, endOfDay);
            List<DineInOrder> dineInOrders = dineInOrderRepository.findByOrderTimeBetween(startOfDay, endOfDay);
            
            // 总营收和订单
            double totalRevenue = 0;
            Map<Integer, Double> hourlyRevenue = new HashMap<>();
            
            // 初始化
            for (int i = 0; i < 24; i++) {
                hourlyRevenue.put(i, 0.0);
            }
            
            // 统计外卖
            for (Order order : takeoutOrders) {
                if (order.getTotal() != null && 
                    (order.getStatus().equals("COMPLETED") || order.getStatus().equals("PROCESSING"))) {
                    totalRevenue += order.getTotal();
                    int hour = order.getOrderTime().getHour();
                    hourlyRevenue.put(hour, hourlyRevenue.get(hour) + order.getTotal());
                }
            }
            
            // 统计堂食
            for (DineInOrder order : dineInOrders) {
                if (order.getTotal() != null && 
                    (order.getStatus().equals("COMPLETED") || order.getStatus().equals("PROCESSING"))) {
                    totalRevenue += order.getTotal();
                    int hour = order.getOrderTime().getHour();
                    hourlyRevenue.put(hour, hourlyRevenue.get(hour) + order.getTotal());
                }
            }
            
            Map<String, Object> result = new HashMap<>();
            result.put("totalRevenue", totalRevenue);
            result.put("totalOrders", takeoutOrders.size() + dineInOrders.size());
            result.put("hourlyRevenue", hourlyRevenue);
            
            return result;
        } catch (Exception e) {
            Map<String, Object> errorResult = new HashMap<>();
            errorResult.put("totalRevenue", 0.0);
            errorResult.put("totalOrders", 0);
            errorResult.put("hourlyRevenue", new HashMap<>());
            return errorResult;
        }
    }
    
    public Map<String, Object> getMonthlyReport() {

        LocalDateTime startOfMonth = YearMonth.now().atDay(1).atStartOfDay();
        LocalDateTime endOfMonth = YearMonth.now().atEndOfMonth().plusDays(1).atStartOfDay();
        

        List<Order> takeoutOrders = orderRepository.findByOrderTimeBetween(startOfMonth, endOfMonth);
        List<DineInOrder> dineInOrders = dineInOrderRepository.findByOrderTimeBetween(startOfMonth, endOfMonth);
        

        double totalRevenue = 0;
        Map<Integer, Double> dailyRevenue = new HashMap<>();
        

        for (int i = 1; i <= YearMonth.now().lengthOfMonth(); i++) {
            dailyRevenue.put(i, 0.0);
        }
        

        for (Order order : takeoutOrders) {
            if (order.getTotal() != null && (order.getStatus().equals("COMPLETED") || order.getStatus().equals("PROCESSING"))) {
                totalRevenue += order.getTotal();
                int day = order.getOrderTime().getDayOfMonth();
                dailyRevenue.put(day, dailyRevenue.get(day) + order.getTotal());
            }
        }
        

        for (DineInOrder order : dineInOrders) {
            if (order.getTotal() != null && (order.getStatus().equals("COMPLETED") || order.getStatus().equals("PROCESSING"))) {
                totalRevenue += order.getTotal();
                int day = order.getOrderTime().getDayOfMonth();
                dailyRevenue.put(day, dailyRevenue.get(day) + order.getTotal());
            }
        }
        
        Map<String, Object> result = new HashMap<>();
        result.put("totalRevenue", totalRevenue);
        result.put("totalOrders", takeoutOrders.size() + dineInOrders.size());
        result.put("dailyRevenue", dailyRevenue);
        
        return result;
    }
    
    public Map<String, Object> getYearlyReport() {

        int year = LocalDate.now().getYear();
        LocalDateTime startOfYear = LocalDate.of(year, 1, 1).atStartOfDay();
        LocalDateTime endOfYear = LocalDate.of(year, 12, 31).plusDays(1).atStartOfDay();
        

        List<Order> takeoutOrders = orderRepository.findByOrderTimeBetween(startOfYear, endOfYear);
        List<DineInOrder> dineInOrders = dineInOrderRepository.findByOrderTimeBetween(startOfYear, endOfYear);
        

        double totalRevenue = 0;
        Map<Integer, Double> monthlyRevenue = new HashMap<>();
        

        for (int i = 1; i <= 12; i++) {
            monthlyRevenue.put(i, 0.0);
        }
        

        for (Order order : takeoutOrders) {
            if (order.getTotal() != null && (order.getStatus().equals("COMPLETED") || order.getStatus().equals("PROCESSING"))) {
                totalRevenue += order.getTotal();
                int month = order.getOrderTime().getMonthValue();
                monthlyRevenue.put(month, monthlyRevenue.get(month) + order.getTotal());
            }
        }
        

        for (DineInOrder order : dineInOrders) {
            if (order.getTotal() != null && (order.getStatus().equals("COMPLETED") || order.getStatus().equals("PROCESSING"))) {
                totalRevenue += order.getTotal();
                int month = order.getOrderTime().getMonthValue();
                monthlyRevenue.put(month, monthlyRevenue.get(month) + order.getTotal());
            }
        }
        
        Map<String, Object> result = new HashMap<>();
        result.put("totalRevenue", totalRevenue);
        result.put("totalOrders", takeoutOrders.size() + dineInOrders.size());
        result.put("monthlyRevenue", monthlyRevenue);
        
        return result;
    }
} 