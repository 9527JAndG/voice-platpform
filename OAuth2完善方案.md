# OAuth2 认证流程完善方案

## 📋 概述

本方案参考 Spring Security OAuth2 标准实现，完善现有的 OAuth2 认证流程，使其符合 RFC 6749 标准，并支持智能音箱平台的各种需求。

## 🎯 目标功能

### 1. 标准 OAuth2 端点

| 端点 | 方法 | 功能 | 状态 |
|------|------|------|------|
| `/oauth2/authorize` | GET | 授权页面（登录 + 同意） | 🔄 需完善 |
| `/oauth2/token` | POST | 获取访问令牌 | 🔄 需完善 |
| `/.well-known/openid-configuration` | GET | OIDC 发现端点 | ➕ 新增 |
| `/oauth2/introspect` | POST | Token 内省 | ➕ 新增 |
| `/oauth2/revoke` | POST | Token 撤销 | ➕ 新增 |

### 2. 支持的授权类型

- ✅ `authorization_code` - 授权码模式（主要）
- ✅ `refresh_token` - 刷新令牌
- ➕ `client_credentials` - 客户端凭证模式
- ❌ `password` - 密码模式（不推荐，不实现）
- ❌ `implicit` - 隐式模式（已废弃，不实现）

### 3. Token 类型

- ✅ **JWT Token**（推荐）
  - 自包含，无需查询数据库
  - 支持签名验证
  - 便于各平台验证
  
- ✅ **Opaque Token**（可选）
  - 随机字符串
  - 需要内省端点验证
  - 更安全，可随时撤销

## 🏗️ 架构设计

### 1. 目录结构

```
src/main/java/com/voice/platform/
├── config/
│   ├── SecurityConfig.java              # Spring Security 配置
│   ├── OAuth2Config.java                # OAuth2 配置
│   └── JwtConfig.java                   # JWT 配置
├── controller/
│   ├── OAuth2AuthorizationController.java  # 授权端点
│   ├── OAuth2TokenController.java          # Token 端点
│   └── OAuth2DiscoveryController.java      # OIDC 发现端点
├── service/
│   ├── OAuth2AuthorizationService.java     # 授权服务
│   ├── OAuth2TokenService.java             # Token 服务
│   ├── JwtTokenService.java                # JWT Token 服务
│   └── UserDetailsServiceImpl.java         # 用户认证服务
├── model/
│   ├── OAuth2Authorization.java            # 授权记录
│   ├── OAuth2Client.java                   # OAuth2 客户端（扩展）
│   └── User.java                           # 用户（扩展）
├── dto/
│   ├── OAuth2AuthorizationRequest.java     # 授权请求
│   ├── OAuth2TokenRequest.java             # Token 请求
│   ├── OAuth2TokenResponse.java            # Token 响应
│   └── OIDCDiscoveryResponse.java          # OIDC 发现响应
└── util/
    ├── JwtUtil.java                        # JWT 工具类
    └── PKCE.java                           # PKCE 支持
```

### 2. 数据库表设计

#### oauth_clients 表（扩展）

```sql
ALTER TABLE oauth_clients ADD COLUMN scopes VARCHAR(500);
ALTER TABLE oauth_clients ADD COLUMN grant_types VARCHAR(200);
ALTER TABLE oauth_clients ADD COLUMN token_type VARCHAR(20) DEFAULT 'jwt';
ALTER TABLE oauth_clients ADD COLUMN access_token_validity INT DEFAULT 3600;
ALTER TABLE oauth_clients ADD COLUMN refresh_token_validity INT DEFAULT 2592000;
```

#### oauth_authorizations 表（新增）

```sql
CREATE TABLE oauth_authorizations (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    authorization_code VARCHAR(100) UNIQUE,
    client_id VARCHAR(100) NOT NULL,
    user_id BIGINT NOT NULL,
    redirect_uri VARCHAR(500),
    scope VARCHAR(500),
    state VARCHAR(100),
    code_challenge VARCHAR(100),
    code_challenge_method VARCHAR(20),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    expires_at TIMESTAMP,
    used BOOLEAN DEFAULT FALSE,
    INDEX idx_code (authorization_code),
    INDEX idx_client (client_id),
    INDEX idx_user (user_id)
);
```

## 📝 实现步骤

### 阶段 1：基础配置（1-2小时）

1. **添加依赖**
   - Spring Security
   - JWT (jjwt)
   - Thymeleaf

2. **创建配置类**
   - SecurityConfig
   - OAuth2Config
   - JwtConfig

3. **更新数据库表**
   - 扩展 oauth_clients
   - 创建 oauth_authorizations

### 阶段 2：核心服务（2-3小时）

1. **JWT Token 服务**
   - 生成 JWT
   - 验证 JWT
   - 解析 JWT

2. **OAuth2 授权服务**
   - 生成授权码
   - 验证授权码
   - PKCE 支持

