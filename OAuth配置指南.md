# OAuth 配置指南

## 📋 概述

本文档详细说明如何为五大智能音箱平台配置 OAuth 2.0 客户端信息。

## 🔧 配置步骤

### 1. 天猫精灵（AliGenie）

#### 获取 OAuth 凭证

1. **访问开放平台**
   - 地址：https://open.bot.tmall.com/
   - 使用淘宝账号登录

2. **创建技能**
   - 点击"创建技能"
   - 选择"智能家居"类型
   - 填写技能基本信息

3. **配置账号授权**
   - 进入技能配置页面
   - 找到"账号授权"部分
   - 授权方式选择"OAuth 2.0"
   - 记录以下信息：
     - Client ID（客户端ID）
     - Client Secret（客户端密钥）

4. **配置回调地址**
   ```
   https://your-domain.com/oauth/authorize
   ```

#### 更新测试数据

在 `test-data.sql` 中替换：
```sql
INSERT INTO oauth_clients (client_id, client_secret, redirect_uri, user_id) 
VALUES (
    'YOUR_ALIGENIE_CLIENT_ID',           -- 替换为实际的 Client ID
    'YOUR_ALIGENIE_CLIENT_SECRET',       -- 替换为实际的 Client Secret
    'https://aligenie.com/callback',     -- 天猫精灵回调地址
    1
);
```

---

### 2. 小度音箱（DuerOS）

#### 获取 OAuth 凭证

1. **访问开放平台**
   - 地址：https://dueros.baidu.com/
   - 使用百度账号登录

2. **创建技能**
   - 点击"控制台" → "技能开发"
   - 创建"智能家居技能"
   - 填写技能基本信息

3. **配置账号关联**
   - 进入技能配置
   - 找到"账号关联"部分
   - 授权类型选择"OAuth 2.0"
   - 记录以下信息：
     - Client ID
     - Client Secret

4. **配置授权 URL**
   ```
   授权页面：https://your-domain.com/oauth/authorize
   Token URL：https://your-domain.com/oauth/token
   ```

#### 更新测试数据

在 `test-data.sql` 中替换：
```sql
INSERT INTO oauth_clients (client_id, client_secret, redirect_uri, user_id) 
VALUES (
    'YOUR_DUEROS_CLIENT_ID',             -- 替换为实际的 Client ID
    'YOUR_DUEROS_CLIENT_SECRET',         -- 替换为实际的 Client Secret
    'https://dueros.baidu.com/callback', -- 小度回调地址
    1
);
```

---

### 3. 小爱同学（MiAI）

#### 获取 OAuth 凭证

1. **访问开放平台**
   - 地址：https://developers.xiaoai.mi.com/
   - 使用小米账号登录

2. **创建技能**
   - 进入"开发者中心"
   - 创建"智能家居技能"
   - 填写技能信息

3. **配置 OAuth**
   - 进入技能配置
   - 找到"账号授权"
   - 选择"OAuth 2.0"
   - 记录：
     - App ID（作为 Client ID）
     - App Secret（作为 Client Secret）

4. **配置回调地址**
   ```
   https://your-domain.com/oauth/authorize
   ```

#### 更新测试数据

在 `test-data.sql` 中替换：
```sql
INSERT INTO oauth_clients (client_id, client_secret, redirect_uri, user_id) 
VALUES (
    'YOUR_MIAI_CLIENT_ID',               -- 替换为实际的 App ID
    'YOUR_MIAI_CLIENT_SECRET',           -- 替换为实际的 App Secret
    'https://xiaoai.mi.com/callback',    -- 小爱回调地址
    1
);
```

---

### 4. AWS Alexa

#### 获取 OAuth 凭证

1. **访问开发者控制台**
   - 地址：https://developer.amazon.com/alexa
   - 使用 Amazon 账号登录

2. **创建 Smart Home Skill**
   - 点击"Create Skill"
   - 选择"Smart Home"模型
   - 选择"Provision your own"

3. **配置 Account Linking**
   - 进入 Skill 配置
   - 找到"Account Linking"部分
   - 配置以下信息：
     ```
     Authorization URI: https://your-domain.com/oauth/authorize
     Access Token URI: https://your-domain.com/oauth/token
     Client ID: 自己生成（建议使用 UUID）
     Client Secret: 自己生成（建议使用随机字符串）
     ```

