# Google Assistant 智能音箱测试指南

## 📋 测试准备

### 1. 环境要求

- ✅ 项目已启动（`mvn spring-boot:run`）
- ✅ 数据库已导入测试数据
- ✅ OAuth 服务正常运行
- ✅ HTTPS 已配置（生产环境必需）
- ✅ 公网域名已配置

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

### 步骤 1：创建 Google Action

1. **登录 Actions Console**
   - 访问：https://console.actions.google.com/
   - 登录 Google 账号

2. **创建 Smart Home Project**
   - 点击 "New project"
   - 输入项目名称：`Smart Vacuum Demo`
   - 选择 "Smart Home"
   - 点击 "Create project"

3. **记录项目信息**
   - Project ID：记录下来，后续需要

### 步骤 2：配置 Account Linking

1. **在 Actions Console 中选择 "Develop" → "Account linking"**

2. **配置 OAuth 信息**：
   ```
   Client ID: google_client_id
   Client Secret: google_client_secret
   Authorization URL: https://your-domain.com/oauth/authorize
   Token URL: https://your-domain.com/oauth/token
   ```

3. **配置 Scopes**（可选）：
   ```
   device:control
   ```

4. **保存配置**

### 步骤 3：配置 Fulfillment

1. **在 Actions Console 中选择 "Develop" → "Actions"**

2. **添加 Fulfillment URL**：
   ```
   https://your-domain.com/google/fulfillment
   ```

3. **确保 URL 支持 HTTPS**

4. **保存配置**

### 步骤 4：添加测试数据

在数据库中添加 Google OAuth 客户端：

```sql
INSERT INTO oauth_clients (client_id, client_secret, redirect_uri, user_id) 
VALUES (
    'google_client_id',
    'google_client_secret',
    'https://oauth-redirect.googleusercontent.com/r/YOUR_PROJECT_ID',
    1
) ON DUPLICATE KEY UPDATE 
    client_secret = VALUES(client_secret),
    redirect_uri = VALUES(redirect_uri);
```

## 🔧 Postman 测试

### 1. SYNC Intent - 设备发现

**请求**：
```http
POST http://localhost:8080/google/fulfillment
Authorization: Bearer YOUR_ACCESS_TOKEN
Content-Type: application/json

{
  "requestId": "test-sync-001",
  "inputs": [{
    "intent": "action.devices.SYNC"
  }]
}
```

**预期响应**：
```json
{
  "requestId": "test-sync-001",
  "payload": {
    "agentUserId": "user_1",
    "deviceList": [
      {
        "id": "robot_001",
        "type": "action.devices.types.VACUUM",
        "traits": [
          "action.devices.traits.StartStop",
          "action.devices.traits.OnOff",
          "action.devices.traits.Dock",
          "action.devices.traits.Modes",
          "action.devices.traits.Locator",
          "action.devices.traits.EnergyStorage"
        ],
        "name": {
          "defaultNames": ["Smart Vacuum"],
          "name": "Living Room Vacuum",
          "nicknames": ["vacuum", "robot"]
        },
        "willReportState": true,
        "roomHint": "Living Room",
        "deviceInfo": {
          "manufacturer": "Smart Home Demo",
          "model": "V1.0",
          "hwVersion": "1.0",
          "swVersion": "1.0.0"
        },
        "attributes": {
          "pausable": true,
          "availableModes": [...],
          "isRechargeable": true,
          "queryOnlyEnergyStorage": true
        }
      }
    ]
  }
}
```

### 2. QUERY Intent - 状态查询

**请求**：
```http
POST http://localhost:8080/google/fulfillment
Authorization: Bearer YOUR_ACCESS_TOKEN
Content-Type: application/json

{
  "requestId": "test-query-001",
  "inputs": [{
    "intent": "action.devices.QUERY",
    "payload": {
      "devices": [
        {"id": "robot_001"}
      ]
    }
  }]
}
```

**预期响应**：
```json
{
  "requestId": "test-query-001",
  "payload": {
    "deviceStates": {
      "robot_001": {
        "online": true,
        "status": "SUCCESS",
        "on": true,
        "isRunning": true,
        "isPaused": false,
        "isDocked": false,
        "currentModeSettings": {
          "clean_mode": "auto"
        },
        "descriptiveCapacityRemaining": "FULL",
        "capacityRemaining": [
          {
            "rawValue": 85,
            "unit": "PERCENTAGE"
          }
        ]
      }
    }
  }
}
```

