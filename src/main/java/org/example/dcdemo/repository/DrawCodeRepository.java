package org.example.dcdemo.repository;

import org.example.dcdemo.model.DrawCode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface DrawCodeRepository extends JpaRepository<DrawCode, Long> {
    Optional<DrawCode> findByCode(String code);
}