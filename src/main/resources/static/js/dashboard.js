// 初始化图表
document.addEventListener('DOMContentLoaded', function() {
    // 获取仪表盘数据
    fetch('/api/dashboard/data')
        .then(response => response.json())
        .then(data => {
            initializeCharts(data);
        })
        .catch(error => console.error('Error fetching dashboard data:', error));
});

function initializeCharts(data) {
    // 初始化热门菜品销量图表
    const dishSalesChart = echarts.init(document.getElementById('dishSalesChart'));
    const dishSalesOption = {
        title: {
            text: '热门菜品销量TOP10',
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
            type: 'category',
            data: data.topSellingDishes.map(dish => dish.dishName),
            axisLabel: {
                interval: 0,
                rotate: 30
            }
        },
        yAxis: {
            type: 'value',
            name: '销量'
        },
        series: [{
            name: '销量',
            type: 'bar',
            data: data.topSellingDishes.map(dish => dish.salesCount),
            itemStyle: {
                color: '#409EFF'
            }
        }]
    };
    dishSalesChart.setOption(dishSalesOption);

    // 初始化用户活跃度趋势图表
    const userActivityChart = echarts.init(document.getElementById('userActivityChart'));
    const userActivityOption = {
        title: {
            text: '近7天用户活跃度',
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
            data: data.userActivityTrend.map(item => item.date),
            axisLabel: {
                formatter: (value) => {
                    return value.substring(5); // 只显示月-日
                }
            }
        },
        yAxis: {
            type: 'value',
            name: '活跃用户数'
        },
        series: [{
            name: '活跃用户',
            type: 'line',
            smooth: true,
            data: data.userActivityTrend.map(item => item.userCount),
            itemStyle: {
                color: '#67C23A'
            },
            areaStyle: {
                color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [{
                    offset: 0,
                    color: 'rgba(103,194,58,0.3)'
                }, {
                    offset: 1,
                    color: 'rgba(103,194,58,0.1)'
                }])
            }
        }]
    };
    userActivityChart.setOption(userActivityOption);

    // 监听窗口大小变化，调整图表大小
    window.addEventListener('resize', function() {
        dishSalesChart.resize();
        userActivityChart.resize();
    });
}