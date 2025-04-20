package org.example.dcdemo.repository;

import org.example.dcdemo.model.Operation;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OperationRepository extends JpaRepository<Operation, Long> {
    boolean existsByEmail(String email);
    Operation findByEmail(String email);
} 