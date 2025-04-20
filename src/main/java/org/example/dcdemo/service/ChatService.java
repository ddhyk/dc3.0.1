package org.example.dcdemo.service;

import org.example.dcdemo.config.DoubaoConfig;
import org.example.dcdemo.model.*;
import org.example.dcdemo.repository.MenuItemRepository;
import org.example.dcdemo.repository.CartItemRepository;
import org.springframework.stereotype.Service;
import com.volcengine.ark.runtime.service.ArkService;
import com.volcengine.ark.runtime.model.completion.chat.ChatMessage;
import com.volcengine.ark.runtime.model.completion.chat.ChatMessageRole;
import com.volcengine.ark.runtime.model.completion.chat.ChatCompletionRequest;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import jakarta.servlet.http.HttpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.regex.*;
import java.util.stream.Collectors;
import java.math.BigDecimal;
import java.io.IOException;

@Service
public class ChatService {
    private static final Logger logger = LoggerFactory.getLogger(ChatService.class);
    private final ArkService arkService;
    private final DoubaoConfig doubaoConfig;
    private final MenuItemRepository menuItemRepository;
    private final CartItemRepository cartItemRepository;
    private final OrderService orderService;

    private final Object orderLock = new Object();
    private volatile boolean isOrderSubmitting = false;

    public ChatService(DoubaoConfig doubaoConfig,
                       MenuItemRepository menuItemRepository,
                       CartItemRepository cartItemRepository,
                       OrderService orderService) {
        this.doubaoConfig = doubaoConfig;
        this.menuItemRepository = menuItemRepository;
        this.cartItemRepository = cartItemRepository;
        this.orderService = orderService;
        this.arkService = new ArkService(doubaoConfig.getApiKey());
    }

