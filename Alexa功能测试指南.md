# Alexa 功能测试指南

## 🎯 测试目标

验证 Alexa Smart Home Skill 的核心功能：
1. AcceptGrant 授权流程
2. 设备发现（Discovery）
3. 设备控制（PowerController、ModeController）
4. 状态报告（ReportState）
5. 主动状态推送（ChangeReport）

## 📋 前置条件

### 1. 环境准备
```bash
# 1. 启动数据库
# 确保 MySQL 运行在 localhost:3306

# 2. 配置 Alexa 凭证
export ALEXA_CLIENT_ID=amzn1.application-oa2-client.xxxxx
export ALEXA_CLIENT_SECRET=your-secret-here

# 3. 启动应用
./start.sh  # Linux/Mac
start.bat   # Windows
```

### 2. Alexa Developer Console 配置

1. 登录 [Alexa Developer Console](https://developer.amazon.com/alexa/console/ask)
2. 创建 Smart Home Skill
3. 配置 Account Linking:
   - Authorization URI: `https://your-domain.com/oauth2/authorize`
   - Access Token URI: `https://your-domain.com/oauth2/token`
   - Client ID: 从 OAuth2 配置获取
   - Client Secret: 从 OAuth2 配置获取
   - Scope: `device:control`
4. 配置 Smart Home API:
   - Default Endpoint: `https://your-domain.com/alexa`

### 3. 测试账号
```
用户名: user1
密码: password
设备: device-001 (Living Room Vacuum)
```

## 🧪 测试用例

### 测试 1: AcceptGrant 授权流程

**目的**: 验证授权接受和 Token 交换功能

**步骤**:
1. 在 Alexa App 中启用技能
2. 完成账号关联（OAuth2 登录）
3. Alexa 自动发送 AcceptGrant 请求

**请求示例**:
```bash
curl -X POST http://localhost:8080/alexa \
  -H "Content-Type: application/json" \
  -d '{
    "directive": {
      "header": {
        "namespace": "Alexa.Authorization",
        "name": "AcceptGrant",
        "payloadVersion": "3",
        "messageId": "test-message-001"
      },
      "payload": {
        "grant": {
          "type": "OAuth2.AuthorizationCode",
          "code": "test-authorization-code"
        },
        "grantee": {
          "type": "BearerToken",
          "token": "test-grantee-token"
        }
      }
    }
  }'
```

**预期响应**:
```json
{
  "event": {
    "header": {
      "namespace": "Alexa.Authorization",
      "name": "AcceptGrant.Response",
      "payloadVersion": "3",
      "messageId": "..."
    },
    "payload": {}
  }
}
```

**验证**:
```sql
-- 检查数据库中的 Token
SELECT * FROM alexa_tokens WHERE user_id = 1;
```

**预期结果**:
- ✅ 返回 200 OK
- ✅ 响应包含 AcceptGrant.Response
- ✅ 数据库中存在 Token 记录
- ✅ 日志显示 "✓ AcceptGrant 处理成功"

---

### 测试 2: 设备发现

**目的**: 验证设备发现功能

**步骤**:
1. 获取 OAuth2 Access Token
2. 发送 Discovery 请求

**获取 Token**:
```bash
# 1. 获取授权码
curl "http://localhost:8080/oauth2/authorize?response_type=code&client_id=alexa-client&redirect_uri=https://pitangui.amazon.com/api/skill/link/xxxxx&state=test-state"

# 2. 用授权码换取 Token
curl -X POST http://localhost:8080/oauth2/token \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -d "grant_type=authorization_code&code=YOUR_CODE&client_id=alexa-client&client_secret=alexa-secret&redirect_uri=https://pitangui.amazon.com/api/skill/link/xxxxx"
```

**Discovery 请求**:
```bash
curl -X POST http://localhost:8080/alexa \
  -H "Content-Type: application/json" \
  -d '{
    "directive": {
      "header": {
        "namespace": "Alexa.Discovery",
        "name": "Discover",
        "payloadVersion": "3",
        "messageId": "test-discovery-001"
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

**预期响应**:
```json
{
  "event": {
    "header": {
      "namespace": "Alexa.Discovery",
      "name": "Discover.Response",
      "payloadVersion": "3",
      "messageId": "..."
    },
    "payload": {
      "endpoints": [
        {
          "endpointId": "device-001",
          "manufacturerName": "Smart Home Demo",
          "friendlyName": "Living Room Vacuum",
          "description": "Smart Robot Vacuum Cleaner",
          "displayCategories": ["VACUUM_CLEANER"],
          "capabilities": [
            {
              "type": "AlexaInterface",
              "interface": "Alexa",
              "version": "3"
            },
            {
              "type": "AlexaInterface",
              "interface": "Alexa.PowerController",
              "version": "3",
              "properties": {
                "supported": [{"name": "powerState"}],
                "proactivelyReported": true,
                "retrievable": true
              }
            },
            {
              "type": "AlexaInterface",
              "interface": "Alexa.ModeController",
              "version": "3",
              "instance": "VacuumMode",
              "properties": {
                "supported": [{"name": "mode"}],
                "proactivelyReported": true,
                "retrievable": true
              },
              "capabilityResources": {
                "friendlyNames": [
                  {
                    "@type": "text",
                    "value": {
                      "text": "Cleaning Mode",
                      "locale": "en-US"
                    }
                  }
                ]
              },
              "configuration": {
                "ordered": false,
                "supportedModes": [
                  {
                    "value": "Auto",
                    "modeResources": {
                      "friendlyNames": [
                        {
                          "@type": "text",
                          "value": {
                            "text": "Auto",
                            "locale": "en-US"
                          }
                        }
                      ]
                    }
                  },
                  {
                    "value": "Spot",
                    "modeResources": {
                      "friendlyNames": [
                        {
                          "@type": "text",
                          "value": {
                            "text": "Spot",
                            "locale": "en-US"
                          }
                        }
                      ]
                    }
                  },
                  {
                    "value": "Edge",
                    "modeResources": {
                      "friendlyNames": [
                        {
                          "@type": "text",
                          "value": {
                            "text": "Edge",
                            "locale": "en-US"
                          }
                        }
                      ]
                    }
                  }
                ]
              }
            },
            {
              "type": "AlexaInterface",
              "interface": "Alexa.EndpointHealth",
              "version": "3",
              "properties": {
                "supported": [{"name": "connectivity"}],
                "proactivelyReported": true,
                "retrievable": true
              }
            }
          ]
        }
      ]
    }
  }
}
```

**验证**:
- ✅ 返回 200 OK
- ✅ 响应包含设备列表
- ✅ 设备包含所有必需的 capabilities
- ✅ EndpointHealth capability 存在

---

### 测试 3: 电源控制

**目的**: 验证设备开关功能

**TurnOn 请求**:
```bash
curl -X POST http://localhost:8080/alexa \
  -H "Content-Type: application/json" \
  -d '{
    "directive": {
      "header": {
        "namespace": "Alexa.PowerController",
        "name": "TurnOn",
        "payloadVersion": "3",
        "messageId": "test-turnon-001",
        "correlationToken": "test-correlation-001"
      },
      "endpoint": {
        "scope": {
          "type": "BearerToken",
          "token": "YOUR_ACCESS_TOKEN"
        },
        "endpointId": "device-001"
      },
      "payload": {}
    }
  }'
