# API 接口文档

## 1. OAuth 授权接口

### 1.1 获取授权码

**请求方式**: GET

**请求地址**: `/authorize`

**请求参数**:

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| client_id | String | 是 | 客户端ID |
| redirect_uri | String | 是 | 回调地址 |
| state | String | 是 | 状态码，用于防止CSRF攻击 |
| response_type | String | 是 | 固定值：code |

**示例**:
```
GET /authorize?client_id=test_client_id&redirect_uri=https://aligenie.com/callback&state=xyz&response_type=code
```

**响应**: 返回授权页面，用户确认后重定向到 redirect_uri

### 1.2 获取访问令牌

**请求方式**: POST

**请求地址**: `/token`

**请求参数**:

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| grant_type | String | 是 | authorization_code 或 refresh_token |
| client_id | String | 是 | 客户端ID |
| client_secret | String | 是 | 客户端密钥 |
| code | String | 条件 | 授权码（grant_type=authorization_code时必填） |
| refresh_token | String | 条件 | 刷新令牌（grant_type=refresh_token时必填） |

**响应示例**:
```json
{
  "access_token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "token_type": "Bearer",
  "expires_in": 259200,
  "refresh_token": "tGzv3JOkF0XG5Qx2TlKWIA..."
}
```

## 2. 智能家居接口

所有智能家居接口都需要在 Header 中携带访问令牌：
```
Authorization: Bearer YOUR_ACCESS_TOKEN
```

### 2.1 设备发现

**请求方式**: POST

**请求地址**: `/smarthome/discovery`

**请求示例**:
```json
{
  "header": {
    "namespace": "AliGenie.Iot.Device.Discovery",
    "name": "DiscoveryRequest",
    "messageId": "1234567890",
    "payloadVersion": "1"
  },
  "payload": {}
}
```

**响应示例**:
```json
{
  "header": {
    "namespace": "AliGenie.Iot.Device.Discovery",
    "name": "DiscoveryResponse",
    "messageId": "1234567890",
    "payloadVersion": "1"
  },
  "payload": {
    "devices": [
      {
        "deviceId": "robot_001",
        "deviceName": "客厅扫地机器人",
        "deviceType": "robot_cleaner",
        "zone": "客厅",
        "brand": "自定义品牌",
        "model": "V1.0",
        "icon": "https://example.com/icon.png",
        "properties": {
          "powerstate": "off",
          "mode": "auto",
          "battery": 85
        },
        "actions": ["TurnOn", "TurnOff", "Pause", "Continue", "SetMode"]
      }
    ]
  }
}
```

### 2.2 设备控制 - 开机

**请求方式**: POST

**请求地址**: `/smarthome/control`

**请求示例**:
```json
{
  "header": {
    "namespace": "AliGenie.Iot.Device.Control",
    "name": "TurnOn",
    "messageId": "1234567891",
    "payloadVersion": "1"
  },
  "payload": {
    "deviceId": "robot_001"
  }
}
```

**响应示例**:
```json
{
  "header": {
    "namespace": "AliGenie.Iot.Device.Control",
    "name": "TurnOnResponse",
    "messageId": "1234567891",
    "payloadVersion": "1"
  },
  "payload": {}
}
```

### 2.3 设备控制 - 关机

**请求示例**:
```json
{
  "header": {
    "namespace": "AliGenie.Iot.Device.Control",
    "name": "TurnOff",
    "messageId": "1234567892",
    "payloadVersion": "1"
  },
  "payload": {
    "deviceId": "robot_001"
  }
}
```

### 2.4 设备控制 - 暂停

**请求示例**:
```json
{
  "header": {
    "namespace": "AliGenie.Iot.Device.Control",
    "name": "Pause",
    "messageId": "1234567893",
    "payloadVersion": "1"
  },
  "payload": {
    "deviceId": "robot_001"
  }
}
```

### 2.5 设备控制 - 继续

**请求示例**:
```json
{
  "header": {
    "namespace": "AliGenie.Iot.Device.Control",
    "name": "Continue",
    "messageId": "1234567894",
    "payloadVersion": "1"
  },
  "payload": {
    "deviceId": "robot_001"
  }
}
```

### 2.6 设备控制 - 设置模式

**请求示例**:
```json
{
  "header": {
    "namespace": "AliGenie.Iot.Device.Control",
    "name": "SetMode",
    "messageId": "1234567895",
    "payloadVersion": "1"
  },
  "payload": {
    "deviceId": "robot_001",
    "mode": "spot"
  }
}
```

**支持的模式**:
- `auto`: 自动模式
- `spot`: 定点清扫
- `edge`: 沿边清扫

### 2.7 设备查询

**请求方式**: POST

**请求地址**: `/smarthome/query`

**请求示例**:
```json
{
  "header": {
    "namespace": "AliGenie.Iot.Device.Query",
    "name": "QueryRequest",
    "messageId": "1234567896",
    "payloadVersion": "1"
  },
  "payload": {
    "deviceId": "robot_001"
  }
}
```

**响应示例**:
```json
{
  "header": {
    "namespace": "AliGenie.Iot.Device.Query",
    "name": "QueryResponse",
    "messageId": "1234567896",
    "payloadVersion": "1"
  },
  "payload": {
    "properties": {
      "powerstate": "on",
      "mode": "auto",
      "battery": 85
    }
  }
}
```

## 3. 错误响应

当发生错误时，返回以下格式：

```json
{
  "header": {
    "namespace": "AliGenie.Iot.Device.Error",
    "name": "ErrorResponse",
    "messageId": "1234567890",
    "payloadVersion": "1"
  },
  "payload": {
    "errorCode": "DEVICE_NOT_FOUND",
    "message": "设备不存在"
  }
}
```

**常见错误码**:

| 错误码 | 说明 |
|--------|------|
| INVALID_TOKEN | 访问令牌无效或已过期 |
| DEVICE_NOT_FOUND | 设备不存在 |
| UNSUPPORTED_ACTION | 不支持的操作 |
| DEVICE_OFFLINE | 设备离线 |

## 4. 语音控制示例

配置完成后，可以通过以下语音命令控制设备：

- "天猫精灵，打开客厅扫地机器人"
- "天猫精灵，关闭扫地机器人"
- "天猫精灵，暂停扫地机器人"
- "天猫精灵，继续扫地"
- "天猫精灵，扫地机器人切换到定点模式"
- "天猫精灵，扫地机器人电量还有多少"
