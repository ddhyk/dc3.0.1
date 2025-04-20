package org.example.dcdemo.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Data
@Table(name = "admin_logs")
public class AdminLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private LocalDateTime operationTime;
    private String adminUsername;
    private String ipAddress;
    private String operation;
} 