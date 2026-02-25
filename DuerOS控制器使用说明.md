# 小度音箱（DuerOS）控制器使用说明

## 📖 概述

DuerOS 控制器实现了百度小度音箱智能家居技能的完整功能，支持扫地机器人的语音控制。

## 🎯 支持的功能

### 1. 设备发现
- 自动发现用户绑定的所有设备
- 返回设备详细信息和支持的操作

### 2. 设备控制
- **开机/关机**: 控制扫地机器人电源
- **暂停/继续**: 控制清扫任务
- **模式切换**: 切换清扫模式（自动/定点/沿边）
- **状态查询**: 查询设备当前状态

## 🔌 API 接口

### 1. 设备发现接口

**端点**: `POST /dueros/discovery`

**请求头**:
```
Authorization: Bearer {access_token}
Content-Type: application/json
```

**请求体**:
```json
{
  "header": {
    "namespace": "DuerOS.ConnectedHome.Discovery",
    "name": "DiscoverAppliancesRequest",
    "messageId": "unique-message-id",
    "payloadVersion": "1.0"
  },
  "payload": {
    "accessToken": "your-access-token"
  }
}
```

**响应示例**:
```json
{
  "header": {
    "namespace": "DuerOS.ConnectedHome.Discovery",
    "name": "DiscoverAppliancesResponse",
    "messageId": "unique-message-id",
    "payloadVersion": "1.0"
  },
  "payload": {
    "discoveredAppliances": [
      {
        "applianceId": "robot_001",
        "manufacturerName": "Smart Home Demo",
        "modelName": "智能扫地机器人",
        "version": "1.0",
        "friendlyName": "客厅扫地机器人",
        "friendlyDescription": "智能扫地机器人，支持语音控制",
        "isReachable": true,
        "applianceTypes": ["ROBOT_CLEANER"],
        "actions": ["turnOn", "turnOff", "pause", "continue", "setMode", "getState"],
        "additionalApplianceDetails": {
          "powerState": "off",
          "workMode": "auto",
          "batteryLevel": 85,
          "status": "online"
        }
      }
    ]
  }
}
```

### 2. 设备控制接口

**端点**: `POST /dueros/control`

**请求头**:
```
Authorization: Bearer {access_token}
Content-Type: application/json
```

#### 2.1 开机

**请求体**:
```json
{
  "header": {
    "namespace": "DuerOS.ConnectedHome.Control",
    "name": "TurnOnRequest",
    "messageId": "unique-message-id",
    "payloadVersion": "1.0"
  },
  "payload": {
    "accessToken": "your-access-token",
    "appliance": {
      "applianceId": "robot_001"
    }
  }
}
```

**响应示例**:
```json
{
  "header": {
    "namespace": "DuerOS.ConnectedHome.Control",
    "name": "TurnOnConfirmation",
    "messageId": "unique-message-id",
    "payloadVersion": "1.0"
  },
  "payload": {}
}
```

#### 2.2 关机

**请求体**:
```json
{
  "header": {
    "namespace": "DuerOS.ConnectedHome.Control",
    "name": "TurnOffRequest",
    "messageId": "unique-message-id",
    "payloadVersion": "1.0"
  },
  "payload": {
    "accessToken": "your-access-token",
    "appliance": {
      "applianceId": "robot_001"
    }
  }
}
```

#### 2.3 暂停

**请求体**:
```json
{
  "header": {
    "namespace": "DuerOS.ConnectedHome.Control",
    "name": "PauseRequest",
    "messageId": "unique-message-id",
    "payloadVersion": "1.0"
  },
  "payload": {
    "accessToken": "your-access-token",
    "appliance": {
      "applianceId": "robot_001"
    }
  }
}
```

#### 2.4 继续

**请求体**:
```json
{
  "header": {
    "namespace": "DuerOS.ConnectedHome.Control",
    "name": "ContinueRequest",
    "messageId": "unique-message-id",
    "payloadVersion": "1.0"
  },
  "payload": {
    "accessToken": "your-access-token",
    "appliance": {
      "applianceId": "robot_001"
    }
  }
}
```

#### 2.5 设置模式

**请求体**:
```json
{
  "header": {
    "namespace": "DuerOS.ConnectedHome.Control",
    "name": "SetModeRequest",
    "messageId": "unique-message-id",
    "payloadVersion": "1.0"
  },
  "payload": {
    "accessToken": "your-access-token",
    "appliance": {
      "applianceId": "robot_001"
    },
    "additionalInfo": {
      "mode": "auto"
    }
  }
}
```

