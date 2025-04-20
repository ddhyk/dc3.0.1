// 页面加载完成后执行
document.addEventListener('DOMContentLoaded', function() {
    // 侧边栏菜单切换
    const menuItems = document.querySelectorAll('.menu-item');
    menuItems.forEach(item => {
        item.addEventListener('click', function() {
            // 移除所有active类
            menuItems.forEach(i => i.classList.remove('active'));
            // 添加当前active类
            this.classList.add('active');
            
            // 切换面板显示
            const targetId = this.getAttribute('data-target');
            document.querySelectorAll('.panel').forEach(panel => {
                panel.classList.remove('active');
            });
            const targetPanel = document.getElementById(targetId);
            if (targetPanel) {
                targetPanel.classList.add('active');
            }
        });
    });

    // 初始化模态框关闭功能
    const modal = document.getElementById('modal');
    const closeBtn = document.querySelector('.close');
    closeBtn.onclick = function() {
        modal.style.display = "none";
    }
    window.onclick = function(event) {
        if (event.target == modal) {
            modal.style.display = "none";
        }
    }

    // 页面加载时显示日报表
    generateDailyReport();

    // 初始加载订单时不需要任何过滤条件
    loadOrders(true);

    // 设置日期选择器的默认值为今天
    const today = new Date().toISOString().split('T')[0];
    document.getElementById('startDate').value = today;
    document.getElementById('endDate').value = today;
});

// 客户管理相关函数
function searchCustomers() {
    const keyword = document.getElementById('customerSearch').value;
    fetch(`/admin/api/customers?keyword=${encodeURIComponent(keyword)}`)
        .then(response => response.json())
        .then(customers => {
            const tbody = document.getElementById('customerTableBody');
            tbody.innerHTML = customers.map(customer => `
                <tr>
                    <td>${customer.id}</td>
                    <td>${customer.name}</td>
                    <td>${customer.email}</td>
                    <td>${customer.phone || ''}</td>
                    <td>${customer.address || ''}</td>
                    <td>
                        <button class="btn-edit" onclick="editCustomer(${customer.id})">编辑</button>
                        ${customer.userType !== 'admin' ? 
                            `<button class="btn-delete" onclick="deleteCustomer(${customer.id})">删除</button>` 
                            : ''}
                    </td>
                </tr>
            `).join('');
        });
}

function showAddCustomerModal() {
    const modalContent = document.getElementById('modalContent');
    modalContent.innerHTML = `
        <h2>添加客户</h2>
        <form id="customerForm" onsubmit="saveCustomer(event)">
            <div class="form-group">
                <label>姓名</label>
                <input type="text" name="name" required>
            </div>
            <div class="form-group">
                <label>邮箱</label>
                <input type="email" name="email" required>
            </div>
            <div class="form-group">
                <label>密码</label>
                <input type="password" name="password" required>
            </div>
            <div class="form-group">
                <label>电话</label>
                <input type="tel" name="phone">
            </div>
            <div class="form-group">
                <label>地址</label>
                <input type="text" name="address">
            </div>
            <button type="submit" class="btn-primary">保存</button>
        </form>
    `;
    document.getElementById('modal').style.display = "block";
}

function editCustomer(id) {
    fetch(`/admin/api/customers/${id}`)
        .then(response => response.json())
        .then(customer => {
            const modalContent = document.getElementById('modalContent');
            modalContent.innerHTML = `
                <h2>编辑客户</h2>
                <form id="customerForm" onsubmit="updateCustomer(event, ${id})">
                    <div class="form-group">
                        <label>姓名</label>
                        <input type="text" name="name" value="${customer.name}" required>
                    </div>
                    <div class="form-group">
                        <label>邮箱</label>
                        <input type="email" name="email" value="${customer.email}" required>
                    </div>
                    <div class="form-group">
                        <label>电话</label>
                        <input type="tel" name="phone" value="${customer.phone || ''}">
                    </div>
                    <div class="form-group">
                        <label>地址</label>
                        <input type="text" name="address" value="${customer.address || ''}">
                    </div>
                    <button type="submit" class="btn-primary">保存</button>
                </form>
            `;
            document.getElementById('modal').style.display = "block";
        });
}