3. **OAuth2 Token 服务**
   - 授权码换 Token
   - 刷新 Token
   - 客户端凭证模式

### 阶段 3：控制器实现（2-3小时）

1. **授权端点**
   - GET /oauth2/authorize
   - 登录页面
   - 同意页面

2. **Token 端点**
   - POST /oauth2/token
   - 支持多种 grant_type

3. **OIDC 发现端点**
   - GET /.well-known/openid-configuration

### 阶段 4：前端页面（1-2小时）

1. **登录页面**
   - templates/login.html

2. **同意页面**
   - templates/consent.html

### 阶段 5：测试和文档（1-2小时）

1. **单元测试**
2. **集成测试**
3. **Postman 集合**
4. **使用文档**

## 🔐 安全特性

### 1. PKCE 支持

```java
// 客户端生成
String codeVerifier = generateCodeVerifier();
String codeChallenge = generateCodeChallenge(codeVerifier);

// 授权请求
GET /oauth2/authorize?
    client_id=xxx&
    redirect_uri=xxx&
    response_type=code&
    code_challenge=xxx&
    code_challenge_method=S256

// Token 请求
POST /oauth2/token
    grant_type=authorization_code&
    code=xxx&
    code_verifier=xxx
```

### 2. State 参数

防止 CSRF 攻击，客户端必须验证返回的 state 参数。

### 3. JWT 签名

使用 RS256 或 HS256 算法签名，防止 Token 被篡改。

### 4. Token 过期

- Access Token: 1小时
- Refresh Token: 30天
- Authorization Code: 10分钟

## 📊 JWT Token 结构

### Header

```json
{
  "alg": "HS256",
  "typ": "JWT"
}
```

### Payload

```json
{
  "sub": "user_1",
  "client_id": "test_client_id",
  "scope": "device:control",
  "iss": "https://your-domain.com",
  "aud": "test_client_id",
  "exp": 1709654400,
  "iat": 1709650800,
  "jti": "unique-token-id"
}
```

### Signature

```
HMACSHA256(
  base64UrlEncode(header) + "." +
  base64UrlEncode(payload),
  secret
)
```

## 🔄 授权流程

### 1. 授权码模式（Authorization Code）

```
┌─────────┐                                           ┌─────────┐
│         │                                           │         │
│  Client │                                           │ Resource│
│         │                                           │  Owner  │
│         │                                           │         │
└────┬────┘                                           └────┬────┘
     │                                                     │
     │  1. Authorization Request                          │
     │───────────────────────────────────────────────────>│
     │                                                     │
     │  2. User Login & Consent                           │
     │<───────────────────────────────────────────────────│
     │                                                     │
     │  3. Authorization Code                             │
     │<───────────────────────────────────────────────────│
     │                                                     │
┌────┴────┐                                           ┌────┴────┐
│         │  4. Token Request (code)                  │         │
│  Client │──────────────────────────────────────────>│  Auth   │
│         │                                           │  Server │
│         │  5. Access Token + Refresh Token          │         │
│         │<──────────────────────────────────────────│         │
└─────────┘                                           └─────────┘
```

### 2. 刷新令牌模式（Refresh Token）

```
┌─────────┐                                           ┌─────────┐
│         │  1. Refresh Token Request                 │         │
│  Client │──────────────────────────────────────────>│  Auth   │
│         │                                           │  Server │
│         │  2. New Access Token + Refresh Token      │         │
│         │<──────────────────────────────────────────│         │
└─────────┘                                           └─────────┘
```

### 3. 客户端凭证模式（Client Credentials）

```
┌─────────┐                                           ┌─────────┐
│         │  1. Token Request (client credentials)    │         │
│  Client │──────────────────────────────────────────>│  Auth   │
│         │                                           │  Server │
│         │  2. Access Token                          │         │
│         │<──────────────────────────────────────────│         │
└─────────┘                                           └─────────┘
```

## 🌐 OIDC 发现端点

### GET /.well-known/openid-configuration

```json
{
  "issuer": "https://your-domain.com",
  "authorization_endpoint": "https://your-domain.com/oauth2/authorize",
  "token_endpoint": "https://your-domain.com/oauth2/token",
  "introspection_endpoint": "https://your-domain.com/oauth2/introspect",
  "revocation_endpoint": "https://your-domain.com/oauth2/revoke",
  "jwks_uri": "https://your-domain.com/.well-known/jwks.json",
  "response_types_supported": ["code"],
  "grant_types_supported": [
    "authorization_code",
    "refresh_token",
    "client_credentials"
  ],
  "token_endpoint_auth_methods_supported": [
    "client_secret_basic",
    "client_secret_post"
  ],
  "code_challenge_methods_supported": ["S256"],
  "scopes_supported": ["device:control", "device:read"],
  "claims_supported": ["sub", "iss", "aud", "exp", "iat"]
}
```

## 📱 智能音箱平台适配

### 1. 天猫精灵