```

**预期响应**:
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
      "endpointId": "device-001"
    },
    "payload": {}
  },
  "context": {
    "properties": [
      {
        "namespace": "Alexa.PowerController",
        "name": "powerState",
        "value": "ON",
        "timeOfSample": "2024-01-01T00:00:00Z",
        "uncertaintyInMilliseconds": 500
      },
      {
        "namespace": "Alexa.EndpointHealth",
        "name": "connectivity",
        "value": {
          "value": "OK"
        },
        "timeOfSample": "2024-01-01T00:00:00Z",
        "uncertaintyInMilliseconds": 500
      }
    ]
  }
}
```

**TurnOff 请求**:
```bash
# 将 "TurnOn" 改为 "TurnOff"
# 预期 powerState 为 "OFF"
```

**验证**:
```sql
-- 检查设备状态
SELECT device_id, device_name, power_state, status 
FROM devices 
WHERE device_id = 'device-001';
```

**预期结果**:
- ✅ 返回 200 OK
- ✅ 响应包含 powerState 属性
- ✅ 响应包含 connectivity 属性
- ✅ 数据库中设备状态已更新
- ✅ 日志显示 "✓ 设备开机成功" 或 "✓ 设备关机成功"

---

### 测试 4: 模式控制

**目的**: 验证清扫模式设置功能

**SetMode 请求**:
```bash
curl -X POST http://localhost:8080/alexa \
  -H "Content-Type: application/json" \
  -d '{
    "directive": {
      "header": {
        "namespace": "Alexa.ModeController",
        "name": "SetMode",
        "payloadVersion": "3",
        "messageId": "test-setmode-001",
        "correlationToken": "test-correlation-002"
      },
      "endpoint": {
        "scope": {
          "type": "BearerToken",
          "token": "YOUR_ACCESS_TOKEN"
        },
        "endpointId": "device-001"
      },
      "payload": {
        "mode": "Spot"
      }
    }
  }'
```

