package org.example.dcdemo.service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.time.LocalDate;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import org.example.dcdemo.dto.DashboardData;
import org.example.dcdemo.model.AdminLog;
import org.example.dcdemo.repository.OrderRepository;
import org.example.dcdemo.repository.UserRepository;
import org.example.dcdemo.repository.DineInOrderRepository;
import org.example.dcdemo.repository.AdminLogRepository;
import org.springframework.ui.Model;

@Service
public class DashboardService {
    @Autowired
    private OrderRepository orderRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private DineInOrderRepository dineInOrderRepository;
    
    @Autowired
    private AdminLogRepository adminLogRepository;

    public void populateDashboardModel(Model model) {
        DashboardData dashboardData = getDashboardData();
        model.addAttribute("dashboardData", dashboardData);
        
        List<AdminLog> adminLogs = adminLogRepository.findTop10ByOrderByOperationTimeDesc();
        model.addAttribute("adminLogs", adminLogs);
    }

    public DashboardData getDashboardData() {
        DashboardData data = new DashboardData();
        LocalDateTime today = LocalDate.now().atStartOfDay();
        LocalDateTime yesterday = today.minusDays(1);
        
        // 今日订单数
        int todayOrders = orderRepository.countByOrderTimeBetween(today, LocalDateTime.now()) +
                          dineInOrderRepository.countByOrderTimeBetween(today, LocalDateTime.now());
        int yesterdayOrders = orderRepository.countByOrderTimeBetween(yesterday, today) +
                              dineInOrderRepository.countByOrderTimeBetween(yesterday, today);
        data.setTodayOrderCount(todayOrders);
        data.setOrderGrowthRate(calculateGrowthRate(yesterdayOrders, todayOrders));
        
        // 今日新增用户数
        int todayNewUsers = userRepository.countNewUsersBetween(today, LocalDateTime.now());
        int yesterdayNewUsers = userRepository.countNewUsersBetween(yesterday, today);
        data.setNewUserCount(todayNewUsers);
        data.setNewUserGrowthRate(calculateGrowthRate(yesterdayNewUsers, todayNewUsers));
        
        // 活跃用户数（30天内有下单的用户）
        LocalDateTime thirtyDaysAgo = today.minusDays(30);
        int activeUsers = userRepository.countActiveUsersSince(thirtyDaysAgo);
        int previousActiveUsers = userRepository.countActiveUsersSince(thirtyDaysAgo.minusDays(30));
        data.setActiveUserCount(activeUsers);
        data.setUserGrowthRate(calculateGrowthRate(previousActiveUsers, activeUsers));
        
        // 总营业额
        BigDecimal todayRevenue = orderRepository.sumRevenueByDate(today, LocalDateTime.now());
        BigDecimal todayDineInRevenue = dineInOrderRepository.sumRevenueByDate(today, LocalDateTime.now());
        BigDecimal yesterdayRevenue = orderRepository.sumRevenueByDate(yesterday, today);
        BigDecimal yesterdayDineInRevenue = dineInOrderRepository.sumRevenueByDate(yesterday, today);
        
        // 处理null值
        todayRevenue = todayRevenue != null ? todayRevenue : BigDecimal.ZERO;
        todayDineInRevenue = todayDineInRevenue != null ? todayDineInRevenue : BigDecimal.ZERO;
        yesterdayRevenue = yesterdayRevenue != null ? yesterdayRevenue : BigDecimal.ZERO;
        yesterdayDineInRevenue = yesterdayDineInRevenue != null ? yesterdayDineInRevenue : BigDecimal.ZERO;
        
        // 计算总收入
        BigDecimal totalTodayRevenue = todayRevenue.add(todayDineInRevenue);
        BigDecimal totalYesterdayRevenue = yesterdayRevenue.add(yesterdayDineInRevenue);
        
        data.setTotalRevenue(totalTodayRevenue);
        data.setRevenueGrowthRate(calculateGrowthRate(totalYesterdayRevenue, totalTodayRevenue));
        
        // 获取热门菜品数据
        List<Map<String, Object>> topDishes = orderRepository.findTopSellingDishes(10);
        data.setTopSellingDishes(topDishes);
        
        // 获取用户活跃度趋势
        List<Map<String, Object>> activityTrend = orderRepository.findUserActivityTrend(7);
        data.setUserActivityTrend(activityTrend);
        
        return data;
    }
    
    private double calculateGrowthRate(Number previous, Number current) {
        if (previous == null || current == null) {
            return 0.0;
        }
        if (previous.doubleValue() == 0) {
            return current.doubleValue() > 0 ? 100.0 : 0.0;
        }
        return (current.doubleValue() - previous.doubleValue()) / previous.doubleValue() * 100;
    }
} 