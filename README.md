# DC 3.0.1 智能点餐系统

## 项目介绍
这是一个基于Spring Boot的智能点餐系统(Digital Catering 3.0.1)，为餐厅提供高效、便捷的智能化点餐解决方案。系统支持堂食点餐、在线预订、外卖管理、订单处理、支付结算等功能，并提供数据分析和会员管理能力，帮助餐厅提升服务质量和经营效率。

### 核心功能
- **堂食点餐**：桌台管理、菜品浏览、下单、加菜、取号等
- **菜品管理**：菜品分类、价格设置、库存管理、特价菜设置
- **订单管理**：实时订单查看、订单状态跟踪、历史订单查询
- **会员系统**：会员注册、积分管理、会员优惠
- **支付结算**：多种支付方式支持、订单结算、消费记录
- **数据分析**：销售数据统计、热门菜品分析、营业额报表
- **后台管理**：员工账户管理、权限设置、系统设置

## 技术栈
- **Java 17**：应用程序核心语言
- **Spring Boot 3.2.1**：应用程序框架
- **Spring Data JPA**：数据持久层框架
- **Spring Security**：安全框架与用户认证
- **Thymeleaf**：服务器端模板引擎，用于前端页面渲染
- **MySQL**：关系型数据库，存储菜品、订单、用户数据
- **Maven**：项目管理和构建工具
- **Lombok**：减少Java代码冗余的工具库
- **Bootstrap**：前端UI框架，提供响应式设计

## 系统架构
系统采用经典的MVC架构设计，主要包括：
- **表示层**：用户界面和控制器，处理用户交互
- **业务逻辑层**：实现核心业务功能和业务规则
- **数据访问层**：与数据库交互，处理数据的存取
- **基础设施层**：提供通用功能支持，如安全、日志等

## 项目结构
```
src/main/java/org/example/dcdemo/
├── config/          # 系统配置类
├── controller/      # Web控制器和API接口
│   ├── AdminController.java     # 管理员功能控制器
│   ├── CustomerController.java  # 客户功能控制器
│   ├── OrderController.java     # 订单管理控制器
│   └── ...
├── dto/             # 数据传输对象
├── model/           # 数据模型和实体类
│   ├── MenuItem.java     # 菜品实体
│   ├── Order.java        # 订单实体
│   ├── DineInOrder.java  # 堂食订单实体
│   ├── User.java         # 用户实体
│   └── ...
├── repository/      # 数据访问层
├── service/         # 业务逻辑实现
│   ├── OrderService.java     # 订单服务
│   ├── MenuService.java      # 菜单服务
│   ├── UserService.java      # 用户服务
│   └── ...
├── util/            # 工具类
└── DcDemoApplication.java # 程序入口
```

## 快速开始

### 环境要求
- JDK 17+
- Maven 3.6+
- MySQL 8.0+

### 本地运行
1. 克隆仓库
```bash
git clone https://github.com/ddhyk/dc3.0.1.git
cd dc3.0.1
```

2. 配置数据库
编辑 `src/main/resources/application.properties` 文件，配置您的数据库连接

3. 构建项目
```bash
mvn clean package
```

4. 运行应用
```bash
java -jar target/dc-demo-0.0.1-SNAPSHOT.jar
```

5. 访问应用
在浏览器中访问 `http://localhost:8080`
- 默认管理员账号：admin
- 默认密码：admin123 (建议首次登录后立即修改)

## 用户角色
系统支持以下用户角色：
- **管理员**：完全的系统管理权限
- **收银员**：处理订单和结账
- **厨师**：查看和处理厨房订单
- **服务员**：管理堂食订单和桌台
- **客户**：浏览菜单、下单和支付

## 主要特点
- **直观的用户界面**：简洁明了的点餐流程，提升用户体验
- **实时订单更新**：厨房和前台实时同步订单状态
- **灵活的菜单管理**：轻松添加、修改和下架菜品
- **多终端支持**：适配PC、平板和移动设备
- **数据安全**：严格的权限控制和数据加密
- **可扩展性**：模块化设计，方便功能扩展

## 开发指南
1. 创建新的实体类在 `model` 包中
2. 为实体类创建对应的 `repository` 接口
3. 在 `service` 包中实现业务逻辑
4. 在 `controller` 包中创建API接口或页面控制器

## 贡献指南
1. Fork 项目
2. 创建功能分支 (`git checkout -b feature/amazing-feature`)
3. 提交更改 (`git commit -m 'Add some amazing feature'`)
4. 推送到分支 (`git push origin feature/amazing-feature`)
5. 创建Pull Request

## 作者
- 赫英魁 (@ddhyk)

## 许可证
该项目采用 MIT 许可证 - 详情请参阅 [LICENSE](LICENSE) 文件 