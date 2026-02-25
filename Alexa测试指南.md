# Alexa 智能音箱测试指南

## 📋 测试准备

### 1. 环境要求

- ✅ 项目已启动（`mvn spring-boot:run`）
- ✅ 数据库已导入测试数据
- ✅ OAuth 服务正常运行
- ✅ HTTPS 已配置（生产环境）

### 2. 测试账号

| 用户名 | 密码 | 设备数量 |
|--------|------|---------|
| testuser | password123 | 4 台 |

### 3. 测试设备

| 设备ID | 设备名称 | 状态 | 电源 | 模式 | 电量 |
|--------|---------|------|------|------|------|
| robot_001 | Living Room Vacuum | 在线 | 开机 | auto | 85% |
| robot_002 | Bedroom Vacuum | 在线 | 关机 | auto | 100% |
| robot_003 | Study Room Vacuum | 在线 | 开机 | spot | 15% |

## 🧪 测试步骤

### 步骤 1：配置 Alexa Skill

1. **登录 Alexa 开发者控制台**
   - 访问：https://developer.amazon.com/alexa/console/ask
   - 登录 Amazon 开发者账号

2. **创建 Smart Home Skill**
   - 点击"Create Skill"
   - Skill Name：`Smart Vacuum Demo`
   - Default Language：`en-US`
   - Choose a model：`Smart Home`
   - 点击"Create Skill"

3. **配置 Skill 信息**
   - Skill ID：记录下来，后续需要
   - Invocation Name：`smart vacuum`

### 步骤 2：配置 OAuth

1. **在 Skill 配置中找到"Account Linking"**

2. **填写 OAuth 配置**：
   ```
   Authorization URI: https://your-domain.com/authorize
   Access Token URI: https://your-domain.com/token
   Client ID: alexa_client_id
   Client Secret: alexa_client_secret
   Scope: (留空或填写 device:control)
   ```

3. **记录 Redirect URLs**
   - Alexa 会提供 3 个回调地址
   - 将这些地址添加到数据库的 OAuth 客户端配置中

### 步骤 3：配置 Lambda 或 HTTPS 端点

#### 方式一：使用 HTTPS 端点（推荐用于测试）

1. **在 Skill 配置中选择"Smart Home"**
2. **填写端点 URL**：
   ```
   Default endpoint: https://your-domain.com/alexa
   ```
3. **保存配置**

#### 方式二：使用 Lambda 函数

1. **创建 Lambda 函数**
   - 登录 AWS Console
   - 创建新的 Lambda 函数
   - 运行时：Node.js 或 Python
   - 触发器：Alexa Smart Home

2. **配置 Lambda 代码**（转发到你的服务器）：
   ```javascript
   const https = require('https');
   
   exports.handler = async (event) => {
       const options = {
           hostname: 'your-domain.com',
           port: 443,
           path: '/alexa',
           method: 'POST',
           headers: {
               'Content-Type': 'application/json'
           }
       };
       
       return new Promise((resolve, reject) => {
           const req = https.request(options, (res) => {
               let data = '';
               res.on('data', (chunk) => { data += chunk; });
               res.on('end', () => { resolve(JSON.parse(data)); });
           });
           
           req.on('error', reject);
           req.write(JSON.stringify(event));
           req.end();
       });
   };
   ```

3. **在 Skill 配置中填写 Lambda ARN**

### 步骤 4：添加测试数据

在数据库中添加 Alexa OAuth 客户端：

```sql
INSERT INTO oauth_clients (client_id, client_secret, redirect_uri, user_id) 
VALUES (
    'alexa_client_id',
    'alexa_client_secret',
    'https://pitangui.amazon.com/api/skill/link/YOUR_VENDOR_ID',
    1
) ON DUPLICATE KEY UPDATE 
    client_secret = VALUES(client_secret),
    redirect_uri = VALUES(redirect_uri);
```

## 🔧 Postman 测试

### 1. 设备发现测试

**请求**：
```http
POST http://localhost:8080/alexa
Content-Type: application/json

{
  "directive": {
    "header": {
      "namespace": "Alexa.Discovery",
      "name": "Discover",
      "payloadVersion": "3",
      "messageId": "test-message-001"
    },
    "payload": {
      "scope": {
        "type": "BearerToken",
        "token": "YOUR_ACCESS_TOKEN"
      }
    }
  }
}
```

**预期响应**：
```json
{
  "event": {
    "header": {
      "namespace": "Alexa.Discovery",
      "name": "Discover.Response",
      "payloadVersion": "3",
      "messageId": "test-message-001"
    },
    "payload": {
      "endpoints": [
        {
          "endpointId": "robot_001",
          "manufacturerName": "Smart Home Demo",
          "friendlyName": "Living Room Vacuum",
          "description": "Smart Robot Vacuum Cleaner",
          "displayCategories": ["VACUUM_CLEANER"],
          "capabilities": [...]
        }
      ]
    }
  }
}
```

### 2. 开机测试

**请求**：
```http
POST http://localhost:8080/alexa
Content-Type: application/json

{
  "directive": {
    "header": {
      "namespace": "Alexa.PowerController",
      "name": "TurnOn",
      "payloadVersion": "3",
      "messageId": "test-message-002",
      "correlationToken": "test-correlation-001"
    },
    "endpoint": {
      "scope": {
        "type": "BearerToken",
        "token": "YOUR_ACCESS_TOKEN"
      },
      "endpointId": "robot_001",
      "cookie": {}
    },
    "payload": {}
  }
}
```

