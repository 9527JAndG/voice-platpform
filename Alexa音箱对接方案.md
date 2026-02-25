# Alexa 智能音箱对接方案 - 扫地机器人控制

## 📖 概述

本文档详细说明如何将扫地机器人接入 Amazon Alexa 智能音箱平台，实现语音控制功能。

## 🎯 Alexa 平台特点

### 与其他平台的对比

| 特性 | Alexa | 天猫精灵 | 小度音箱 | 小爱同学 |
|------|-------|---------|---------|---------|
| 市场定位 | 全球市场 | 中国市场 | 中国市场 | 中国市场 |
| 市场份额 | 全球第一 | 中国第一 | 中国第二 | 中国第三 |
| 技术成熟度 | ⭐⭐⭐⭐⭐ | ⭐⭐⭐⭐ | ⭐⭐⭐⭐ | ⭐⭐⭐⭐ |
| 文档完善度 | ⭐⭐⭐⭐⭐ | ⭐⭐⭐ | ⭐⭐⭐ | ⭐⭐⭐ |
| 开发难度 | 中等 | 中等 | 中等 | 简单 |
| 授权方式 | OAuth 2.0 + LWA | OAuth 2.0 | OAuth 2.0 | OAuth 2.0 |
| 协议格式 | JSON (复杂) | JSON (中等) | JSON (复杂) | JSON (简单) |

### Alexa 的优势

1. **全球化**：支持多语言，覆盖全球市场
2. **生态完善**：与 AWS 深度集成，技术栈成熟
3. **文档详细**：官方文档非常完善，示例丰富
4. **功能强大**：支持的设备类型和功能最全面
5. **开发工具**：提供完善的开发工具和测试环境

### Alexa 的挑战

1. **协议复杂**：消息格式比国内平台复杂
2. **认证严格**：需要通过 Amazon 的认证流程
3. **服务器要求**：必须使用 HTTPS，建议部署在 AWS
4. **账号体系**：需要 Amazon 账号和 AWS 账号

## 🏗️ 架构设计

### 整体架构

```
用户语音
    ↓
Alexa 设备（Echo、Echo Dot 等）
    ↓
Alexa 云服务
    ↓
Smart Home Skill（你的技能）
    ↓
Lambda 函数（AWS）或 HTTPS 端点
    ↓
你的后端服务
    ↓
扫地机器人设备
```

### 核心组件

1. **Smart Home Skill**：在 Alexa 开发者控制台创建
2. **Lambda 函数**：处理 Alexa 请求（推荐）
3. **OAuth 2.0 服务器**：处理账号授权
4. **设备控制服务**：控制实际设备

## 📋 Alexa Smart Home API

### 支持的接口（Interfaces）

#### 1. Alexa.PowerController
控制设备开关

**支持的指令**：
- `TurnOn`：开机
- `TurnOff`：关机

**语音示例**：
- "Alexa, turn on the vacuum"
- "Alexa, turn off the robot vacuum"

#### 2. Alexa.ModeController
控制设备模式

**支持的模式**（扫地机器人）：
- `Auto`：自动清扫
- `Spot`：定点清扫
- `Edge`：沿边清扫
- `Max`：强力清扫

**语音示例**：
- "Alexa, set vacuum to auto mode"
- "Alexa, change vacuum to spot cleaning"

#### 3. Alexa.RangeController
控制数值范围（如吸力、速度）

**支持的操作**：
- `SetRangeValue`：设置具体值
- `AdjustRangeValue`：调整值

**语音示例**：
- "Alexa, set vacuum suction to 5"
- "Alexa, increase vacuum power"

#### 4. Alexa.PercentageController
控制百分比（如清洁进度）

**支持的操作**：
- `SetPercentage`：设置百分比
- `AdjustPercentage`：调整百分比

#### 5. Alexa.EndpointHealth
报告设备健康状态

**状态**：
- `OK`：正常
- `UNREACHABLE`：不可达

#### 6. Alexa.BatteryLevel (可选)
报告电池电量

**属性**：
- `level`：电量百分比（0-100）

## 🔐 OAuth 2.0 授权流程

### Alexa 的 OAuth 流程

Alexa 使用标准的 OAuth 2.0 授权码模式，与国内平台类似：

```
1. 用户在 Alexa App 中启用技能
2. Alexa 重定向到你的授权页面
3. 用户登录并授权
4. 重定向回 Alexa，携带授权码
5. Alexa 用授权码换取 access_token
6. Alexa 使用 access_token 调用你的接口
```

### 与国内平台的差异

| 项目 | Alexa | 国内平台 |
|------|-------|---------|
| 授权页面 | 必须 HTTPS | 可以 HTTP（测试） |
| Token 格式 | 标准 OAuth 2.0 | 标准 OAuth 2.0 |
| 刷新机制 | 支持 refresh_token | 支持 refresh_token |
| 额外要求 | 需要 LWA（Login with Amazon）| 无 |

## 📝 消息格式

### 设备发现请求（Discovery）

