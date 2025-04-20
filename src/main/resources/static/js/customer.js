// 更新购物车数量显示
function updateCartCount() {
    fetch('/customer/cart/count')
        .then(response => response.json())
        .then(data => {
            document.getElementById('cartCount').textContent = data.count;
        })
        .catch(error => console.error('Error updating cart count:', error));
}

// 增加数量
function increaseQuantity(btn) {
    const input = btn.parentElement.querySelector('.quantity-input');
    input.value = parseInt(input.value) + 1;
}

// 减少数量
function decreaseQuantity(btn) {
    const input = btn.parentElement.querySelector('.quantity-input');
    if (parseInt(input.value) > 1) {
        input.value = parseInt(input.value) - 1;
    }
}

// 添加到购物车
function addToCart(menuItemId, btn) {
    const quantityInput = btn.parentElement.querySelector('.quantity-input');
    const quantity = parseInt(quantityInput.value);

    fetch('/customer/cart/add', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/x-www-form-urlencoded',
        },
        body: `menuItemId=${menuItemId}&quantity=${quantity}`
    })
    .then(response => response.json())
    .then(data => {
        if (data.success) {
            alert(data.message);
            quantityInput.value = 1;
            updateCartCount();  // 添加商品后更新购物车数量
        } else {
            alert('添加失败：' + data.message);
        }
    })
    .catch(error => {
        console.error('Error:', error);
        alert('添加失败，请重试');
    });
}

// 订单类型选择
function selectOrderType(type) {
    // 更新隐藏输入字段
    document.getElementById('orderType').value = type;
    
    // 更新选项样式
    document.querySelectorAll('.type-option').forEach(option => {
        option.classList.remove('active');
    });
    document.querySelector(`[data-type="${type}"]`).classList.add('active');
    
    // 切换表单字段
    const takeoutFields = document.getElementById('takeoutFields');
    const dineInFields = document.getElementById('dineInFields');
    
    if (type === 'TAKEOUT') {
        takeoutFields.style.display = 'block';
        dineInFields.style.display = 'none';
        // 设置必填字段
        document.querySelector('input[name="customerName"]').required = true;
        document.querySelector('input[name="contactPhone"]').required = true;
        document.querySelector('input[name="deliveryAddress"]').required = true;
        document.querySelector('input[name="tableNumber"]').required = false;
    } else {
        takeoutFields.style.display = 'none';
        dineInFields.style.display = 'block';
        // 设置必填字段
        document.querySelector('input[name="customerName"]').required = false;
        document.querySelector('input[name="contactPhone"]').required = false;
        document.querySelector('input[name="deliveryAddress"]').required = false;
        document.querySelector('input[name="tableNumber"]').required = true;
    }
}

// 全局变量，用于标识订单是否已经提交

// 页面加载时的处理
document.addEventListener('DOMContentLoaded', function() {
    updateCartCount();
    
    // 检查URL中是否有抽奖码参数
    const urlParams = new URLSearchParams(window.location.search);
    const drawCode = urlParams.get('drawCode');
    if (drawCode) {
        // 显示抽奖模态框
        const modal = document.getElementById('luckyDrawModal');
        modal.style.display = 'flex';
        
        // 自动填入抽奖码
        document.getElementById('drawCode').value = drawCode;
        
        // 自动验证抽奖码
        verifyDrawCode();
        
        // 清除URL中的抽奖码参数
        window.history.replaceState({}, document.title, '/customer');
    }
    
    // 绑定订单类型选择事件
    const typeOptions = document.querySelectorAll('.type-option');
    typeOptions.forEach(option => {
        option.addEventListener('click', function() {
            selectOrderType(this.dataset.type);
        });
    });
    
    // 绑定聊天相关事件
    const sendMessageBtn = document.getElementById('sendMessageBtn');
    if (sendMessageBtn) {
        sendMessageBtn.addEventListener('click', sendMessage);
    }
    
    const messageInput = document.getElementById('messageInput');
    if (messageInput) {
        messageInput.addEventListener('keypress', function(e) {
            if (e.key === 'Enter') {
                sendMessage();
            }
        });
    }
});

// 全局变量
let currentMessageContent = null; // 当前消息的内容元素
let currentTypewriterTimeout = null; // 当前打字机定时器
let isRotating = false;
let canvas, ctx;
let verifiedCode = null;

