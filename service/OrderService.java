package org.example.dcdemo.service;

import org.example.dcdemo.model.*;
import org.example.dcdemo.repository.OrderRepository;
import org.example.dcdemo.repository.CartItemRepository;
import org.example.dcdemo.repository.DineInOrderRepository;
import org.example.dcdemo.repository.MenuItemRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDateTime;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.stream.Collectors;
import java.math.BigDecimal;

@Service
public class OrderService {
    private final OrderRepository orderRepository;
    private final CartItemRepository cartItemRepository;
    private final DrawCodeService drawCodeService;
    private final DineInOrderRepository dineInOrderRepository;
    @Autowired
    private MenuItemRepository menuItemRepository;

    public OrderService(OrderRepository orderRepository, 
                       CartItemRepository cartItemRepository,
                       DrawCodeService drawCodeService,
                       DineInOrderRepository dineInOrderRepository) {
        this.orderRepository = orderRepository;
        this.cartItemRepository = cartItemRepository;
        this.drawCodeService = drawCodeService;
        this.dineInOrderRepository = dineInOrderRepository;
    }

    public List<Order> getAllOrders() {
        // 获取外卖订单
        List<Order> takeoutOrders = orderRepository.findAllByOrderByOrderTimeDesc();
        
        // 获取堂食订单并转换为Order对象
        List<DineInOrder> dineInOrders = dineInOrderRepository.findAll();
        List<Order> convertedDineInOrders = dineInOrders.stream()
            .map(dineInOrder -> {
                Order order = new Order();
                order.setId(dineInOrder.getId());
                order.setUser(dineInOrder.getUser());
                order.setOrderTime(dineInOrder.getOrderTime());
                order.setStatus(dineInOrder.getStatus());
                order.setTotal(dineInOrder.getTotal());
                order.setRemarks(dineInOrder.getRemarks());
                order.setOrderItems(dineInOrder.getOrderItems());
                order.setDrawCode(dineInOrder.getDrawCode());
                if (dineInOrder.getUser() != null) {
                    order.setCustomerName(dineInOrder.getUser().getName());
                    order.setContactPhone(dineInOrder.getUser().getPhone());
                    order.setDeliveryAddress(dineInOrder.getUser().getAddress());
                }
                return order;
            })
            .toList();
        
        // 合并两种订单并按时间排序
        List<Order> allOrders = new ArrayList<>();
        allOrders.addAll(takeoutOrders);
        allOrders.addAll(convertedDineInOrders);
        allOrders.sort((o1, o2) -> o2.getOrderTime().compareTo(o1.getOrderTime()));
        
        return allOrders;
    }

    @Transactional
    public Order updateOrderStatus(Long id, String status) {
        // 先尝试更新外卖订单
        Order takeoutOrder = orderRepository.findById(id).orElse(null);
        if (takeoutOrder != null) {
            takeoutOrder.setStatus(status);
            return orderRepository.save(takeoutOrder);
        }
        
        // 如果不是外卖订单，尝试更新堂食订单
        DineInOrder dineInOrder = dineInOrderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("订单不存在"));
        dineInOrder.setStatus(status);
        DineInOrder savedDineInOrder = dineInOrderRepository.save(dineInOrder);
        
