function showAddStaffModal() {
    const modal = document.getElementById('addStaffModal');
    if (modal) {
        modal.style.display = 'block';
    }
}

function hideAddStaffModal() {
    const modal = document.getElementById('addStaffModal');
    if (modal) {
        modal.style.display = 'none';
    }
}

document.addEventListener('DOMContentLoaded', function() {
    const addStaffForm = document.getElementById('addStaffForm');
    if (addStaffForm) {
        addStaffForm.addEventListener('submit', function(e) {
            e.preventDefault();
            
            const formData = new FormData(this);
            const staffData = {};
            formData.forEach((value, key) => {
                staffData[key] = value;
            });
            
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
                    hideAddStaffModal();
                    location.reload();
                } else {
                    alert('添加失败：' + result);
                }
            })
            .catch(error => {
                alert('添加失败：' + error);
            });
        });
    }
});

// 菜品销售排行图表
function initDishSalesChart(data) {
    console.log('Initializing dish sales chart with data:', data);
    const chartDom = document.getElementById('dishSalesChart');
    console.log('Chart container:', chartDom);
    
    // 确保容器存在且有尺寸
    if (!chartDom) {
        console.error('Chart container not found');
        return;
    }
    
    // 设置容器尺寸
    if (!chartDom.style.height) {
        chartDom.style.height = '400px';
    }
    
    const chart = echarts.init(chartDom);
    const option = {
        title: {
            text: '菜品销售排行',
            left: 'center'
        },
        tooltip: {
            trigger: 'axis',
            axisPointer: {
                type: 'shadow'
            }
        },
        grid: {
            left: '3%',
            right: '4%',
            bottom: '3%',
            containLabel: true
        },
        xAxis: {
            type: 'value',
            name: '销售数量'
        },
        yAxis: {
            type: 'category',
            data: data.map(item => item.dishName || item.name)
        },
        series: [{
            name: '销售数量',
            type: 'bar',
            data: data.map(item => item.salesCount || item.count),
            itemStyle: {
                color: '#0d6efd'
            }
        }]
    };
    chart.setOption(option);
    return chart;
}

// 用户活跃度趋势图表
function initUserActivityChart(data) {
    const chartDom = document.getElementById('userActivityChart');
    
    // 确保容器存在且有尺寸
    if (!chartDom) {
        console.error('Chart container not found');
        return;
    }
    
    // 设置容器尺寸
    if (!chartDom.style.height) {
        chartDom.style.height = '400px';
    }
    
    const chart = echarts.init(chartDom);
    const option = {
        title: {
            text: '用户活跃度趋势',
            left: 'center'
        },
        tooltip: {
            trigger: 'axis'
        },
        grid: {
            left: '3%',
            right: '4%',
            bottom: '3%',
            containLabel: true
        },
        xAxis: {
            type: 'category',
            data: data.map(item => item.date),
            name: '日期'
        },
        yAxis: {
            type: 'value',
            name: '活跃用户数'
        },
        series: [{
            name: '活跃用户数',
            type: 'line',
            smooth: true,
            data: data.map(item => item.userCount || item.count),
            itemStyle: {
                color: '#198754'
            },
            areaStyle: {
                color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [{
                    offset: 0,
                    color: 'rgba(25,135,84,0.3)'
                }, {
                    offset: 1,
                    color: 'rgba(25,135,84,0.1)'
                }])
            }
        }]
    };
    chart.setOption(option);
    return chart;
}

// 页面加载完成后初始化图表
document.addEventListener('DOMContentLoaded', function() {
    let charts = [];
    
    // 获取图表数据并初始化
    fetch('/api/dashboard/data')
        .then(response => response.json())
        .then(data => {
            console.log('Dashboard data:', data);
            // 初始化销售排行图表
            if (data.topSellingDishes && data.topSellingDishes.length > 0) {
                console.log('Top selling dishes:', data.topSellingDishes);
                const dishChart = initDishSalesChart(data.topSellingDishes);
                if (dishChart) charts.push(dishChart);
            } else {
                console.log('No top selling dishes data available');
            }
            
            // 初始化用户活跃度趋势图表
            if (data.userActivityTrend && data.userActivityTrend.length > 0) {
                console.log('User activity trend:', data.userActivityTrend);
                const userChart = initUserActivityChart(data.userActivityTrend);
                if (userChart) charts.push(userChart);
            } else {
                console.log('No user activity trend data available');
            }
        })
        .catch(error => {
            console.error('获取数据失败:', error);
        });

    // 自适应大小
    window.addEventListener('resize', function() {
        charts.forEach(chart => {
            if (chart) {
                chart.resize();
            }
        });
    });
}); 