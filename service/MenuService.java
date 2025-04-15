package org.example.dcdemo.service;

import org.example.dcdemo.model.MenuItem;
import org.example.dcdemo.repository.MenuItemRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class MenuService {
    
    @Autowired
    private MenuItemRepository menuItemRepository;
    
    public List<MenuItem> getAllMenuItems() {
        System.out.println("Fetching all menu items...");
        List<MenuItem> items = menuItemRepository.findAll();
        System.out.println("Found " + items.size() + " items in database");
        return items;
    }
    
    public List<MenuItem> searchMenuItems(String keyword, String category) {
        if (category != null && !category.isEmpty()) {
            return menuItemRepository.findByCategoryAndNameContaining(category, keyword);
        }
        return menuItemRepository.findByNameContaining(keyword);
    }
    
    public MenuItem getMenuItemById(Long id) {
        return menuItemRepository.findById(id).orElse(null);
    }
    
    @Transactional
    public MenuItem saveMenuItem(MenuItem menuItem) {
        return menuItemRepository.save(menuItem);
    }
    
    @Transactional
    public void deleteMenuItem(Long id) {
        menuItemRepository.deleteById(id);
    }
} 