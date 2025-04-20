// 全局变量，用于标识订单是否已经提交
let isOrderSubmitted = false;

// 提交订单
function submitOrder(event) {
    event.preventDefault();
    if (isOrderSubmitted) {
        console.log('订单已提交，忽略重复请求');
        return;
    }
    console.log('准备提交订单...');  // 添加日志
    const form = event.target;
    const orderType = document.getElementById('orderType').value;
    
    // 构建订单数据
    const orderData = {
        orderType: orderType,
        remarks: form.remarks.value
    };

    // 根据订单类型添加相应字段
    if (orderType === 'TAKEOUT') {
        if (!form.customerName.value || !form.contactPhone.value || !form.deliveryAddress.value) {
            alert('请填写完整的外卖配送信息');
            return;
        }
        orderData.customerName = form.customerName.value;
        orderData.contactPhone = form.contactPhone.value;
        orderData.deliveryAddress = form.deliveryAddress.value;
    } else if (orderType === 'DINE_IN') {
        const tableNumber = form.tableNumber.value;
        if (!tableNumber || tableNumber.trim() === '') {
            alert('请输入桌号！');
            return;
        }
        orderData.tableNumber = tableNumber.trim();
    }

    console.log('提交订单数据:', orderData);  // 添加日志

    // 发送订单数据
    fetch('/customer/cart/submit', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
            'Accept': 'application/json'
        },
        body: JSON.stringify(orderData)
    })
    .then(response => {
        if (!response.ok) {
            return response.json().then(data => {
                throw new Error(data.message || '网络请求失败');
            });
        }
        return response.json();
    })
    .then(data => {
        if (data.success) {
            isOrderSubmitted = true;  // 设置标志位，防止重复提交
            console.log('订单提交成功，标志位已设置');  // 添加日志
            if (data.drawCode) {
                if (confirm(`恭喜您！订单提交成功！\n您的订单金额满100元，获得抽奖码：${data.drawCode}\n点击"确定"立即抽奖，点击"取消"可稍后在首页参与抽奖！`)) {
                    // 存储抽奖码并跳转
                    sessionStorage.setItem('pendingDrawCode', data.drawCode);
                    window.location.href = '/customer';
                } else {
                    window.location.href = '/customer/orders';
                }
            } else {
                alert('订单提交成功！');
                window.location.href = '/customer/orders';
            }
        } else {
            alert(data.message || '提交订单失败');
        }
    })
    .catch(error => {
        console.error('Error:', error);
        alert(error.message || '提交订单失败，请重试');
    });
}

// 切换订单类型
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

// 页面加载完成后添加事件监听
document.addEventListener('DOMContentLoaded', function() {
    const typeOptions = document.querySelectorAll('.type-option');
    typeOptions.forEach(option => {
        option.addEventListener('click', function() {
            selectOrderType(this.dataset.type);
        });
    });
});

// 从购物车中删除商品
function removeFromCart(itemId) {
    if (confirm('确定要删除这个菜品吗？')) {
        fetch(`/customer/cart/${itemId}`, {
            method: 'DELETE'
        })
        .then(response => response.json())
        .then(data => {
            if (data.success) {
                location.reload();  // 刷新页面以更新购物车
            } else {
                alert('删除失败：' + data.message);
            }
        })
        .catch(error => {
            console.error('Error:', error);
            alert('删除失败，请重试');
        });
    }
} 