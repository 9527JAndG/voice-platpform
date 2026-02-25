package com.voice.platform.controller;

import com.voice.platform.dto.TokenResponse;
import com.voice.platform.service.OAuth2TokenService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * OAuth2 Token 控制器
 * 处理 Token 请求、刷新和撤销
 */
@Slf4j
@RestController
@RequestMapping("/oauth2")
public class OAuth2TokenController {
    
    @Autowired
    private OAuth2TokenService tokenService;
    
    /**
     * Token 端点
     * POST /oauth2/token
     */
    @PostMapping("/token")
    public ResponseEntity<?> token(
            @RequestParam("grant_type") String grantType,
            @RequestParam("client_id") String clientId,
            @RequestParam("client_secret") String clientSecret,
            @RequestParam(value = "code", required = false) String code,
            @RequestParam(value = "redirect_uri", required = false) String redirectUri,
            @RequestParam(value = "refresh_token", required = false) String refreshToken,
            @RequestParam(value = "scope", required = false) String scope,
            @RequestParam(value = "code_verifier", required = false) String codeVerifier) {
        
        log.info("Token 请求: grantType={}, clientId={}", grantType, clientId);
        
        TokenResponse tokenResponse = null;
        
        try {
            switch (grantType) {
                case "authorization_code":
                    // 授权码模式
                    if (code == null || redirectUri == null) {
                        return createErrorResponse("invalid_request", 
                                "缺少必需参数: code 或 redirect_uri", HttpStatus.BAD_REQUEST);
                    }
                    tokenResponse = tokenService.exchangeAuthorizationCode(
                            code, clientId, clientSecret, redirectUri, codeVerifier);
                    break;
                    
                case "refresh_token":
                    // 刷新令牌模式
                    if (refreshToken == null) {
                        return createErrorResponse("invalid_request", 
                                "缺少必需参数: refresh_token", HttpStatus.BAD_REQUEST);
                    }
                    tokenResponse = tokenService.refreshAccessToken(
                            refreshToken, clientId, clientSecret);
                    break;
                    
                case "client_credentials":
                    // 客户端凭证模式
                    tokenResponse = tokenService.clientCredentials(
                            clientId, clientSecret, scope);
                    break;
                    
                default:
                    return createErrorResponse("unsupported_grant_type", 
                            "不支持的授权类型: " + grantType, HttpStatus.BAD_REQUEST);
            }
            
            if (tokenResponse == null) {
                return createErrorResponse("invalid_grant", 
                        "授权无效或已过期", HttpStatus.BAD_REQUEST);
            }
            
            log.info("Token 生成成功: clientId={}, grantType={}", clientId, grantType);
            
            return ResponseEntity.ok(tokenResponse);
            
        } catch (Exception e) {
            log.error("Token 生成失败", e);
            return createErrorResponse("server_error", 
                    "服务器内部错误", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    
    /**
     * Token 内省端点
     * POST /oauth2/introspect
     */
    @PostMapping("/introspect")
    public ResponseEntity<?> introspect(
            @RequestParam("token") String token,
            @RequestParam(value = "token_type_hint", required = false) String tokenTypeHint) {
        
        log.info("Token 内省请求: tokenTypeHint={}", tokenTypeHint);
        
        Map<String, Object> response = new HashMap<>();
        
        boolean active = tokenService.validateAccessToken(token);
        response.put("active", active);
        
        if (active) {
            // 可以添加更多 Token 信息
            response.put("token_type", "Bearer");
            // 如果是 JWT，可以解析更多信息
        }
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * Token 撤销端点
     * POST /oauth2/revoke
     */
    @PostMapping("/revoke")
    public ResponseEntity<?> revoke(
            @RequestParam("token") String token,
            @RequestParam(value = "token_type_hint", required = false) String tokenTypeHint,
            @RequestParam("client_id") String clientId,
            @RequestParam("client_secret") String clientSecret) {
        
        log.info("Token 撤销请求: clientId={}, tokenTypeHint={}", clientId, tokenTypeHint);
        
        // 验证客户端（简化处理）
        // 实际应该验证客户端是否有权撤销此 Token
        
        boolean revoked = false;
        
        if ("refresh_token".equals(tokenTypeHint)) {
            revoked = tokenService.revokeRefreshToken(token);
        } else {
            revoked = tokenService.revokeAccessToken(token);
        }
        
        if (revoked) {
            log.info("Token 撤销成功: clientId={}", clientId);
        } else {
            log.warn("Token 撤销失败: token 不存在或已撤销");
        }
        
        // RFC 7009: 撤销端点应该返回 200 OK，即使 Token 不存在
        return ResponseEntity.ok().build();
    }
    
    /**
     * 创建错误响应
     */
    private ResponseEntity<?> createErrorResponse(String error, String description, HttpStatus status) {
        Map<String, String> errorResponse = new HashMap<>();
        errorResponse.put("error", error);
        errorResponse.put("error_description", description);
        return ResponseEntity.status(status).body(errorResponse);
    }
}
