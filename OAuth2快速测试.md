# OAuth2 快速测试指南

## 测试账号

| 用户名 | 密码 |
|--------|------|
| user1 | password |
| user2 | password |
| testuser | password |

## 完整测试流程

### 步骤 1: 访问授权端点

在浏览器中访问：

```
http://localhost:8080/oauth2/authorize?response_type=code&client_id=test_client_id&redirect_uri=http://localhost:8080/callback&state=test123
```

**参数说明：**
- `response_type=code`: 使用授权码模式
- `client_id=test_client_id`: 客户端 ID（测试用）
- `redirect_uri=http://localhost:8080/callback`: 回调地址
- `state=test123`: 状态参数（防止 CSRF）

### 步骤 2: 登录

系统会自动跳转到登录页面，输入测试账号：

- 用户名: `testuser`
- 密码: `password`

点击"登录"按钮。

### 步骤 3: 授权同意

登录成功后，会显示授权同意页面，点击"授权"按钮。

### 步骤 4: 获取授权码

系统会重定向到回调地址，URL 中包含授权码：

```
http://localhost:8080/callback?code=AUTHORIZATION_CODE&state=test123
```

复制 `code` 参数的值（授权码）。

### 步骤 5: 交换 Token

使用 Postman 或 curl 交换访问令牌：

```bash
curl -X POST http://localhost:8080/oauth2/token \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -d "grant_type=authorization_code" \
  -d "code=YOUR_AUTHORIZATION_CODE" \
  -d "client_id=test_client_id" \
  -d "client_secret=test_client_secret" \
  -d "redirect_uri=http://localhost:8080/callback"
```

**响应示例：**

```json
{
  "access_token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "token_type": "Bearer",
  "expires_in": 3600,
  "refresh_token": "tGzv3JOkF0XG5Qx2TlKWIA",
  "scope": "device:control,device:read"
}
```

### 步骤 6: 使用 Token 调用 API

使用获取的访问令牌调用 API：

```bash
curl -X POST http://localhost:8080/alexa/v3/directives \
  -H "Authorization: Bearer YOUR_ACCESS_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "directive": {
      "header": {
        "namespace": "Alexa.Discovery",
        "name": "Discover",
        "payloadVersion": "3",
        "messageId": "test-message-id"
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

## 使用 Postman 测试

### 1. 导入测试集合

导入 `OAuth2_Postman_Collection.json` 文件。

### 2. 设置变量

在集合变量中设置：
- `base_url`: `http://localhost:8080`
- `client_id`: `test_client_id`
- `client_secret`: `test_client_secret`
- `redirect_uri`: `http://localhost:8080/callback`

### 3. 执行测试

按照集合中的顺序执行：

1. **获取授权页面** - 在浏览器中完成登录和授权
2. **交换 Token** - 自动保存 access_token 和 refresh_token
3. **刷新 Token** - 使用 refresh_token 获取新的 access_token
4. **验证 Token** - 检查 token 是否有效
5. **使用 Token 调用 API** - 测试 Alexa 或 Google API

## 常见问题

### Q1: 授权码已过期

**问题**: 提示 "授权码无效或已过期"

**解决**: 授权码默认有效期为 120 秒，请在获取后立即使用。如果过期，重新执行步骤 1-4。

### Q2: 客户端认证失败

**问题**: 提示 "invalid_client"

**解决**: 检查 `client_id` 和 `client_secret` 是否正确。测试环境使用：
- client_id: `test_client_id`
- client_secret: `test_client_secret`

### Q3: 重定向 URI 不匹配

**问题**: 提示 "redirect_uri 不匹配"

**解决**: 确保授权请求和 Token 请求中的 `redirect_uri` 完全一致。

### Q4: Token 验证失败

**问题**: 使用 Token 调用 API 时返回 401

**解决**: 
1. 检查 Token 是否已过期
2. 检查 Authorization header 格式：`Bearer YOUR_TOKEN`
3. 使用 refresh_token 获取新的 access_token

## 测试其他授权模式

### 客户端凭证模式

直接获取 Token，无需用户登录：

```bash
curl -X POST http://localhost:8080/oauth2/token \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -d "grant_type=client_credentials" \
  -d "client_id=test_client_id" \
  -d "client_secret=test_client_secret" \
  -d "scope=device:control,device:read"
```

### 刷新令牌模式

使用 refresh_token 获取新的 access_token：

```bash
curl -X POST http://localhost:8080/oauth2/token \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -d "grant_type=refresh_token" \
  -d "refresh_token=YOUR_REFRESH_TOKEN" \
  -d "client_id=test_client_id" \
  -d "client_secret=test_client_secret"
```

## OIDC 发现端点

查看 OpenID Connect 配置：

```
http://localhost:8080/.well-known/openid-configuration
```

## 相关文档

- [OAuth2 使用指南](OAuth2使用指南.md) - 完整文档
- [测试账号](测试账号.md) - 测试账号信息
- [OAuth 配置指南](OAuth配置指南.md) - 平台配置

## 技术支持

如遇问题，请检查：
1. 应用程序日志（DEBUG 级别）
2. 浏览器开发者工具（Network 标签）
3. Postman Console 输出

---

**最后更新**: 2024-02-24
