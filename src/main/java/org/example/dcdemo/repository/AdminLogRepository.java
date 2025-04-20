package org.example.dcdemo.repository;

import org.example.dcdemo.model.AdminLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AdminLogRepository extends JpaRepository<AdminLog, Long> {
    List<AdminLog> findTop10ByOrderByOperationTimeDesc();
} 