- ✅ 支持授权码模式
- ✅ 需要 HTTPS
- ✅ 支持 state 参数
- ⚠️ 不支持 PKCE

### 2. 小度音箱

- ✅ 支持授权码模式
- ✅ 需要 HTTPS
- ✅ 支持 state 参数
- ⚠️ 不支持 PKCE

### 3. 小爱同学

- ✅ 支持授权码模式
- ✅ 需要 HTTPS
- ✅ 支持 state 参数
- ⚠️ 不支持 PKCE

### 4. AWS Alexa

- ✅ 支持授权码模式
- ✅ 需要 HTTPS
- ✅ 支持 state 参数
- ✅ 支持 PKCE（推荐）

### 5. Google Assistant

- ✅ 支持授权码模式
- ✅ 需要 HTTPS
- ✅ 支持 state 参数
- ✅ 支持 PKCE（推荐）
- ✅ 检查 OIDC 发现端点

## 🧪 测试用例

### 1. 授权码流程测试

```bash
# 1. 获取授权码
curl -X GET "http://localhost:8080/oauth2/authorize?\
client_id=test_client_id&\
redirect_uri=http://localhost:8080/callback&\
response_type=code&\
state=random_state&\
scope=device:control"

# 2. 用户登录并同意

# 3. 获取 Token
curl -X POST "http://localhost:8080/oauth2/token" \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -d "grant_type=authorization_code" \
  -d "code=AUTH_CODE" \
  -d "client_id=test_client_id" \
  -d "client_secret=test_client_secret" \
  -d "redirect_uri=http://localhost:8080/callback"
```

### 2. 刷新令牌测试

```bash
curl -X POST "http://localhost:8080/oauth2/token" \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -d "grant_type=refresh_token" \
  -d "refresh_token=REFRESH_TOKEN" \
  -d "client_id=test_client_id" \
  -d "client_secret=test_client_secret"
```

### 3. 客户端凭证测试

```bash
curl -X POST "http://localhost:8080/oauth2/token" \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -d "grant_type=client_credentials" \
  -d "client_id=test_client_id" \
  -d "client_secret=test_client_secret" \
  -d "scope=device:control"
```

## 📚 参考资料

### 官方规范

- [RFC 6749 - OAuth 2.0](https://tools.ietf.org/html/rfc6749)
- [RFC 7519 - JWT](https://tools.ietf.org/html/rfc7519)
- [RFC 7636 - PKCE](https://tools.ietf.org/html/rfc7636)
- [OpenID Connect Discovery](https://openid.net/specs/openid-connect-discovery-1_0.html)

### 参考项目

- [Spring Security OAuth2](https://github.com/spring-projects/spring-security)
- [spring-security-oauth2-sample](https://github.com/ReLive27/spring-security-oauth2-sample)

## ⚠️ 注意事项

### 1. 生产环境要求

- ✅ 必须使用 HTTPS
- ✅ 使用强密钥（至少 256 位）
- ✅ 定期轮换密钥
- ✅ 实现 Token 撤销
- ✅ 记录审计日志

### 2. 性能优化

- ✅ JWT Token 无需查询数据库
- ✅ 使用 Redis 缓存授权码
- ✅ 使用连接池
- ✅ 异步处理日志

### 3. 兼容性

- ✅ 保持向后兼容
- ✅ 支持旧的 /authorize 和 /token 端点
- ✅ 逐步迁移到新端点

## 🚀 实施计划

### 第一阶段：最小可行方案（MVP）

**时间**：2-3 天

**内容**：
1. ✅ 添加依赖
2. ✅ 创建基础配置
3. ✅ 实现 JWT Token 服务
4. ✅ 实现授权码流程
5. ✅ 创建登录和同意页面
6. ✅ 基础测试

### 第二阶段：完善功能

**时间**：2-3 天

**内容**：
1. ✅ 实现刷新令牌
2. ✅ 实现客户端凭证模式
3. ✅ 实现 OIDC 发现端点
4. ✅ 实现 PKCE 支持
5. ✅ 完善错误处理

### 第三阶段：优化和测试

**时间**：1-2 天

**内容**：
1. ✅ 性能优化
2. ✅ 安全加固
3. ✅ 完整测试
4. ✅ 文档完善

## 💡 建议

由于这是一个较大的改动，建议：

1. **分支开发**：创建新分支 `feature/oauth2-enhancement`
2. **逐步实施**：按阶段实施，每个阶段都要测试
3. **保持兼容**：保留旧的端点，逐步迁移
4. **充分测试**：每个功能都要有单元测试和集成测试
5. **文档先行**：先完善文档，再开始编码

## ❓ 是否继续实施？

请确认是否要继续实施此方案。如果确认，我将：

1. 创建数据库迁移脚本
2. 实现核心服务类
3. 创建控制器
4. 创建前端页面
5. 编写测试用例
6. 更新文档

---

**创建时间**：2026-02-24  
**预计工时**：5-8 天  
**优先级**：高  
**风险等级**：中