**Alexa 请求**：
```json
{
  "directive": {
    "header": {
      "namespace": "Alexa.Discovery",
      "name": "Discover",
      "payloadVersion": "3",
      "messageId": "abc-123-def-456"
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

**你的响应**：
```json
{
  "event": {
    "header": {
      "namespace": "Alexa.Discovery",
      "name": "Discover.Response",
      "payloadVersion": "3",
      "messageId": "abc-123-def-456"
    },
    "payload": {
      "endpoints": [
        {
          "endpointId": "robot_001",
          "manufacturerName": "Your Brand",
          "friendlyName": "Living Room Vacuum",
          "description": "Smart Robot Vacuum Cleaner",
          "displayCategories": ["VACUUM_CLEANER"],
          "capabilities": [
            {
              "type": "AlexaInterface",
              "interface": "Alexa.PowerController",
              "version": "3",
              "properties": {
                "supported": [
                  {"name": "powerState"}
                ],
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
                "supported": [
                  {"name": "mode"}
                ],
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
                  }
                ]
              }
            },
            {
              "type": "AlexaInterface",
              "interface": "Alexa.EndpointHealth",
              "version": "3",
              "properties": {
                "supported": [
                  {"name": "connectivity"}
                ],
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

### 设备控制请求（Control）

**开机请求**：
```json
{
  "directive": {
    "header": {
      "namespace": "Alexa.PowerController",
      "name": "TurnOn",
      "payloadVersion": "3",
      "messageId": "abc-123-def-456",
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

**你的响应**：
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

## 🔄 与现有项目的集成

### 代码复用分析

基于现有的三平台对接项目，Alexa 对接的代码复用率：

| 层级 | 复用率 | 说明 |
|------|--------|------|
| OAuth 层 | 90% | 基本相同，需要添加 LWA 支持 |
| Service 层 | 100% | 完全复用 |
| Repository 层 | 100% | 完全复用 |
| Model 层 | 100% | 完全复用 |
| Controller 层 | 0% | 需要新建 AlexaController |
| DTO 层 | 0% | 需要新建 Alexa 专用 DTO |

### 预计开发时间

- **Controller + DTO**：8-10 小时
- **Lambda 函数**：4-6 小时（如果使用）
- **测试调试**：4-6 小时
- **文档编写**：2-3 小时
- **总计**：18-25 小时（约 3 天）

### 核心差异

1. **消息格式更复杂**：
   - 使用 `directive` 和 `event` 结构
   - 需要 `context` 返回设备状态
   - 支持 `correlationToken` 关联请求

2. **设备发现更详细**：
   - 需要定义 `capabilities`
   - 需要定义 `displayCategories`
   - 需要定义 `friendlyNames` 和资源

3. **状态报告**：
   - 支持主动状态报告（Proactive State Reporting）
   - 需要实现 `StateReport` 接口

## 🚀 开发步骤

### 步骤 1：准备工作

1. **注册 Amazon 开发者账号**
   - 访问：https://developer.amazon.com
   - 注册并登录

2. **注册 AWS 账号**（如果使用 Lambda）
   - 访问：https://aws.amazon.com
   - 注册并配置

3. **准备 HTTPS 服务器**
   - 必须使用 HTTPS
   - 建议使用 AWS 或其他云服务

### 步骤 2：创建 Smart Home Skill

1. 登录 Alexa 开发者控制台
2. 点击"Create Skill"
3. 选择"Smart Home"类型
4. 配置技能信息：
   - Skill Name：你的技能名称
   - Default Language：en-US
   - Model：Smart Home

### 步骤 3：配置 OAuth

1. 在技能配置中找到"Account Linking"
2. 配置 OAuth 信息：
   - Authorization URI：`https://your-domain.com/authorize`
   - Access Token URI：`https://your-domain.com/token`
   - Client ID：你的 client_id
   - Client Secret：你的 client_secret
   - Scope：可选
   - Redirect URLs：Alexa 提供的回调地址

### 步骤 4：配置 Lambda 或 HTTPS 端点

#### 方式一：使用 Lambda（推荐）

1. 在 AWS Lambda 创建函数
2. 选择运行时：Node.js 或 Python
3. 配置触发器：Alexa Smart Home
4. 上传代码
5. 在技能配置中填写 Lambda ARN

#### 方式二：使用 HTTPS 端点

1. 部署你的 Spring Boot 应用
2. 配置 HTTPS
3. 在技能配置中填写端点 URL：
   - `https://your-domain.com/alexa`

### 步骤 5：实现后端接口

创建 `AlexaController.java`：

```java
@RestController
@RequestMapping("/alexa")
public class AlexaController {
    
    @PostMapping
    public ResponseEntity<?> handleRequest(@RequestBody AlexaRequest request) {
        String namespace = request.getDirective().getHeader().getNamespace();
        String name = request.getDirective().getHeader().getName();
        
        if ("Alexa.Discovery".equals(namespace) && "Discover".equals(name)) {
            return handleDiscovery(request);
        } else if ("Alexa.PowerController".equals(namespace)) {
            return handlePowerControl(request);
        }
        // ... 其他处理
    }
}
```

### 步骤 6：测试

1. **使用 Alexa 模拟器测试**
   - 在开发者控制台的"Test"标签
   - 输入语音命令测试

2. **使用真实设备测试**
   - 在 Alexa App 中启用技能
   - 授权账号
   - 发现设备
   - 语音控制测试

### 步骤 7：提交认证

1. 完善技能信息
2. 添加隐私政策和服务条款
3. 提交认证审核
4. 等待 Amazon 审核通过

## 📊 与现有平台的对比

### 协议复杂度对比

| 平台 | 复杂度 | 消息层级 | 特殊要求 |
|------|--------|---------|---------|
| Alexa | ⭐⭐⭐⭐⭐ | 3-4 层 | Lambda/HTTPS, LWA |
| 天猫精灵 | ⭐⭐⭐ | 2 层 | 无 |
| 小度音箱 | ⭐⭐⭐⭐ | 3 层 | 无 |
| 小爱同学 | ⭐⭐ | 1 层 | 无 |

### 开发时间对比

| 平台 | 首次开发 | 基于现有项目 | 效率提升 |
|------|---------|-------------|---------|
| 天猫精灵 | 7 天 | - | - |
| 小度音箱 | 7 天 | 6 小时 | 90% |
| 小爱同学 | 7 天 | 4 小时 | 95% |
| Alexa | 7 天 | 18-25 小时 | 70% |

## 💡 最佳实践

### 1. 使用 Lambda 函数

**优势**：
- 无需管理服务器
- 自动扩展
- 与 Alexa 深度集成
- 成本低

**示例**（Node.js）：
```javascript
exports.handler = async (event) => {
    const namespace = event.directive.header.namespace;
    const name = event.directive.header.name;
    
    if (namespace === 'Alexa.Discovery' && name === 'Discover') {
        return handleDiscovery(event);
    }
    // ... 其他处理
};
```

### 2. 实现状态缓存

由于 Alexa 会频繁查询设备状态，建议使用 Redis 缓存：

```java
@Service
public class AlexaDeviceService {
    
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;
    
    public DeviceState getDeviceState(String endpointId) {
        // 先从缓存获取
        DeviceState state = (DeviceState) redisTemplate
            .opsForValue()
            .get("device:state:" + endpointId);
        
        if (state == null) {
            // 从数据库获取并缓存
            state = deviceRepository.findState(endpointId);
            redisTemplate.opsForValue()
                .set("device:state:" + endpointId, state, 5, TimeUnit.MINUTES);
        }
        
        return state;
    }
}
```

### 3. 实现主动状态报告

当设备状态改变时，主动通知 Alexa：

```java
public void reportStateChange(String endpointId, String powerState) {
    AlexaStateReport report = AlexaStateReport.builder()
        .endpointId(endpointId)
        .property("powerState", powerState)
        .build();
    
    alexaEventGateway.sendStateReport(report);
}
```

### 4. 错误处理

返回标准的 Alexa 错误响应：

```json
{
  "event": {
    "header": {
      "namespace": "Alexa",
      "name": "ErrorResponse",
      "payloadVersion": "3",
      "messageId": "message-id"
    },
    "endpoint": {
      "endpointId": "robot_001"
    },
    "payload": {
      "type": "ENDPOINT_UNREACHABLE",
      "message": "Device is offline"
    }
  }
}
```

## 📚 参考资源

### 官方文档
- [Alexa Smart Home Skill API](https://developer.amazon.com/docs/smarthome/understand-the-smart-home-skill-api.html)
- [Device Capabilities](https://developer.amazon.com/docs/smarthome/index-device-capabilities.html)
- [OAuth 2.0 Integration](https://developer.amazon.com/docs/smarthome/authenticate-a-customer-permissions.html)

### 开发工具
- [Alexa Developer Console](https://developer.amazon.com/alexa/console/ask)
- [AWS Lambda Console](https://console.aws.amazon.com/lambda)
- [Alexa Skills Kit SDK](https://github.com/alexa/alexa-skills-kit-sdk-for-nodejs)

### 示例代码
- [Smart Home Sample](https://github.com/alexa/alexa-smarthome)
- [Lambda Samples](https://github.com/alexa-samples)

## 🎯 总结

### Alexa 对接的特点

**优势**：
1. 全球化平台，市场潜力大
2. 技术栈成熟，文档完善
3. 与 AWS 深度集成
4. 功能强大，支持的设备类型最全

**挑战**：
1. 协议最复杂，学习曲线陡峭
2. 需要 AWS 账号和 Lambda 函数
3. 认证流程严格
4. 开发时间相对较长

### 是否值得对接？

**适合对接的情况**：
- 产品面向全球市场
- 有 AWS 使用经验
- 追求技术完善度
- 有充足的开发时间

**可以暂缓的情况**：
- 只面向中国市场
- 开发资源有限
- 需要快速上线
- 对国际化要求不高

### 建议

对于现有项目，建议：
1. 先完善国内三大平台（天猫精灵、小度、小爱）
2. 积累用户和经验
3. 评估国际化需求
4. 再考虑对接 Alexa

如果决定对接 Alexa，预计需要 3 天开发时间，代码复用率约 70%。

---

**最后更新**：2026-02-24  
**文档版本**：v1.0  
**作者**：Kiro AI Assistant