**支持的模式**:
- `auto` - 自动清扫模式
- `spot` - 定点清扫模式
- `edge` - 沿边清扫模式

#### 2.6 查询状态

**请求体**:
```json
{
  "header": {
    "namespace": "DuerOS.ConnectedHome.Query",
    "name": "GetStateRequest",
    "messageId": "unique-message-id",
    "payloadVersion": "1.0"
  },
  "payload": {
    "accessToken": "your-access-token",
    "appliance": {
      "applianceId": "robot_001"
    }
  }
}
```

**响应示例**:
```json
{
  "header": {
    "namespace": "DuerOS.ConnectedHome.Query",
    "name": "GetStateResponse",
    "messageId": "unique-message-id",
    "payloadVersion": "1.0"
  },
  "payload": {
    "deviceState": {
      "powerState": "on",
      "workMode": "auto",
      "batteryLevel": 85,
      "status": "online"
    }
  }
}
```

## ⚠️ 错误响应

### 错误格式

```json
{
  "header": {
    "namespace": "DuerOS.ConnectedHome.Control",
    "name": "ErrorResponse",
    "messageId": "unique-message-id",
    "payloadVersion": "1.0"
  },
  "payload": {
    "errorCode": "ERROR_CODE",
    "message": "错误描述信息"
  }
}
```

### 错误代码列表

| 错误代码 | HTTP状态 | 说明 | 解决方案 |
|---------|---------|------|---------|
| `INVALID_TOKEN` | 401 | 访问令牌无效或已过期 | 重新获取 access_token |
| `DEVICE_NOT_FOUND` | 200 | 设备不存在 | 检查设备ID是否正确 |
| `DEVICE_OFFLINE` | 200 | 设备离线或无法访问 | 检查设备网络连接 |
| `INVALID_MODE` | 200 | 无效的工作模式 | 使用支持的模式值 |
| `MISSING_PARAMETER` | 200 | 缺少必需参数 | 检查请求参数完整性 |
| `UNSUPPORTED_ACTION` | 200 | 不支持的操作 | 检查操作名称是否正确 |
| `INTERNAL_ERROR` | 200 | 服务器内部错误 | 联系技术支持 |

## 🎮 语音控制示例

配置完成后，可以使用以下语音命令：

### 基础控制
- "小度小度，打开扫地机器人"
- "小度小度，关闭扫地机器人"
- "小度小度，暂停扫地机器人"
- "小度小度，继续扫地"

### 模式控制
- "小度小度，扫地机器人切换到自动模式"
- "小度小度，扫地机器人切换到定点模式"
- "小度小度，扫地机器人切换到沿边模式"

### 状态查询
- "小度小度，扫地机器人的状态"
- "小度小度，扫地机器人的电量"

## 🔧 快速测试

### 使用测试脚本

Windows 系统：
```bash
test-dueros-quick.bat
```

### 使用 curl 命令

1. 获取 access_token（参考 OAuth2 文档）

2. 测试设备发现：
```bash
curl -X POST "http://localhost:8080/dueros/discovery" \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -H "Content-Type: application/json" \
  -d @discovery_request.json
```

3. 测试设备控制：
```bash
curl -X POST "http://localhost:8080/dueros/control" \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -H "Content-Type: application/json" \
  -d @control_request.json
```

### 使用 Postman

1. 导入 `DuerOS_Test_Requests.json`
2. 设置环境变量 `ACCESS_TOKEN`
3. 依次执行测试用例

## 📊 日志说明

### 正常操作日志

```
=== 收到小度设备发现请求 ===
MessageId: test-discovery-001
Namespace: DuerOS.ConnectedHome.Discovery
Name: DiscoverAppliancesRequest
查询到用户设备: userId=1, totalDevices=1
添加设备: applianceId=robot_001, friendlyName=客厅扫地机器人
小度设备发现完成: userId=1, discoveredDevices=1
```

```
=== 收到小度设备控制请求 ===
Action: TurnOnRequest
MessageId: test-control-001
设备ID: robot_001
找到设备: deviceName=客厅扫地机器人, currentPowerState=off, status=online
✓ 设备开机成功: deviceId=robot_001, deviceName=客厅扫地机器人
```

### 错误日志

```
Token 验证失败: token=null
```

```
设备不存在: deviceId=non_existent_device
```

```
设备离线: deviceId=robot_001, status=offline
```

## 🔍 故障排查

### 问题 1: Token 验证失败