**预期响应**:
```json
{
  "event": {
    "header": {
      "namespace": "Alexa",
      "name": "Response",
      "payloadVersion": "3",
      "messageId": "...",
      "correlationToken": "test-correlation-002"
    },
    "endpoint": {
      "scope": {
        "type": "BearerToken",
        "token": "YOUR_ACCESS_TOKEN"
      },
      "endpointId": "device-001"
    },
    "payload": {}
  },
  "context": {
    "properties": [
      {
        "namespace": "Alexa.ModeController",
        "name": "mode",
        "value": "Spot",
        "timeOfSample": "2024-01-01T00:00:00Z",
        "uncertaintyInMilliseconds": 500
      },
      {
        "namespace": "Alexa.EndpointHealth",
        "name": "connectivity",
        "value": {
          "value": "OK"
        },
        "timeOfSample": "2024-01-01T00:00:00Z",
        "uncertaintyInMilliseconds": 500
      }
    ]
  }
}
```

**测试不同模式**:
```bash
# Auto 模式
"payload": { "mode": "Auto" }

# Edge 模式
"payload": { "mode": "Edge" }
```

**验证**:
```sql
-- 检查设备模式
SELECT device_id, device_name, work_mode 
FROM devices 
WHERE device_id = 'device-001';
```

**预期结果**:
- ✅ 返回 200 OK
- ✅ 响应包含 mode 属性
- ✅ 数据库中设备模式已更新
- ✅ 日志显示 "✓ 模式设置成功"

---

### 测试 5: 状态报告

**目的**: 验证设备状态查询功能

**ReportState 请求**:
```bash
curl -X POST http://localhost:8080/alexa \
  -H "Content-Type: application/json" \
  -d '{
    "directive": {
      "header": {
        "namespace": "Alexa",
        "name": "ReportState",
        "payloadVersion": "3",
        "messageId": "test-reportstate-001",
        "correlationToken": "test-correlation-003"
      },
      "endpoint": {
        "scope": {
          "type": "BearerToken",
          "token": "YOUR_ACCESS_TOKEN"
        },
        "endpointId": "device-001"
      },
      "payload": {}
    }
  }'
```

**预期响应**:
```json
{
  "event": {
    "header": {
      "namespace": "Alexa",
      "name": "StateReport",
      "payloadVersion": "3",
      "messageId": "...",
      "correlationToken": "test-correlation-003"
    },
    "endpoint": {
      "scope": {
        "type": "BearerToken",
        "token": "YOUR_ACCESS_TOKEN"
      },
      "endpointId": "device-001"
    },
    "payload": {}
  },
  "context": {
    "properties": [
      {
        "namespace": "Alexa.PowerController",
        "name": "powerState",
        "value": "ON",
        "timeOfSample": "2024-01-01T00:00:00Z",
        "uncertaintyInMilliseconds": 500
      },
      {
        "namespace": "Alexa.ModeController",
        "name": "mode",
        "value": "Auto",
        "timeOfSample": "2024-01-01T00:00:00Z",
        "uncertaintyInMilliseconds": 500
      },
      {
        "namespace": "Alexa.EndpointHealth",
        "name": "connectivity",
        "value": {
          "value": "OK"
        },
        "timeOfSample": "2024-01-01T00:00:00Z",
        "uncertaintyInMilliseconds": 500
      }
    ]
  }
}
```

**预期结果**:
- ✅ 返回 200 OK
- ✅ 响应包含所有设备属性
- ✅ 状态与数据库一致

---

### 测试 6: ChangeReport 主动推送（待集成）

**目的**: 验证主动状态推送功能

**注意**: 此功能需要先完成 AlexaStateReporter 集成到控制方法中

**测试步骤**:
1. 确保 AcceptGrant 已完成（有有效的 Alexa Token）
2. 通过 API 控制设备
3. 检查日志确认 ChangeReport 发送
4. 在 Alexa App 中验证状态更新

**手动触发测试**:
```java
// 在 AlexaController.handlePowerControl() 中添加
Device device = deviceOpt.get();
String oldPowerState = device.getPowerState();

// 执行控制
deviceService.turnOn(endpointId);

// 报告状态变化
alexaStateReporter.reportStateChange(
    device, 
    oldPowerState, 
    "ON", 
    null, 
    null
);
```

**预期日志**:
```
检测到电源状态变化: off -> on
✓ 状态报告发送成功: deviceId=device-001, deviceName=Living Room Vacuum, changes=1
Event Gateway 响应成功: status=202
```

**验证**:
- ✅ 日志显示 ChangeReport 发送成功
- ✅ Alexa App 中设备状态实时更新
- ✅ 无需手动刷新即可看到最新状态

---

## 🔍 故障排查

### 问题 1: AcceptGrant 失败

**症状**: 返回 ACCEPT_GRANT_FAILED 错误

