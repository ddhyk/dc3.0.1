package org.example.dcdemo.model;

import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;
@Data
@Entity
@Table(name = "menu_items")
public class MenuItem {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private String name;
    
    private String category;
    
    private BigDecimal price;
    
    @Column(length = 1000)
    private String description;
    
    private String imageUrl;
} 