function saveCustomer(event) {
    event.preventDefault();
    
    // 获取表单元素
    const form = event.target;
    
    // 直接获取密码输入框的值
    const password = form.querySelector('input[name="password"]').value;
    
    // 检查密码是否为空
    if (!password || password.trim() === '') {
        alert('密码不能为空');
        return;
    }
    
    const customerData = {
        name: form.querySelector('input[name="name"]').value,
        email: form.querySelector('input[name="email"]').value,
        password: password,
        phone: form.querySelector('input[name="phone"]').value || '',
        address: form.querySelector('input[name="address"]').value || ''
    };
    
    fetch('/admin/api/customers', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json'
        },
        body: JSON.stringify(customerData)
    })
    .then(response => {
        if (!response.ok) {
            return response.json().then(data => Promise.reject(data));
        }
        return response.json();
    })
    .then(data => {
        if (data.error) {
            alert(data.error);
        } else {
            document.getElementById('modal').style.display = "none";
            searchCustomers();
        }
    })
    .catch(error => {
        alert(error.error || '保存失败，请重试');
    });
}

function updateCustomer(event, id) {
    event.preventDefault();
    const formData = new FormData(event.target);
    const customerData = Object.fromEntries(formData.entries());
    
    fetch(`/admin/api/customers/${id}`, {
        method: 'PUT',
        headers: {
            'Content-Type': 'application/json'
        },
        body: JSON.stringify(customerData)
    })
    .then(response => response.json())
    .then(data => {
        if (data.error) {
            alert(data.error);
        } else {
            document.getElementById('modal').style.display = "none";
            searchCustomers();
        }
    });
}

function deleteCustomer(id) {
    if (confirm('确定要删除这个客户吗？')) {
        fetch(`/admin/api/customers/${id}`, {
            method: 'DELETE'
        })
        .then(response => response.json())
        .then(data => {
            alert(data.message);
            searchCustomers();
        });
    }
}

// 菜单管理相关函数
function searchMenuItems() {
    const keyword = document.getElementById('menuSearch').value;
    const category = document.getElementById('categoryFilter').value;
    
    fetch(`/admin/api/menu-items?keyword=${encodeURIComponent(keyword)}&category=${encodeURIComponent(category)}`)
        .then(response => response.json())
        .then(menuItems => {
            const tbody = document.getElementById('menuTableBody');
            tbody.innerHTML = menuItems.map(item => `
                <tr>
                    <td>${item.id}</td>
                    <td><img src="${item.imageUrl}" alt="${item.name}" class="menu-item-image"></td>
                    <td>${item.name}</td>
                    <td>${item.category}</td>
                    <td>￥${item.price}</td>
                    <td>${item.description}</td>
                    <td>
                        <button class="btn-edit" onclick="editMenuItem(${item.id})">编辑</button>
                        <button class="btn-delete" onclick="deleteMenuItem(${item.id})">删除</button>
                    </td>
                </tr>
            `).join('');
        });
}

function showAddMenuItemModal() {
    const modalContent = document.getElementById('modalContent');
    modalContent.innerHTML = `
        <h2>添加菜品</h2>
        <form id="menuItemForm" onsubmit="saveMenuItem(event)">
            <div class="form-group">
                <label>名称</label>
                <input type="text" name="name" required>
            </div>
            <div class="form-group">
                <label>分类</label>
                <select name="category" required>
                    <option value="热菜">热菜</option>
                    <option value="凉菜">凉菜</option>
                    <option value="汤品">汤品</option>
                    <option value="主食">主食</option>
                </select>
            </div>
            <div class="form-group">
                <label>价格</label>
                <input type="number" name="price" step="0.01" required>
            </div>
            <div class="form-group">
                <label>描述</label>
                <textarea name="description" required></textarea>
            </div>
            <div class="form-group">
                <label>菜品图片</label>
                <input type="file" name="image" accept="image/*" required onchange="previewImage(this)">
                <img id="imagePreview" style="max-width: 200px; margin-top: 10px; display: none;">
                <input type="hidden" name="imageUrl">
            </div>
            <button type="submit" class="btn-primary">保存</button>
        </form>
    `;
    document.getElementById('modal').style.display = "block";
}

