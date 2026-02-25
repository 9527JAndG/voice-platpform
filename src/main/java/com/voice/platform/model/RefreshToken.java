package com.voice.platform.model;

import java.time.LocalDateTime;
import jakarta.persistence.*;
import lombok.Data;

/**
 * OAuth 刷新令牌实体
 * 用于存储和管理 OAuth 2.0 刷新令牌
 * 刷新令牌用于获取新的访问令牌，有效期较长
 */
@Data
@Entity
@Table(name = "oauth_refresh_tokens")
public class RefreshToken {
    
    /**
     * 主键ID
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    /**
     * 刷新令牌字符串
     * 用于获取新的访问令牌，有效期较长（通常30天）
     */
    @Column(name = "refresh_token", nullable = false, unique = true)
    private String token;
    
    /**
     * 客户端ID
     * 标识哪个客户端应用获得了此令牌
     */
    @Column(name = "client_id", nullable = false)
    private String clientId;
    
    /**
     * 用户ID
     * 标识令牌所属的用户，可选字段
     */
    @Column(name = "user_id")
    private Long userId;
    
    /**
     * 权限范围
     * 以逗号分隔的权限列表
     */
    @Column(name = "scope")
    private String scope;
    
    /**
     * 过期时间
     * 令牌失效的时间点
     */
    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;
    
    /**
     * 创建时间
     */
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    // 向后兼容的 getter
    public String getRefreshToken() {
        return token;
    }
    
    public void setRefreshToken(String token) {
        this.token = token;
    }
}
