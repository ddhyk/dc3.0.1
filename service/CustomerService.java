package org.example.dcdemo.service;

import org.example.dcdemo.model.User;
import org.example.dcdemo.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
public class CustomerService {
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    public List<User> getAllCustomers() {
        return userRepository.findByUserType("customer");
    }
    
    public List<User> searchCustomers(String keyword) {
        return userRepository.findByUserTypeAndNameContainingOrEmailContainingOrPhoneContaining(
            "customer", keyword, keyword, keyword);
    }
    
    public User getCustomerById(Long id) {
        return userRepository.findById(id).orElse(null);
    }
    
    public User saveCustomer(User customer) {
        if (customer.getId() == null) {
            customer.setCreateTime(LocalDateTime.now());
        }
        customer.setUpdateTime(LocalDateTime.now());

        if (customer.getPassword() == null || customer.getPassword().trim().isEmpty()) {
            throw new IllegalArgumentException("密码不能为空");
        }
        
        // 加密
        customer.setPassword(passwordEncoder.encode(customer.getPassword()));
        
        return userRepository.save(customer);
    }
    
    public User updateCustomer(Long id, User customerDetails) {
        User customer = userRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Customer not found"));
        
        customer.setName(customerDetails.getName());
        customer.setEmail(customerDetails.getEmail());
        customer.setPhone(customerDetails.getPhone());
        customer.setAddress(customerDetails.getAddress());
        customer.setUpdateTime(LocalDateTime.now());
        
        // 只有当提供了新密码时才更新密码
        if (customerDetails.getPassword() != null && !customerDetails.getPassword().isEmpty()) {
            customer.setPassword(passwordEncoder.encode(customerDetails.getPassword()));
        }
        
        return userRepository.save(customer);
    }
    
    @Transactional
    public void deleteCustomer(Long id) {
        User user = userRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("用户不存在"));
            
        // 清除关联数据
        user.getCartItems().clear();
        user.getOrders().clear();
        user.getDineInOrders().clear();
        
        userRepository.delete(user);
    }
    
    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }
} 