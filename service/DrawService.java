package org.example.dcdemo.service;

import org.example.dcdemo.model.DrawCode;
import org.example.dcdemo.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class DrawService {
    @Autowired
    private DrawCodeService drawCodeService;

    public Map<String, Object> generateDrawCodeForOrder(User user, Long orderId) {
        DrawCode drawCode = drawCodeService.generateDrawCode(user, orderId);
        return Map.of(
            "success", true,
            "code", drawCode.getCode()
        );
    }

    public Map<String, Object> verifyDrawCode(User user, String code) {
        if (code == null || code.trim().isEmpty()) {
            throw new IllegalArgumentException("抽奖码不能为空");
        }

        drawCodeService.verifyAndUseCode(code, user);
        return Map.of(
            "success", true,
            "message", "抽奖码验证成功"
        );
    }
} 