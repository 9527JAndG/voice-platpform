# 项目结构说明

```
aligenie-smarthome/
│
├── src/main/
│   ├── java/com/example/aligenie/
│   │   │
│   │   ├── AligenieApplication.java          # Spring Boot 启动类
│   │   │
│   │   ├── controller/                        # 控制器层
│   │   │   ├── OAuthController.java          # OAuth 授权控制器
│   │   │   └── SmartHomeController.java      # 智能家居控制器
│   │   │
│   │   ├── service/                           # 业务逻辑层
│   │   │   ├── OAuthService.java             # OAuth 业务逻辑
│   │   │   └── DeviceService.java            # 设备业务逻辑
│   │   │
│   │   ├── model/                             # 数据模型层
│   │   │   ├── OAuthClient.java              # OAuth 客户端实体
│   │   │   ├── AuthorizationCode.java        # 授权码实体
│   │   │   ├── AccessToken.java              # 访问令牌实体
│   │   │   ├── RefreshToken.java             # 刷新令牌实体
│   │   │   └── Device.java                   # 设备实体
│   │   │
│   │   ├── repository/                        # 数据访问层
│   │   │   ├── OAuthClientRepository.java
│   │   │   ├── AuthorizationCodeRepository.java
│   │   │   ├── AccessTokenRepository.java
│   │   │   ├── RefreshTokenRepository.java
│   │   │   └── DeviceRepository.java
│   │   │
│   │   ├── dto/                               # 数据传输对象
│   │   │   ├── TokenResponse.java            # Token 响应对象
│   │   │   ├── AligenieRequest.java          # 天猫精灵请求对象
│   │   │   └── AligenieResponse.java         # 天猫精灵响应对象
│   │   │
│   │   └── util/                              # 工具类
│   │       └── TokenGenerator.java           # Token 生成工具
│   │
│   └── resources/
│       ├── application.yml                    # Spring Boot 配置文件
│       ├── smarthomedb.sql                         # 数据库初始化脚本
│       └── static/
│           └── authorize.html                 # OAuth 授权页面
│
├── target/                                    # Maven 编译输出目录
│   ├── classes/                              # 编译后的 class 文件
│   └── aligenie-smarthome-1.0.0.jar         # 打包后的 jar 文件
│
├── .gitignore                                # Git 忽略配置
├── pom.xml                                   # Maven 项目配置
│
├── README.md                                 # 项目说明文档
├── QUICKSTART.md                            # 快速开始指南
├── API.md                                   # API 接口文档
├── ARCHITECTURE.md                          # 架构设计文档
├── DEPLOYMENT.md                            # 部署指南
├── PROJECT_SUMMARY.md                       # 项目总结
├── STRUCTURE.md                             # 项目结构说明（本文件）
└── postman_collection.json                  # Postman 测试集合
```

## 📂 目录说明

### 源代码目录 (src/main/java)

#### controller/ - 控制器层
负责处理 HTTP 请求和响应
- `OAuthController.java`: 处理 OAuth 授权流程
  - GET /authorize - 展示授权页面
  - POST /authorize/confirm - 处理用户授权确认
  - POST /token - 发放访问令牌

- `SmartHomeController.java`: 处理智能家居相关请求
  - POST /smarthome/discovery - 设备发现
  - POST /smarthome/control - 设备控制
  - POST /smarthome/query - 设备查询

#### service/ - 业务逻辑层
包含核心业务逻辑
- `OAuthService.java`: OAuth 相关业务
  - 客户端验证
  - 授权码生成与验证
  - Token 生成、验证和刷新

- `DeviceService.java`: 设备相关业务
  - 设备发现
  - 设备控制（开/关/暂停/继续/设置模式）
  - 设备状态查询

#### model/ - 数据模型层
JPA 实体类，映射数据库表
- `OAuthClient.java`: OAuth 客户端信息
- `AuthorizationCode.java`: 授权码
- `AccessToken.java`: 访问令牌
- `RefreshToken.java`: 刷新令牌
- `Device.java`: 设备信息

#### repository/ - 数据访问层
Spring Data JPA 仓库接口
- 提供数据库 CRUD 操作
- 自定义查询方法

#### dto/ - 数据传输对象
用于 API 请求和响应
- `TokenResponse.java`: Token 响应格式
- `AligenieRequest.java`: 天猫精灵请求格式
- `AligenieResponse.java`: 天猫精灵响应格式