**可能原因**:
1. Alexa Client ID 或 Secret 配置错误
2. 授权码无效或已过期
3. 网络连接问题

**解决方法**:
```bash
# 1. 检查配置
echo $ALEXA_CLIENT_ID
echo $ALEXA_CLIENT_SECRET

# 2. 检查日志
tail -f logs/application.log | grep "Alexa Token"

# 3. 测试网络连接
curl https://api.amazon.com/auth/o2/token
```

### 问题 2: Token 验证失败

**症状**: 返回 INVALID_AUTHORIZATION_CREDENTIAL 错误

**可能原因**:
1. Access Token 无效或已过期
2. Token 格式错误
3. OAuth2 配置问题

**解决方法**:
```sql
-- 检查 Token 状态
SELECT user_id, expires_at, 
       CASE WHEN expires_at > NOW() THEN 'Valid' ELSE 'Expired' END as status
FROM access_tokens;

-- 检查 Alexa Token
SELECT user_id, expires_at,
       CASE WHEN expires_at > NOW() THEN 'Valid' ELSE 'Expired' END as status
FROM alexa_tokens;
```

### 问题 3: 设备不在线

**症状**: 返回 ENDPOINT_UNREACHABLE 错误

**可能原因**:
1. 设备状态为 offline
2. 设备不存在

**解决方法**:
```sql
-- 更新设备状态
UPDATE devices SET status = 'online' WHERE device_id = 'device-001';

-- 检查设备
SELECT * FROM devices WHERE device_id = 'device-001';
```

### 问题 4: ChangeReport 发送失败

**症状**: 日志显示 "发送到 Event Gateway 失败"

**可能原因**:
1. Alexa Token 无效
2. Event Gateway URL 错误
3. 网络连接问题

**解决方法**:
```bash
# 1. 检查 Token
curl -X POST http://localhost:8080/alexa \
  -H "Content-Type: application/json" \
  -d '{"directive": {"header": {"namespace": "Alexa", "name": "ReportState", ...}}}'

# 2. 测试 Event Gateway 连接
curl -X POST https://api.amazonalexa.com/v3/events \
  -H "Authorization: Bearer YOUR_ALEXA_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{...}'
```

## 📊 测试检查清单

### 功能测试
- [ ] AcceptGrant 授权成功
- [ ] Token 保存到数据库
- [ ] 设备发现返回正确的设备列表
- [ ] TurnOn 控制成功
- [ ] TurnOff 控制成功
- [ ] SetMode 控制成功（Auto/Spot/Edge）
- [ ] ReportState 返回正确的状态
- [ ] ChangeReport 发送成功（待集成）

### 错误处理测试
- [ ] 无效 Token 返回正确错误
- [ ] 设备不存在返回正确错误
- [ ] 设备离线返回正确错误
- [ ] 无效模式返回正确错误

### 性能测试
- [ ] AcceptGrant 响应时间 < 2s
- [ ] Discovery 响应时间 < 1s
- [ ] 控制响应时间 < 500ms
- [ ] ChangeReport 推送延迟 < 1s

### 安全测试
- [ ] Token 验证正常工作
- [ ] 过期 Token 自动刷新
- [ ] 敏感信息不在日志中泄露

## 📝 测试报告模板

```markdown
# Alexa 功能测试报告

## 测试信息
- 测试日期: YYYY-MM-DD
- 测试人员: [姓名]
- 测试环境: [开发/测试/生产]
- 应用版本: [版本号]

## 测试结果

### AcceptGrant 授权
- 状态: ✅ 通过 / ❌ 失败
- 响应时间: [时间]
- 备注: [说明]

### 设备发现
- 状态: ✅ 通过 / ❌ 失败
- 发现设备数: [数量]
- 响应时间: [时间]
- 备注: [说明]

### 电源控制
- TurnOn: ✅ 通过 / ❌ 失败
- TurnOff: ✅ 通过 / ❌ 失败
- 响应时间: [时间]
- 备注: [说明]

### 模式控制
- Auto: ✅ 通过 / ❌ 失败
- Spot: ✅ 通过 / ❌ 失败
- Edge: ✅ 通过 / ❌ 失败
- 响应时间: [时间]
- 备注: [说明]

### 状态报告
- 状态: ✅ 通过 / ❌ 失败
- 响应时间: [时间]
- 备注: [说明]

### ChangeReport 推送
- 状态: ✅ 通过 / ❌ 失败 / ⏳ 待集成
- 推送延迟: [时间]
- 备注: [说明]

## 问题列表
1. [问题描述]
   - 严重程度: 高/中/低
   - 状态: 待修复/已修复
   - 备注: [说明]

## 总结
[测试总结]
```

---

**创建时间**: 2024-01-01  
**最后更新**: 2024-01-01  
**版本**: 1.0
