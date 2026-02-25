package com.voice.platform.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * JWT 配置
 */
@Configuration
@ConfigurationProperties(prefix = "jwt")
public class JwtConfig {
    
    /**
     * JWT 签名密钥（生产环境应使用环境变量）
     */
    private String secret = "your-256-bit-secret-key-change-this-in-production-environment-please";
    
    /**
     * JWT 发行者
     */
    private String issuer = "https://your-domain.com";
    
    /**
     * 访问令牌有效期（秒）
     */
    private long accessTokenValidity = 3600; // 1 小时
    
    /**
     * 刷新令牌有效期（秒）
     */
    private long refreshTokenValidity = 2592000; // 30 天
    
    /**
     * 授权码有效期（秒）
     */
    private long authorizationCodeValidity = 600; // 10 分钟
    
    // Getters and Setters
    
    public String getSecret() {
        return secret;
    }
    
    public void setSecret(String secret) {
        this.secret = secret;
    }
    
    public String getIssuer() {
        return issuer;
    }
    
    public void setIssuer(String issuer) {
        this.issuer = issuer;
    }
    
    public long getAccessTokenValidity() {
        return accessTokenValidity;
    }
    
    public void setAccessTokenValidity(long accessTokenValidity) {
        this.accessTokenValidity = accessTokenValidity;
    }
    
    public long getRefreshTokenValidity() {
        return refreshTokenValidity;
    }
    
    public void setRefreshTokenValidity(long refreshTokenValidity) {
        this.refreshTokenValidity = refreshTokenValidity;
    }
    
    public long getAuthorizationCodeValidity() {
        return authorizationCodeValidity;
    }
    
    public void setAuthorizationCodeValidity(long authorizationCodeValidity) {
        this.authorizationCodeValidity = authorizationCodeValidity;
    }
}