// 奖品配置
const prizes = [
    { name: '免单券', color: '#FF5722', probability: 0.05 },
    { name: '8折券', color: '#9C27B0', probability: 0.1 },
    { name: '7折券', color: '#FF9800', probability: 0.15 },
    { name: '50元券', color: '#FFC107', probability: 0.2 },
    { name: '谢谢参与', color: '#F44336', probability: 0.3 },
    { name: '30元券', color: '#FF5722', probability: 0.2 }
];

// 抽奖相关函数
function createLuckyDrawModal() {
    // 创建模态框容器
    const modal = document.createElement('div');
    modal.id = 'luckyDrawModal';
    modal.className = 'modal';

    // 创建模态框内容
    const modalContent = document.createElement('div');
    modalContent.className = 'modal-content lucky-draw-container';


    // 添加关闭按钮
    const closeBtn = document.createElement('span');
    closeBtn.className = 'close';
    closeBtn.innerHTML = '&times;';
    closeBtn.onclick = hideLuckyDraw;

    // 添加标题
    const title = document.createElement('h2');
    title.textContent = '幸运大转盘';

    // 创建抽奖码输入域
    const drawCodeInput = document.createElement('div');
    drawCodeInput.className = 'draw-code-input';
    drawCodeInput.innerHTML = `
        <input type="text" id="drawCode" placeholder="请输入抽奖码">
        <button onclick="verifyDrawCode()">验证抽奖码</button>
    `;

    // 创建
    const codeStatus = document.createElement('div');
    codeStatus.id = 'codeStatus';
    codeStatus.className = 'code-status';

    // 创建转盘容器
    const wheelContainer = document.createElement('div');
    wheelContainer.className = 'wheel-container';

    // 创建指针
    const wheelPointer = document.createElement('div');
    wheelPointer.className = 'wheel-pointer';

    // 创建转盘画布
    const canvasElement = document.createElement('canvas');
    canvasElement.id = 'luckyWheel';
    canvasElement.width = 300;
    canvasElement.height = 300;

    // 创建中心按钮
    const centerButton = document.createElement('div');
    centerButton.className = 'center-button';
    const startBtn = document.createElement('button');
    startBtn.className = 'start-btn';
    startBtn.onclick = startLuckyDraw;
    startBtn.disabled = true;
    startBtn.textContent = '开始';
    centerButton.appendChild(startBtn);

    // 创建结果消息区域
    const resultMessage = document.createElement('div');
    resultMessage.id = 'resultMessage';
    resultMessage.className = 'result-message';

    // 组装转盘容器
    wheelContainer.appendChild(wheelPointer);
    wheelContainer.appendChild(canvasElement);
    wheelContainer.appendChild(centerButton);

    // 组装模态框内容
    modalContent.appendChild(closeBtn);
    modalContent.appendChild(title);
    modalContent.appendChild(drawCodeInput);
    modalContent.appendChild(codeStatus);
    modalContent.appendChild(wheelContainer);
    modalContent.appendChild(resultMessage);

    // 组装模态框
    modal.appendChild(modalContent);

    // 添加到body
    document.body.appendChild(modal);
}

function drawWheel() {
    // 如果模态框不存在，先创建
    if (!document.getElementById('luckyDrawModal')) {
        createLuckyDrawModal();
    }
    
    // 显示模态框
    const modal = document.getElementById('luckyDrawModal');
    modal.style.display = 'flex';
    
    // 初始化转盘
    canvas = document.getElementById('luckyWheel');
    if (canvas) {
        ctx = canvas.getContext('2d');
        drawWheelCanvas();
    }
}

