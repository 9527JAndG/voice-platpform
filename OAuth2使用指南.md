# OAuth2 使用指南

## 概述

本项目已完成 OAuth2 认证流程的完善，支持标准的 OAuth2 授权流程和 OIDC 发现端点。

## 功能特性

### 1. 支持的授权类型

- **authorization_code**: 授权码模式（推荐用于智能音箱平台）
- **refresh_token**: 刷新令牌模式
- **client_credentials**: 客户端凭证模式

### 2. Token 类型

- **JWT Token**: 推荐使用，便于各平台验证
- **Opaque Token**: 不透明令牌，需要通过 introspect 端点验证

### 3. 安全特性

- 支持 PKCE（Proof Key for Code Exchange）
- 支持 state 参数防止 CSRF 攻击
- 支持 scope 权限控制
- Token 过期时间可配置

## 快速测试

### 测试账号

系统内置了以下测试账号，可以直接使用：

| 用户名 | 密码 | 显示名称 |
|--------|------|----------|
| user1 | password | 张三 |
| user2 | password | 李四 |
| testuser | password | 测试用户 |

**注意**: 这些是演示用的测试账号，生产环境请连接真实的用户数据库。

### 快速体验

1. 启动应用程序
2. 访问授权页面：
```
http://localhost:8080/oauth2/authorize?response_type=code&client_id=test_client_id&redirect_uri=http://localhost:8080/callback&state=test123
```
3. 使用测试账号登录（例如：用户名 `user1`，密码 `password`）
4. 点击"授权"按钮
5. 获取授权码后，使用 Postman 或 curl 交换 Token

## API 端点

### 1. 授权端点

#### GET /oauth2/authorize

用户授权页面，支持登录和授权同意。

**请求参数：**

```
response_type: code（固定值）
client_id: 客户端 ID
redirect_uri: 回调地址
scope: 权限范围（可选，默认：read write）
state: 状态参数（推荐）
code_challenge: PKCE 挑战码（可选）
code_challenge_method: PKCE 方法（可选，S256 或 plain）
```

**示例：**

```
GET /oauth2/authorize?response_type=code&client_id=alexa_client&redirect_uri=https://pitangui.amazon.com/api/skill/link/xxx&scope=read%20write&state=xyz123
```

### 2. Token 端点

#### POST /oauth2/token

获取访问令牌。

**请求参数（表单提交）：**

**授权码模式：**
```
grant_type: authorization_code
code: 授权码
client_id: 客户端 ID
client_secret: 客户端密钥
redirect_uri: 回调地址（必须与授权时一致）
code_verifier: PKCE 验证码（如果使用了 PKCE）
```

**刷新令牌模式：**
```
grant_type: refresh_token
refresh_token: 刷新令牌
client_id: 客户端 ID
client_secret: 客户端密钥
```

**客户端凭证模式：**
```
grant_type: client_credentials
client_id: 客户端 ID
client_secret: 客户端密钥
scope: 权限范围（可选）
```

**响应示例：**

```json
{
  "access_token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "token_type": "Bearer",
  "expires_in": 3600,
  "refresh_token": "tGzv3JOkF0XG5Qx2TlKWIA",
  "scope": "read write"
}
```

### 3. Token 验证端点

#### POST /oauth2/introspect

验证 Token 是否有效。

**请求参数：**
```
token: 要验证的 Token
client_id: 客户端 ID
client_secret: 客户端密钥
```

**响应示例：**

```json
{
  "active": true,
  "client_id": "alexa_client",
  "username": "user1",
  "scope": "read write",
  "exp": 1708876800
}
```

### 4. Token 撤销端点

#### POST /oauth2/revoke

撤销 Token。

**请求参数：**
```
token: 要撤销的 Token
client_id: 客户端 ID
client_secret: 客户端密钥
```

### 5. OIDC 发现端点

#### GET /.well-known/openid-configuration

返回 OIDC 配置信息（Google Assistant 会检查此端点）。

**响应示例：**

