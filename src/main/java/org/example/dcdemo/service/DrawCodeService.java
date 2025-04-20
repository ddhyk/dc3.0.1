package org.example.dcdemo.service;

import org.example.dcdemo.model.DrawCode;
import org.example.dcdemo.model.User;
import org.example.dcdemo.repository.DrawCodeRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.Random;

@Service
public class DrawCodeService {
    private final DrawCodeRepository drawCodeRepository;
    private static final Logger logger = LoggerFactory.getLogger(DrawCodeService.class);

    public DrawCodeService(DrawCodeRepository drawCodeRepository) {
        this.drawCodeRepository = drawCodeRepository;
    }

    @Transactional
    public DrawCode generateDrawCode(User user, Long orderId) {
        String code = generateRandomCode();
        DrawCode drawCode = new DrawCode();
        drawCode.setCode(code);
        drawCode.setUser(user);
        drawCode.setOrderId(orderId);
        drawCode.setUsed(false);
        logger.info("Generating draw code for user: {} and orderId: {}", user.getId(), orderId);
        return drawCodeRepository.save(drawCode);
    }

    @Transactional
    public DrawCode verifyAndUseCode(String code, User user) {
        DrawCode drawCode = drawCodeRepository.findByCode(code)
            .orElseThrow(() -> new IllegalArgumentException("无效的抽奖码"));

        if (!drawCode.getUser().getId().equals(user.getId())) {
            throw new IllegalArgumentException("抽奖码不属于当前用户");
        }

        if (drawCode.isUsed()) {
            throw new IllegalArgumentException("抽奖码已使用");
        }

        drawCode.setUsed(true);
        logger.info("Draw code verified and marked as used: {} for user: {}", code, user.getId());
        return drawCodeRepository.save(drawCode);
    }

    private String generateRandomCode() {
        Random random = new Random();
        StringBuilder code = new StringBuilder();
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        
        for (int i = 0; i < 8; i++) {
            code.append(chars.charAt(random.nextInt(chars.length())));
        }
        
        return code.toString();
    }
} 