function editMenuItem(id) {
    fetch(`/admin/api/menu-items/${id}`)
        .then(response => response.json())
        .then(item => {
            const modalContent = document.getElementById('modalContent');
            modalContent.innerHTML = `
                <h2>编辑菜品</h2>
                <form id="menuItemForm" onsubmit="updateMenuItem(event, ${id})">
                    <div class="form-group">
                        <label>名称</label>
                        <input type="text" name="name" value="${item.name}" required>
                    </div>
                    <div class="form-group">
                        <label>分类</label>
                        <select name="category" required>
                            <option value="热菜" ${item.category === '热菜' ? 'selected' : ''}>热菜</option>
                            <option value="凉菜" ${item.category === '凉菜' ? 'selected' : ''}>凉菜</option>
                            <option value="汤品" ${item.category === '汤品' ? 'selected' : ''}>汤品</option>
                            <option value="主食" ${item.category === '主食' ? 'selected' : ''}>主食</option>
                        </select>
                    </div>
                    <div class="form-group">
                        <label>价格</label>
                        <input type="number" name="price" step="0.01" value="${item.price}" required>
                    </div>
                    <div class="form-group">
                        <label>描述</label>
                        <textarea name="description" required>${item.description}</textarea>
                    </div>
                    <div class="form-group">
                        <label>菜品图片</label>
                        <input type="file" name="image" accept="image/*" onchange="previewImage(this)">
                        <img id="imagePreview" src="${item.imageUrl}" style="max-width: 200px; margin-top: 10px;">
                        <input type="hidden" name="imageUrl" value="${item.imageUrl}">
                    </div>
                    <button type="submit" class="btn-primary">保存</button>
                </form>
            `;
            document.getElementById('modal').style.display = "block";
        });
}

function previewImage(input) {
    const preview = document.getElementById('imagePreview');
    if (input.files && input.files[0]) {
        const reader = new FileReader();
        reader.onload = function(e) {
            preview.src = e.target.result;
            preview.style.display = 'block';
        }
        reader.readAsDataURL(input.files[0]);
    } else {
        preview.style.display = 'none';
    }
}

async function uploadImage(file, menuItemId = null) {
    const formData = new FormData();
    formData.append('file', file);
    if (menuItemId) {
        formData.append('menuItemId', menuItemId);
    }
    
    try {
        const response = await fetch('/admin/api/upload', {
            method: 'POST',
            body: formData
        });
        
        if (!response.ok) {
            throw new Error('上传失败');
        }
        
        const data = await response.json();
        if (data.error) {
            throw new Error(data.error);
        }
        
        return data.menuItem.imageUrl;
    } catch (error) {
        console.error('上传失败:', error);
        throw error;
    }
}

async function saveMenuItem(event) {
    event.preventDefault();
    const form = event.target;
    const formData = new FormData(form);
    
    try {
        // 上传图片
        const imageFile = form.image.files[0];
        if (imageFile) {
            const imageUrl = await uploadImage(imageFile);
            if (imageUrl) {
                formData.set('imageUrl', imageUrl);
            }
        }
        
        // 转换为JSON对象
        const menuItemData = Object.fromEntries(formData.entries());
        
        // 保存菜品信息
        const response = await fetch('/admin/api/menu-items', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(menuItemData)
        });
        
        const data = await response.json();
        document.getElementById('modal').style.display = "none";
        searchMenuItems();
    } catch (error) {
        alert('保存失败：' + error.message);
    }
}