```json
{
  "issuer": "https://voice-platform.example.com",
  "authorization_endpoint": "https://voice-platform.example.com/oauth2/authorize",
  "token_endpoint": "https://voice-platform.example.com/oauth2/token",
  "token_endpoint_auth_methods_supported": ["client_secret_post", "client_secret_basic"],
  "grant_types_supported": ["authorization_code", "refresh_token", "client_credentials"],
  "response_types_supported": ["code"],
  "scopes_supported": ["read", "write", "openid"],
  "code_challenge_methods_supported": ["S256", "plain"]
}
```

## 配置说明

### 1. application.yml 配置

```yaml
oauth2:
  # 授权码有效期（秒）
  code-expire-seconds: 120
  # Access Token 有效期（秒）
  access-token-expire-seconds: 259200
  # Refresh Token 有效期（秒）
  refresh-token-expire-seconds: 2592000
  
  # JWT 配置
  jwt:
    # JWT 签名密钥（生产环境请使用更复杂的密钥）
    secret: VoicePlatformJwtSecretKey2024ForSmartHomePlatformIntegration
    # JWT 发行者
    issuer: https://voice-platform.example.com
    # Access Token 有效期（秒）
    access-token-validity: 3600
    # Refresh Token 有效期（秒）
    refresh-token-validity: 2592000
    # Token 类型：jwt 或 opaque
    token-type: jwt
```

### 2. 客户端配置

客户端信息存储在数据库的 `oauth_clients` 表中。

**字段说明：**

- `client_id`: 客户端 ID
- `client_secret`: 客户端密钥（BCrypt 加密）
- `redirect_uris`: 回调地址（多个用逗号分隔）
- `scopes`: 支持的权限范围（多个用空格分隔）
- `grant_types`: 支持的授权类型（多个用逗号分隔）
- `token_type`: Token 类型（jwt 或 opaque）
- `access_token_validity`: Access Token 有效期（秒）
- `refresh_token_validity`: Refresh Token 有效期（秒）

**示例数据：**

```sql
INSERT INTO oauth_clients (client_id, client_secret, redirect_uris, scopes, grant_types, token_type, access_token_validity, refresh_token_validity) 
VALUES (
  'alexa_client',
  '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EH',
  'https://pitangui.amazon.com/api/skill/link/xxx,https://layla.amazon.com/api/skill/link/xxx',
  'read write',
  'authorization_code,refresh_token',
  'jwt',
  3600,
  2592000
);
```

## 使用流程

### 1. 授权码模式（标准流程）

#### 步骤 1: 用户授权

用户访问授权页面：

```
https://your-domain.com/oauth2/authorize?response_type=code&client_id=alexa_client&redirect_uri=https://pitangui.amazon.com/api/skill/link/xxx&state=xyz123
```

#### 步骤 2: 用户登录

系统会重定向到登录页面 `/oauth2/login`，用户输入用户名和密码。

**测试用户：**
- 用户名: `user1` / `user2` / `testuser`
- 密码: `password`

#### 步骤 3: 用户授权

登录成功后，系统显示授权同意页面，用户点击"授权"按钮。

#### 步骤 4: 获取授权码

系统重定向到回调地址，并附带授权码：

```
https://pitangui.amazon.com/api/skill/link/xxx?code=abc123&state=xyz123
```

#### 步骤 5: 交换 Token

客户端使用授权码交换访问令牌：

```bash
curl -X POST https://your-domain.com/oauth2/token \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -d "grant_type=authorization_code" \
  -d "code=abc123" \
  -d "client_id=alexa_client" \
  -d "client_secret=alexa_secret" \
  -d "redirect_uri=https://pitangui.amazon.com/api/skill/link/xxx"
```

#### 步骤 6: 使用 Token

客户端使用访问令牌调用 API：

```bash
curl -X POST https://your-domain.com/alexa/v3/directives \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..." \
  -H "Content-Type: application/json" \
  -d '{"directive": {...}}'
```

### 2. 刷新令牌

当访问令牌过期时，使用刷新令牌获取新的访问令牌：

```bash
curl -X POST https://your-domain.com/oauth2/token \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -d "grant_type=refresh_token" \
  -d "refresh_token=tGzv3JOkF0XG5Qx2TlKWIA" \
  -d "client_id=alexa_client" \
  -d "client_secret=alexa_secret"
```

### 3. 客户端凭证模式

用于服务器到服务器的通信：

