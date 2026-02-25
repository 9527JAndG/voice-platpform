-- ============================================
-- OAuth2 增强功能 - 数据库迁移脚本
-- ============================================
-- 说明：
-- 1. 扩展 oauth_clients 表，添加更多 OAuth2 标准字段
-- 2. 创建 oauth_authorizations 表，存储授权记录
-- 3. 添加必要的索引和约束
-- ============================================

USE smarthomedb;

-- ============================================
-- 1. 扩展 oauth_clients 表
-- ============================================

-- 检查并添加字段（使用存储过程避免重复添加）
DELIMITER //

CREATE PROCEDURE IF NOT EXISTS add_oauth_client_columns()
BEGIN
    -- 添加 scopes 字段
    IF NOT EXISTS (
        SELECT * FROM information_schema.COLUMNS 
        WHERE TABLE_SCHEMA = 'smarthomedb' 
        AND TABLE_NAME = 'oauth_clients' 
        AND COLUMN_NAME = 'scopes'
    ) THEN
        ALTER TABLE oauth_clients 
        ADD COLUMN scopes VARCHAR(500) DEFAULT 'device:control,device:read';
    END IF;
    
    -- 添加 grant_types 字段
    IF NOT EXISTS (
        SELECT * FROM information_schema.COLUMNS 
        WHERE TABLE_SCHEMA = 'smarthomedb' 
        AND TABLE_NAME = 'oauth_clients' 
        AND COLUMN_NAME = 'grant_types'
    ) THEN
        ALTER TABLE oauth_clients 
        ADD COLUMN grant_types VARCHAR(200) DEFAULT 'authorization_code,refresh_token';
    END IF;
    
    -- 添加 token_type 字段
    IF NOT EXISTS (
        SELECT * FROM information_schema.COLUMNS 
        WHERE TABLE_SCHEMA = 'smarthomedb' 
        AND TABLE_NAME = 'oauth_clients' 
        AND COLUMN_NAME = 'token_type'
    ) THEN
        ALTER TABLE oauth_clients 
        ADD COLUMN token_type VARCHAR(20) DEFAULT 'jwt';
    END IF;
    
    -- 添加 access_token_validity 字段
    IF NOT EXISTS (
        SELECT * FROM information_schema.COLUMNS 
        WHERE TABLE_SCHEMA = 'smarthomedb' 
        AND TABLE_NAME = 'oauth_clients' 
        AND COLUMN_NAME = 'access_token_validity'
    ) THEN
        ALTER TABLE oauth_clients 
        ADD COLUMN access_token_validity INT DEFAULT 3600;
    END IF;
    
    -- 添加 refresh_token_validity 字段
    IF NOT EXISTS (
        SELECT * FROM information_schema.COLUMNS 
        WHERE TABLE_SCHEMA = 'smarthomedb' 
        AND TABLE_NAME = 'oauth_clients' 
        AND COLUMN_NAME = 'refresh_token_validity'
    ) THEN
        ALTER TABLE oauth_clients 
        ADD COLUMN refresh_token_validity INT DEFAULT 2592000;
    END IF;
    
    -- 添加 auto_approve 字段
    IF NOT EXISTS (
        SELECT * FROM information_schema.COLUMNS 
        WHERE TABLE_SCHEMA = 'smarthomedb' 
        AND TABLE_NAME = 'oauth_clients' 
        AND COLUMN_NAME = 'auto_approve'
    ) THEN
        ALTER TABLE oauth_clients 
        ADD COLUMN auto_approve BOOLEAN DEFAULT FALSE;
    END IF;
    
    -- 添加 updated_at 字段
    IF NOT EXISTS (
        SELECT * FROM information_schema.COLUMNS 
        WHERE TABLE_SCHEMA = 'smarthomedb' 
        AND TABLE_NAME = 'oauth_clients' 
        AND COLUMN_NAME = 'updated_at'
    ) THEN
        ALTER TABLE oauth_clients 
        ADD COLUMN updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP;
    END IF;
END //

DELIMITER ;