4. **获取 Redirect URLs**
   - Alexa 会显示 3 个 Redirect URLs
   - 选择其中一个，格式如：
     ```
     https://pitangui.amazon.com/api/skill/link/YOUR_VENDOR_ID
     ```

5. **配置 Smart Home API**
   - Default Endpoint: `https://your-domain.com/alexa`

#### 更新测试数据

在 `test-data.sql` 中替换：
```sql
INSERT INTO oauth_clients (client_id, client_secret, redirect_uri, user_id) 
VALUES (
    'YOUR_ALEXA_CLIENT_ID',              -- 替换为你生成的 Client ID
    'YOUR_ALEXA_CLIENT_SECRET',          -- 替换为你生成的 Client Secret
    'https://pitangui.amazon.com/api/skill/link/YOUR_VENDOR_ID', -- 替换 YOUR_VENDOR_ID
    1
);
```

#### 生成 Client ID 和 Secret 示例

```bash
# 生成 Client ID（UUID）
uuidgen
# 或
python -c "import uuid; print(uuid.uuid4())"

# 生成 Client Secret（随机字符串）
openssl rand -base64 32
# 或
python -c "import secrets; print(secrets.token_urlsafe(32))"
```

---

### 5. Google Assistant

#### 获取 OAuth 凭证

1. **访问 Actions Console**
   - 地址：https://console.actions.google.com/
   - 使用 Google 账号登录

2. **创建 Smart Home Project**
   - 点击"New project"
   - 输入项目名称
   - 选择"Smart Home"

3. **配置 Account Linking**
   - 进入"Develop" → "Account linking"
   - 配置以下信息：
     ```
     Client ID: 自己生成（建议使用 UUID）
     Client Secret: 自己生成（建议使用随机字符串）
     Authorization URL: https://your-domain.com/oauth/authorize
     Token URL: https://your-domain.com/oauth/token
     ```

4. **获取 Project ID**
   - 在项目设置中找到 Project ID
   - 格式如：`your-project-id-123456`

5. **获取 Redirect URI**
   - Google 会自动生成，格式如：
     ```
     https://oauth-redirect.googleusercontent.com/r/YOUR_PROJECT_ID
     ```

6. **配置 Fulfillment**
   - 进入"Develop" → "Actions"
   - 配置 Fulfillment URL：
     ```
     https://your-domain.com/google/fulfillment
     ```

#### 更新测试数据

在 `test-data.sql` 中替换：
```sql
INSERT INTO oauth_clients (client_id, client_secret, redirect_uri, user_id) 
VALUES (
    'YOUR_GOOGLE_CLIENT_ID',             -- 替换为你生成的 Client ID
    'YOUR_GOOGLE_CLIENT_SECRET',         -- 替换为你生成的 Client Secret
    'https://oauth-redirect.googleusercontent.com/r/YOUR_PROJECT_ID', -- 替换 YOUR_PROJECT_ID
    1
);
```

---

## 📝 配置检查清单

### 天猫精灵
- [ ] 已获取 Client ID
- [ ] 已获取 Client Secret
- [ ] 已配置回调地址
- [ ] 已更新 test-data.sql
- [ ] 已测试授权流程

### 小度音箱
- [ ] 已获取 Client ID
- [ ] 已获取 Client Secret
- [ ] 已配置授权 URL
- [ ] 已配置 Token URL
- [ ] 已更新 test-data.sql
- [ ] 已测试授权流程

### 小爱同学
- [ ] 已获取 App ID
- [ ] 已获取 App Secret
- [ ] 已配置回调地址
- [ ] 已更新 test-data.sql
- [ ] 已测试授权流程

### AWS Alexa
- [ ] 已生成 Client ID
- [ ] 已生成 Client Secret
- [ ] 已获取 Redirect URL
- [ ] 已配置 Account Linking
- [ ] 已配置 Smart Home API
- [ ] 已更新 test-data.sql
- [ ] 已测试授权流程

### Google Assistant
- [ ] 已生成 Client ID
- [ ] 已生成 Client Secret
- [ ] 已获取 Project ID
- [ ] 已配置 Account Linking
- [ ] 已配置 Fulfillment URL
- [ ] 已更新 test-data.sql
- [ ] 已测试授权流程

