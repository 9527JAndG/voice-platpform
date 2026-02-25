package com.voice.platform.util;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

/**
 * 密码哈希生成工具
 * 用于生成 BCrypt 密码哈希
 */
public class PasswordHashGenerator {
    
    public static void main(String[] args) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        
        String password = "password";
        String hash = encoder.encode(password);
        
        System.out.println("明文密码: " + password);
        System.out.println("BCrypt 哈希: " + hash);
        System.out.println();
        
        // 验证
        boolean matches = encoder.matches(password, hash);
        System.out.println("验证结果: " + matches);
        
        // 生成多个用户的哈希
        System.out.println("\n=== 测试用户密码哈希 ===");
        for (String user : new String[]{"user1", "user2", "testuser"}) {
            String userHash = encoder.encode(password);
            System.out.println(user + ": " + userHash);
        }
    }
}
