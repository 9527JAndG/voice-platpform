# 快速开始指南

## 5分钟快速体验

### 步骤 1: 准备环境

确保已安装：
- JDK 21+
- MySQL 8.0+
- Maven 3.6+

### 步骤 2: 创建数据库

```bash
mysql -u root -p
```

```sql
CREATE DATABASE smarthomedb DEFAULT CHARACTER SET utf8mb4;
USE smarthomedb;
source src/main/resources/smarthomedb.sql;
```

### 步骤 3: 修改配置

编辑 `src/main/resources/application.yml`，修改数据库密码：

```yaml
spring:
  datasource:
    password: your_mysql_password
```

### 步骤 4: 启动应用

```bash
mvn spring-boot:run
```

看到以下输出表示启动成功：
```
========================================
天猫精灵智能家居服务启动成功！
========================================
```

### 步骤 5: 测试 OAuth 授权

1. 浏览器访问：
```
http://localhost:8080/authorize?client_id=test_client_id&redirect_uri=http://localhost:8080/callback&state=test123&response_type=code
```

2. 点击"授权"按钮

3. 从重定向的 URL 中获取 code 参数

### 步骤 6: 获取 Access Token

使用 Postman 或 curl：

```bash
curl -X POST "http://localhost:8080/token" \
  -d "grant_type=authorization_code" \
  -d "client_id=test_client_id" \
  -d "client_secret=test_client_secret" \
  -d "code=YOUR_CODE_HERE"
```

响应示例：
```json
{
  "access_token": "abc123...",
  "token_type": "Bearer",
  "expires_in": 259200,
  "refresh_token": "xyz789..."
}
```

### 步骤 7: 测试设备发现

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

### 步骤 8: 测试设备控制

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

## 配置天猫精灵（需要公网地址）

### 方案 1: 使用 ngrok（推荐用于开发测试）

```bash
# 安装 ngrok
# 下载地址: https://ngrok.com/download

# 启动内网穿透
ngrok http 8080

# 复制生成的 https 地址，例如：
# https://abc123.ngrok.io
```

### 方案 2: 部署到云服务器

将应用部署到阿里云、腾讯云等云服务器，配置域名和 SSL 证书。

### 在天猫精灵开放平台配置

1. 登录 [天猫精灵开放平台](https://aligenie.com)

2. 创建"智能家居"技能

3. 配置 OAuth：
   - 授权地址: `https://your-domain.com/authorize`
   - Token 地址: `https://your-domain.com/token`
   - Client ID: `test_client_id`
   - Client Secret: `test_client_secret`

4. 配置 Webhook：
   - 设备发现: `https://your-domain.com/smarthome/discovery`
   - 设备控制: `https://your-domain.com/smarthome/control`
   - 设备查询: `https://your-domain.com/smarthome/query`

5. 保存并提交审核

### 在天猫精灵 App 中测试

1. 打开天猫精灵 App
2. 进入"智能家居" → "添加设备"
3. 找到你的技能并授权
4. 发现设备后，尝试语音控制：
   - "天猫精灵，打开客厅扫地机器人"
   - "天猫精灵，关闭扫地机器人"
   - "天猫精灵，暂停扫地机器人"

## 常见问题

### Q1: 启动失败，提示数据库连接错误

**A**: 检查 MySQL 是否启动，用户名密码是否正确。

```bash
# 检查 MySQL 状态
systemctl status mysql

# 或
mysql -u root -p
```

### Q2: Token 验证失败

**A**: 检查 Token 是否过期（默认3天），或者 Authorization header 格式是否正确。

正确格式：`Authorization: Bearer YOUR_TOKEN`

### Q3: 设备控制没有反应

**A**: 
1. 检查设备 ID 是否正确
2. 查看应用日志确认请求是否到达
3. 确认数据库中设备数据是否存在

### Q4: 天猫精灵无法访问我的服务

**A**: 
1. 确保使用 HTTPS（天猫精灵要求）
2. 检查防火墙是否开放端口
3. 使用 ngrok 等工具进行内网穿透

## 下一步

- 阅读 [API.md](API.md) 了解完整的 API 文档
- 阅读 [ARCHITECTURE.md](ARCHITECTURE.md) 了解系统架构
- 阅读 [DEPLOYMENT.md](DEPLOYMENT.md) 了解生产环境部署

## 技术支持

如有问题，请查看：
- 项目 Issues
- 天猫精灵开放平台文档
- Spring Boot 官方文档
