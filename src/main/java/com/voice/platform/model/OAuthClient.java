package com.voice.platform.model;

import lombok.Data;
import javax.persistence.*;
import java.time.LocalDateTime;

/**
 * OAuth 客户端实体
 * 用于存储和管理 OAuth 2.0 客户端应用信息
 */
@Data
@Entity
@Table(name = "oauth_clients")
public class OAuthClient {
    
    /**
     * 主键ID
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    /**
     * 客户端ID
     * 客户端应用的唯一标识符
     */
    @Column(name = "client_id", nullable = false, unique = true)
    private String clientId;
    
    /**
     * 客户端密钥
     * 用于验证客户端身份的密钥
     */
    @Column(name = "client_secret", nullable = false)
    private String clientSecret;
    
    /**
     * 重定向URI
     * 授权完成后的回调地址
     */
    @Column(name = "redirect_uri", nullable = false)
    private String redirectUri;
    
    /**
     * 用户ID
     * 客户端所属的用户，可选字段
     */
    @Column(name = "user_id")
    private Long userId;
    
    /**
     * 权限范围列表
     * 以逗号分隔，默认：device:control,device:read
     */
    @Column(name = "scopes", length = 500)
    private String scopes = "device:control,device:read";
    
    /**
     * 支持的授权类型
     * 以逗号分隔，默认：authorization_code,refresh_token
     */
    @Column(name = "grant_types", length = 200)
    private String grantTypes = "authorization_code,refresh_token";
    
    /**
     * 令牌类型
     * jwt（推荐）或 opaque（不透明令牌）
     */
    @Column(name = "token_type", length = 20)
    private String tokenType = "jwt";
    
    /**
     * 访问令牌有效期（秒）
     * 默认：3600（1小时）
     */
    @Column(name = "access_token_validity")
    private Integer accessTokenValidity = 3600;
    
    /**
     * 刷新令牌有效期（秒）
     * 默认：2592000（30天）
     */
    @Column(name = "refresh_token_validity")
    private Integer refreshTokenValidity = 2592000;
    
    /**
     * 是否自动批准授权
     * true：跳过用户授权确认页面
     */
    @Column(name = "auto_approve")
    private Boolean autoApprove = false;

    /**
     * 描述
     * 描述
     */
    @Column(name = "description", length = 50)
    private String description = "jwt";
    
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
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
    
    /**
     * 检查是否支持指定的授权类型
     */
    public boolean supportsGrantType(String grantType) {
        return grantTypes != null && grantTypes.contains(grantType);
    }
    
    /**
     * 检查是否支持指定的权限范围
     */
    public boolean supportsScope(String scope) {
        if (scopes == null || scope == null) {
            return false;
        }
        String[] requestedScopes = scope.split(",");
        String[] supportedScopes = scopes.split(",");
        
        for (String requestedScope : requestedScopes) {
            boolean found = false;
            for (String supportedScope : supportedScopes) {
                if (requestedScope.trim().equals(supportedScope.trim())) {
                    found = true;
                    break;
                }
            }
            if (!found) {
                return false;
            }
        }
        return true;
    }
}