async function updateMenuItem(event, id) {
    event.preventDefault();
    const form = event.target;
    const formData = new FormData(form);
    
    try {
        // 上传图片
        const imageFile = form.image.files[0];
        if (imageFile) {
            const imageUrl = await uploadImage(imageFile, id);
            if (imageUrl) {
                formData.set('imageUrl', imageUrl);
            }
        }
        
        // 转换为JSON对象
        const menuItemData = Object.fromEntries(formData.entries());
        
        const response = await fetch(`/admin/api/menu-items/${id}`, {
            method: 'PUT',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(menuItemData)
        });
        
        const data = await response.json();
        document.getElementById('modal').style.display = "none";
        searchMenuItems();
    } catch (error) {
        alert('更新失败：' + error.message);
    }
}

function deleteMenuItem(id) {
    if (confirm('确定要删除这个菜品吗？')) {
        fetch(`/admin/api/menu-items/${id}`, {
            method: 'DELETE'
        })
        .then(response => response.json())
        .then(data => {
            alert(data.message);
            searchMenuItems();
        });
    }
}

// 订单管理相关函数
function loadOrders(isInitialLoad = false) {
    const keyword = document.getElementById('orderSearch')?.value || '';
    const status = document.getElementById('orderStatusFilter')?.value || '';
    const startDate = document.getElementById('startDate')?.value || '';
    const endDate = document.getElementById('endDate')?.value || '';
    
    const params = new URLSearchParams();
    if (!isInitialLoad) {
        if (keyword) params.append('keyword', keyword);
        if (status) params.append('status', status);
        if (startDate) params.append('startDate', startDate);
        if (endDate) params.append('endDate', endDate);
    }
    
    fetch(`/admin/api/orders${params.toString() ? '?' + params.toString() : ''}`)
        .then(response => response.json())
        .then(data => {
            const tbody = document.getElementById('orderTableBody');
            tbody.innerHTML = data.orders.map(order => `
                <tr data-order-id="${order.id}">
                    <td>#${order.id}</td>
                    <td>
                        <span class="type-badge ${order.type.toLowerCase()}">
                            ${order.type === 'TAKEOUT' ? '外卖' : '堂食'}
                        </span>
                    </td>
                    <td>${order.customerInfo}</td>
                    <td>${order.contact}</td>
                    <td>${order.address}</td>
                    <td>￥${order.total?.toFixed(2) || '0.00'}</td>
                    <td>
                        <span class="status-badge ${order.status.toLowerCase()}">
                            ${getStatusText(order.status)}
                        </span>
                    </td>
                    <td class="action-buttons">
                        <button class="btn-action btn-view" onclick="viewOrderDetails('${order.id}', '${order.type.toLowerCase()}')">
                            <i class="fas fa-eye"></i> 查看
                        </button>
                        ${order.status !== 'COMPLETED' && order.status !== 'CANCELLED' ? `
                            <button class="btn-action btn-complete" onclick="updateOrderStatus('${order.id}', '${order.type.toLowerCase()}', 'COMPLETED')">
                                <i class="fas fa-check"></i> 完成
                            </button>
                            <button class="btn-action btn-cancel" onclick="updateOrderStatus('${order.id}', '${order.type.toLowerCase()}', 'CANCELLED')">
                                <i class="fas fa-times"></i> 取消
                            </button>
                        ` : ''}
                    </td>
                </tr>
            `).join('');
        })
        .catch(error => {
            console.error('加载订单失败:', error);
            alert('加载订单失败，请刷新页面重试');
        });
}

// 添加重置搜索条件的函数
function resetOrderSearch() {
    // 获取当前日期
    const today = new Date().toISOString().split('T')[0];
    
    // 重置所有搜索条件
    document.getElementById('orderSearch').value = '';
    document.getElementById('orderStatusFilter').value = '';
    document.getElementById('startDate').value = today;
    document.getElementById('endDate').value = today;
    
    // 重新加载订单
    loadOrders();
}