function drawWheelCanvas() {
    if (!canvas) {
        canvas = document.getElementById('luckyWheel');
        ctx = canvas.getContext('2d');
    }
    
    const centerX = canvas.width / 2;
    const centerY = canvas.height / 2;
    const radius = Math.min(centerX, centerY) - 10;
    
    ctx.clearRect(0, 0, canvas.width, canvas.height);
    
    // 绘制转盘扇形
    const anglePerPrize = (Math.PI * 2) / prizes.length;
    prizes.forEach((prize, index) => {
        ctx.beginPath();
        ctx.moveTo(centerX, centerY);
        // 修改：调整起始角度，使第一个奖品位于正上方（-90度或-π/2）
        const startAngle = -Math.PI/2 + index * anglePerPrize;
        const endAngle = -Math.PI/2 + (index + 1) * anglePerPrize;
        ctx.arc(centerX, centerY, radius, startAngle, endAngle);
        ctx.closePath();
        
        // 使用渐变色
        const gradient = ctx.createRadialGradient(
            centerX, centerY, 0,
            centerX, centerY, radius
        );
        gradient.addColorStop(0, '#FFFFFF');
        gradient.addColorStop(1, prize.color);
        
        ctx.fillStyle = gradient;
        ctx.fill();
        ctx.strokeStyle = 'white';
        ctx.lineWidth = 1;
        ctx.stroke();
        
        // 绘制奖品文字
        ctx.save();
        ctx.translate(centerX, centerY);
        ctx.rotate(startAngle + anglePerPrize / 2);
        ctx.textAlign = 'center';
        ctx.fillStyle = 'white';
        ctx.font = 'bold 14px Arial';
        ctx.shadowColor = 'rgba(0, 0, 0, 0.5)';
        ctx.shadowBlur = 2;
        ctx.shadowOffsetX = 1;
        ctx.shadowOffsetY = 1;
        ctx.fillText(prize.name, radius * 0.75, 0);
        ctx.restore();
    });
    
    // 绘制中
    ctx.beginPath();
    ctx.arc(centerX, centerY, 30, 0, Math.PI * 2);
    ctx.fillStyle = '#FF5722';
    ctx.fill();
    ctx.strokeStyle = 'white';
    ctx.lineWidth = 2;
    ctx.stroke();
}

function hideLuckyDraw() {
    const modal = document.getElementById('luckyDrawModal');
    modal.style.display = 'none';
    // 重置状态
    document.getElementById('drawCode').value = '';
    document.getElementById('codeStatus').textContent = '';
    document.querySelector('.start-btn').disabled = true;
    document.getElementById('resultMessage').style.display = 'none';
}

async function verifyDrawCode() {
    const codeInput = document.getElementById('drawCode');
    const codeStatus = document.getElementById('codeStatus');
    const startBtn = document.querySelector('.start-btn');
    const code = codeInput.value.trim();

    if (!code) {
        codeStatus.textContent = '请输入抽奖码';
        codeStatus.className = 'code-status error';
        return;
    }

    try {
        const response = await fetch('/api/draw/verify', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
            },
            body: JSON.stringify({ code: code })
        });

        const data = await response.json();

        if (response.ok) {
            codeStatus.textContent = '抽奖码验证成功';
            codeStatus.className = 'code-status success';
            startBtn.disabled = false;
            verifiedCode = code;
        } else {
            codeStatus.textContent = data.error || '验证失败';
            codeStatus.className = 'code-status error';
            startBtn.disabled = true;
            verifiedCode = null;
        }
    } catch (error) {
        codeStatus.textContent = '验证过程出错，请重试';
        codeStatus.className = 'code-status error';
        startBtn.disabled = true;
        verifiedCode = null;
    }
}

function startLuckyDraw() {
    if (isRotating || !verifiedCode) return;
    isRotating = true;
    
    const resultMessage = document.getElementById('resultMessage');
    resultMessage.style.display = 'none';
    
    // 随机选择奖品
    const random = Math.random();
    let probabilitySum = 0;
    let selectedIndex = prizes.length - 1;
    
    for (let i = 0; i < prizes.length; i++) {
        probabilitySum += prizes[i].probability;
        if (random < probabilitySum) {
            selectedIndex = i;
            break;
        }
    }
    
    // 计算旋转角度
    const anglePerPrize = 360 / prizes.length;
    const targetAngle = -(360 * 5 + anglePerPrize * selectedIndex);
    let currentAngle = 0;
    let startTime = null;
    const duration = 6000; // 旋转时间
    
    function rotate(timestamp) {
        if (!startTime) startTime = timestamp;
        const progress = (timestamp - startTime) / duration;
        
        if (progress < 1) {
            currentAngle = easeInOutQuart(progress) * targetAngle;
            
            ctx.save();
            ctx.translate(canvas.width/2, canvas.height/2);
            ctx.rotate(currentAngle * Math.PI / 180);
            ctx.translate(-canvas.width/2, -canvas.height/2);
            drawWheelCanvas();
            ctx.restore();
            
            requestAnimationFrame(rotate);
        } else {
            isRotating = false;
            resultMessage.innerHTML = `
                <div class="prize-result">
                    <i class="fas fa-gift" style="color: ${prizes[selectedIndex].color}; font-size: 24px;"></i>
                    <h3>恭喜您获得：${prizes[selectedIndex].name}！</h3>
                </div>
            `;
            resultMessage.style.display = 'block';
            document.querySelector('.start-btn').disabled = true;
        }
    }
    
    requestAnimationFrame(rotate);
}

