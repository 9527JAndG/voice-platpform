# 架构设计文档

## 系统架构

```
┌─────────────────┐
│   天猫精灵App    │
└────────┬────────┘
         │ 1. 用户语音指令
         ▼
┌─────────────────┐
│  天猫精灵平台    │
└────────┬────────┘
         │ 2. OAuth授权 + API调用
         ▼
┌─────────────────────────────────────┐
│        Spring Boot 应用              │
│  ┌─────────────────────────────┐   │
│  │   Controller 层              │   │
│  │  - OAuthController          │   │
│  │  - SmartHomeController      │   │
│  └──────────┬──────────────────┘   │
│             │                       │
│  ┌──────────▼──────────────────┐   │
│  │   Service 层                 │   │
│  │  - OAuthService             │   │
│  │  - DeviceService            │   │
│  └──────────┬──────────────────┘   │
│             │                       │
│  ┌──────────▼──────────────────┐   │
│  │   Repository 层              │   │
│  │  - JPA Repositories         │   │
│  └──────────┬──────────────────┘   │
└─────────────┼───────────────────────┘
              │
              ▼
      ┌──────────────┐
      │   MySQL DB   │
      └──────────────┘
```

## 核心流程

### 1. OAuth 授权流程

```
用户 → 天猫精灵App → 天猫精灵平台
                        │
                        ▼
                   /authorize (展示授权页面)
                        │
                        ▼ 用户确认
                   生成 code
                        │
                        ▼
                   重定向到天猫精灵
                        │
                        ▼
                   /token (用code换token)
                        │
                        ▼
                   返回 access_token
```

### 2. 设备发现流程

```
用户: "天猫精灵，发现设备"
         │
         ▼
    天猫精灵平台
         │
         ▼
    POST /smarthome/discovery
    Header: Authorization: Bearer {token}
         │
         ▼
    验证 Token → 查询用户设备 → 返回设备列表
```

### 3. 设备控制流程

```
用户: "天猫精灵，打开扫地机器人"
         │
         ▼
    天猫精灵平台 (解析语音指令)
         │
         ▼
    POST /smarthome/control
    {
      "header": {"name": "TurnOn"},
      "payload": {"deviceId": "robot_001"}
    }
         │
         ▼
    验证 Token → 执行设备操作 → 返回结果
```

## 数据模型

### OAuth 相关表

```sql
oauth_clients              -- OAuth客户端
├── client_id             -- 客户端ID
├── client_secret         -- 客户端密钥
└── redirect_uri          -- 回调地址

oauth_authorization_codes  -- 授权码
├── authorization_code    -- 授权码
├── client_id            -- 客户端ID
├── expires_at           -- 过期时间
└── redirect_uri         -- 回调地址

oauth_access_tokens       -- 访问令牌
├── access_token         -- 访问令牌
├── client_id           -- 客户端ID
└── expires_at          -- 过期时间

oauth_refresh_tokens      -- 刷新令牌
├── refresh_token        -- 刷新令牌
├── client_id           -- 客户端ID
└── expires_at          -- 过期时间
```

### 设备相关表

```sql
users                     -- 用户表
├── id                   -- 用户ID
├── username            -- 用户名
└── password            -- 密码

devices                   -- 设备表
├── device_id           -- 设备ID
├── device_name         -- 设备名称
├── device_type         -- 设备类型
├── user_id            -- 所属用户
├── status             -- 设备状态
├── power_state        -- 电源状态
├── work_mode          -- 工作模式
└── battery_level      -- 电量
```

## 技术选型

### 后端框架
- **Spring Boot 2.7.18**: 主框架
- **Spring Data JPA**: 数据访问层
- **MySQL 8.0**: 关系型数据库
- **Lombok**: 简化代码
- **Fastjson**: JSON处理

### 安全机制
- **OAuth 2.0**: 授权协议
- **Bearer Token**: 访问令牌
- **HTTPS**: 传输加密（生产环境）

## 扩展点

### 1. 添加新设备类型

在 `DeviceService` 中添加新的设备控制方法：

```java
public AligenieResponse newDeviceAction(String deviceId, String messageId) {
    // 实现新设备的控制逻辑
}
```

在 `SmartHomeController` 的 `control` 方法中添加新的 action 分支。

### 2. 集成真实设备

修改 `DeviceService` 中的控制方法，调用真实设备的 API：

```java
@Transactional
public AligenieResponse turnOn(String deviceId, String messageId) {
    Device device = deviceRepository.findByDeviceId(deviceId).orElseThrow();
    
    // 调用真实设备API
    realDeviceClient.turnOn(device.getDeviceId());
    
    device.setPowerState("on");
    deviceRepository.save(device);
    
    return AligenieResponse.success(...);
}
```

### 3. 添加缓存

使用 Redis 缓存 Token 和设备状态：

```java
@Cacheable(value = "tokens", key = "#accessToken")
public boolean validateAccessToken(String accessToken) {
    // ...
}
```

### 4. 添加消息队列

使用 RabbitMQ 或 Kafka 处理设备控制命令：

```java
@Autowired
private RabbitTemplate rabbitTemplate;

public void sendDeviceCommand(String deviceId, String command) {
    rabbitTemplate.convertAndSend("device.exchange", "device.command", 
        new DeviceCommand(deviceId, command));
}
```

## 性能优化

### 1. 数据库优化
- 添加索引：device_id, client_id, access_token
- 使用连接池：HikariCP
- 定期清理过期 Token

### 2. 接口优化
- 使用异步处理设备控制
- 添加接口限流
- 实现请求缓存

### 3. 监控告警
- 集成 Prometheus + Grafana
- 添加日志收集（ELK）
- 配置告警规则

## 安全建议

1. **生产环境必须使用 HTTPS**
2. **Token 加密存储**
3. **添加请求签名验证**
4. **实现 IP 白名单**
5. **添加访问频率限制**
6. **定期更新依赖版本**
7. **敏感信息不要硬编码**

## 部署架构（生产环境）

```
                    ┌──────────────┐
                    │   Nginx      │
                    │  (负载均衡)   │
                    └──────┬───────┘
                           │
            ┌──────────────┼──────────────┐
            │              │              │
    ┌───────▼──────┐ ┌────▼──────┐ ┌────▼──────┐
    │ Spring Boot  │ │Spring Boot│ │Spring Boot│
    │  Instance 1  │ │Instance 2 │ │Instance 3 │
    └───────┬──────┘ └────┬──────┘ └────┬──────┘
            │              │              │
            └──────────────┼──────────────┘
                           │
                    ┌──────▼───────┐
                    │  MySQL       │
                    │  (主从复制)   │
                    └──────────────┘
```

## 测试策略

### 1. 单元测试
- Service 层业务逻辑测试
- Repository 层数据访问测试

### 2. 集成测试
- OAuth 授权流程测试
- 设备控制接口测试

### 3. 压力测试
- 并发用户测试
- Token 验证性能测试
- 设备控制响应时间测试
