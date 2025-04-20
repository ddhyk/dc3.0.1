package org.example.dcdemo.service;

import org.example.dcdemo.model.User;
import org.example.dcdemo.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public User findByEmail(String email) {
        return userRepository.findByEmail(email)
            .orElse(null);
    }

    public void saveUser(User user) {
        // 确保密码被正确加密
        if (!user.getPassword().startsWith("$2a$")) {  // 检查是否已经是BCrypt加密
            user.setPassword(passwordEncoder.encode(user.getPassword()));
        }
        userRepository.save(user);
    }
} 