#### util/ - 工具类
通用工具方法
- `TokenGenerator.java`: 生成随机 Token

### 资源目录 (src/main/resources)

- `application.yml`: Spring Boot 配置
  - 数据库连接配置
  - OAuth 配置（过期时间等）
  - 日志配置

- `smarthomedb.sql`: 数据库初始化脚本
  - 创建数据库
  - 创建表结构
  - 插入测试数据

- `static/authorize.html`: OAuth 授权页面
  - 美观的 UI 设计
  - 响应式布局
  - 用户授权确认

### 文档目录

- `README.md`: 项目主文档
  - 项目介绍
  - 快速开始
  - 功能特性

- `QUICKSTART.md`: 快速开始指南
  - 5分钟快速体验
  - 详细的步骤说明
  - 常见问题解答

- `API.md`: API 接口文档
  - 完整的接口说明
  - 请求响应示例
  - 错误码说明

- `ARCHITECTURE.md`: 架构设计文档
  - 系统架构图
  - 核心流程说明
  - 技术选型
  - 扩展点说明

- `DEPLOYMENT.md`: 部署指南
  - 环境准备
  - 部署步骤
  - 天猫精灵平台配置
  - 测试流程

- `PROJECT_SUMMARY.md`: 项目总结
  - 完成情况
  - 文件清单
  - 技术亮点
  - 未来规划

- `STRUCTURE.md`: 项目结构说明（本文件）

### 配置文件

- `pom.xml`: Maven 项目配置
  - 依赖管理
  - 插件配置
  - 项目信息

- `.gitignore`: Git 忽略配置
  - 忽略编译文件
  - 忽略 IDE 配置
  - 忽略日志文件

- `postman_collection.json`: Postman 测试集合
  - 完整的 API 测试用例
  - 自动化测试脚本

## 🔄 数据流向

### OAuth 授权流程
```
用户请求
    ↓
OAuthController (接收请求)
    ↓
OAuthService (业务处理)
    ↓
Repository (数据访问)
    ↓
MySQL 数据库
```

### 设备控制流程
```
天猫精灵请求
    ↓
SmartHomeController (接收请求)
    ↓
OAuthService (验证 Token)
    ↓
DeviceService (设备控制)
    ↓
Repository (数据访问)
    ↓
MySQL 数据库
```

## 📊 代码统计

| 类型 | 数量 | 说明 |
|------|------|------|
| Controller | 2 | 控制器类 |
| Service | 2 | 业务逻辑类 |
| Model | 5 | 实体类 |
| Repository | 5 | 数据访问接口 |
| DTO | 3 | 数据传输对象 |
| Util | 1 | 工具类 |
| 配置文件 | 4 | yml, xml, sql 等 |
| 文档文件 | 8 | md 文件 |
| 前端页面 | 1 | html 文件 |
| **总计** | **31** | **文件总数** |

## 🎯 关键文件说明

### 必须修改的文件
1. `application.yml` - 修改数据库连接信息
2. `smarthomedb.sql` - 根据需要调整数据库结构

### 核心业务文件
1. `OAuthService.java` - OAuth 核心逻辑
2. `DeviceService.java` - 设备控制核心逻辑
3. `SmartHomeController.java` - API 入口

### 扩展点文件
1. `DeviceService.java` - 添加新的设备控制方法
2. `Device.java` - 添加新的设备属性
3. `smarthomedb.sql` - 添加新的数据表

## 📝 命名规范

### Java 类命名
- Controller: `XxxController.java`
- Service: `XxxService.java`
- Repository: `XxxRepository.java`
- Model: 实体名称，如 `Device.java`
- DTO: 用途+类型，如 `TokenResponse.java`

### 数据库表命名
- 使用下划线分隔: `oauth_clients`
- 复数形式: `devices`
- 关联表: `user_devices`

### API 路径命名
- 使用小写字母
- 使用斜杠分隔: `/smarthome/discovery`
- RESTful 风格

## 🔍 快速定位

### 需要修改配置？
→ `src/main/resources/application.yml`

### 需要添加新接口？
→ `src/main/java/com/example/aligenie/controller/`

### 需要修改业务逻辑？
→ `src/main/java/com/example/aligenie/service/`

### 需要修改数据库？
→ `src/main/resources/smarthomedb.sql`

### 需要查看 API 文档？
→ `API.md`

### 需要部署应用？
→ `DEPLOYMENT.md`