    private User getCurrentUser() {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes != null) {
            HttpSession session = attributes.getRequest().getSession(false);
            if (session != null) {
                return (User) session.getAttribute("user");
            }
        }
        return null;
    }

    private void addToCart(String dishName, User user) {
        if (user == null) {
            return;
        }

        // 查找菜品
        List<MenuItem> menuItems = menuItemRepository.findByNameContaining(dishName);
        if (!menuItems.isEmpty()) {
            MenuItem menuItem = menuItems.get(0);
            
            // 创建购物车项
            CartItem cartItem = new CartItem();
            cartItem.setUser(user);
            cartItem.setMenuItem(menuItem);
            cartItem.setQuantity(1);
            cartItem.setSubtotal(menuItem.getPrice().multiply(BigDecimal.valueOf(1)).doubleValue());
            
            cartItemRepository.save(cartItem);
        }
    }

    public SseEmitter chatStream(String userMessage) {SseEmitter emitter = new SseEmitter();

        User currentUser = getCurrentUser();
        if (currentUser == null) {
            try {
                emitter.send(SseEmitter.event()
                    .name("error")
                    .data("请先登录后再使用聊天功能")
                    .build());
                emitter.complete();
            } catch (Exception e) {
            }
            return emitter;
        }


        if (userMessage == null || userMessage.trim().isEmpty()) {
            try {
                emitter.complete();
            } catch (Exception e) {
            }
            return emitter;
        }
        

        List<MenuItem> menuItems = menuItemRepository.findAll();
        String menuInfo = menuItems.stream()
                .map(item -> String.format("- %s: %s, 价格：￥%.2f, 描述：%s",
                        item.getName(),
                        item.getCategory(),
                        item.getPrice(),
                        item.getDescription()))
                .collect(Collectors.joining("\n"));

        List<ChatMessage> messages = new ArrayList<>();
        messages.add(ChatMessage.builder()
                .role(ChatMessageRole.SYSTEM)
                .content("#SYSTEM_INIT#你是一个专业的餐厅点餐助手，可以帮助顾客推荐菜品和解答关于菜品的问题。" +
                        "以下是我们餐厅的完整菜单信息，请基于这些信息来回答客人的问题。\n\n" +
                        "回答要求：\n" +
                        "1. 使用优雅的中文回答\n" +
                        "2. 分段落组织内容，每个段落之间用换行分隔\n" +
                        "3. 提到菜品时，使用以下格式：\n" +
                        "   - 菜品名：使用**加粗**\n" +
                        "   - 价格：￥xx.xx\n" +
                        "   - 描述：使用普通文本\n" +
                        "4. 重要信息使用*斜体*标注\n" +
                        "5. 如果是列举多个菜品，使用换行和序号标注\n" +
                        "6. 只有当客户确认表达想吃哪个或者点哪个菜时，才在回答的最后加上#AUTO_ADD#标记\n" +
                        "7. 当客户要求清空购物车时，在回答的最后加上#CLEAR_CART#标记\n" +
                        "8. 当客户询问购物车内容时，只发送#CHECK_CART#标记\n" +
                        "订单处理规则：\n" +
                        "1. 当添加菜品到购物车后，主动询问客户是否要提交订单\n" +
                        "2. 当客户表示同意提交订单时（说'是'、'好'、'可以'等），立即询问'请问您是堂食还是外卖？'\n" +
                        "3. 当客户明确表示'堂食'时，立即询问'请问您的桌号是多少？'\n" +
                        "4. 当客户表示'外卖'时，回复'为了保护个人隐私，外卖订单只支持手动下单哦~'\n" +
                        "5. 当客户具体桌号时（包括纯数字）（例如说'11'、'45'、'15号'或'12桌'），使用#TABLE_NUMBER=数字#标记提交订单\n\n" +
                        "菜单信息：\n" + menuInfo)
                .build());
                
        messages.add(ChatMessage.builder()
                .role(ChatMessageRole.USER)
                .content(userMessage)
                .build());

        ChatCompletionRequest request = ChatCompletionRequest.builder()
                .model(doubaoConfig.getEndpointId())
                .messages(messages)
                .stream(true)  // 启用流式响应
                .build();

        try {
            
            logger.info("开始聊天流处理，菜单项数量：{}，用户消息：{}，模型：{}", 
                menuItems.size(), userMessage, doubaoConfig.getEndpointId());
            
            // 使用异步处理流式响应
            new Thread(() -> {
                StringBuilder messageBuilder = new StringBuilder();
                try {
                    final boolean[] isCompleted = {false};
                    
                    // 完成处理
                    emitter.onCompletion(() -> {
                        isCompleted[0] = true;
                        logger.info("SSE连接已完成");
                    });
                    
                    // 超时处理
                    emitter.onTimeout(() -> {
                        isCompleted[0] = true;
                        logger.warn("SSE连接超时");
                    });
                    
                    logger.info("开始调用ARK API");
                    var stream = arkService.streamChatCompletion(request);
                    logger.info("成功获取到对象");
                    
                    // 获取响应
                    var chunks = stream.toList().blockingGet();
                    logger.info("收到 {} 个响应块", chunks.size());
                    
                    if (!chunks.isEmpty()) {
                        // 收集所有响应块
                        for (var chunk : chunks) {
                            if (isCompleted[0]) break;
                            
                            try {
                                var message = chunk.getChoices().get(0).getMessage();
                                String content = String.valueOf(message.getContent());
                                
                                if (content != null && !content.isEmpty() && !"null".equals(content)) {
                                    messageBuilder.append(content);
                                    String cleanContent = content
                                        .replace("#AUTO_ADD#", "")
                                        .replace("#SYSTEM_INIT#", "");
                                    
                                    if (!content.contains("#SYSTEM_INIT#")) {
                                        emitter.send(SseEmitter.event()
                                            .data(cleanContent)
                                            .build());
                                    }
                                }
                            } catch (Exception e) {
                                logger.error("处理响应块时出错: {}", e.getMessage(), e);
                            }
                        }
                        
                        // 所有响应块接收完成后，统一处理标记
                        String fullResponse = messageBuilder.toString();
                        boolean hasProcessedOrder = false;  // 用于标记是否已处理订单
                        
                        // 处理订单提交
                        Pattern pattern = Pattern.compile("#TABLE_NUMBER=(\\d+)#");
                        Matcher matcher = pattern.matcher(fullResponse);
                        if (matcher.find() && !hasProcessedOrder) {
                            String tableNumber = matcher.group(1);
                            handleSubmitOrder(tableNumber, currentUser, emitter);
                            return;  // 订单提交后直接返回
                        }
                        
                        // 处理其他操作
                        if (!hasProcessedOrder) {
                            // 处理添加到购物车
                            if (fullResponse.contains("#AUTO_ADD#")) {
                                Pattern dishPattern = Pattern.compile("\\*\\*(.*?)\\*\\*");
                                Matcher dishMatcher = dishPattern.matcher(fullResponse);
                                while (dishMatcher.find()) {
                                    String dishName = dishMatcher.group(1);
                                    addToCart(dishName, currentUser);
                                }
                            }
                            
                            // 处理清空购物车
                            if (fullResponse.contains("#CLEAR_CART#")) {
                                try {
                                    orderService.clearUserCart(currentUser);
                                    emitter.send(SseEmitter.event()
                                        .data("\n\n*已为您清空购物车。*")
                                        .build());
                                } catch (Exception e) {
                                    logger.error("清空购物车时出错", e);
                                    emitter.send(SseEmitter.event()
                                        .data("\n\n*抱歉，清空购物车失败，请重试。*")
                                        .build());
                                }
                            }

                            // 处理查看购物车
                            if (fullResponse.contains("#CHECK_CART#")) {
                                try {
                                    List<CartItem> cartItems = cartItemRepository.findByUser(currentUser);
                                    StringBuilder cartInfo = new StringBuilder();
                                    
                                    if (cartItems.isEmpty()) {
                                        cartInfo.append("\n\n您的购物车目前是空的。");
                                    } else {
                                        cartInfo.append("\n\n您的购物车中有以下商品：\n");
                                        double total = 0;
                                        for (CartItem item : cartItems) {
                                            cartInfo.append("- **").append(item.getMenuItem().getName())
                                                   .append("** × ").append(item.getQuantity())
                                                   .append("，小计：￥").append(String.format("%.2f", item.getSubtotal()))
                                                   .append("\n");
                                            total += item.getSubtotal();
                                        }
                                        cartInfo.append("\n*总计：￥").append(String.format("%.2f", total)).append("*");
                                    }
                                    
                                    emitter.send(SseEmitter.event()
                                        .data(cartInfo.toString())
                                        .build());
                                } catch (Exception e) {
                                    logger.error("查询购物车时出错", e);
                                }
                            }
                        }
                        
                        // 发送完成消息
                        if (!isCompleted[0]) {
                            logger.info("对话完成，总响应长度: {}", messageBuilder.length());
                            emitter.complete();
                        }
                    } else {
                        logger.warn("未收到任何响应块");
                        emitter.send(SseEmitter.event()
                            .name("error")
                            .data("未收到有效响应，请稍后重试")
                            .build());
                        emitter.complete();
                    }
                    
                } catch (Exception e) {
                    logger.error("流式对话出错: {}", e.getMessage(), e);
                    try {
                        emitter.send(SseEmitter.event()
                            .name("error")
                            .data("对话出错，请稍后重试")
                            .build());
                        emitter.complete();
                    } catch (Exception ex) {
                        logger.error("发送错误消息时出错", ex);
                    }
                }
            }).start();
            
            return emitter;
            
        } catch (Exception e) {
            logger.error("创建流式对话时出错: {}", e.getMessage(), e);
            emitter.completeWithError(e);
            return emitter;
        }           
    }

    private void handleSubmitOrder(String tableNumber, User user, SseEmitter emitter) throws IOException {
        if (tableNumber == null || tableNumber.trim().isEmpty()) {
            emitter.send(SseEmitter.event()
                .data("请输入桌号，例如：提交订单 A1")
                .build());
            return;
        }

        logger.info("开始处理订单提交，桌号: {}, 用户: {}", tableNumber, user.getName());
        

        synchronized (orderLock) {
            if (isOrderSubmitting) {
                logger.warn("订单正在提交中，忽略重复请求。桌号: {}, 用户: {}", tableNumber, user.getName());
                return;  // 如果正在提交订单，直接返回
            }
            isOrderSubmitting = true;
            logger.info("订单提交状态已设置为正在提交。桌号: {}, 用户: {}", tableNumber, user.getName());
        }

        try {
            // 提交订单
            Order order = orderService.submitOrder(user, tableNumber, "DINE_IN", "");
            logger.info("订单提交成功，订单ID: {}, 总金额: ￥{}. 桌号: {}, 用户: {}", order.getId(), order.getTotal(), tableNumber, user.getName());

            // 构建订单消息
            String orderMessage = String.format(
                "好呀，您的桌号是%s号，我已经为您成功提交订单啦，您可以稍等片刻，美味马上就来。\n\n" +
                "订单号：#%d\n总金额：￥%.2f\n桌号：%s", 
                tableNumber, order.getId(), order.getTotal(), tableNumber
            );
            
            if (order.getDrawCode() != null) {
                orderMessage += "\n\n恭喜您单笔订单达到100元获得抽奖码：" + order.getDrawCode()+"\n点餐主页右下角单击礼包按钮可参与抽奖";
                logger.info("用户获得抽奖码: {}", order.getDrawCode());
            }
            
            // 发送订单消息
            emitter.send(SseEmitter.event()
                .data(orderMessage)
                .build());
            
            // 发送一个隐藏的事件来触发前端刷新
            emitter.send(SseEmitter.event()
                .name("order_submitted")
                .data("")
                .build());
            
        } catch (Exception e) {
            String errorMessage = e.getMessage();
            if (errorMessage == null || errorMessage.isEmpty()) {
                errorMessage = "订单提交失败，请稍后重试";
            }
            logger.error("订单提交失败，错误信息: {}. 桌号: {}, 用户: {}", errorMessage, tableNumber, user.getName(), e);
            emitter.send(SseEmitter.event()
                .data(errorMessage)
                .build());
        } finally {
            // 重置订单提交状态
            synchronized (orderLock) {
                isOrderSubmitting = false;
                logger.info("订单提交状态已重置。桌号: {}, 用户: {}", tableNumber, user.getName());
            }
        }
    }
} 