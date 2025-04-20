// 处理服务器发送的事件
eventSource.onmessage = function(event) {
    // 显示消息
    appendMessage(event.data, 'bot');
}; 

// 处理订单提交事件
eventSource.addEventListener('order_submitted', function(event) {
    markOrderSubmitted();
    refreshCart();
});

// 添加消息到聊天窗口的通用函数
function appendMessage(content, type) {
    // 如果消息为空，不显示
    if (!content) return;
    
    const messageDiv = document.createElement('div');
    messageDiv.className = `message ${type}-message`;
    
    const contentDiv = document.createElement('div');
    contentDiv.className = 'message-content';
    contentDiv.innerHTML = content.replace(/\n/g, '<br>');
    
    messageDiv.appendChild(contentDiv);
    chatMessages.appendChild(messageDiv);
    scrollToBottom();
}

// 标记订单已提交
function markOrderSubmitted() {
    const submitButton = document.querySelector('#submit-order-btn');
    if (submitButton) {
        submitButton.disabled = true;
        submitButton.textContent = '订单已提交';
    }
}

// 刷新购物车
function refreshCart() {
    // 清空购物车显示
    const cartItemsContainer = document.querySelector('#cart-items');
    if (cartItemsContainer) {
        cartItemsContainer.innerHTML = '';
    }
    
    // 更新购物车数量
    updateCartItemCount(0);
    
    // 隐藏购物车面板（如果显示的话）
    const cartPanel = document.querySelector('#cart-panel');
    if (cartPanel && cartPanel.style.display !== 'none') {
        cartPanel.style.display = 'none';
    }
} 