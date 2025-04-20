package org.example.dcdemo.repository;

import org.example.dcdemo.model.DineInOrder;
import org.example.dcdemo.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface DineInOrderRepository extends JpaRepository<DineInOrder, Long> {
    List<DineInOrder> findByUserOrderByOrderTimeDesc(User user);
    List<DineInOrder> findByStatusOrderByOrderTimeDesc(String status);
    List<DineInOrder> findByOrderTimeBetween(LocalDateTime startOfDay, LocalDateTime endOfDay);
    List<DineInOrder> findByStatusAndOrderTimeBetween(String status, LocalDateTime start, LocalDateTime end);
    List<DineInOrder> findAllByOrderByStatusAscOrderTimeDesc();
    int countByOrderTimeBetween(LocalDateTime start, LocalDateTime end);

    @Query("SELECT SUM(d.total) FROM DineInOrder d WHERE d.orderTime BETWEEN :start AND :end")
    BigDecimal sumRevenueByDate(LocalDateTime start, LocalDateTime end);
} 