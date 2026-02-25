package com.voice.platform.util;

import java.security.SecureRandom;
import java.util.Base64;

/**
 * Token 生成工具类
 */
public class TokenGenerator {
    
    private static final SecureRandom RANDOM = new SecureRandom();
    private static final Base64.Encoder ENCODER = Base64.getUrlEncoder().withoutPadding();
    
    /**
     * 生成授权码
     */
    public static String generateCode() {
        byte[] buffer = new byte[32];
        RANDOM.nextBytes(buffer);
        return ENCODER.encodeToString(buffer);
    }
    
    /**
     * 生成通用 Token（用于授权码、访问令牌等）
     */
    public static String generateToken() {
        byte[] buffer = new byte[40];
        RANDOM.nextBytes(buffer);
        return ENCODER.encodeToString(buffer);
    }
    
    /**
     * 生成访问令牌
     */
    public static String generateAccessToken() {
        return generateToken();
    }
    
    /**
     * 生成刷新令牌
     */
    public static String generateRefreshToken() {
        return generateToken();
    }
}
