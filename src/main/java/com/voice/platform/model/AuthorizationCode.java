package com.voice.platform.model;

import lombok.Data;
import javax.persistence.*;
import java.time.LocalDateTime;

/**
 * OAuth 授权码实体
 * 用于存储和管理 OAuth 2.0 授权码
 * 授权码是临时的，用于交换访问令牌
 */
@Data
@Entity
@Table(name = "oauth_authorization_codes")
public class AuthorizationCode {
    
    /**
     * 主键ID
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    /**
     * 授权码字符串
     * 用于交换访问令牌，有效期很短（通常10分钟）
     */
    @Column(name = "authorization_code", nullable = false, unique = true)
    private String authorizationCode;
    
    /**
     * 客户端ID
     * 标识哪个客户端应用请求了此授权码
     */
    @Column(name = "client_id", nullable = false)
    private String clientId;
    
    /**
     * 重定向URI
     * 授权完成后重定向的地址，必须与注册时的地址匹配
     */
    @Column(name = "redirect_uri", nullable = false)
    private String redirectUri;
    
    /**
     * 过期时间
     * 授权码失效的时间点
     */
    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;
    
    /**
     * 创建时间
     */
    @Column(name = "created_at")
    private LocalDateTime createdAt;
}
