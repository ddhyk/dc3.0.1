package org.example.dcdemo.service;

import org.example.dcdemo.model.Operation;
import org.example.dcdemo.repository.OperationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;

@Service
public class OperationStaffService {
    @Autowired
    private OperationRepository operationRepository;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    public Operation createStaff(Operation staff) {
        if (operationRepository.existsByEmail(staff.getEmail())) {
            throw new RuntimeException("邮箱已被使用");
        }

        String rawPassword = staff.getPassword();
        String encodedPassword = passwordEncoder.encode(rawPassword);
        System.out.println("Creating staff - Raw password: " + rawPassword);
        System.out.println("Encoded password: " + encodedPassword);
        
        staff.setPassword(encodedPassword);
        staff.setCreateTime(LocalDateTime.now());
        staff.setIsActive(true);
        
        return operationRepository.save(staff);
    }

    public Operation findByEmail(String email) {
        return operationRepository.findByEmail(email);
    }
} 