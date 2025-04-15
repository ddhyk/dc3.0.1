package org.example.dcdemo.service;

import org.example.dcdemo.model.AdminLog;
import org.example.dcdemo.repository.AdminLogRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import java.time.LocalDateTime;
import java.util.StringTokenizer;

@Service
public class AdminLogService {
    
    @Autowired
    private AdminLogRepository adminLogRepository;
    
    public void logAdminOperation(String operation, HttpServletRequest request) {
        AdminLog log = new AdminLog();
        log.setOperationTime(LocalDateTime.now());
        
        // 从session中获取管理员邮箱
        HttpSession session = request.getSession();
        String adminEmail = (String) session.getAttribute("userEmail");
        log.setAdminUsername(adminEmail != null ? adminEmail : "未知用户");
        
        // 获取客户端IP地址
        String ipAddress = getClientIpAddress(request);
        log.setIpAddress(ipAddress);
        
        // 设置操作内容
        log.setOperation(operation);
        
        // 保存日志
        adminLogRepository.save(log);
    }
    
    private String getClientIpAddress(HttpServletRequest request) {
        String xForwardedForHeader = request.getHeader("X-Forwarded-For");
        
        if (xForwardedForHeader != null && !xForwardedForHeader.isEmpty()) {
            StringTokenizer tokenizer = new StringTokenizer(xForwardedForHeader, ",");
            if (tokenizer.hasMoreTokens()) {
                return tokenizer.nextToken().trim();
            }
        }
        
        String[] headers = {
            "Proxy-Client-IP",
            "WL-Proxy-Client-IP",
            "HTTP_CLIENT_IP",
            "HTTP_X_FORWARDED_FOR"
        };
        
        for (String header : headers) {
            String ip = request.getHeader(header);
            if (ip != null && !ip.isEmpty() && !"unknown".equalsIgnoreCase(ip)) {
                return ip.trim();
            }
        }
        
        String remoteAddr = request.getRemoteAddr();
        if ("0:0:0:0:0:0:0:1".equals(remoteAddr)) {
            return "127.0.0.1";
        }
        return remoteAddr;
    }
} 