### 3. EXECUTE Intent - 开机

**请求**：
```http
POST http://localhost:8080/google/fulfillment
Authorization: Bearer YOUR_ACCESS_TOKEN
Content-Type: application/json

{
  "requestId": "test-execute-001",
  "inputs": [{
    "intent": "action.devices.EXECUTE",
    "payload": {
      "commands": [{
        "devices": [
          {"id": "robot_001"}
        ],
        "execution": [{
          "command": "action.devices.commands.OnOff",
          "params": {
            "on": true
          }
        }]
      }]
    }
  }]
}
```

**预期响应**：
```json
{
  "requestId": "test-execute-001",
  "payload": {
    "commands": [{
      "ids": ["robot_001"],
      "status": "SUCCESS",
      "states": {
        "online": true,
        "on": true,
        "isRunning": true
      }
    }]
  }
}
```

### 4. EXECUTE Intent - 启动清扫

**请求**：
```http
POST http://localhost:8080/google/fulfillment
Authorization: Bearer YOUR_ACCESS_TOKEN
Content-Type: application/json

{
  "requestId": "test-execute-002",
  "inputs": [{
    "intent": "action.devices.EXECUTE",
    "payload": {
      "commands": [{
        "devices": [
          {"id": "robot_001"}
        ],
        "execution": [{
          "command": "action.devices.commands.StartStop",
          "params": {
            "start": true
          }
        }]
      }]
    }
  }]
}
```

### 5. EXECUTE Intent - 回充

**请求**：
```http
POST http://localhost:8080/google/fulfillment
Authorization: Bearer YOUR_ACCESS_TOKEN
Content-Type: application/json

{
  "requestId": "test-execute-003",
  "inputs": [{
    "intent": "action.devices.EXECUTE",
    "payload": {
      "commands": [{
        "devices": [
          {"id": "robot_001"}
        ],
        "execution": [{
          "command": "action.devices.commands.Dock"
        }]
      }]
    }
  }]
}
```

### 6. EXECUTE Intent - 设置模式

**请求**：
```http
POST http://localhost:8080/google/fulfillment
Authorization: Bearer YOUR_ACCESS_TOKEN
Content-Type: application/json

{
  "requestId": "test-execute-004",
  "inputs": [{
    "intent": "action.devices.EXECUTE",
    "payload": {
      "commands": [{
        "devices": [
          {"id": "robot_001"}
        ],
        "execution": [{
          "command": "action.devices.commands.SetModes",
          "params": {
            "updateModeSettings": {
              "clean_mode": "spot"
            }
          }
        }]
      }]
    }
  }]
}
```

### 7. EXECUTE Intent - 定位设备

**请求**：
```http
POST http://localhost:8080/google/fulfillment
Authorization: Bearer YOUR_ACCESS_TOKEN
Content-Type: application/json

{
  "requestId": "test-execute-005",
  "inputs": [{
    "intent": "action.devices.EXECUTE",
    "payload": {
      "commands": [{
        "devices": [
          {"id": "robot_001"}
        ],
        "execution": [{
          "command": "action.devices.commands.Locate",
          "params": {
            "silent": false
          }
        }]
      }]
    }
  }]
}
```

### 8. DISCONNECT Intent - 账号解绑

**请求**：
```http
POST http://localhost:8080/google/fulfillment
Authorization: Bearer YOUR_ACCESS_TOKEN
Content-Type: application/json

{
  "requestId": "test-disconnect-001",
  "inputs": [{
    "intent": "action.devices.DISCONNECT"
  }]
}
```

**预期响应**：
```json
{
  "requestId": "test-disconnect-001"
}
```

## 🎤 语音测试

### 1. 在 Google Home App 中添加设备

1. 打开 Google Home App
2. 点击 "+" → "Set up device"
3. 选择 "Works with Google"
4. 搜索你的 Action 名称
5. 点击并登录授权（testuser / password123）

### 2. 发现设备

**语音命令**：
```
"Hey Google, sync my devices"
```

**预期结果**：
- Google 会说："I found 3 devices"
- 在 App 中可以看到 3 台扫地机器人

