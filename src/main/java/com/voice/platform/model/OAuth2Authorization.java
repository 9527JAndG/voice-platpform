package com.voice.platform.model;

import lombok.Data;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * OAuth2 授权记录实体
 * 用于存储 OAuth 2.0 授权流程中的授权信息
 * 支持 PKCE（Proof Key for Code Exchange）扩展
 */
@Data
@Entity
@Table(name = "oauth_authorizations")
public class OAuth2Authorization {
    
    /**
     * 主键ID
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    /**
     * 授权码
     * 用于交换访问令牌的临时凭证
     */
    @Column(name = "authorization_code", unique = true, nullable = false, length = 100)
    private String authorizationCode;
    
    /**
     * 客户端ID
     * 标识请求授权的客户端应用
     */
    @Column(name = "client_id", nullable = false, length = 100)
    private String clientId;
    
    /**
     * 用户ID
     * 授权所属的用户
     */
    @Column(name = "user_id", nullable = false)
    private Long userId;
    
    /**
     * 重定向URI
     * 授权完成后的回调地址
     */
    @Column(name = "redirect_uri", nullable = false, length = 500)
    private String redirectUri;
    
    /**
     * 权限范围
     * 以逗号分隔的权限列表
     */
    @Column(name = "scope", length = 500)
    private String scope;
    
    /**
     * 状态参数
     * 用于防止 CSRF 攻击，客户端传入的随机字符串
     */
    @Column(name = "state", length = 100)
    private String state;
    
    /**
     * PKCE 代码挑战
     * 用于增强授权码流程的安全性
     */
    @Column(name = "code_challenge", length = 100)
    private String codeChallenge;
    
    /**
     * PKCE 代码挑战方法
     * 例如：S256（SHA-256）、plain（明文）
     */
    @Column(name = "code_challenge_method", length = 20)
    private String codeChallengeMethod;
    
    /**
     * 创建时间
     */
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
    
    /**
     * 过期时间
     */
    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;
    
    /**
     * 是否已使用
     * 授权码只能使用一次
     */
    @Column(name = "used", nullable = false)
    private Boolean used = false;
    
    /**
     * 使用时间
     * 授权码被使用的时间点
     */
    @Column(name = "used_at")
    private LocalDateTime usedAt;
    
    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }
    
    /**
     * 检查授权码是否过期
     */
    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expiresAt);
    }
    
    /**
     * 检查授权码是否已使用
     */
    public boolean isUsed() {
        return used != null && used;
    }
    
    /**
     * 标记为已使用
     */
    public void markAsUsed() {
        this.used = true;
        this.usedAt = LocalDateTime.now();
    }
}
