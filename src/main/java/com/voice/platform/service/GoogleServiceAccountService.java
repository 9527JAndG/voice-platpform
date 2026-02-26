package com.voice.platform.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.Instant;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

/**
 * Google Service Account Service
 * 管理 Google Service Account 认证
 * 用于调用 HomeGraph API (Request Sync, Report State)
 * 
 * @author Voice Platform Team
 * @version 1.0
 */
@Slf4j
@Service
public class GoogleServiceAccountService {
    
    private final RestTemplate restTemplate;
    
    @Value("${google.service-account.client-email:}")
    private String clientEmail;
    
    @Value("${google.service-account.private-key:}")
    private String privateKey;
    
    @Value("${google.project-id:}")
    private String projectId;
    
    private static final String TOKEN_URL = "https://oauth2.googleapis.com/token";
    private static final String SCOPE = "https://www.googleapis.com/auth/homegraph";
    
    // Token 缓存
    private String cachedAccessToken;
    private Instant tokenExpiresAt;
    
    public GoogleServiceAccountService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }
    
    /**
     * 获取 Google Service Account Access Token
     * 使用 JWT 方式认证
     * 
     * @return Access Token
     */
    public String getAccessToken() {
        // 检查缓存的 Token 是否有效
        if (cachedAccessToken != null && tokenExpiresAt != null && 
            Instant.now().isBefore(tokenExpiresAt.minusSeconds(60))) {
            log.debug("使用缓存的 Google Access Token");
            return cachedAccessToken;
        }
        
        log.info("开始获取 Google Service Account Access Token");
        
        try {
            // 创建 JWT
            String jwt = createJwt();
            
            // 构建请求参数
            Map<String, String> params = new HashMap<>();
            params.put("grant_type", "urn:ietf:params:oauth:grant-type:jwt-bearer");
            params.put("assertion", jwt);
            
            // 发送请求
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            
            HttpEntity<Map<String, String>> request = new HttpEntity<>(params, headers);
            
            ResponseEntity<Map> response = restTemplate.postForEntity(TOKEN_URL, request, Map.class);
            
            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                Map<String, Object> body = response.getBody();
                
                cachedAccessToken = (String) body.get("access_token");
                Integer expiresIn = (Integer) body.get("expires_in");
                
                if (expiresIn != null) {
                    tokenExpiresAt = Instant.now().plusSeconds(expiresIn);
                }
                
                log.info("✓ Google Access Token 获取成功: expiresIn={}秒", expiresIn);
                
                return cachedAccessToken;
            } else {
                log.error("Google Access Token 获取失败: status={}", response.getStatusCode());
                throw new RuntimeException("获取 Google Access Token 失败");
            }
            
        } catch (Exception e) {
            log.error("Google Access Token 获取异常", e);
            throw new RuntimeException("获取 Google Access Token 失败: " + e.getMessage(), e);
        }
    }
    
    /**
     * 创建 JWT (JSON Web Token)
     * 用于 Service Account 认证
     * 
     * @return JWT 字符串
     */
    private String createJwt() {
        try {
            long now = Instant.now().getEpochSecond();
            long exp = now + 3600; // 1 小时后过期
            
            // JWT Header
            Map<String, Object> header = new HashMap<>();
            header.put("alg", "RS256");
            header.put("typ", "JWT");
            
            // JWT Payload
            Map<String, Object> payload = new HashMap<>();
            payload.put("iss", clientEmail);
            payload.put("scope", SCOPE);
            payload.put("aud", TOKEN_URL);
            payload.put("exp", exp);
            payload.put("iat", now);
            
            // 编码 Header 和 Payload
            String encodedHeader = base64UrlEncode(toJson(header));
            String encodedPayload = base64UrlEncode(toJson(payload));
            
            // 创建签名输入
            String signatureInput = encodedHeader + "." + encodedPayload;
            
            // 签名（简化版本，生产环境需要使用真实的 RSA 签名）
            // TODO: 实现真实的 RSA 签名
            String signature = base64UrlEncode("signature-placeholder");
            
            return signatureInput + "." + signature;
            
        } catch (Exception e) {
            log.error("创建 JWT 失败", e);
            throw new RuntimeException("创建 JWT 失败: " + e.getMessage(), e);
        }
    }
    
    /**
     * Base64 URL 编码
     */
    private String base64UrlEncode(String input) {
        return Base64.getUrlEncoder().withoutPadding().encodeToString(input.getBytes());
    }
    
    /**
     * 转换为 JSON 字符串
     */
    private String toJson(Map<String, Object> map) {
        // 简化版本，生产环境使用 Jackson 或 Gson
        StringBuilder json = new StringBuilder("{");
        boolean first = true;
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            if (!first) {
                json.append(",");
            }
            json.append("\"").append(entry.getKey()).append("\":");
            Object value = entry.getValue();
            if (value instanceof String) {
                json.append("\"").append(value).append("\"");
            } else {
                json.append(value);
            }
            first = false;
        }
        json.append("}");
        return json.toString();
    }
    
    /**
     * 清除缓存的 Token
     */
    public void clearCache() {
        cachedAccessToken = null;
        tokenExpiresAt = null;
        log.info("Google Access Token 缓存已清除");
    }
}