---

## 🔐 安全建议

### Client Secret 管理

1. **不要提交到版本控制**
   - 将 `test-data.sql` 添加到 `.gitignore`
   - 或使用环境变量

2. **定期更换**
   - 建议每 3-6 个月更换一次
   - 发现泄露立即更换

3. **使用强密码**
   - 至少 32 个字符
   - 包含大小写字母、数字、特殊字符

### 环境变量方式（推荐）

创建 `.env` 文件：
```bash
# 天猫精灵
ALIGENIE_CLIENT_ID=your_client_id
ALIGENIE_CLIENT_SECRET=your_client_secret

# 小度音箱
DUEROS_CLIENT_ID=your_client_id
DUEROS_CLIENT_SECRET=your_client_secret

# 小爱同学
MIAI_CLIENT_ID=your_client_id
MIAI_CLIENT_SECRET=your_client_secret

# AWS Alexa
ALEXA_CLIENT_ID=your_client_id
ALEXA_CLIENT_SECRET=your_client_secret
ALEXA_VENDOR_ID=your_vendor_id

# Google Assistant
GOOGLE_CLIENT_ID=your_client_id
GOOGLE_CLIENT_SECRET=your_client_secret
GOOGLE_PROJECT_ID=your_project_id
```

在应用中读取：
```java
String clientId = System.getenv("ALIGENIE_CLIENT_ID");
String clientSecret = System.getenv("ALIGENIE_CLIENT_SECRET");
```

---

## 🧪 测试配置

### 1. 导入测试数据

```bash
# 确保已替换所有 YOUR_* 占位符
mysql -u root -p smarthomedb < src/main/resources/test-data.sql
```

### 2. 验证数据

```sql
-- 查看 OAuth 客户端
SELECT 
    id,
    client_id,
    CONCAT(LEFT(client_secret, 10), '...') AS secret_preview,
    redirect_uri
FROM oauth_clients;

-- 应该看到 6 条记录（5个平台 + 1个测试）
```

### 3. 测试授权流程

使用浏览器访问：
```
http://localhost:8080/oauth/authorize?client_id=YOUR_CLIENT_ID&redirect_uri=YOUR_REDIRECT_URI&response_type=code
```

### 4. 使用 Postman 测试

导入对应的 Postman 集合：
- `postman_collection.json`（天猫精灵）
- `Alexa_Postman_Collection.json`（Alexa）
- `Google_Postman_Collection.json`（Google）

---

## ❓ 常见问题

### Q1: 找不到 Client ID 和 Client Secret

**A**: 不同平台的位置不同：
- 天猫精灵：技能配置 → 账号授权
- 小度音箱：技能配置 → 账号关联
- 小爱同学：技能配置 → 账号授权
- Alexa：需要自己生成
- Google：需要自己生成

### Q2: Redirect URI 不匹配

**A**: 确保数据库中的 `redirect_uri` 与平台配置完全一致，包括：
- 协议（http/https）
- 域名
- 路径
- 端口号（如果有）

### Q3: 授权失败

**A**: 检查以下几点：
1. Client ID 和 Secret 是否正确
2. Redirect URI 是否匹配
3. 服务是否正常运行
4. 网络是否可达
5. HTTPS 证书是否有效（生产环境）

### Q4: Token 获取失败

**A**: 检查：
1. Authorization Code 是否有效
2. Token URL 是否正确
3. Client 认证是否正确
4. 数据库连接是否正常

---

## 📞 获取帮助

### 官方文档

- [天猫精灵开放平台文档](https://open.bot.tmall.com/docs)
- [小度开放平台文档](https://dueros.baidu.com/doc)
- [小爱开放平台文档](https://developers.xiaoai.mi.com/doc)
- [Alexa Smart Home 文档](https://developer.amazon.com/docs/smarthome)
- [Google Smart Home 文档](https://developers.google.com/assistant/smarthome)

### 项目文档

- [测试数据说明.md](测试数据说明.md)
- [平台配置说明.md](平台配置说明.md)
- [使用说明.md](使用说明.md)

---

**最后更新**：2026-02-24  
**版本**：1.0.0

祝你配置顺利！🎉
