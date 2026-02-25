package com.voice.platform.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * OAuth2 Token 响应对象
 * 符合 RFC 6749 标准的 Token 响应格式
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TokenResponse {
    
    /**
     * 访问令牌
     * 用于访问受保护的资源，有效期较短（通常1小时）
     */
    @JsonProperty("access_token")
    private String accessToken;
    
    /**
     * 令牌类型
     * 固定为 "Bearer"，表示持有者令牌
     */
    @JsonProperty("token_type")
    private String tokenType = "Bearer";
    
    /**
     * 访问令牌过期时间（秒）
     * 例如：3600 表示1小时后过期
     */
    @JsonProperty("expires_in")
    private Long expiresIn;
    
    /**
     * 刷新令牌
     * 用于获取新的访问令牌，有效期较长（通常30天）
     * 可选字段，某些授权类型（如 client_credentials）不返回
     */
    @JsonProperty("refresh_token")
    private String refreshToken;
    
    /**
     * 权限范围
     * 以逗号分隔的权限列表，例如："device:control,device:read"
     */
    @JsonProperty("scope")
    private String scope;
    
    public TokenResponse(String accessToken, Long expiresIn, String refreshToken) {
        this.accessToken = accessToken;
        this.expiresIn = expiresIn;
        this.refreshToken = refreshToken;
        this.tokenType = "Bearer";
    }
}
