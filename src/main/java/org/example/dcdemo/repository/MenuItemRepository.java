package org.example.dcdemo.repository;

import org.example.dcdemo.model.MenuItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MenuItemRepository extends JpaRepository<MenuItem, Long> {
    List<MenuItem> findByNameContaining(String keyword);
    List<MenuItem> findByCategoryAndNameContaining(String category, String keyword);
} 