# Alexa 控制器使用说明

## 📖 概述

本文档说明如何使用和测试完善后的 Alexa Smart Home 控制器。

## 🎯 功能特性

### 支持的接口

| 接口 | 命名空间 | 操作 | 说明 | 状态 |
|------|---------|------|------|------|
| 授权管理 | Alexa.Authorization | AcceptGrant | 接受授权 | ✅ 已完成 |
| 设备发现 | Alexa.Discovery | Discover | 返回用户的所有设备 | ✅ 已完成 |
| 电源控制 | Alexa.PowerController | TurnOn, TurnOff | 开关设备 | ✅ 已完成 |
| 模式控制 | Alexa.ModeController | SetMode, AdjustMode | 设置清扫模式 | ✅ 已完成 |
| 状态报告 | Alexa | ReportState | 查询设备状态 | ✅ 已完成 |
| 健康状态 | Alexa.EndpointHealth | - | 设备连接状态 | ✅ 已完成 |
| 状态推送 | Alexa | ChangeReport | 主动推送状态变化 | ✅ 已完成（待集成） |

### Token 管理功能（新增）

- ✅ **Token 交换**：用授权码换取 Alexa Access Token
- ✅ **Token 保存**：安全存储到数据库
- ✅ **Token 刷新**：自动刷新过期的 Token（提前 5 分钟）
- ✅ **Token 获取**：获取有效的 Access Token

### 支持的清扫模式

- **Auto** - 自动模式
- **Spot** - 定点清扫
- **Edge** - 沿边清扫

## 🚀 快速开始

### 1. 启动应用

```bash
# 方式一: 使用 Maven
mvn spring-boot:run

# 方式二: 使用启动脚本
# Windows
start.bat

# Linux/Mac
./start.sh
```

### 2. 获取访问令牌

#### 步骤 1: 获取授权码

```http
GET http://localhost:8080/authorize?client_id=alexa_client_id&redirect_uri=https://example.com/callback&state=xyz&response_type=code
```

#### 步骤 2: 交换访问令牌

```http
POST http://localhost:8080/token
Content-Type: application/x-www-form-urlencoded

grant_type=authorization_code&client_id=alexa_client_id&client_secret=alexa_client_secret&code=授权码
```

响应:
```json
{
  "access_token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "token_type": "Bearer",
  "expires_in": 259200,
  "refresh_token": "tGzv3JOkF0XG5Qx2TlKWIA..."
}
```

### 3. 测试接口

#### 方式一: 使用快速测试脚本

```bash
# Windows
test-alexa-quick.bat

# 注意: 需要先修改脚本中的 TOKEN 变量为实际的 access_token
```

#### 方式二: 使用 Postman

1. 导入 `Alexa_Test_Requests.json`
2. 替换所有 `YOUR_ACCESS_TOKEN_HERE` 为实际的 token
3. 按顺序执行测试用例

#### 方式三: 使用 curl

```bash
# 设备发现
curl -X POST http://localhost:8080/alexa \
  -H "Content-Type: application/json" \
  -d '{
    "directive": {
      "header": {
        "namespace": "Alexa.Discovery",
        "name": "Discover",
        "payloadVersion": "3",
        "messageId": "test-001"
      },
      "payload": {
        "scope": {
          "type": "BearerToken",
          "token": "YOUR_ACCESS_TOKEN"
        }
      }
    }
  }'
```

## 📝 接口详细说明

### 0. AcceptGrant 授权接受（新增）

**请求示例:**
```json
{
  "directive": {
    "header": {
      "namespace": "Alexa.Authorization",
      "name": "AcceptGrant",
      "payloadVersion": "3",
      "messageId": "unique-message-id"
    },
    "payload": {
      "grant": {
        "type": "OAuth2.AuthorizationCode",
        "code": "authorization-code"
      },
      "grantee": {
        "type": "BearerToken",
        "token": "grantee-token"
      }
    }
  }
}
```

**响应示例:**
```json
{
  "event": {
    "header": {
      "namespace": "Alexa.Authorization",
      "name": "AcceptGrant.Response",
      "payloadVersion": "3",
      "messageId": "response-message-id"
    },
    "payload": {}
  }
}
```

**功能说明**：
- 当用户在 Alexa App 中启用技能时调用
- 用授权码换取 Alexa Access Token
- 保存 Token 到数据库供后续使用
- 为主动状态推送（ChangeReport）做准备

