package org.example.dcdemo.model;

import jakarta.persistence.*;
import lombok.Data;
import com.fasterxml.jackson.annotation.JsonIgnore;

@Data
@Entity
public class CartItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JsonIgnore
    private User user;

    @ManyToOne(fetch = FetchType.EAGER)
    private MenuItem menuItem;

    private Integer quantity;
    private Double subtotal;
} 