**症状**: 返回 401 Unauthorized

**检查项**:
1. Token 是否正确
2. Token 是否过期
3. Authorization header 格式是否正确

**解决方案**:
```bash
# 重新获取 token
curl -X POST "http://localhost:8080/token" \
  -d "grant_type=authorization_code" \
  -d "client_id=xiaodu_client_id" \
  -d "client_secret=xiaodu_client_secret" \
  -d "code=YOUR_CODE"
```

### 问题 2: 设备不存在

**症状**: 返回 DEVICE_NOT_FOUND 错误

**检查项**:
1. 设备 ID 是否正确
2. 数据库中是否有该设备
3. 用户 ID 是否匹配

**解决方案**:
```sql
-- 检查设备是否存在
SELECT * FROM devices WHERE device_id = 'robot_001';

-- 检查设备所属用户
SELECT * FROM devices WHERE device_id = 'robot_001' AND user_id = 1;
```

### 问题 3: 设备离线

**症状**: 返回 DEVICE_OFFLINE 错误

**检查项**:
1. 设备状态是否为 online
2. 设备网络连接是否正常

**解决方案**:
```sql
-- 更新设备状态
UPDATE devices SET status = 'online' WHERE device_id = 'robot_001';
```

### 问题 4: 模式设置失败

**症状**: 返回 INVALID_MODE 错误

**检查项**:
1. 模式值是否为支持的值（auto/spot/edge）
2. 模式值是否为小写

**解决方案**:
```json
{
  "additionalInfo": {
    "mode": "auto"  // 必须是小写
  }
}
```

## 📈 性能优化建议

### 1. 数据库索引
```sql
-- 为常用查询字段添加索引
CREATE INDEX idx_device_id ON devices(device_id);
CREATE INDEX idx_user_id ON devices(user_id);
CREATE INDEX idx_status ON devices(status);
```

### 2. Token 缓存
- 使用 Redis 缓存有效的 token
- 减少数据库查询次数

### 3. 连接池配置
```yaml
spring:
  datasource:
    hikari:
      maximum-pool-size: 20
      minimum-idle: 5
```

## 🔐 安全建议

### 1. HTTPS
生产环境必须使用 HTTPS：
```yaml
server:
  ssl:
    enabled: true
    key-store: classpath:keystore.p12
    key-store-password: your-password
```

### 2. Token 过期时间
```java
// 建议设置合理的过期时间
private static final long TOKEN_EXPIRY = 3 * 24 * 60 * 60 * 1000; // 3天
```

### 3. 请求频率限制
```java
@RateLimiter(name = "dueros", fallbackMethod = "rateLimitFallback")
public ResponseEntity<?> control(...) {
    // ...
}
```

## 📚 相关文档

- [DuerOS控制器完善总结.md](./DuerOS控制器完善总结.md) - 完善工作总结
- [DuerOS官方规范对照清单.md](./DuerOS官方规范对照清单.md) - API规范对照
- [小度音箱测试指南.md](./小度音箱测试指南.md) - 详细测试指南
- [OAuth2使用指南.md](./OAuth2使用指南.md) - OAuth 授权流程

## 💡 最佳实践

### 1. 错误处理
- 始终返回明确的错误信息
- 使用标准的错误代码
- 记录详细的错误日志

### 2. 日志记录
- 记录所有关键操作
- 使用结构化日志格式
- 包含足够的上下文信息

### 3. 参数验证
- 验证所有输入参数
- 使用白名单验证
- 提供清晰的验证错误信息

### 4. 响应格式
- 遵循 DuerOS API 规范
- 保持响应格式一致
- 包含必要的元数据

## 🎓 开发指南

### 添加新的控制操作

1. 在 `executeControl` 方法中添加新的 case：
```java
case "NewActionRequest":
    // 实现新操作
    response = DuerOSResponse.controlConfirmation("NewAction", messageId);
    log.info("✓ 新操作成功: deviceId={}", deviceId);
    break;
```

2. 更新设备发现中的 actions 列表：
```java
List<String> actions = Arrays.asList(
    "turnOn", "turnOff", "pause", "continue", "setMode", "getState", "newAction"
);
```

3. 添加测试用例到 `DuerOS_Test_Requests.json`

4. 更新文档

## 📞 技术支持

如有问题，请：
1. 查看日志文件
2. 参考故障排查章节
3. 查阅相关文档
4. 联系开发团队

---

**版本**: 1.0  
**最后更新**: 2026-02-25  
**维护者**: Voice Platform Team