// 更新订单状态
function updateOrderStatus(orderId, type, status) {
    fetch(`/admin/api/orders/${orderId}/status?type=${type}`, {
        method: 'PUT',
        headers: {
            'Content-Type': 'application/json'
        },
        body: JSON.stringify({ status: status })
    })
    .then(response => {
        if (!response.ok) {
            throw new Error('更新状态失败');
        }
        return response.json().catch(() => {
            // 如果解析JSON失败，返回一个基本的成功对象
            return { success: true };
        });
    })
    .then(data => {
        // 显示成功提示
        const statusText = getStatusText(status);
        alert(`订单状态已更新为：${statusText}`);
        
        // 更新当前行的状态显示
        const row = document.querySelector(`tr[data-order-id="${orderId}"]`);
        if (row) {
            const statusCell = row.querySelector('.status-badge');
            if (statusCell) {
                statusCell.textContent = statusText;
                statusCell.className = `status-badge ${status.toLowerCase()}`;
            }
            
            // 如果状态是已完成或已取消，移除操作按钮
            if (status === 'COMPLETED' || status === 'CANCELLED') {
                const actionCell = row.querySelector('.action-buttons');
                if (actionCell) {
                    actionCell.innerHTML = `
                        <button class="btn-action btn-view" onclick="viewOrderDetails('${orderId}', '${type}')">
                            <i class="fas fa-eye"></i> 查看
                        </button>
                    `;
                }
            }
        }
        
        // 重新加载订单列表以确保数据同步
        loadOrders();
    })
    .catch(error => {
        console.error('更新订单状态失败:', error);
        alert('更新订单状态失败，请重试');
    });
}

// 获取状态显示文本
function getStatusText(status) {
    const statusMap = {
        'PENDING': '待处理',
        'PROCESSING': '处理中',
        'COMPLETED': '已完成',
        'CANCELLED': '已取消'
    };
    return statusMap[status] || status;
}

function formatDateTime(dateTimeStr) {
    if (!dateTimeStr) return '';
    const date = new Date(dateTimeStr);
    return date.toLocaleString('zh-CN', {
        year: 'numeric',
        month: '2-digit',
        day: '2-digit',
        hour: '2-digit',
        minute: '2-digit'
    });
}

function viewOrderDetails(orderId, type) {
    fetch(`/admin/api/orders/${orderId}?type=${type}`)
        .then(response => response.json())
        .then(order => {
            const modalContent = document.getElementById('modalContent');
            
            let orderItemsHtml = '';
            if (type === 'dine_in' && order.orderDetails) {
                // 堂食订单显示详细菜品信息
                orderItemsHtml = `
                    <div class="order-items-list">
                        <h3>订单内容</h3>
                        <table class="details-table">
                            <thead>
                                <tr>
                                    <th>菜品名</th>
                                    <th>数量</th>
                                    <th>单价</th>
                                    <th>小计</th>
                                </tr>
                            </thead>
                            <tbody>
                                ${order.orderDetails.map(detail => `
                                    <tr>
                                        <td>${detail.menuItemName}</td>
                                        <td>${detail.quantity}</td>
                                        <td>￥${detail.price.toFixed(2)}</td>
                                        <td>￥${detail.subtotal.toFixed(2)}</td>
                                    </tr>
                                `).join('')}
                            </tbody>
                        </table>
                    </div>`;
            } else {
                // 外卖订单显示简单列表
                orderItemsHtml = `
                    <div class="order-items-list">
                        <h3>订单内容</h3>
                        <p>${order.orderItems}</p>
                    </div>`;
            }

            modalContent.innerHTML = `
                <div class="order-details-modal">
                    <div class="order-details-header">
                        <h2>订单详情 #${order.id}</h2>
                        <span class="status-badge ${order.status.toLowerCase()}">${getStatusText(order.status)}</span>
                    </div>
                    <div class="order-info-grid">
                        ${type === 'takeout' ? `
                            <div class="order-info-item">
                                <label>收货人</label>
                                <div>${order.customerName || ''}</div>
                            </div>
                            <div class="order-info-item">
                                <label>联系电话</label>
                                <div>${order.contactPhone || ''}</div>
                            </div>
                            <div class="order-info-item">
                                <label>配送地址</label>
                                <div>${order.deliveryAddress || ''}</div>
                            </div>
                        ` : `
                            <div class="order-info-item">
                                <label>桌号</label>
                                <div>${order.tableNumber || ''}</div>
                            </div>
                        `}
                        <div class="order-info-item">
                            <label>下单时间</label>
                            <div>${formatDateTime(order.orderTime)}</div>
                        </div>
                    </div>
                    ${orderItemsHtml}
                    <div class="order-info-item">
                        <label>备注</label>
                        <div>${order.remarks || '无'}</div>
                    </div>
                    <div class="order-total">
                        <h3>总计：￥${order.total?.toFixed(2) || '0.00'}</h3>
                    </div>
                </div>
            `;
            document.getElementById('modal').style.display = "block";
        })
        .catch(error => {
            console.error('Error:', error);
            alert('获取订单详情失败');
        });
}

