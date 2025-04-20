# DC 3.0.1 演示应用

## 项目介绍
这是一个基于Spring Boot的Web应用程序演示项目，旨在展示Spring Boot的基本功能和用法。该项目使用了现代Java开发技术和框架，提供了一个可扩展、易维护的应用程序架构。

## 技术栈
- **Java 17**：应用程序核心语言
- **Spring Boot 3.2.1**：应用程序框架
- **Spring Data JPA**：数据持久层框架
- **Spring Security**：安全框架
- **Thymeleaf**：服务器端模板引擎
- **MySQL**：数据库服务
- **Maven**：项目管理和构建工具
- **Lombok**：减少Java代码冗余工具
- **火山引擎SDK**：集成火山引擎服务

## 项目结构
```
src/main/java/org/example/dcdemo/
├── config/          # 配置类
├── controller/      # 控制器
├── dto/             # 数据传输对象
├── model/           # 数据模型
├── repository/      # 数据访问层
├── service/         # 业务逻辑层
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
