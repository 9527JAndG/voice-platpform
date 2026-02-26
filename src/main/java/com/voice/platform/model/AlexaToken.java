package com.voice.platform.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * Alexa Token 实体
 * 用于存储 Alexa Event Gateway 所需的访问令牌
 * 支持主动状态报告（Proactive State Reporting）
 */
@Data
@Entity
@Table(name = "alexa_tokens")
public class AlexaToken {
    
    /**
     * 主键ID
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    /**
     * 用户ID
     */
    @Column(name = "user_id", nullable = false)
    private Long userId;
    
    /**
     * Grantee Token
     * 从 AcceptGrant 请求中获取
     */
    @Column(name = "grantee_token", nullable = false, unique = true, length = 500)
    private String granteeToken;
    
    /**
     * Alexa Access Token
     * 用于调用 Alexa Event Gateway API
     */
    @Column(name = "access_token", nullable = false, length = 1000)
    private String accessToken;
    
    /**
     * Refresh Token
     * 用于刷新 Access Token
     */
    @Column(name = "refresh_token", length = 1000)
    private String refreshToken;
    
    /**
     * Token 过期时间
     */
    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;
    
    /**
     * 创建时间
     */
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    /**
     * 更新时间
     */
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    /**
     * 检查 Token 是否过期
     */
    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expiresAt);
    }
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
