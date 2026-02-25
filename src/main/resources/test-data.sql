-- ============================================
-- 多平台智能音箱对接项目 - 测试数据
-- ============================================
-- 说明：
-- 1. 本脚本包含五个平台的测试数据（天猫精灵、小度音箱、小爱同学、AWS Alexa、Google Assistant）
-- 2. 包含多个测试用户和扫地机器人设备
-- 3. client_id 和 client_secret 需要替换为实际的平台配置
-- ============================================

USE smarthomedb;

-- ============================================
-- 1. 清空现有测试数据（可选）
-- ============================================
-- TRUNCATE TABLE oauth_authorization_codes;
-- TRUNCATE TABLE oauth_access_tokens;
-- TRUNCATE TABLE oauth_refresh_tokens;
-- DELETE FROM devices WHERE id > 0;
-- DELETE FROM users WHERE id > 0;
-- DELETE FROM oauth_clients WHERE id > 0;

-- ============================================
-- 1.5. 用户数据
-- ============================================
-- 注意：密码使用 BCrypt 加密，明文密码为 "password"
-- BCrypt 加密后的 "password": $2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EH

INSERT INTO users (username, password, email, enabled, account_non_expired, account_non_locked, credentials_non_expired, created_at, updated_at)
VALUES 
    ('user1', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EH', 'user1@example.com', TRUE, TRUE, TRUE, TRUE, NOW(), NOW()),
    ('user2', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EH', 'user2@example.com', TRUE, TRUE, TRUE, TRUE, NOW(), NOW()),
    ('testuser', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EH', 'testuser@example.com', TRUE, TRUE, TRUE, TRUE, NOW(), NOW())
ON DUPLICATE KEY UPDATE 
    password = VALUES(password),
    email = VALUES(email),
    updated_at = NOW();

-- ============================================
-- 2. OAuth 客户端数据（五个平台）
-- ============================================

-- 天猫精灵 OAuth 客户端
-- 注意：请将 YOUR_ALIGENIE_CLIENT_ID 和 YOUR_ALIGENIE_CLIENT_SECRET 替换为实际值
INSERT INTO oauth_clients (client_id, client_secret, redirect_uri, user_id) 
VALUES (
    'YOUR_ALIGENIE_CLIENT_ID',           -- 替换为天猫精灵开放平台的 client_id
    'YOUR_ALIGENIE_CLIENT_SECRET',       -- 替换为天猫精灵开放平台的 client_secret
    'https://aligenie.com/callback',     -- 天猫精灵回调地址
    1
) ON DUPLICATE KEY UPDATE 
    client_secret = VALUES(client_secret),
    redirect_uri = VALUES(redirect_uri);

-- 小度音箱 OAuth 客户端
-- 注意：请将 YOUR_DUEROS_CLIENT_ID 和 YOUR_DUEROS_CLIENT_SECRET 替换为实际值
INSERT INTO oauth_clients (client_id, client_secret, redirect_uri, user_id) 
VALUES (
    'YOUR_DUEROS_CLIENT_ID',             -- 替换为小度开放平台的 client_id
    'YOUR_DUEROS_CLIENT_SECRET',         -- 替换为小度开放平台的 client_secret
    'https://dueros.baidu.com/callback', -- 小度音箱回调地址
    1
) ON DUPLICATE KEY UPDATE 
    client_secret = VALUES(client_secret),
    redirect_uri = VALUES(redirect_uri);

-- 小爱同学 OAuth 客户端
-- 注意：请将 YOUR_MIAI_CLIENT_ID 和 YOUR_MIAI_CLIENT_SECRET 替换为实际值
INSERT INTO oauth_clients (client_id, client_secret, redirect_uri, user_id) 
VALUES (
    'YOUR_MIAI_CLIENT_ID',               -- 替换为小米 IoT 平台的 client_id
    'YOUR_MIAI_CLIENT_SECRET',           -- 替换为小米 IoT 平台的 client_secret
    'https://xiaoai.mi.com/callback',    -- 小爱同学回调地址
    1
) ON DUPLICATE KEY UPDATE 
    client_secret = VALUES(client_secret),
    redirect_uri = VALUES(redirect_uri);

-- AWS Alexa OAuth 客户端
-- 注意：请将 YOUR_ALEXA_CLIENT_ID 和 YOUR_ALEXA_CLIENT_SECRET 替换为实际值
INSERT INTO oauth_clients (client_id, client_secret, redirect_uri, user_id) 
VALUES (
    'YOUR_ALEXA_CLIENT_ID',              -- 替换为 Alexa 开发者控制台的 client_id
    'YOUR_ALEXA_CLIENT_SECRET',          -- 替换为 Alexa 开发者控制台的 client_secret
    'https://pitangui.amazon.com/api/skill/link/YOUR_VENDOR_ID', -- Alexa 回调地址（需替换 YOUR_VENDOR_ID）
    1
) ON DUPLICATE KEY UPDATE 
    client_secret = VALUES(client_secret),
    redirect_uri = VALUES(redirect_uri);

-- Google Assistant OAuth 客户端
-- 注意：请将 YOUR_GOOGLE_CLIENT_ID 和 YOUR_GOOGLE_CLIENT_SECRET 替换为实际值
INSERT INTO oauth_clients (client_id, client_secret, redirect_uri, user_id) 
VALUES (
    'YOUR_GOOGLE_CLIENT_ID',             -- 替换为 Google Actions Console 的 client_id
    'YOUR_GOOGLE_CLIENT_SECRET',         -- 替换为 Google Actions Console 的 client_secret
    'https://oauth-redirect.googleusercontent.com/r/YOUR_PROJECT_ID', -- Google 回调地址（需替换 YOUR_PROJECT_ID）
    1
) ON DUPLICATE KEY UPDATE 
    client_secret = VALUES(client_secret),
    redirect_uri = VALUES(redirect_uri);

-- 本地测试客户端（用于 Postman 测试）
INSERT INTO oauth_clients (client_id, client_secret, redirect_uri, user_id) 
VALUES (
    'test_client_id',
    'test_client_secret',
    'http://localhost:8080/callback',
    1
) ON DUPLICATE KEY UPDATE 
    client_secret = VALUES(client_secret),
    redirect_uri = VALUES(redirect_uri);

-- ============================================
-- 3. 用户数据
-- ============================================

-- 测试用户 1（主要测试账号）
INSERT INTO users (id, username, password, email) 
VALUES (
    1,
    'testuser',
    'password123',
    'test@example.com'
) ON DUPLICATE KEY UPDATE 
    password = VALUES(password),
    email = VALUES(email);

-- 测试用户 2
INSERT INTO users (id, username, password, email) 
VALUES (
    2,
    'zhangsan',
    'password123',
    'zhangsan@example.com'
) ON DUPLICATE KEY UPDATE 
    password = VALUES(password),
    email = VALUES(email);

-- 测试用户 3
INSERT INTO users (id, username, password, email) 
VALUES (
    3,
    'lisi',
    'password123',
    'lisi@example.com'
) ON DUPLICATE KEY UPDATE 
    password = VALUES(password),
    email = VALUES(email);

-- ============================================
-- 4. 扫地机器人设备数据
-- ============================================

-- 用户1的设备（3台扫地机器人）

-- 设备1：客厅扫地机器人（在线，工作中）
INSERT INTO devices (device_id, device_name, device_type, user_id, status, power_state, work_mode, battery_level)
VALUES (
    'robot_001',
    '客厅扫地机器人',
    'robot_cleaner',
    1,
    'online',      -- 在线
    'on',          -- 开机
    'auto',        -- 自动模式
    85             -- 电量 85%
) ON DUPLICATE KEY UPDATE 
    device_name = VALUES(device_name),
    status = VALUES(status),
    power_state = VALUES(power_state),
    work_mode = VALUES(work_mode),
    battery_level = VALUES(battery_level);

-- 设备2：卧室扫地机器人（在线，待机）
INSERT INTO devices (device_id, device_name, device_type, user_id, status, power_state, work_mode, battery_level)
VALUES (
    'robot_002',
    '卧室扫地机器人',
    'robot_cleaner',
    1,
    'online',      -- 在线
    'off',         -- 关机
    'auto',        -- 自动模式
    100            -- 电量 100%（充满电）
) ON DUPLICATE KEY UPDATE 
    device_name = VALUES(device_name),
    status = VALUES(status),
    power_state = VALUES(power_state),
    work_mode = VALUES(work_mode),
    battery_level = VALUES(battery_level);

-- 设备3：书房扫地机器人（在线，低电量）
INSERT INTO devices (device_id, device_name, device_type, user_id, status, power_state, work_mode, battery_level)
VALUES (
    'robot_003',
    '书房扫地机器人',
    'robot_cleaner',
    1,
    'online',      -- 在线
    'on',          -- 开机
    'spot',        -- 定点清扫模式
    15             -- 电量 15%（低电量）
) ON DUPLICATE KEY UPDATE 
    device_name = VALUES(device_name),
    status = VALUES(status),
    power_state = VALUES(power_state),
    work_mode = VALUES(work_mode),
    battery_level = VALUES(battery_level);

-- 设备4：厨房扫地机器人（离线）
INSERT INTO devices (device_id, device_name, device_type, user_id, status, power_state, work_mode, battery_level)
VALUES (
    'robot_004',
    '厨房扫地机器人',
    'robot_cleaner',
    1,
    'offline',     -- 离线
    'off',         -- 关机
    'auto',        -- 自动模式
    0              -- 电量 0%（没电）
) ON DUPLICATE KEY UPDATE 
    device_name = VALUES(device_name),
    status = VALUES(status),
    power_state = VALUES(power_state),
    work_mode = VALUES(work_mode),
    battery_level = VALUES(battery_level);

-- 用户2的设备（2台扫地机器人）

-- 设备5：张三的客厅扫地机器人
INSERT INTO devices (device_id, device_name, device_type, user_id, status, power_state, work_mode, battery_level)
VALUES (
    'robot_005',
    '客厅扫地机器人',
    'robot_cleaner',
    2,
    'online',      -- 在线
    'on',          -- 开机
    'edge',        -- 沿边清扫模式
    60             -- 电量 60%
) ON DUPLICATE KEY UPDATE 
    device_name = VALUES(device_name),
    status = VALUES(status),
    power_state = VALUES(power_state),
    work_mode = VALUES(work_mode),
    battery_level = VALUES(battery_level);

-- 设备6：张三的卧室扫地机器人
INSERT INTO devices (device_id, device_name, device_type, user_id, status, power_state, work_mode, battery_level)
VALUES (
    'robot_006',
    '卧室扫地机器人',
    'robot_cleaner',
    2,
    'online',      -- 在线
    'off',         -- 关机
    'auto',        -- 自动模式
    95             -- 电量 95%
) ON DUPLICATE KEY UPDATE 
    device_name = VALUES(device_name),
    status = VALUES(status),
    power_state = VALUES(power_state),
    work_mode = VALUES(work_mode),
    battery_level = VALUES(battery_level);

-- 用户3的设备（1台扫地机器人）

-- 设备7：李四的扫地机器人
INSERT INTO devices (device_id, device_name, device_type, user_id, status, power_state, work_mode, battery_level)
VALUES (
    'robot_007',
    '全屋扫地机器人',
    'robot_cleaner',
    3,
    'online',      -- 在线
    'on',          -- 开机
    'auto',        -- 自动模式
    75             -- 电量 75%
) ON DUPLICATE KEY UPDATE 
    device_name = VALUES(device_name),
    status = VALUES(status),
    power_state = VALUES(power_state),
    work_mode = VALUES(work_mode),
    battery_level = VALUES(battery_level);

-- ============================================
-- 额外测试设备（用于各种测试场景）
-- ============================================

-- 设备8：阳台扫地机器人（在线，沿边模式）
INSERT INTO devices (device_id, device_name, device_type, user_id, status, power_state, work_mode, battery_level)
VALUES (
    'robot_008',
    '阳台扫地机器人',
    'robot_cleaner',
    1,
    'online',      -- 在线
    'on',          -- 开机
    'edge',        -- 沿边清扫模式
    50             -- 电量 50%
) ON DUPLICATE KEY UPDATE 
    device_name = VALUES(device_name),
    status = VALUES(status),
    power_state = VALUES(power_state),
    work_mode = VALUES(work_mode),
    battery_level = VALUES(battery_level);

-- 设备9：餐厅扫地机器人（在线，充电中）
INSERT INTO devices (device_id, device_name, device_type, user_id, status, power_state, work_mode, battery_level)
VALUES (
    'robot_009',
    '餐厅扫地机器人',
    'robot_cleaner',
    1,
    'online',      -- 在线
    'off',         -- 关机（充电中）
    'auto',        -- 自动模式
    88             -- 电量 88%（充电中）
) ON DUPLICATE KEY UPDATE 
    device_name = VALUES(device_name),
    status = VALUES(status),
    power_state = VALUES(power_state),
    work_mode = VALUES(work_mode),
    battery_level = VALUES(battery_level);

-- 设备10：玄关扫地机器人（在线，定点模式）
INSERT INTO devices (device_id, device_name, device_type, user_id, status, power_state, work_mode, battery_level)
VALUES (
    'robot_010',
    '玄关扫地机器人',
    'robot_cleaner',
    1,
    'online',      -- 在线
    'on',          -- 开机
    'spot',        -- 定点清扫模式
    70             -- 电量 70%
) ON DUPLICATE KEY UPDATE 
    device_name = VALUES(device_name),
    status = VALUES(status),
    power_state = VALUES(power_state),
    work_mode = VALUES(work_mode),
    battery_level = VALUES(battery_level);

-- 设备11：主卧扫地机器人（在线，满电待机）
INSERT INTO devices (device_id, device_name, device_type, user_id, status, power_state, work_mode, battery_level)
VALUES (
    'robot_011',
    '主卧扫地机器人',
    'robot_cleaner',
    2,
    'online',      -- 在线
    'off',         -- 关机
    'auto',        -- 自动模式
    100            -- 电量 100%（满电）
) ON DUPLICATE KEY UPDATE 
    device_name = VALUES(device_name),
    status = VALUES(status),
    power_state = VALUES(power_state),
    work_mode = VALUES(work_mode),
    battery_level = VALUES(battery_level);

-- 设备12：次卧扫地机器人（在线，中等电量）
INSERT INTO devices (device_id, device_name, device_type, user_id, status, power_state, work_mode, battery_level)
VALUES (
    'robot_012',
    '次卧扫地机器人',
    'robot_cleaner',
    2,
    'online',      -- 在线
    'on',          -- 开机
    'auto',        -- 自动模式
    45             -- 电量 45%
) ON DUPLICATE KEY UPDATE 
    device_name = VALUES(device_name),
    status = VALUES(status),
    power_state = VALUES(power_state),
    work_mode = VALUES(work_mode),
    battery_level = VALUES(battery_level);

-- 设备13：儿童房扫地机器人（在线，低电量警告）
INSERT INTO devices (device_id, device_name, device_type, user_id, status, power_state, work_mode, battery_level)
VALUES (
    'robot_013',
    '儿童房扫地机器人',
    'robot_cleaner',
    3,
    'online',      -- 在线
    'on',          -- 开机
    'spot',        -- 定点清扫模式
    10             -- 电量 10%（低电量警告）
) ON DUPLICATE KEY UPDATE 
    device_name = VALUES(device_name),
    status = VALUES(status),
    power_state = VALUES(power_state),
    work_mode = VALUES(work_mode),
    battery_level = VALUES(battery_level);

-- 设备14：老人房扫地机器人（在线，沿边模式）
INSERT INTO devices (device_id, device_name, device_type, user_id, status, power_state, work_mode, battery_level)
VALUES (
    'robot_014',
    '老人房扫地机器人',
    'robot_cleaner',
    3,
    'online',      -- 在线
    'on',          -- 开机
    'edge',        -- 沿边清扫模式
    80             -- 电量 80%
) ON DUPLICATE KEY UPDATE 
    device_name = VALUES(device_name),
    status = VALUES(status),
    power_state = VALUES(power_state),
    work_mode = VALUES(work_mode),
    battery_level = VALUES(battery_level);

-- 设备15：地下室扫地机器人（离线，测试离线场景）
INSERT INTO devices (device_id, device_name, device_type, user_id, status, power_state, work_mode, battery_level)
VALUES (
    'robot_015',
    '地下室扫地机器人',
    'robot_cleaner',
    1,
    'offline',     -- 离线
    'off',         -- 关机
    'auto',        -- 自动模式
    5              -- 电量 5%（几乎没电）
) ON DUPLICATE KEY UPDATE 
    device_name = VALUES(device_name),
    status = VALUES(status),
    power_state = VALUES(power_state),
    work_mode = VALUES(work_mode),
    battery_level = VALUES(battery_level);

-- ============================================
-- 5. 查询测试数据
-- ============================================

-- 查看所有 OAuth 客户端
SELECT 
    id,
    client_id,
    CONCAT(LEFT(client_secret, 10), '...') AS client_secret_preview,
    redirect_uri,
    user_id,
    created_at
FROM oauth_clients
ORDER BY id;

-- 查看所有用户
SELECT 
    id,
    username,
    email,
    created_at
FROM users
ORDER BY id;

-- 查看所有设备
SELECT 
    d.id,
    d.device_id,
    d.device_name,
    d.device_type,
    u.username AS owner,
    d.status,
    d.power_state,
    d.work_mode,
    CONCAT(d.battery_level, '%') AS battery,
    d.created_at
FROM devices d
LEFT JOIN users u ON d.user_id = u.id
ORDER BY d.user_id, d.id;

-- 统计信息
SELECT 
    '客户端数量' AS item,
    COUNT(*) AS count
FROM oauth_clients
UNION ALL
SELECT 
    '用户数量' AS item,
    COUNT(*) AS count
FROM users
UNION ALL
SELECT 
    '设备数量' AS item,
    COUNT(*) AS count
FROM devices
UNION ALL
SELECT 
    '在线设备' AS item,
    COUNT(*) AS count
FROM devices
WHERE status = 'online'
UNION ALL
SELECT 
    '工作中设备' AS item,
    COUNT(*) AS count
FROM devices
WHERE power_state = 'on';

-- ============================================
-- 6. 测试数据说明
-- ============================================

/*
设备状态说明（15台扫地机器人）：

用户1（testuser）的设备：
- robot_001: 客厅扫地机器人，在线工作中，电量85%，自动模式 ✅ 正常工作
- robot_002: 卧室扫地机器人，在线待机，电量100%，自动模式 ✅ 满电待机
- robot_003: 书房扫地机器人，在线工作中，电量15%，定点模式 ⚠️ 低电量
- robot_004: 厨房扫地机器人，离线，电量0% ❌ 离线测试
- robot_008: 阳台扫地机器人，在线工作中，电量50%，沿边模式 ✅ 沿边清扫
- robot_009: 餐厅扫地机器人，在线充电中，电量88%，自动模式 🔋 充电中
- robot_010: 玄关扫地机器人，在线工作中，电量70%，定点模式 ✅ 定点清扫
- robot_015: 地下室扫地机器人，离线，电量5% ❌ 离线低电

用户2（zhangsan）的设备：
- robot_005: 客厅扫地机器人，在线工作中，电量60%，沿边模式 ✅ 沿边清扫
- robot_006: 卧室扫地机器人，在线待机，电量95%，自动模式 ✅ 高电量待机
- robot_011: 主卧扫地机器人，在线待机，电量100%，自动模式 ✅ 满电待机
- robot_012: 次卧扫地机器人，在线工作中，电量45%，自动模式 ✅ 中等电量

用户3（lisi）的设备：
- robot_007: 全屋扫地机器人，在线工作中，电量75%，自动模式 ✅ 正常工作
- robot_013: 儿童房扫地机器人，在线工作中，电量10%，定点模式 ⚠️ 低电量警告
- robot_014: 老人房扫地机器人，在线工作中，电量80%，沿边模式 ✅ 正常工作

工作模式说明：
- auto: 自动清扫模式（全屋清扫）- 最常用模式
- spot: 定点清扫模式（局部清扫）- 针对特定区域
- edge: 沿边清扫模式（沿墙边清扫）- 清扫边角

电量等级说明：
- 100-80%: 高电量 ✅ 可以长时间工作
- 79-50%: 中等电量 ✅ 正常工作
- 49-20%: 低电量 ⚠️ 建议充电
- 19-0%: 极低电量 ❌ 需要立即充电

测试场景覆盖：
1. 正常工作场景：robot_001, robot_005, robot_007, robot_008, robot_010, robot_012, robot_014
2. 待机场景：robot_002, robot_006, robot_009, robot_011
3. 低电量场景：robot_003, robot_013
4. 离线场景：robot_004, robot_015
5. 不同工作模式：
   - auto 模式：robot_001, robot_002, robot_006, robot_007, robot_009, robot_011, robot_012
   - spot 模式：robot_003, robot_010, robot_013
   - edge 模式：robot_005, robot_008, robot_014
6. 不同电量等级：
   - 满电（100%）：robot_002, robot_011
   - 高电量（80-99%）：robot_001, robot_006, robot_009, robot_014
   - 中等电量（50-79%）：robot_005, robot_007, robot_010
   - 低电量（20-49%）：robot_008, robot_012
   - 极低电量（0-19%）：robot_003, robot_004, robot_013, robot_015

OAuth 客户端配置（五个平台）：
1. 天猫精灵：
   - 获取地址：https://open.bot.tmall.com/
   - 替换：YOUR_ALIGENIE_CLIENT_ID, YOUR_ALIGENIE_CLIENT_SECRET
   
2. 小度音箱：
   - 获取地址：https://dueros.baidu.com/
   - 替换：YOUR_DUEROS_CLIENT_ID, YOUR_DUEROS_CLIENT_SECRET
   
3. 小爱同学：
   - 获取地址：https://developers.xiaoai.mi.com/
   - 替换：YOUR_MIAI_CLIENT_ID, YOUR_MIAI_CLIENT_SECRET
   
4. AWS Alexa：
   - 获取地址：https://developer.amazon.com/alexa
   - 替换：YOUR_ALEXA_CLIENT_ID, YOUR_ALEXA_CLIENT_SECRET, YOUR_VENDOR_ID
   - 注意：需要在 Alexa 开发者控制台创建 Smart Home Skill
   
5. Google Assistant：
   - 获取地址：https://console.actions.google.com/
   - 替换：YOUR_GOOGLE_CLIENT_ID, YOUR_GOOGLE_CLIENT_SECRET, YOUR_PROJECT_ID
   - 注意：需要在 Actions Console 创建 Smart Home Action

6. 本地测试：
   - 使用 test_client_id / test_client_secret 进行 Postman 测试
   - 无需替换，可直接使用

使用步骤：
1. 替换所有 YOUR_* 占位符为实际的平台配置
2. 执行本脚本导入测试数据：
   mysql -u root -p smarthomedb < test-data.sql
3. 验证数据导入成功：
   - 查看 OAuth 客户端：SELECT * FROM oauth_clients;
   - 查看用户：SELECT * FROM users;
   - 查看设备：SELECT * FROM devices;
4. 使用 Postman 测试集合进行接口测试
5. 在各平台 App 中进行语音控制测试

测试建议：
1. 先使用 Postman 测试基本功能
2. 测试不同状态的设备（在线/离线/低电量）
3. 测试不同工作模式的切换
4. 测试多设备批量操作
5. 测试错误处理（离线设备、不存在的设备等）
6. 最后在实际音箱上进行语音测试

常用测试命令（语音）：
- "打开客厅扫地机器人"
- "关闭卧室扫地机器人"
- "开始清扫"
- "暂停清扫"
- "继续清扫"
- "回充"
- "设置为定点清扫模式"
- "查询扫地机器人状态"
- "扫地机器人电量还有多少"

数据统计：
- OAuth 客户端：6 个（5个平台 + 1个本地测试）
- 测试用户：3 个
- 扫地机器人设备：15 台
- 在线设备：13 台
- 离线设备：2 台
- 工作中设备：9 台
- 待机设备：4 台
- 充电中设备：1 台
*/
