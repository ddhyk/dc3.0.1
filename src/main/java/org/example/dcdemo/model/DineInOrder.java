package org.example.dcdemo.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonIgnore;

@Data
@EqualsAndHashCode(callSuper = false)
@Entity
@Table(name = "dine_in_orders")
public class DineInOrder {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "table_number")
    private String tableNumber;
    
    @Column(name = "order_items", columnDefinition = "TEXT")
    private String orderItems;
    
    @Column(name = "remarks")
    private String remarks;
    
    @Column(name = "order_time")
    private LocalDateTime orderTime;
    
    @Column(name = "status")
    private String status = "PENDING";
    
    @Column(name = "total")
    private Double total;
    
    @Column(name = "draw_code")
    private String drawCode;
    
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id")
    @JsonIgnore
    private User user;
    
    @OneToMany(mappedBy = "dineInOrder", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderDetail> orderDetails = new ArrayList<>();
    
    public void setOrderDetails(List<OrderDetail> details) {
        this.orderDetails.clear();
        if (details != null) {
            details.forEach(detail -> {
                detail.setDineInOrder(this);
                this.orderDetails.add(detail);
            });
        }
    }
} 