### 3. 控制设备

**开关机**：
```
"Hey Google, turn on Living Room Vacuum"
"Hey Google, turn off the vacuum"
```

**启动/停止清扫**：
```
"Hey Google, start the vacuum"
"Hey Google, stop the vacuum"
"Hey Google, pause the vacuum"
```

**回充**：
```
"Hey Google, send the vacuum home"
"Hey Google, dock the vacuum"
```

**设置模式**：
```
"Hey Google, set vacuum to spot cleaning mode"
"Hey Google, change vacuum to auto mode"
```

**定位设备**：
```
"Hey Google, find my vacuum"
"Hey Google, locate the vacuum"
```

**状态查询**：
```
"Hey Google, is the vacuum running?"
"Hey Google, what's the battery level of the vacuum?"
```

## 📊 测试场景

### 场景 1：正常控制流程

1. ✅ 发现设备
2. ✅ 查询状态
3. ✅ 开机
4. ✅ 启动清扫
5. ✅ 暂停
6. ✅ 继续
7. ✅ 回充
8. ✅ 关机

### 场景 2：模式控制

1. ✅ 设置自动模式
2. ✅ 设置定点模式
3. ✅ 设置沿边模式
4. ✅ 查询当前模式

### 场景 3：错误处理

**测试离线设备**：
```
"Hey Google, turn on Kitchen Vacuum"  # robot_004 离线
```

**预期结果**：
- Google 会说："Kitchen Vacuum is offline"

**测试不存在的设备**：
```
"Hey Google, turn on Nonexistent Vacuum"
```

**预期结果**：
- Google 会说："I couldn't find that device"

### 场景 4：Token 验证

**测试无效 Token**：
- 使用过期的 access_token
- 预期返回 authFailure 错误

### 场景 5：批量操作

**同时控制多个设备**：
```
"Hey Google, turn on all vacuums"
```

## 🐛 常见问题

### Q1: 设备发现失败

**问题**：Google 说"I couldn't find any devices"

**解决**：
1. 检查 OAuth Token 是否有效
2. 检查数据库中是否有设备数据
3. 查看服务器日志
4. 验证 Fulfillment URL 是否正确
5. 确保 HTTPS 配置正确

### Q2: 控制命令无响应

**问题**：Google 说"Sorry, something went wrong"

**解决**：
1. 检查设备是否在线
2. 检查 Token 是否有效
3. 查看服务器日志中的错误信息
4. 验证响应格式是否正确
5. 检查命令参数是否正确

### Q3: OAuth 授权失败

**问题**：无法完成账号关联

**解决**：
1. 检查 OAuth 配置是否正确
2. 验证 redirect_uri 是否匹配
3. 检查 client_id 和 client_secret
4. 确保授权页面可访问
5. 检查 HTTPS 证书是否有效

### Q4: Fulfillment 超时

**问题**：请求超时

**解决**：
1. 优化后端响应速度（< 5 秒）
2. 检查网络连接
3. 添加日志监控
4. 检查数据库查询性能

## 📝 测试检查清单

### 功能测试
- [ ] SYNC Intent 正常
- [ ] QUERY Intent 正常
- [ ] EXECUTE - OnOff 正常
- [ ] EXECUTE - StartStop 正常
- [ ] EXECUTE - Dock 正常
- [ ] EXECUTE - SetModes 正常
- [ ] EXECUTE - Locate 正常
- [ ] DISCONNECT Intent 正常

### 错误处理
- [ ] 无效 Token 处理
- [ ] 设备不存在处理
- [ ] 设备离线处理
- [ ] 无效参数处理
- [ ] 内部错误处理

### 性能测试
- [ ] 响应时间 < 5 秒
- [ ] 并发请求处理
- [ ] 大量设备发现

### 安全测试
- [ ] Token 验证
- [ ] HTTPS 加密
- [ ] 权限检查

## 📞 技术支持

如有问题，请参考：
- [GoogleAssistant音箱对接方案.md](GoogleAssistant音箱对接方案.md)
- [五平台智能音箱完整对比.md](五平台智能音箱完整对比.md)
- [Google Smart Home Documentation](https://developers.google.com/assistant/smarthome)

---

**最后更新**：2026-02-24  
**测试状态**：✅ 代码已完成  
**建议测试时间**：3-4 小时
