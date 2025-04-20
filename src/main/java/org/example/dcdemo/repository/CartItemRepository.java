package org.example.dcdemo.repository;

import org.example.dcdemo.model.CartItem;
import org.example.dcdemo.model.MenuItem;
import org.example.dcdemo.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CartItemRepository extends JpaRepository<CartItem, Long> {
    List<CartItem> findByUser(User user);
    void deleteByUser(User user);
    long countByUser(User user);
    List<CartItem> findByUserAndMenuItem(User user, MenuItem menuItem);
} 