        // 转换为Order对象返回
        Order order = new Order();
        order.setId(savedDineInOrder.getId());
        order.setUser(savedDineInOrder.getUser());
        order.setOrderTime(savedDineInOrder.getOrderTime());
        order.setStatus(savedDineInOrder.getStatus());
        order.setTotal(savedDineInOrder.getTotal());
        order.setRemarks(savedDineInOrder.getRemarks());
        order.setOrderItems(savedDineInOrder.getOrderItems());
        order.setDrawCode(savedDineInOrder.getDrawCode());
        if (savedDineInOrder.getUser() != null) {
            order.setCustomerName(savedDineInOrder.getUser().getName());
            order.setContactPhone(savedDineInOrder.getUser().getPhone());
            order.setDeliveryAddress(savedDineInOrder.getUser().getAddress());
        }
        return order;
    }

    @Transactional
    public Order addOrder(Order order) {
        return orderRepository.save(order);
    }

    @Transactional
    public void updateOrderTotals() {
        List<Order> ordersWithoutTotal = orderRepository.findByTotalIsNull();
        for (Order order : ordersWithoutTotal) {
            String[] items = order.getOrderItems().split(", ");
            double total = 0.0;
            for (String item : items) {
                int start = item.lastIndexOf("￥") + 1;
                int end = item.lastIndexOf(")");
                if (start > 0 && end > start) {
                    total += Double.parseDouble(item.substring(start, end));
                }
            }
            order.setTotal(total);
            orderRepository.save(order);
        }
    }

    public List<Order> getCustomerOrders(User user) {
        // 获取外卖订单
        List<Order> takeoutOrders = orderRepository.findByUserOrderByOrderTimeDesc(user);
        // 标记但不存储到数据库
        takeoutOrders.forEach(order -> order.setOrderType("TAKEOUT"));

        // 获取堂食订单
        List<DineInOrder> dineInOrderList = dineInOrderRepository.findByUserOrderByOrderTimeDesc(user);
        List<Order> dineInOrders = dineInOrderList.stream()
            .map(dineInOrder -> {
                Order order = new Order();
                order.setId(dineInOrder.getId());
                order.setUser(dineInOrder.getUser());
                order.setOrderTime(dineInOrder.getOrderTime());
                order.setStatus(dineInOrder.getStatus());
                order.setTotal(dineInOrder.getTotal());
                order.setRemarks(dineInOrder.getRemarks());
                order.setOrderItems(dineInOrder.getOrderItems());
                order.setDrawCode(dineInOrder.getDrawCode());
                order.setOrderType("DINE_IN");
                order.setTableNumber(dineInOrder.getTableNumber());
                if (dineInOrder.getUser() != null) {
                    order.setCustomerName(dineInOrder.getUser().getName());
                    order.setContactPhone(dineInOrder.getUser().getPhone());
                    order.setDeliveryAddress(dineInOrder.getUser().getAddress());
                }
                return order;
            })
            .collect(Collectors.toList());

        // 合并订单
        List<Order> allOrders = new ArrayList<>();
        allOrders.addAll(takeoutOrders);
        allOrders.addAll(dineInOrders);
        allOrders.sort((o1, o2) -> o2.getOrderTime().compareTo(o1.getOrderTime()));

        return allOrders;
    }

    public DineInOrder updateDineInOrderStatus(Long id, String newStatus) {
        DineInOrder order = dineInOrderRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("堂食订单不存在"));
        order.setStatus(newStatus);
        return dineInOrderRepository.save(order);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW, rollbackFor = Exception.class)
    public synchronized Order submitOrder(User user, String tableNumberOrRemarks, String orderType, String originalRemarks) {
        if (user == null) {
            throw new IllegalStateException("用户未登录");
        }

        // 获取用户的购物车项
        List<CartItem> cartItems = cartItemRepository.findByUser(user);
        
        // 检查购物车是否为空
        if (cartItems == null || cartItems.isEmpty()) {
            throw new IllegalStateException("购物车为空，无法提交订单");
        }

        // 计算总金额
        double total = cartItems.stream()
                .mapToDouble(CartItem::getSubtotal)
                .sum();

        if ("DINE_IN".equals(orderType)) {
            // 创建堂食订单
            DineInOrder dineInOrder = new DineInOrder();
            dineInOrder.setUser(user);
            dineInOrder.setOrderTime(LocalDateTime.now());
            dineInOrder.setStatus("PENDING");
            dineInOrder.setTotal(total);
            
            // 确保桌号被正确设置
            if (tableNumberOrRemarks == null || tableNumberOrRemarks.trim().isEmpty()) {
                throw new IllegalArgumentException("堂食订单必须指定桌号");
            }
            String tableNumber = tableNumberOrRemarks.trim();
            dineInOrder.setTableNumber(tableNumber);
            dineInOrder.setRemarks(originalRemarks);

            // 创建订单详情
            List<OrderDetail> orderDetails = new ArrayList<>();
            StringBuilder orderItems = new StringBuilder();
            
            for (CartItem item : cartItems) {
                OrderDetail detail = new OrderDetail();
                detail.setMenuItem(item.getMenuItem());
                detail.setQuantity(item.getQuantity());
                detail.setPrice(item.getMenuItem().getPrice().doubleValue());
                detail.setSubtotal(item.getSubtotal());
                detail.setDineInOrder(dineInOrder);
                orderDetails.add(detail);
                
                if (orderItems.length() > 0) {
                    orderItems.append(", ");
                }
                orderItems.append(item.getMenuItem().getName())
                         .append("(￥").append(item.getMenuItem().getPrice())
                         .append(" x ").append(item.getQuantity()).append(")");
            }
            
            dineInOrder.setOrderItems(orderItems.toString());
            dineInOrder.setOrderDetails(orderDetails);
            
            // 保存堂食订单
            DineInOrder savedOrder = dineInOrderRepository.save(dineInOrder);
            
            // 清空购物车
            cartItemRepository.deleteAll(cartItems);
            
            // 如果订单金额满100元，生成抽奖码
            if (total >= 100.0) {
                try {
                    DrawCode drawCode = drawCodeService.generateDrawCode(user, savedOrder.getId());
                    if (drawCode != null) {
                        savedOrder.setDrawCode(drawCode.getCode());
                        savedOrder = dineInOrderRepository.save(savedOrder);
                    }
                } catch (Exception e) {
                }
            }
            
            // 创建普通订单对象用于返回
            Order order = new Order();
            order.setId(savedOrder.getId());
            order.setUser(user);
            order.setOrderTime(savedOrder.getOrderTime());
            order.setStatus(savedOrder.getStatus());
            order.setTotal(savedOrder.getTotal());
            order.setRemarks(savedOrder.getRemarks());
            order.setOrderItems(savedOrder.getOrderItems());
            order.setDrawCode(savedOrder.getDrawCode());
            order.setCustomerName(user.getName());
            order.setContactPhone(user.getPhone());
            order.setDeliveryAddress(user.getAddress());
            order.setOrderType("DINE_IN");
            return order;
            
        } else {
            // 创建外卖订单
            Order order = new Order();
            order.setUser(user);
            order.setOrderTime(LocalDateTime.now());
            order.setStatus("PENDING");
            order.setTotal(total);
            order.setRemarks(originalRemarks);
            order.setCustomerName(user.getName());
            order.setContactPhone(user.getPhone());
            order.setDeliveryAddress(user.getAddress());
            order.setOrderType("TAKEOUT");  // 设置订单类型为外卖

            // 设置订单项
            StringBuilder orderItems = new StringBuilder();
            for (CartItem item : cartItems) {
                if (orderItems.length() > 0) {
                    orderItems.append(", ");
                }
                orderItems.append(item.getMenuItem().getName())
                         .append("(￥").append(item.getMenuItem().getPrice())
                         .append(" x ").append(item.getQuantity()).append(")");
            }
            order.setOrderItems(orderItems.toString());

            // 保存订单
            Order savedOrder = orderRepository.save(order);

            // 如果订单金额满100元，生成抽奖码
            if (total >= 100.0) {
                try {
                    DrawCode drawCode = drawCodeService.generateDrawCode(user, savedOrder.getId());
                    if (drawCode != null) {
                        savedOrder.setDrawCode(drawCode.getCode());
                        savedOrder = orderRepository.save(savedOrder);
                    }
                } catch (Exception e) {
                }
            }

            // 清空购物车
            cartItemRepository.deleteAll(cartItems);

            return savedOrder;
        }
    }

    @Transactional
    public void clearUserCart(User user) {
        if (user == null) {
            throw new IllegalStateException("用户未登录");
        }
        cartItemRepository.deleteByUser(user);
    }

    public Map<String, Object> getCartDetails(User user) {
        if (user == null) {
            throw new IllegalStateException("用户未登录");
        }
        List<CartItem> items = cartItemRepository.findByUser(user);
        double total = items.stream()
                .mapToDouble(CartItem::getSubtotal)
                .sum();
        return Map.of("items", items, "total", total);
    }

    @Transactional
    public void removeCartItem(Long itemId, User user) {
        if (user == null) {
            throw new IllegalStateException("用户未登录");
        }
        cartItemRepository.findById(itemId)
            .filter(item -> item.getUser().getId().equals(user.getId()))
            .ifPresent(cartItemRepository::delete);
    }

    @Transactional
    public Order processOrderSubmission(User user, Map<String, String> orderData) {
        try {
            // 获取用户的购物车项
            List<CartItem> cartItems = cartItemRepository.findByUser(user);
            if (cartItems == null || cartItems.isEmpty()) {
                throw new IllegalStateException("购物车为空，无法提交订单");
            }

            // 计算总金额
            double total = cartItems.stream()
                    .mapToDouble(CartItem::getSubtotal)
                    .sum();

            String orderType = orderData.get("orderType");
            
            // 构建订单项字符串
            StringBuilder orderItems = new StringBuilder();
            for (CartItem item : cartItems) {
                if (orderItems.length() > 0) {
                    orderItems.append(", ");
                }
                orderItems.append(item.getMenuItem().getName())
                         .append("(￥").append(item.getMenuItem().getPrice())
                         .append(" x ").append(item.getQuantity()).append(")");
            }

            Order resultOrder;
            
            if ("DINE_IN".equals(orderType)) {
                // 创建堂食订单
                DineInOrder dineInOrder = new DineInOrder();
                dineInOrder.setUser(user);
                dineInOrder.setOrderTime(LocalDateTime.now());
                dineInOrder.setStatus("PENDING");
                dineInOrder.setTotal(total);
                dineInOrder.setTableNumber(orderData.get("tableNumber"));
                dineInOrder.setRemarks(orderData.get("remarks"));
                dineInOrder.setOrderItems(orderItems.toString());
                
                // 保存堂食订单
                DineInOrder savedDineInOrder = dineInOrderRepository.save(dineInOrder);
                
                // 转换为Order对象用于返回
                resultOrder = new Order();
                resultOrder.setId(savedDineInOrder.getId());
                resultOrder.setOrderTime(savedDineInOrder.getOrderTime());
                resultOrder.setStatus(savedDineInOrder.getStatus());
                resultOrder.setTotal(savedDineInOrder.getTotal());
                resultOrder.setRemarks(savedDineInOrder.getRemarks());
                resultOrder.setOrderItems(savedDineInOrder.getOrderItems());
                resultOrder.setOrderType("DINE_IN");
                resultOrder.setTableNumber(savedDineInOrder.getTableNumber());
                resultOrder.setCustomerName(user.getName());
                resultOrder.setContactPhone(user.getPhone());
                resultOrder.setDeliveryAddress("堂食-" + savedDineInOrder.getTableNumber());
                
            } else {
                // 创建外卖订单
                Order takeoutOrder = new Order();
                takeoutOrder.setUser(user);
                takeoutOrder.setOrderTime(LocalDateTime.now());
                takeoutOrder.setStatus("PENDING");
                takeoutOrder.setTotal(total);
                takeoutOrder.setRemarks(orderData.get("remarks"));
                takeoutOrder.setOrderItems(orderItems.toString());
                takeoutOrder.setOrderType("TAKEOUT");
                takeoutOrder.setCustomerName(orderData.get("customerName"));
                takeoutOrder.setContactPhone(orderData.get("contactPhone"));
                takeoutOrder.setDeliveryAddress(orderData.get("deliveryAddress"));
                
                // 保存外卖订单
                resultOrder = orderRepository.save(takeoutOrder);
            }

            // 如果订单金额满100元，生成抽奖码
            if (total >= 100.0) {
                try {
                    DrawCode drawCode = drawCodeService.generateDrawCode(user, resultOrder.getId());
                    if (drawCode != null) {
                        resultOrder.setDrawCode(drawCode.getCode());
                        if ("DINE_IN".equals(orderType)) {
                            DineInOrder dineInOrder = dineInOrderRepository.findById(resultOrder.getId()).get();
                            dineInOrder.setDrawCode(drawCode.getCode());
                            dineInOrderRepository.save(dineInOrder);
                        } else {
                            orderRepository.save((Order) resultOrder);
                        }
                    }
                } catch (Exception e) {
                    // 抽奖码生成失败不影响订单提交
                }
            }

            // 清空购物车
            cartItemRepository.deleteAll(cartItems);

            return resultOrder;
            
        } catch (Exception e) {
            throw new RuntimeException("订单提交失败：" + e.getMessage());
        }
    }

    public long getCartItemCount(User user) {
        if (user == null) {
            return 0;
        }
        return cartItemRepository.countByUser(user);
    }

    @Transactional
    public void saveCartItem(CartItem cartItem) {
        // 详细的验证
        if (cartItem == null) {
            throw new IllegalStateException("购物车项不能为空");
        }
        if (cartItem.getUser() == null) {
            throw new IllegalStateException("用户信息不能为空");
        }
        if (cartItem.getMenuItem() == null) {
            throw new IllegalStateException("菜品信息不能为空");
        }
        if (cartItem.getQuantity() <= 0) {
            throw new IllegalStateException("商品数量必须大于0");
        }

        // 检查是否已存在相同商品的购物车项
        List<CartItem> existingItems = cartItemRepository.findByUserAndMenuItem(cartItem.getUser(), cartItem.getMenuItem());
        if (!existingItems.isEmpty()) {
            // 如果存在，更新数量
            CartItem existingItem = existingItems.get(0);
            existingItem.setQuantity(existingItem.getQuantity() + cartItem.getQuantity());
            existingItem.setSubtotal(existingItem.getMenuItem().getPrice().doubleValue() * existingItem.getQuantity());
            cartItemRepository.save(existingItem);
        } else {
            // 如果不存在，创建新的购物车项
            cartItem.setSubtotal(cartItem.getMenuItem().getPrice().doubleValue() * cartItem.getQuantity());
            cartItemRepository.save(cartItem);
        }
    }

    public Map<String, Object> addToCart(User user, Long menuItemId, Integer quantity) {
        MenuItem menuItem = menuItemRepository.findById(menuItemId)
            .orElseThrow(() -> new IllegalArgumentException("菜品不存在"));
        
        CartItem cartItem = new CartItem();
        cartItem.setUser(user);
        cartItem.setMenuItem(menuItem);
        cartItem.setQuantity(quantity);
        cartItem.setSubtotal(menuItem.getPrice().multiply(BigDecimal.valueOf(quantity)).doubleValue());
        
        saveCartItem(cartItem);
        
        return Map.of("success", true, "message", "添加到购物车成功！");
    }

    public Map<String, Object> submitOrder(User user, Map<String, String> orderData) {
        Order order = processOrderSubmission(user, orderData);
        return Map.of(
            "success", true,
            "message", "订单提交成功！",
            "drawCode", order.getDrawCode() != null ? order.getDrawCode() : ""
        );
    }
} 