```bash
curl -X POST https://your-domain.com/oauth2/token \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -d "grant_type=client_credentials" \
  -d "client_id=alexa_client" \
  -d "client_secret=alexa_secret" \
  -d "scope=read write"
```

## 智能音箱平台配置

### 1. Amazon Alexa

在 Alexa Developer Console 中配置：

- **Authorization URI**: `https://your-domain.com/oauth2/authorize`
- **Access Token URI**: `https://your-domain.com/oauth2/token`
- **Client ID**: `alexa_client`
- **Client Secret**: `alexa_secret`
- **Scopes**: `read write`

### 2. Google Assistant

在 Google Actions Console 中配置：

- **Authorization URL**: `https://your-domain.com/oauth2/authorize`
- **Token URL**: `https://your-domain.com/oauth2/token`
- **Client ID**: `google_client`
- **Client Secret**: `google_secret`
- **Scopes**: `read write`

Google 会自动检查 `/.well-known/openid-configuration` 端点。

### 3. 天猫精灵 / 小度 / 小爱

这些平台使用旧的 OAuth 端点（向后兼容）：

- **授权地址**: `https://your-domain.com/authorize`
- **Token 地址**: `https://your-domain.com/token`

## 安全建议

### 1. 生产环境配置

- 修改 JWT 密钥为更复杂的随机字符串
- 使用 HTTPS 协议
- 配置 CORS 策略
- 启用请求频率限制

### 2. 客户端密钥管理

- 使用 BCrypt 加密存储客户端密钥
- 定期轮换密钥
- 不要在前端代码中暴露密钥

### 3. Token 管理

- 设置合理的 Token 过期时间
- 实现 Token 撤销机制
- 记录 Token 使用日志

## 测试

### 1. 使用 Postman 测试

项目提供了 Postman 测试集合，包含所有 OAuth2 端点的测试用例。

导入文件：`OAuth2_Postman_Collection.json`

### 2. 使用浏览器测试

访问授权页面：

```
http://localhost:8080/oauth2/authorize?response_type=code&client_id=alexa_client&redirect_uri=http://localhost:8080/callback&state=test123
```

### 3. 使用 curl 测试

完整的授权码流程测试：

```bash
# 1. 获取授权码（需要在浏览器中完成）
# 访问: http://localhost:8080/oauth2/authorize?response_type=code&client_id=alexa_client&redirect_uri=http://localhost:8080/callback&state=test123

# 2. 交换 Token
curl -X POST http://localhost:8080/oauth2/token \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -d "grant_type=authorization_code" \
  -d "code=YOUR_CODE" \
  -d "client_id=alexa_client" \
  -d "client_secret=alexa_secret" \
  -d "redirect_uri=http://localhost:8080/callback"

# 3. 验证 Token
curl -X POST http://localhost:8080/oauth2/introspect \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -d "token=YOUR_TOKEN" \
  -d "client_id=alexa_client" \
  -d "client_secret=alexa_secret"
```

## 故障排查

### 1. 授权码无效

- 检查授权码是否已过期（默认 120 秒）
- 检查授权码是否已被使用
- 检查 redirect_uri 是否与授权时一致

### 2. Token 验证失败

- 检查 JWT 密钥配置是否正确
- 检查 Token 是否已过期
- 检查 Token 格式是否正确

### 3. 客户端认证失败

- 检查 client_id 和 client_secret 是否正确
- 检查客户端是否已在数据库中配置
- 检查客户端密钥是否使用 BCrypt 加密

## 数据库迁移

运行数据库迁移脚本：

```bash
# Linux/Mac
mysql -u root -p smarthomedb < src/main/resources/smarthomedb.sql

# Windows
mysql -u root -p smarthomedb < src\main\resources\smarthomedb.sql
```

或使用提供的导入脚本：

```bash
# Linux/Mac
./import-test-data.sh

# Windows
import-test-data.bat
```

## 相关文档

- [OAuth2 完善方案](OAuth2完善方案.md)
- [OAuth 配置指南](OAuth配置指南.md)
- [API 文档](API.md)
- [部署指南](DEPLOYMENT.md)

## 技术支持

如有问题，请参考：

1. 项目文档目录
2. 测试数据说明
3. Postman 测试集合
4. 日志输出（启用 DEBUG 级别）
