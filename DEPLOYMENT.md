# 部署指南

## 环境要求

- JDK 21+
- MySQL 8.0+
- Maven 3.6+

## 部署步骤

### 1. 数据库配置

```bash
# 登录 MySQL
mysql -u root -p

# 执行数据库脚本
source src/main/resources/smarthomedb.sql
```

### 2. 修改配置文件

编辑 `src/main/resources/application.yml`：

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/smarthomedb?useUnicode=true&characterEncoding=utf8&useSSL=false&serverTimezone=Asia/Shanghai
    username: your_username
    password: your_password
```

### 3. 编译打包

```bash
mvn clean package
```

### 4. 运行应用

```bash
# 开发环境
mvn spring-boot:run

# 生产环境
java -jar target/aligenie-smarthome-1.0.0.jar
```

### 5. 验证服务

访问 http://localhost:8080/authorize 查看授权页面

## 天猫精灵开放平台配置

### 1. 创建技能

1. 登录天猫精灵开放平台
2. 创建"智能家居"技能
3. 选择"自定义设备"

### 2. 配置 OAuth

在技能配置中填写：

- **授权地址**: `https://your-domain.com/authorize`
- **Token 地址**: `https://your-domain.com/token`
- **Client ID**: `test_client_id`（与数据库中的一致）
- **Client Secret**: `test_client_secret`（与数据库中的一致）

### 3. 配置 Webhook

- **设备发现接口**: `https://your-domain.com/smarthome/discovery`
- **设备控制接口**: `https://your-domain.com/smarthome/control`
- **设备查询接口**: `https://your-domain.com/smarthome/query`

### 4. 配置域名

如果在本地开发，需要使用内网穿透工具（如 ngrok、frp）：

```bash
# 使用 ngrok
ngrok http 8080

# 将生成的 https 地址配置到天猫精灵平台
```

## 测试流程

### 1. OAuth 授权测试

```bash
# 访问授权页面
curl "http://localhost:8080/authorize?client_id=test_client_id&redirect_uri=https://aligenie.com/callback&state=xyz&response_type=code"
```

### 2. 获取 Token

```bash
curl -X POST "http://localhost:8080/token" \
  -d "grant_type=authorization_code" \
  -d "client_id=test_client_id" \
  -d "client_secret=test_client_secret" \
  -d "code=YOUR_CODE"
```

### 3. 设备发现

```bash
curl -X POST "http://localhost:8080/smarthome/discovery" \
  -H "Authorization: Bearer YOUR_ACCESS_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "header": {
      "namespace": "AliGenie.Iot.Device.Discovery",
      "name": "DiscoveryRequest",
      "messageId": "test-001",
      "payloadVersion": "1"
    },
    "payload": {}
  }'
```

### 4. 设备控制

```bash
# 开机
curl -X POST "http://localhost:8080/smarthome/control" \
  -H "Authorization: Bearer YOUR_ACCESS_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "header": {
      "namespace": "AliGenie.Iot.Device.Control",
      "name": "TurnOn",
      "messageId": "test-002",
      "payloadVersion": "1"
    },
    "payload": {
      "deviceId": "robot_001"
    }
  }'
```

## 常见问题

### 1. 数据库连接失败

检查 MySQL 服务是否启动，用户名密码是否正确。

### 2. Token 验证失败

检查 Token 是否过期，是否正确传递 Authorization header。

### 3. 设备控制无响应

检查设备 ID 是否正确，设备是否在数据库中存在。

## 生产环境建议

1. 使用 HTTPS 协议
2. 配置 Redis 缓存 Token
3. 添加日志监控
4. 配置数据库连接池
5. 添加限流保护
6. 实现真实的设备控制逻辑
