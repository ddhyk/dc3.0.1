package org.example.dcdemo.dto;

import lombok.Data;

@Data
public class AdminCreateRequest {
    private String name;
    private String email;
    private String password;
    private String phone;
    private String address;
    private String authorPassword;  // 作者密码
} 