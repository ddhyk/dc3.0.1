package org.example.dcdemo.model;

import jakarta.persistence.*;
import lombok.Data;
import com.fasterxml.jackson.annotation.JsonIgnore;

@Data
@Entity
@Table(name = "order_details")
public class OrderDetail {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne
    @JoinColumn(name = "menu_item_id")
    private MenuItem menuItem;
    
    private Integer quantity;
    private Double price;
    private Double subtotal;
    
    @ManyToOne
    @JoinColumn(name = "dine_in_order_id")
    @JsonIgnore
    private DineInOrder dineInOrder;
} 