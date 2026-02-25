package com.voice.platform.model;

import lombok.Data;
import javax.persistence.*;
import java.time.LocalDateTime;

/**
 * 用户实体
 * 用于存储和管理系统用户信息
 */
@Data
@Entity
@Table(name = "users")
public class User {
    
    /**
     * 主键ID
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    /**
     * 用户名
     * 用于登录的唯一标识符
     */
    @Column(name = "username", unique = true, nullable = false, length = 50)
    private String username;
    
    /**
     * 密码
     * 使用 BCrypt 加密存储
     */
    @Column(name = "password", nullable = false, length = 255)
    private String password;
    
    /**
     * 电子邮箱
     * 可选字段
     */
    @Column(name = "email", length = 100)
    private String email;
    
    /**
     * 是否启用
     * true：账号可用，false：账号被禁用
     */
    @Column(name = "enabled")
    private Boolean enabled = true;
    
    /**
     * 账号是否未过期
     * true：账号未过期，false：账号已过期
     */
    @Column(name = "account_non_expired")
    private Boolean accountNonExpired = true;
    
    /**
     * 账号是否未锁定
     * true：账号未锁定，false：账号已锁定
     */
    @Column(name = "account_non_locked")
    private Boolean accountNonLocked = true;
    
    /**
     * 凭证是否未过期
     * true：密码未过期，false：密码已过期
     */
    @Column(name = "credentials_non_expired")
    private Boolean credentialsNonExpired = true;
    
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
}
