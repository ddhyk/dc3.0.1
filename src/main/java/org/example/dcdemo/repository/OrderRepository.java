package org.example.dcdemo.repository;

import org.example.dcdemo.model.Order;
import org.example.dcdemo.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    List<Order> findAllByOrderByStatusAscOrderTimeDesc();

    List<Order> findByTotalIsNull();

    List<Order> findByUserOrderByOrderTimeDesc(User user);
    List<Order> findAllByOrderByOrderTimeDesc();
    List<Order> findByStatusOrderByStatusAscOrderTimeDesc(String status);
    List<Order> findByStatusAndOrderTimeBetween(String status, LocalDateTime start, LocalDateTime end);
    List<Order> findByOrderTimeBetween(LocalDateTime start, LocalDateTime end);
    int countByOrderTimeBetween(LocalDateTime start, LocalDateTime end);
    
    @Query("SELECT SUM(o.total) FROM Order o WHERE o.orderTime BETWEEN :start AND :end")
    BigDecimal sumRevenueByDate(LocalDateTime start, LocalDateTime end);
    
    @Query(value = "SELECT mi.name as dishName, COUNT(*) as salesCount " +
           "FROM order_details od " +
           "JOIN menu_items mi ON od.menu_item_id = mi.id " +
           "GROUP BY mi.id, mi.name " +
           "ORDER BY salesCount DESC " +
           "LIMIT :limit", nativeQuery = true)
    List<Map<String, Object>> findTopSellingDishes(int limit);
    
    @Query(value = "SELECT DATE(o.order_time) as date, COUNT(DISTINCT o.user_id) as userCount " +
           "FROM orders o " +
           "WHERE o.order_time >= DATE_SUB(CURRENT_DATE, INTERVAL :days DAY) " +
           "GROUP BY DATE(o.order_time) " +
           "ORDER BY date", nativeQuery = true)
    List<Map<String, Object>> findUserActivityTrend(int days);

}