**实现状态**：✅ 已完成  
**代码位置**：`AlexaController.handleAcceptGrant()`

### 1. 设备发现 (Discovery)

**请求示例:**
```json
{
  "directive": {
    "header": {
      "namespace": "Alexa.Discovery",
      "name": "Discover",
      "payloadVersion": "3",
      "messageId": "unique-message-id"
    },
    "payload": {
      "scope": {
        "type": "BearerToken",
        "token": "access-token-from-skill"
      }
    }
  }
}
```

**响应示例:**
```json
{
  "event": {
    "header": {
      "namespace": "Alexa.Discovery",
      "name": "Discover.Response",
      "payloadVersion": "3",
      "messageId": "unique-message-id"
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

### 2. 电源控制 (PowerController)

**开机请求:**
```json
{
  "directive": {
    "header": {
      "namespace": "Alexa.PowerController",
      "name": "TurnOn",
      "payloadVersion": "3",
      "messageId": "unique-message-id",
      "correlationToken": "correlation-token"
    },
    "endpoint": {
      "scope": {
        "type": "BearerToken",
        "token": "access-token"
      },
      "endpointId": "robot_001",
      "cookie": {}
    },
    "payload": {}
  }
}
```

**响应示例:**
```json
{
  "event": {
    "header": {
      "namespace": "Alexa",
      "name": "Response",
      "payloadVersion": "3",
      "messageId": "response-message-id",
      "correlationToken": "correlation-token"
    },
    "endpoint": {
      "scope": {
        "type": "BearerToken",
        "token": "access-token"
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
        "timeOfSample": "2024-02-25T10:00:00Z",
        "uncertaintyInMilliseconds": 500
      },
      {
        "namespace": "Alexa.EndpointHealth",
        "name": "connectivity",
        "value": {"value": "OK"},
        "timeOfSample": "2024-02-25T10:00:00Z",
        "uncertaintyInMilliseconds": 500
      }
    ]
  }
}
```

### 3. 模式控制 (ModeController)

**设置模式请求:**
```json
{
  "directive": {
    "header": {
      "namespace": "Alexa.ModeController",
      "name": "SetMode",
      "payloadVersion": "3",
      "messageId": "unique-message-id",
      "correlationToken": "correlation-token"
    },
    "endpoint": {
      "scope": {
        "type": "BearerToken",
        "token": "access-token"
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

**支持的模式值:**
- `Auto` 或 `auto` - 自动模式
- `Spot` 或 `spot` - 定点清扫
- `Edge` 或 `edge` - 沿边清扫

### 4. 状态报告 (ReportState)

**请求示例:**
```json
{
  "directive": {
    "header": {
      "namespace": "Alexa",
      "name": "ReportState",
      "payloadVersion": "3",
      "messageId": "unique-message-id",
      "correlationToken": "correlation-token"
    },
    "endpoint": {
      "scope": {
        "type": "BearerToken",
        "token": "access-token"
      },
      "endpointId": "robot_001",
      "cookie": {}
    },
    "payload": {}
  }
}
```

**响应示例:**
```json
{
  "event": {
    "header": {
      "namespace": "Alexa",
      "name": "StateReport",
      "payloadVersion": "3",
      "messageId": "response-message-id",
      "correlationToken": "correlation-token"
    },
    "endpoint": {
      "scope": {
        "type": "BearerToken",
        "token": "access-token"
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
        "timeOfSample": "2024-02-25T10:00:00Z",
        "uncertaintyInMilliseconds": 500
      },
      {
        "namespace": "Alexa.ModeController",
        "name": "mode",
        "value": "Auto",
        "timeOfSample": "2024-02-25T10:00:00Z",
        "uncertaintyInMilliseconds": 500
      },
      {
        "namespace": "Alexa.EndpointHealth",
        "name": "connectivity",
        "value": {"value": "OK"},
        "timeOfSample": "2024-02-25T10:00:00Z",
        "uncertaintyInMilliseconds": 500
      }
    ]
  }
}
```

## ❌ 错误处理

### 错误响应格式

```json
{
  "event": {
    "header": {
      "namespace": "Alexa",
      "name": "ErrorResponse",
      "payloadVersion": "3",
      "messageId": "response-message-id",
      "correlationToken": "correlation-token"
    },
    "endpoint": {
      "endpointId": "robot_001"
    },
    "payload": {
      "type": "ERROR_TYPE",
      "message": "错误描述"
    }
  }
}
```

### 错误类型

| 错误类型 | 说明 | 触发条件 |
|---------|------|---------|
| INVALID_AUTHORIZATION_CREDENTIAL | Token 无效 | Token 过期或不存在 |
| NO_SUCH_ENDPOINT | 设备不存在 | 设备 ID 不存在 |
| ENDPOINT_UNREACHABLE | 设备离线 | 设备状态为 offline |
| INVALID_VALUE | 参数值无效 | 模式值不支持 |
| INVALID_DIRECTIVE | 不支持的操作 | 命名空间或操作名称错误 |
| INTERNAL_ERROR | 服务器错误 | 服务器内部异常 |

## 🎤 语音命令示例

配置完成后，可以使用以下语音命令:

```
"Alexa, discover devices"
"Alexa, turn on Living Room Vacuum"
"Alexa, turn off the vacuum"
"Alexa, set Living Room Vacuum to spot mode"
"Alexa, set the vacuum to auto mode"
"Alexa, what's the status of Living Room Vacuum?"
```

## 🔍 调试技巧

### 1. 查看日志

应用启动后会输出详细的日志:

```
=== 收到 Alexa 请求 ===
Namespace: Alexa.PowerController
Name: TurnOn
MessageId: test-001
找到设备: deviceName=Living Room Vacuum, currentPowerState=off, status=online
✓ 设备开机成功: endpointId=robot_001, deviceName=Living Room Vacuum
电源控制响应已生成: powerState=ON
```

### 2. 使用 Postman

1. 导入测试集合
2. 设置环境变量 `access_token`
3. 查看响应的 JSON 格式
4. 检查响应时间

### 3. 检查数据库

```sql
-- 查看设备状态
SELECT device_id, device_name, power_state, work_mode, status 
FROM devices 
WHERE user_id = 1;

-- 查看 Token
SELECT access_token, expires_at 
FROM oauth_access_tokens 
WHERE client_id = 'alexa_client_id';
```

## 📚 相关文档

- [Alexa测试指南.md](Alexa测试指南.md) - 完整测试流程
- [Alexa控制器完善总结.md](Alexa控制器完善总结.md) - 改进说明
- [Alexa_Test_Requests.json](Alexa_Test_Requests.json) - Postman 测试集合
- [API.md](API.md) - 完整 API 文档

## 🐛 常见问题

### Q1: Token 验证失败

**问题**: 返回 `INVALID_AUTHORIZATION_CREDENTIAL` 错误

**解决**:
1. 检查 Token 是否过期
2. 重新获取 Token
3. 确认 Token 格式正确

### Q2: 设备不存在

**问题**: 返回 `NO_SUCH_ENDPOINT` 错误

**解决**:
1. 检查设备 ID 是否正确
2. 确认数据库中有该设备
3. 验证设备属于当前用户

### Q3: 设备离线

**问题**: 返回 `ENDPOINT_UNREACHABLE` 错误

**解决**:
1. 检查设备状态字段
2. 更新设备状态为 `online`
3. 确认设备网络连接

### Q4: 模式值无效

**问题**: 返回 `INVALID_VALUE` 错误

**解决**:
1. 检查模式值拼写
2. 使用支持的模式: Auto, Spot, Edge
3. 大小写不敏感

## 💡 最佳实践

1. **Token 管理**
   - 定期刷新 Token
   - 安全存储 Token
   - 处理 Token 过期

2. **错误处理**
   - 捕获所有异常
   - 返回友好的错误消息
   - 记录详细的错误日志

3. **性能优化**
   - 缓存设备信息
   - 异步处理耗时操作
   - 使用连接池

4. **安全建议**
   - 使用 HTTPS
   - 验证所有输入
   - 限制请求频率

## 📞 技术支持

如有问题,请:
1. 查看日志文件
2. 参考相关文档
3. 提交 Issue

---

**版本**: 2.0.0  
**更新时间**: 2026-02-25  
**状态**: ✅ 核心功能已完成（85%）  
**实现进度**：
- ✅ AcceptGrant 授权流程
- ✅ Token 管理（交换、保存、刷新）
- ✅ ChangeReport 状态推送
- ✅ 所有控制接口
- ⏳ AlexaStateReporter 集成（待完成）
