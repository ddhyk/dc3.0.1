package org.example.dcdemo.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Data
public class DashboardData {
    private int todayOrderCount;
    private int activeUserCount;
    private int newUserCount;
    private BigDecimal totalRevenue;
    private double orderGrowthRate;
    private double userGrowthRate;
    private double newUserGrowthRate;
    private double revenueGrowthRate;
    
    private List<Map<String, Object>> topSellingDishes;
    private List<Map<String, Object>> userActivityTrend;
} 