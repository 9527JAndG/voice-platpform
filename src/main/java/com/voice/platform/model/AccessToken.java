package com.voice.platform.model;

import lombok.Data;
import javax.persistence.*;
import java.time.LocalDateTime;

/**
 * OAuth 访问令牌实体
 * 用于存储和管理 OAuth 2.0 访问令牌
 */
@Data
@Entity
@Table(name = "oauth_access_tokens")
public class AccessToken {
    
    /**
     * 主键ID
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    /**
     * 访问令牌字符串
     * 用于访问受保护的资源，有效期较短（通常1小时）
     */
    @Column(name = "access_token", nullable = false, unique = true)
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
     * 以逗号分隔的权限列表，例如："device:control,device:read"
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
    public String getAccessToken() {
        return token;
    }
    
    public void setAccessToken(String token) {
        this.token = token;
    }
}