// 缓动函数
function easeInOutQuart(t) {
    return t < 0.5
        ? 8 * t * t * t * t
        : 1 - Math.pow(-2 * t + 2, 4) / 2;
}

// 页面加载时的处理
document.addEventListener('DOMContentLoaded', function() {
    // 检查是否有待处理的抽奖码
    const pendingDrawCode = sessionStorage.getItem('pendingDrawCode');
    if (pendingDrawCode) {
        sessionStorage.removeItem('pendingDrawCode');
        drawWheel();
        
        // 自动填入抽奖码
        const drawCodeInput = document.getElementById('drawCode');
        if (drawCodeInput) {
            drawCodeInput.value = pendingDrawCode;
            verifyDrawCode();
        }
    }
});

// 聊天相关功能
function toggleChatModal() {
    const modal = document.getElementById('chatModal');
    if (modal.style.display === 'flex') {
        modal.style.display = 'none';
    } else {
        modal.style.display = 'flex';
        if (!modal.dataset.initialized) {
            appendMessage("您好！我是您的智能点餐助手，请问有什么可以帮您的吗？", "assistant");
            modal.dataset.initialized = 'true';
        }
        document.getElementById('messageInput').focus();
    }
}

function appendMessage(message, role) {
    // 如果是系统初始化消息，不添加到聊天界面
    if (message.includes('#SYSTEM_INIT#')) {
        return;
    }
    
    const chatMessages = document.getElementById('chatMessages');
    const formattedMessage = formatMessage(message);
    
    // 如果格式化后的消息为空，不添加到聊天界面
    if (!formattedMessage) {
        return;
    }
    
    // 创建消息容器
    const messageDiv = document.createElement('div');
    messageDiv.className = `message ${role}`;
    
    // 创建消息内容容器
    const contentDiv = document.createElement('div');
    contentDiv.className = 'message-content';
    contentDiv.textContent = formattedMessage;
    
    // 组装消息
    messageDiv.appendChild(contentDiv);
    chatMessages.appendChild(messageDiv);
    chatMessages.scrollTop = chatMessages.scrollHeight;
}

function handleKeyPress(event) {
    if (event.key === 'Enter') {
        sendMessage();
    }
}