**预期响应**：
```json
{
  "event": {
    "header": {
      "namespace": "Alexa",
      "name": "Response",
      "payloadVersion": "3",
      "messageId": "...",
      "correlationToken": "test-correlation-001"
    },
    "endpoint": {
      "scope": {
        "type": "BearerToken",
        "token": "YOUR_ACCESS_TOKEN"
      },
      "endpointId": "robot_001"
    },
    "payload": {}
  },
  "context": {
    "properties": [
      {
        "namespace": "Alexa.PowerController",
        "name": "powerState",
        "value": "ON",
        "timeOfSample": "2024-02-24T10:00:00Z",
        "uncertaintyInMilliseconds": 500
      }
    ]
  }
}
```

### 3. 关机测试

**请求**：
```http
POST http://localhost:8080/alexa
Content-Type: application/json

{
  "directive": {
    "header": {
      "namespace": "Alexa.PowerController",
      "name": "TurnOff",
      "payloadVersion": "3",
      "messageId": "test-message-003",
      "correlationToken": "test-correlation-002"
    },
    "endpoint": {
      "scope": {
        "type": "BearerToken",
        "token": "YOUR_ACCESS_TOKEN"
      },
      "endpointId": "robot_001",
      "cookie": {}
    },
    "payload": {}
  }
}
```

### 4. 设置模式测试

**请求**：
```http
POST http://localhost:8080/alexa
Content-Type: application/json

{
  "directive": {
    "header": {
      "namespace": "Alexa.ModeController",
      "name": "SetMode",
      "payloadVersion": "3",
      "messageId": "test-message-004",
      "correlationToken": "test-correlation-003"
    },
    "endpoint": {
      "scope": {
        "type": "BearerToken",
        "token": "YOUR_ACCESS_TOKEN"
      },
      "endpointId": "robot_001",
      "cookie": {}
    },
    "payload": {
      "mode": "Spot"
    }
  }
}
```

## 🎤 语音测试

### 1. 在 Alexa App 中启用技能

1. 打开 Alexa App
2. 进入"Skills & Games"
3. 搜索你的技能名称
4. 点击"Enable to Use"
5. 登录授权（testuser / password123）

### 2. 发现设备

**语音命令**：
```
"Alexa, discover devices"
```

**预期结果**：
- Alexa 会说："I found 3 devices"
- 在 App 中可以看到 3 台扫地机器人

### 3. 控制设备

**开机**：
```
"Alexa, turn on Living Room Vacuum"
"Alexa, turn on the vacuum"
```

**关机**：
```
"Alexa, turn off Living Room Vacuum"
"Alexa, turn off the vacuum"
```

**设置模式**：
```
"Alexa, set Living Room Vacuum to spot mode"
"Alexa, change vacuum to auto mode"
```

## 📊 测试场景

### 场景 1：正常控制流程

1. ✅ 发现设备
2. ✅ 开机
3. ✅ 关机
4. ✅ 设置模式
5. ✅ 验证设备状态

### 场景 2：错误处理

**测试离线设备**：
```
"Alexa, turn on Kitchen Vacuum"  # robot_004 离线
```

**预期结果**：
- Alexa 会说："Kitchen Vacuum is not responding"

**测试不存在的设备**：
```
"Alexa, turn on Nonexistent Vacuum"
```

**预期结果**：
- Alexa 会说："I couldn't find that device"

### 场景 3：Token 验证

**测试无效 Token**：
- 使用过期的 access_token
- 预期返回 INVALID_AUTHORIZATION_CREDENTIAL 错误

### 场景 4：并发控制

**同时控制多个设备**：
```
"Alexa, turn on all vacuums"
```

## 🐛 常见问题

### Q1: 设备发现失败

**问题**：Alexa 说"I couldn't find any devices"

**解决**：
1. 检查 OAuth Token 是否有效
2. 检查数据库中是否有设备数据
3. 查看服务器日志
4. 验证端点 URL 是否正确

### Q2: 控制命令无响应

**问题**：Alexa 说"Sorry, I'm having trouble"

**解决**：
1. 检查设备是否在线
2. 检查 Token 是否有效
3. 查看服务器日志中的错误信息
4. 验证响应格式是否正确

### Q3: OAuth 授权失败

**问题**：无法完成账号关联

**解决**：
1. 检查 OAuth 配置是否正确
2. 验证 redirect_uri 是否匹配
3. 检查 client_id 和 client_secret
4. 确保授权页面可访问

### Q4: Lambda 函数超时

**问题**：Lambda 函数执行超时

**解决**：
1. 增加 Lambda 超时时间（建议 30 秒）
2. 优化后端响应速度
3. 添加重试机制
4. 检查网络连接

## 📝 测试检查清单

### 功能测试
- [ ] 设备发现成功
- [ ] 开机功能正常
- [ ] 关机功能正常
- [ ] 模式切换正常
- [ ] 状态查询正常

### 错误处理
- [ ] 无效 Token 处理
- [ ] 设备不存在处理
- [ ] 设备离线处理
- [ ] 无效参数处理

### 性能测试
- [ ] 响应时间 < 3 秒
- [ ] 并发请求处理
- [ ] 大量设备发现

### 安全测试
- [ ] Token 验证
- [ ] 权限检查
- [ ] HTTPS 加密

## 📞 技术支持

如有问题，请参考：
- [Alexa音箱对接方案.md](Alexa音箱对接方案.md)
- [四平台对接方案完整对比.md](四平台对接方案完整对比.md)
- [Alexa Developer Console](https://developer.amazon.com/alexa/console/ask)

---

**最后更新**：2026-02-24  
**测试状态**：✅ Demo 已完成  
**建议测试时间**：2-3 小时
