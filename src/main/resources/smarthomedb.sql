/*
 Navicat Premium Dump SQL

 Source Server         : lohas
 Source Server Type    : MySQL
 Source Server Version : 80044 (8.0.44)
 Source Host           : 127.0.0.1:3306
 Source Schema         : smarthomedb

 Target Server Type    : MySQL
 Target Server Version : 80044 (8.0.44)
 File Encoding         : 65001

 Date: 25/02/2026 13:52:31
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for devices
-- ----------------------------
DROP TABLE IF EXISTS `devices`;
CREATE TABLE `devices`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `battery_level` int NULL DEFAULT NULL,
  `created_at` datetime(6) NULL DEFAULT NULL,
  `device_id` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  `device_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  `device_type` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  `power_state` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL,
  `status` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL,
  `updated_at` datetime(6) NULL DEFAULT NULL,
  `user_id` bigint NOT NULL,
  `work_mode` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `UK_kl1viut5wsyxgnd1t3wqf9hbu`(`device_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 23 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of devices
-- ----------------------------
INSERT INTO `devices` VALUES (8, 85, NULL, 'robot_001', '客厅扫地机器人', 'robot_cleaner', 'on', 'online', NULL, 1, 'auto');
INSERT INTO `devices` VALUES (9, 100, NULL, 'robot_002', '卧室扫地机器人', 'robot_cleaner', 'off', 'online', NULL, 1, 'auto');
INSERT INTO `devices` VALUES (10, 15, NULL, 'robot_003', '书房扫地机器人', 'robot_cleaner', 'on', 'online', NULL, 1, 'spot');
INSERT INTO `devices` VALUES (11, 0, NULL, 'robot_004', '厨房扫地机器人', 'robot_cleaner', 'off', 'offline', NULL, 1, 'auto');
INSERT INTO `devices` VALUES (12, 60, NULL, 'robot_005', '客厅扫地机器人', 'robot_cleaner', 'on', 'online', NULL, 2, 'edge');
INSERT INTO `devices` VALUES (13, 95, NULL, 'robot_006', '卧室扫地机器人', 'robot_cleaner', 'off', 'online', NULL, 2, 'auto');
INSERT INTO `devices` VALUES (14, 75, NULL, 'robot_007', '全屋扫地机器人', 'robot_cleaner', 'on', 'online', NULL, 3, 'auto');
INSERT INTO `devices` VALUES (15, 50, NULL, 'robot_008', '阳台扫地机器人', 'robot_cleaner', 'on', 'online', NULL, 1, 'edge');
INSERT INTO `devices` VALUES (16, 88, NULL, 'robot_009', '餐厅扫地机器人', 'robot_cleaner', 'off', 'online', NULL, 1, 'auto');
INSERT INTO `devices` VALUES (17, 70, NULL, 'robot_010', '玄关扫地机器人', 'robot_cleaner', 'on', 'online', NULL, 1, 'spot');
INSERT INTO `devices` VALUES (18, 100, NULL, 'robot_011', '主卧扫地机器人', 'robot_cleaner', 'off', 'online', NULL, 2, 'auto');
INSERT INTO `devices` VALUES (19, 45, NULL, 'robot_012', '次卧扫地机器人', 'robot_cleaner', 'on', 'online', NULL, 2, 'auto');
INSERT INTO `devices` VALUES (20, 10, NULL, 'robot_013', '儿童房扫地机器人', 'robot_cleaner', 'on', 'online', NULL, 3, 'spot');
INSERT INTO `devices` VALUES (21, 80, NULL, 'robot_014', '老人房扫地机器人', 'robot_cleaner', 'on', 'online', NULL, 3, 'edge');
INSERT INTO `devices` VALUES (22, 5, NULL, 'robot_015', '地下室扫地机器人', 'robot_cleaner', 'off', 'offline', NULL, 1, 'auto');

-- ----------------------------
-- Table structure for oauth_access_tokens
-- ----------------------------
DROP TABLE IF EXISTS `oauth_access_tokens`;
CREATE TABLE `oauth_access_tokens`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `access_token` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  `client_id` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  `created_at` datetime(6) NULL DEFAULT NULL,
  `expires_at` datetime(6) NOT NULL,
  `scope` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL,
  `user_id` bigint NULL DEFAULT NULL,
  `jti` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT 'JWT Token ID',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `UK_s5lwm83s1hucehj885b6s3lbg`(`access_token` ASC) USING BTREE,
  UNIQUE INDEX `jti`(`jti` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of oauth_access_tokens
-- ----------------------------

-- ----------------------------
-- Table structure for oauth_authorization_codes
-- ----------------------------
DROP TABLE IF EXISTS `oauth_authorization_codes`;
CREATE TABLE `oauth_authorization_codes`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `authorization_code` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  `client_id` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  `created_at` datetime(6) NULL DEFAULT NULL,
  `expires_at` datetime(6) NOT NULL,
  `redirect_uri` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `UK_fc0cw2xkiehsjiv9x9efw74ql`(`authorization_code` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of oauth_authorization_codes
-- ----------------------------

-- ----------------------------
-- Table structure for oauth_authorizations
-- ----------------------------
DROP TABLE IF EXISTS `oauth_authorizations`;
CREATE TABLE `oauth_authorizations`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `authorization_code` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  `client_id` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  `code_challenge` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL,
  `code_challenge_method` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL,
  `created_at` datetime(6) NOT NULL,
  `expires_at` datetime(6) NOT NULL,
  `redirect_uri` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  `scope` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL,
  `state` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL,
  `used` bit(1) NOT NULL,
  `used_at` datetime(6) NULL DEFAULT NULL,
  `user_id` bigint NOT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `UK_mi8w9i4jo2lwd0u047nwxh6df`(`authorization_code` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of oauth_authorizations
-- ----------------------------

-- ----------------------------
-- Table structure for oauth_clients
-- ----------------------------
DROP TABLE IF EXISTS `oauth_clients`;
CREATE TABLE `oauth_clients`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `client_id` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  `client_secret` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  `redirect_uri` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  `user_id` bigint NULL DEFAULT NULL,
  `access_token_validity` int NULL DEFAULT NULL,
  `auto_approve` bit(1) NULL DEFAULT NULL,
  `grant_types` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL,
  `refresh_token_validity` int NULL DEFAULT NULL,
  `scopes` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL,
  `token_type` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL,
  `description` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '描述',
  `updated_at` datetime(6) NULL DEFAULT NULL,
  `created_at` datetime(6) NULL DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `UK_aaedvpdimcfvgse9vihrb7n7l`(`client_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 11 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of oauth_clients
-- ----------------------------
INSERT INTO `oauth_clients` VALUES (5, 'YOUR_ALIGENIE_CLIENT_ID', 'YOUR_ALIGENIE_CLIENT_SECRET', 'https://aligenie.com/callback', 1, 3600, b'0', 'authorization_code,refresh_token', 2592000, 'device:control,device:read', 'jwt', 'aligenie', NULL, NULL);
INSERT INTO `oauth_clients` VALUES (6, 'YOUR_DUEROS_CLIENT_ID', 'YOUR_DUEROS_CLIENT_SECRET', 'https://dueros.baidu.com/callback', 1, 3600, b'0', 'authorization_code,refresh_token', 2592000, 'device:control,device:read', 'jwt', 'dueros', NULL, NULL);
INSERT INTO `oauth_clients` VALUES (7, 'YOUR_MIAI_CLIENT_ID', 'YOUR_MIAI_CLIENT_SECRET', 'https://xiaoai.mi.com/callback', 1, 3600, b'0', 'authorization_code,refresh_token', 2592000, 'device:control,device:read', 'jwt', 'xiaoai', NULL, NULL);
INSERT INTO `oauth_clients` VALUES (8, 'YOUR_ALEXA_CLIENT_ID', 'YOUR_ALEXA_CLIENT_SECRET', 'https://pitangui.amazon.com/api/skill/link/YOUR_VENDOR_ID', 1, 3600, b'0', 'authorization_code,refresh_token', 2592000, 'device:control,device:read', 'jwt', 'Alexa', NULL, NULL);
INSERT INTO `oauth_clients` VALUES (9, 'YOUR_GOOGLE_CLIENT_ID', 'YOUR_GOOGLE_CLIENT_SECRET', 'https://oauth-redirect.googleusercontent.com/r/YOUR_PROJECT_ID', 1, 3600, b'0', 'authorization_code,refresh_token', 2592000, 'device:control,device:read', 'jwt', 'Google Assistant', NULL, NULL);
INSERT INTO `oauth_clients` VALUES (10, 'test_client_id', 'test_client_secret', 'http://localhost:8080/callback', 1, 3600, b'1', 'authorization_code,refresh_token,client_credentials', 2592000, 'device:control,device:read', 'jwt', NULL, NULL, NULL);

-- ----------------------------
-- Table structure for oauth_refresh_tokens
-- ----------------------------
DROP TABLE IF EXISTS `oauth_refresh_tokens`;
CREATE TABLE `oauth_refresh_tokens`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `client_id` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  `created_at` datetime(6) NULL DEFAULT NULL,
  `expires_at` datetime(6) NOT NULL,
  `refresh_token` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  `scope` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL,
  `user_id` bigint NULL DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `UK_j7s0aq4hgkbx6kxf7k0262dcf`(`refresh_token` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of oauth_refresh_tokens
-- ----------------------------

-- ----------------------------
-- Table structure for user_roles
-- ----------------------------
DROP TABLE IF EXISTS `user_roles`;
CREATE TABLE `user_roles`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL,
  `role` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_user_role`(`user_id` ASC, `role` ASC) USING BTREE,
  INDEX `idx_user`(`user_id` ASC) USING BTREE,
  INDEX `idx_role`(`role` ASC) USING BTREE,
  CONSTRAINT `user_roles_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '用户角色表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of user_roles
-- ----------------------------

-- ----------------------------
-- Table structure for users
-- ----------------------------
DROP TABLE IF EXISTS `users`;
CREATE TABLE `users`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `username` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '用户名',
  `password` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '密码',
  `email` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '邮箱',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `account_non_expired` bit(1) NULL DEFAULT NULL,
  `account_non_locked` bit(1) NULL DEFAULT NULL,
  `credentials_non_expired` bit(1) NULL DEFAULT NULL,
  `enabled` bit(1) NULL DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `username`(`username` ASC) USING BTREE,
  INDEX `idx_username`(`username` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 10 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '用户表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of users
-- ----------------------------
INSERT INTO `users` VALUES (7, 'user1', '$2a$10$xy0BO5FcQDgfsDElXtAcMulqFNALZRAjJgq9kjIzL6dvH0P3aqf56', 'user1@example.com', '2026-02-25 09:20:09', '2026-02-25 09:20:09', b'1', b'1', b'1', b'1');
INSERT INTO `users` VALUES (8, 'user2', '$2a$10$A/5oXwzNANH3bW80ShAzHOSD.sCaBsfucKRy.mLtlbNs7cYo67vW2', 'user2@example.com', '2026-02-25 09:20:09', '2026-02-25 09:20:09', b'1', b'1', b'1', b'1');
INSERT INTO `users` VALUES (9, 'testuser', '$2a$10$HAqcLYGRkM1Rv.REg8qkReKXkEI.wjdDoAGEsTFEPuMHat8SvgX56', 'testuser@example.com', '2026-02-25 09:20:09', '2026-02-25 09:20:09', b'1', b'1', b'1', b'1');

-- ----------------------------
-- Procedure structure for cleanup_expired_authorizations
-- ----------------------------
DROP PROCEDURE IF EXISTS `cleanup_expired_authorizations`;
delimiter ;;
CREATE PROCEDURE `cleanup_expired_authorizations`()
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
END
;;
delimiter ;

-- ----------------------------
-- Event structure for cleanup_expired_data
-- ----------------------------
DROP EVENT IF EXISTS `cleanup_expired_data`;
delimiter ;;
CREATE EVENT `cleanup_expired_data`
ON SCHEDULE
EVERY '1' HOUR STARTS '2026-02-24 19:00:30'
DO CALL cleanup_expired_authorizations()
;;
delimiter ;

SET FOREIGN_KEY_CHECKS = 1;
