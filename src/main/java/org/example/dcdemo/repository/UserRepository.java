package org.example.dcdemo.repository;

import org.example.dcdemo.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);

    List<User> findByUserType(String userType);
    List<User> findByUserTypeAndNameContainingOrEmailContainingOrPhoneContaining(
        String userType, String name, String email, String phone);
    
    @Query("SELECT COUNT(DISTINCT o.user) FROM Order o WHERE o.orderTime >= :since")
    int countActiveUsersSince(LocalDateTime since);
    
    // 统计新注册的用户数
    @Query("SELECT COUNT(u) FROM User u WHERE u.createTime >= :startTime AND u.createTime < :endTime")
    int countNewUsersBetween(LocalDateTime startTime, LocalDateTime endTime);
} 