// 格式化消息文本
function formatMessage(message) {
    if (!message) return '';
    
    if (message.includes('#SYSTEM_INIT#')) {
        return '';
    }
    
    message = message.replace(/#TABLE_NUMBER=[^#]*#/g, '')  // 修改正则表达式以匹配任何#TABLE_NUMBER=后的内容直到下一个#
                    .replace(/#AUTO_ADD#/g, '')
                    .replace(/#CLEAR_CART#/g, '')
                    .replace(/#CHECK_CART#/g, '')
                    .replace(/#SUBMIT_ORDER#/g, '');
    
    message = message.replace(/\n{3,}/g, '\n\n')
                    .replace(/^\s+|\s+$/g, '');
    
    return message;
}

function typeNextChunk() {
    if (currentIndex >= fullResponse.length) {
        isTyping = false;
        
        // 检查是否需要清空购物车
        if (fullResponse.includes('#CLEAR_CART#')) {
            clearCart();
        }
        
        // 检查是否需要查询购物车
        if (fullResponse.includes('#CHECK_CART#')) {
            checkCart();
            return;
        }
        
        // 检查是否需要提交订单
        if (fullResponse.includes('#SUBMIT_ORDER#')) {
            submitOrder();
            return;
        }
        
        // 检查是否有桌号
        const tableNumberMatch = fullResponse.match(/#TABLE_NUMBER=(\d+)#/);
        if (tableNumberMatch) {
            const tableNumber = tableNumberMatch[1];
            submitDineInOrder(tableNumber);
            return;
        }
        return;
    }
    
    isTyping = true;
    const char = fullResponse.charAt(currentIndex);
    currentIndex++;
    
    // 格式化并显示内容
    const formattedText = formatMessage(fullResponse.substring(0, currentIndex));
    if (formattedText) {
        currentMessageContent.innerHTML = marked.parse(formattedText);
        chatMessages.scrollTop = chatMessages.scrollHeight;
    }
    
    // 继续打字
    setTimeout(typeNextChunk, 20);
}

// 清空购物车
function clearCart() {
    fetch('/customer/cart/clear', {
        method: 'DELETE'
    })
    .then(response => response.json())
    .then(data => {
        if (data.success) {
            updateCartCount();
            // 如果购物车页面，刷新页面
            if (window.location.pathname === '/customer/cart') {
                location.reload();
            }
        }
    })
    .catch(error => {
        console.error('Error:', error);
    });
}

async function sendMessage() {
    const messageInput = document.getElementById('messageInput');
    const message = messageInput.value.trim();
    
    if (!message) return;
    
    // 清空输入框
    messageInput.value = '';
    
    // 显示用户信息
    appendMessage(message, "user");
    
    try {
        // 创建新的消息容器元素
        currentMessageContent = document.createElement('div');
        currentMessageContent.className = 'message-content';
        
        // 创建机器人消息容器
        const botMessage = document.createElement('div');
        botMessage.className = 'message assistant';
        botMessage.appendChild(currentMessageContent);
        
        // 添加到聊天记录
        const chatMessages = document.getElementById('chatMessages');
        chatMessages.appendChild(botMessage);
        
        // 使用 SSE 接收流式响应
        const eventSource = new EventSource(`/api/chat/stream?message=${encodeURIComponent(message)}`);
        
        let fullResponse = '';
        let hasError = false;
        let currentIndex = 0;
        let isTyping = false;
        
        function typeNextChunk() {
            if (currentIndex >= fullResponse.length) {
                isTyping = false;
                
                // 检查是否需要清空购物车
                if (fullResponse.includes('#CLEAR_CART#')) {
                    clearCart();
                }
                
                // 检查是否需要查询购物车
                if (fullResponse.includes('#CHECK_CART#')) {
                    checkCart();
                    return;
                }
                
                // 检查是否需要提交订单
                if (fullResponse.includes('#SUBMIT_ORDER#')) {
                    submitOrder();
                    return;
                }
                
                // 检查是否有桌号
                const tableNumberMatch = fullResponse.match(/#TABLE_NUMBER=(\d+)#/);
                if (tableNumberMatch) {
                    const tableNumber = tableNumberMatch[1];
                    submitDineInOrder(tableNumber);
                    return;
                }
                return;
            }
            
            isTyping = true;
            const char = fullResponse.charAt(currentIndex);
            currentIndex++;
            
            // 格式化并显示内容
            const formattedText = formatMessage(fullResponse.substring(0, currentIndex));
            if (formattedText) {
                currentMessageContent.innerHTML = marked.parse(formattedText);
                chatMessages.scrollTop = chatMessages.scrollHeight;
            }
            
            // 继续打字
            setTimeout(typeNextChunk, 20);
        }
        
        eventSource.onmessage = function(event) {
            if (hasError) return;
            
            const newText = event.data;
            fullResponse += newText;
            
            // 检查是否有桌号标记
            const tableNumberMatch = newText.match(/#TABLE_NUMBER=(\d+)#/);
            if (tableNumberMatch) {
                const tableNumber = tableNumberMatch[1];
                // 移除标记后再显示消息
                fullResponse = fullResponse.replace(/#TABLE_NUMBER=\d+#/g, '');
                submitDineInOrder(tableNumber);
                return;
            }
            
            // 如果没有正在打字，开始新的打字效果
            if (!isTyping) {
                typeNextChunk();
            }
        };
        
        eventSource.onerror = function(error) {
            console.error('SSE Error:', error);
            hasError = true;
            eventSource.close();
            if (!fullResponse) {
                currentMessageContent.textContent = '抱歉，发生错误，请重试。';
            }
        };
        
        eventSource.addEventListener('error', function(event) {
            if (hasError) return;
            console.error('SSE Error Event:', event.data);
            hasError = true;
            eventSource.close();
            if (event.data) {
                try {
                    const errorData = JSON.parse(event.data);
                    currentMessageContent.textContent = errorData.message || '抱歉，发生错误，请重试。';
                } catch (e) {
                    currentMessageContent.textContent = event.data;
                }
            } else {
                currentMessageContent.textContent = '抱歉，发生错误，请重试。';
            }
        });
        
    } catch (error) {
        console.error('Error:', error);
        appendMessage('抱歉，发生错误，请重试。', "assistant");
    }
}

const markdownScript = document.createElement('script');
markdownScript.src = 'https://cdn.jsdelivr.net/npm/marked/marked.min.js';
document.head.appendChild(markdownScript); 

