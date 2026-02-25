-- 创建数据库
CREATE DATABASE IF NOT EXISTS smarthomedb DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE smarthomedb;

-- OAuth 客户端表
CREATE TABLE IF NOT EXISTS oauth_clients (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    client_id VARCHAR(100) NOT NULL UNIQUE COMMENT 'OAuth客户端ID',
    client_secret VARCHAR(100) NOT NULL COMMENT 'OAuth客户端密钥',
    redirect_uri VARCHAR(500) NOT NULL COMMENT '回调地址',
    user_id BIGINT COMMENT '关联用户ID',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_client_id (client_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='OAuth客户端表';

-- 授权码表
CREATE TABLE IF NOT EXISTS oauth_authorization_codes (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    authorization_code VARCHAR(100) NOT NULL UNIQUE COMMENT '授权码',
    client_id VARCHAR(100) NOT NULL COMMENT '客户端ID',
    redirect_uri VARCHAR(500) NOT NULL COMMENT '回调地址',
    expires_at TIMESTAMP NOT NULL COMMENT '过期时间',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_code (authorization_code),
    INDEX idx_client_id (client_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='授权码表';

-- 访问令牌表
CREATE TABLE IF NOT EXISTS oauth_access_tokens (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    access_token VARCHAR(100) NOT NULL UNIQUE COMMENT '访问令牌',
    client_id VARCHAR(100) NOT NULL COMMENT '客户端ID',
    expires_at TIMESTAMP NOT NULL COMMENT '过期时间',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_token (access_token),
    INDEX idx_client_id (client_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='访问令牌表';

-- 刷新令牌表
CREATE TABLE IF NOT EXISTS oauth_refresh_tokens (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    refresh_token VARCHAR(100) NOT NULL UNIQUE COMMENT '刷新令牌',
    client_id VARCHAR(100) NOT NULL COMMENT '客户端ID',
    expires_at TIMESTAMP NOT NULL COMMENT '过期时间',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_token (refresh_token),
    INDEX idx_client_id (client_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='刷新令牌表';

-- 用户表
CREATE TABLE IF NOT EXISTS users (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(50) NOT NULL UNIQUE COMMENT '用户名',
    password VARCHAR(100) NOT NULL COMMENT '密码',
    email VARCHAR(100) COMMENT '邮箱',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_username (username)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

-- 设备表
CREATE TABLE IF NOT EXISTS devices (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    device_id VARCHAR(100) NOT NULL UNIQUE COMMENT '设备ID',
    device_name VARCHAR(100) NOT NULL COMMENT '设备名称',
    device_type VARCHAR(50) NOT NULL COMMENT '设备类型：robot_cleaner',
    user_id BIGINT NOT NULL COMMENT '所属用户ID',
    status VARCHAR(20) DEFAULT 'offline' COMMENT '设备状态：online/offline',
    power_state VARCHAR(10) DEFAULT 'off' COMMENT '电源状态：on/off',
    work_mode VARCHAR(20) COMMENT '工作模式：auto/spot/edge',
    battery_level INT DEFAULT 100 COMMENT '电量百分比',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_device_id (device_id),
    INDEX idx_user_id (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='设备表';

-- 插入测试数据
INSERT INTO oauth_clients (client_id, client_secret, redirect_uri, user_id) 
VALUES ('test_client_id', 'test_client_secret', 'https://aligenie.com/callback', 1)
ON DUPLICATE KEY UPDATE client_secret='test_client_secret';

INSERT INTO users (username, password, email) 
VALUES ('testuser', 'password123', 'test@example.com')
ON DUPLICATE KEY UPDATE password='password123';

INSERT INTO devices (device_id, device_name, device_type, user_id, status, power_state, work_mode, battery_level)
VALUES ('robot_001', '客厅扫地机器人', 'robot_cleaner', 1, 'online', 'off', 'auto', 85)
ON DUPLICATE KEY UPDATE device_name='客厅扫地机器人';