// 统计报表相关函数
let currentChart = null;

function switchReport(button, type) {
    // 更新按钮状态
    document.querySelectorAll('.report-controls button').forEach(btn => {
        btn.classList.remove('active');
    });
    button.classList.add('active');
    
    // 根据类型生成对应报表
    switch(type) {
        case 'daily':
            generateDailyReport();
            break;
        case 'monthly':
            generateMonthlyReport();
            break;
        case 'yearly':
            generateYearlyReport();
            break;
    }
}

function generateDailyReport() {
    fetch('/admin/api/statistics/daily')
        .then(response => response.json())
        .then(data => {
            updateStatistics(data.totalRevenue, data.totalOrders);
            
            const hours = Array.from({length: 24}, (_, i) => `${i}:00`);
            const revenues = hours.map(hour => data.hourlyRevenue[parseInt(hour)] || 0);
            
            initChart('每小时营收', hours, revenues, 'line');
        })
        .catch(error => {
            console.error('加载日报表失败:', error);
            const hours = Array.from({length: 24}, (_, i) => `${i}:00`);
            initChart('每小时营收', hours, Array(24).fill(0), 'line');
            updateStatistics(0, 0);
        });
}

function generateMonthlyReport() {
    fetch('/admin/api/statistics/monthly')
        .then(response => response.json())
        .then(data => {
            updateStatistics(data.totalRevenue, data.totalOrders);
            
            // 获取当月天数
            const daysInMonth = new Date(new Date().getFullYear(), new Date().getMonth() + 1, 0).getDate();
            const days = Array.from({length: daysInMonth}, (_, i) => `${i + 1}日`);
            const revenues = days.map((_, i) => data.dailyRevenue[i + 1] || 0);
            
            initChart('每日营收', days, revenues, 'line');
        })
        .catch(error => {
            console.error('加载月报表失败:', error);
            const daysInMonth = new Date(new Date().getFullYear(), new Date().getMonth() + 1, 0).getDate();
            const days = Array.from({length: daysInMonth}, (_, i) => `${i + 1}日`);
            initChart('每日营收', days, Array(daysInMonth).fill(0), 'line');
            updateStatistics(0, 0);
        });
}

function generateYearlyReport() {
    fetch('/admin/api/statistics/yearly')
        .then(response => response.json())
        .then(data => {
            updateStatistics(data.totalRevenue, data.totalOrders);
            
            const months = ['1月', '2月', '3月', '4月', '5月', '6月', 
                          '7月', '8月', '9月', '10月', '11月', '12月'];
            const revenues = months.map((_, i) => data.monthlyRevenue[i + 1] || 0);
            
            initChart('每月营收', months, revenues, 'bar');
        })
        .catch(error => {
            console.error('加载年报表失败:', error);
            const months = ['1月', '2月', '3月', '4月', '5月', '6月', 
                          '7月', '8月', '9月', '10月', '11月', '12月'];
            initChart('每月营收', months, Array(12).fill(0), 'bar');
            updateStatistics(0, 0);
        });
}

function updateStatistics(revenue, orders) {
    document.getElementById('totalRevenue').textContent = `￥${revenue.toFixed(2)}`;
    document.getElementById('totalOrders').textContent = orders;
}