-- 执行存储过程
CALL add_oauth_client_columns();

-- 删除存储过程
DROP PROCEDURE IF EXISTS add_oauth_client_columns;

-- ============================================
-- 2. 创建 oauth_authorizations 表
-- ============================================

CREATE TABLE IF NOT EXISTS oauth_authorizations (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    authorization_code VARCHAR(100) UNIQUE NOT NULL,
    client_id VARCHAR(100) NOT NULL,
    user_id BIGINT NOT NULL,
    redirect_uri VARCHAR(500) NOT NULL,
    scope VARCHAR(500),
    state VARCHAR(100),
    code_challenge VARCHAR(100),
    code_challenge_method VARCHAR(20),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    expires_at TIMESTAMP NOT NULL,
    used BOOLEAN DEFAULT FALSE,
    used_at TIMESTAMP NULL,
    
    INDEX idx_code (authorization_code),
    INDEX idx_client (client_id),
    INDEX idx_user (user_id),
    INDEX idx_expires (expires_at),
    
    FOREIGN KEY (client_id) REFERENCES oauth_clients(client_id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='OAuth2 授权记录表';

-- ============================================
-- 3. 更新现有 oauth_clients 数据
-- ============================================

-- 更新测试客户端
UPDATE oauth_clients 
SET 
    scopes = 'device:control,device:read',
    grant_types = 'authorization_code,refresh_token,client_credentials',
    token_type = 'jwt',
    access_token_validity = 3600,
    refresh_token_validity = 2592000,
    auto_approve = TRUE
WHERE client_id = 'test_client_id';

-- 更新平台客户端（如果存在）
UPDATE oauth_clients 
SET 
    scopes = 'device:control,device:read',
    grant_types = 'authorization_code,refresh_token',
    token_type = 'jwt',
    access_token_validity = 3600,
    refresh_token_validity = 2592000,
    auto_approve = FALSE
WHERE client_id LIKE 'YOUR_%';

-- ============================================
-- 4. 创建用户表（如果不存在）
-- ============================================

CREATE TABLE IF NOT EXISTS users (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(50) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    email VARCHAR(100),
    enabled BOOLEAN DEFAULT TRUE,
    account_non_expired BOOLEAN DEFAULT TRUE,
    account_non_locked BOOLEAN DEFAULT TRUE,
    credentials_non_expired BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    INDEX idx_username (username),
    INDEX idx_email (email)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

-- ============================================
-- 5. 创建用户角色表
-- ============================================

CREATE TABLE IF NOT EXISTS user_roles (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    role VARCHAR(50) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    INDEX idx_user (user_id),
    INDEX idx_role (role),
    UNIQUE KEY uk_user_role (user_id, role),
    
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户角色表';

-- ============================================
-- 6. 更新 oauth_access_tokens 表
-- ============================================

DELIMITER //

CREATE PROCEDURE IF NOT EXISTS add_access_token_columns()
BEGIN
    -- 添加 user_id 字段
    IF NOT EXISTS (
        SELECT * FROM information_schema.COLUMNS 
        WHERE TABLE_SCHEMA = 'smarthomedb' 
        AND TABLE_NAME = 'oauth_access_tokens' 
        AND COLUMN_NAME = 'user_id'
    ) THEN
        ALTER TABLE oauth_access_tokens 
        ADD COLUMN user_id BIGINT COMMENT '用户ID';
    END IF;
    
    -- 添加 scope 字段
    IF NOT EXISTS (
        SELECT * FROM information_schema.COLUMNS 
        WHERE TABLE_SCHEMA = 'smarthomedb' 
        AND TABLE_NAME = 'oauth_access_tokens' 
        AND COLUMN_NAME = 'scope'
    ) THEN
        ALTER TABLE oauth_access_tokens 
        ADD COLUMN scope VARCHAR(500) COMMENT 'Token 权限范围';
    END IF;
    
    -- 添加 jti 字段
    IF NOT EXISTS (
        SELECT * FROM information_schema.COLUMNS 
        WHERE TABLE_SCHEMA = 'smarthomedb' 
        AND TABLE_NAME = 'oauth_access_tokens' 
        AND COLUMN_NAME = 'jti'
    ) THEN
        ALTER TABLE oauth_access_tokens 
        ADD COLUMN jti VARCHAR(100) UNIQUE COMMENT 'JWT Token ID';
    END IF;
END //

DELIMITER ;

CALL add_access_token_columns();
DROP PROCEDURE IF EXISTS add_access_token_columns;

-- ============================================
-- 7. 更新 oauth_refresh_tokens 表
-- ============================================

DELIMITER //

CREATE PROCEDURE IF NOT EXISTS add_refresh_token_columns()
BEGIN
    -- 添加 user_id 字段
    IF NOT EXISTS (
        SELECT * FROM information_schema.COLUMNS 
        WHERE TABLE_SCHEMA = 'smarthomedb' 
        AND TABLE_NAME = 'oauth_refresh_tokens' 
        AND COLUMN_NAME = 'user_id'
    ) THEN
        ALTER TABLE oauth_refresh_tokens 
        ADD COLUMN user_id BIGINT COMMENT '用户ID';
    END IF;
    
    -- 添加 scope 字段
    IF NOT EXISTS (
        SELECT * FROM information_schema.COLUMNS 
        WHERE TABLE_SCHEMA = 'smarthomedb' 
        AND TABLE_NAME = 'oauth_refresh_tokens' 
        AND COLUMN_NAME = 'scope'
    ) THEN
        ALTER TABLE oauth_refresh_tokens 
        ADD COLUMN scope VARCHAR(500) COMMENT 'Token 权限范围';
    END IF;
END //

DELIMITER ;

CALL add_refresh_token_columns();
DROP PROCEDURE IF EXISTS add_refresh_token_columns;

-- ============================================
-- 8. 查询验证
-- ============================================

-- 查看 oauth_clients 表结构
DESCRIBE oauth_clients;

-- 查看 oauth_authorizations 表结构
DESCRIBE oauth_authorizations;

-- 查看 users 表结构
DESCRIBE users;

-- 查看 user_roles 表结构
DESCRIBE user_roles;

-- 统计信息
SELECT 
    'oauth_clients' AS table_name,
    COUNT(*) AS count
FROM oauth_clients
UNION ALL
SELECT 
    'oauth_authorizations' AS table_name,
    COUNT(*) AS count
FROM oauth_authorizations
UNION ALL
SELECT 
    'users' AS table_name,
    COUNT(*) AS count
FROM users
UNION ALL
SELECT 
    'user_roles' AS table_name,
    COUNT(*) AS count
FROM user_roles;

-- ============================================
-- 9. 清理过期数据的存储过程（可选）
-- ============================================

DELIMITER //

CREATE PROCEDURE IF NOT EXISTS cleanup_expired_authorizations()
BEGIN
    -- 删除过期的授权码（超过 10 分钟）
    DELETE FROM oauth_authorizations 
    WHERE expires_at < NOW() 
    OR (used = TRUE AND used_at < DATE_SUB(NOW(), INTERVAL 1 DAY));
    
    -- 删除过期的访问令牌
    DELETE FROM oauth_access_tokens 
    WHERE expires_at < NOW();
    
    -- 删除过期的刷新令牌
    DELETE FROM oauth_refresh_tokens 
    WHERE expires_at < NOW();
    
    SELECT 
        'Cleanup completed' AS status,
        ROW_COUNT() AS rows_affected;
END //

DELIMITER ;

-- ============================================
-- 10. 创建定时清理事件（可选）
-- ============================================

-- 启用事件调度器
SET GLOBAL event_scheduler = ON;

-- 创建每小时清理一次的事件
CREATE EVENT IF NOT EXISTS cleanup_expired_data
ON SCHEDULE EVERY 1 HOUR
DO CALL cleanup_expired_authorizations();

-- ============================================
-- 完成
-- ============================================

SELECT 'OAuth2 数据库迁移完成！' AS message;