function initChart(label, labels, data, type) {
    if (typeof Chart === 'undefined') {
        console.error('Chart.js 未加载');
        return;
    }

    const ctx = document.getElementById('revenueChart');
    if (!ctx) {
        console.error('找不到图表容器');
        return;
    }

    try {
        if (currentChart) {
            currentChart.destroy();
        }

        const config = {
            type: type,
            data: {
                labels: labels,
                datasets: [{
                    label: label,
                    data: data,
                    borderColor: '#1890ff',
                    backgroundColor: type === 'bar' ? 'rgba(24,144,255,0.6)' : 'rgba(24,144,255,0.1)',
                    borderWidth: type === 'bar' ? 0 : 2,
                    fill: type === 'line',
                    tension: 0.4
                }]
            },
            options: {
                responsive: true,
                maintainAspectRatio: false,
                plugins: {
                    legend: {
                        display: false
                    },
                    tooltip: {
                        callbacks: {
                            label: function(context) {
                                return `￥${context.parsed.y.toFixed(2)}`;
                            }
                        }
                    }
                },
                scales: {
                    y: {
                        beginAtZero: true,
                        grid: {
                            color: '#f0f0f0'
                        },
                        ticks: {
                            callback: function(value) {
                                return '￥' + value;
                            }
                        }
                    },
                    x: {
                        grid: {
                            display: false
                        }
                    }
                }
            }
        };

        currentChart = new Chart(ctx, config);
    } catch (error) {
        console.error('创建图表失败:', error);
    }
}

function showAddAdminModal() {
    const modalContent = document.getElementById('modalContent');
    modalContent.innerHTML = `
        <h2>添加管理员</h2>
        <form id="adminForm" onsubmit="saveAdmin(event)">
            <div class="form-group">
                <label>姓名</label>
                <input type="text" name="name" required>
            </div>
            <div class="form-group">
                <label>邮箱</label>
                <input type="email" name="email" required>
            </div>
            <div class="form-group">
                <label>密码</label>
                <input type="password" name="password" required>
            </div>
            <div class="form-group">
                <label>电话</label>
                <input type="tel" name="phone">
            </div>
            <div class="form-group">
                <label>地址</label>
                <input type="text" name="address">
            </div>
            <div class="form-group">
                <label>作者密码</label>
                <input type="password" name="authorPassword" required>
            </div>
            <button type="submit" class="btn-primary">保存</button>
        </form>
    `;
    document.getElementById('modal').style.display = "block";
}

function saveAdmin(event) {
    event.preventDefault();
    const formData = new FormData(event.target);
    const adminData = Object.fromEntries(formData.entries());
    
    fetch('/admin/api/admins', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json'
        },
        body: JSON.stringify(adminData)
    })
    .then(response => response.json())
    .then(data => {
        if (data.error) {
            alert(data.error);
        } else {
            document.getElementById('modal').style.display = "none";
            alert('管理员添加成功！');
        }
    });
}

function showAddOperationStaffModal() {
    const modalContent = document.getElementById('modalContent');
    modalContent.innerHTML = `
        <h2>添加运营人员</h2>
        <form id="operationStaffForm" onsubmit="saveOperationStaff(event)">
            <div class="form-group">
                <label>真实姓名</label>
                <input type="text" name="realName" required>
            </div>
            <div class="form-group">
                <label>手机号</label>
                <input type="tel" name="phone" required>
            </div>
            <div class="form-group">
                <label>登录邮箱</label>
                <input type="email" name="email" required>
            </div>
            <div class="form-group">
                <label>登录密码</label>
                <input type="password" name="password" required>
            </div>
            <button type="submit" class="btn-primary">保存</button>
        </form>
    `;
    document.getElementById('modal').style.display = "block";
}

function saveOperationStaff(event) {
    event.preventDefault();
    const formData = new FormData(event.target);
    const staffData = Object.fromEntries(formData.entries());
    
    fetch('/admin/operation-staff/add', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json'
        },
        body: JSON.stringify(staffData)
    })
    .then(response => response.text())
    .then(result => {
        if (result === 'success') {
            alert('运营人员添加成功！');
            document.getElementById('modal').style.display = "none";
        } else {
            alert('添加失败：' + result);
        }
    })
    .catch(error => {
        alert('添加失败：' + error);
    });
} 