package com.voice.platform.service;

import com.voice.platform.model.AlexaToken;
import com.voice.platform.repository.AlexaTokenRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;

/**
 * Alexa Token Service
 * 管理 Alexa Event Gateway 所需的访问令牌
 * 支持 AcceptGrant 和 Token 刷新
 */
@Slf4j
@Service
public class AlexaTokenService {
    
    @Autowired
    private AlexaTokenRepository tokenRepository;
    
    @Autowired
    private RestTemplate restTemplate;
    
    @Value("${alexa.client-id:}")
    private String alexaClientId;
    
    @Value("${alexa.client-secret:}")
    private String alexaClientSecret;
    
    private static final String TOKEN_URL = "https://api.amazon.com/auth/o2/token";
    
    /**
     * 用授权码换取 Alexa Access Token
     * 在 AcceptGrant 时调用
     */
    public AlexaToken exchangeToken(String code, Long userId) {
        log.info("开始交换 Alexa Token: userId={}", userId);
        
        try {
            // 构建请求参数
            MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
            params.add("grant_type", "authorization_code");
            params.add("code", code);
            params.add("client_id", alexaClientId);
            params.add("client_secret", alexaClientSecret);
            
            // 发送请求
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
            
            HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(params, headers);
            
            ResponseEntity<Map> response = restTemplate.postForEntity(TOKEN_URL, request, Map.class);
            
            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                Map<String, Object> body = response.getBody();
                
                String accessToken = (String) body.get("access_token");
                String refreshToken = (String) body.get("refresh_token");
                Integer expiresIn = (Integer) body.get("expires_in");
                
                log.info("✓ Alexa Token 交换成功: userId={}, expiresIn={}秒", userId, expiresIn);
                
                // 创建 Token 实体
                AlexaToken token = new AlexaToken();
                token.setUserId(userId);
                token.setAccessToken(accessToken);
                token.setRefreshToken(refreshToken);
                token.setExpiresAt(LocalDateTime.now().plusSeconds(expiresIn != null ? expiresIn : 3600));
                
                return token;
            } else {
                log.error("Alexa Token 交换失败: status={}", response.getStatusCode());
                throw new RuntimeException("Token 交换失败");
            }
            
        } catch (Exception e) {
            log.error("Alexa Token 交换异常: userId={}", userId, e);
            throw new RuntimeException("Token 交换失败: " + e.getMessage(), e);
        }
    }
    
    /**
     * 保存 Token
     */
    public void saveToken(String granteeToken, AlexaToken token) {
        token.setGranteeToken(granteeToken);
        
        // 检查是否已存在
        Optional<AlexaToken> existing = tokenRepository.findByUserId(token.getUserId());
        if (existing.isPresent()) {
            // 更新现有记录
            AlexaToken existingToken = existing.get();
            existingToken.setGranteeToken(granteeToken);
            existingToken.setAccessToken(token.getAccessToken());
            existingToken.setRefreshToken(token.getRefreshToken());
            existingToken.setExpiresAt(token.getExpiresAt());
            tokenRepository.save(existingToken);
            log.info("✓ 更新 Alexa Token: userId={}", token.getUserId());
        } else {
            // 创建新记录
            tokenRepository.save(token);
            log.info("✓ 保存 Alexa Token: userId={}", token.getUserId());
        }
    }
    
    /**
     * 获取用户的 Alexa Access Token
     * 如果过期则自动刷新
     */
    public String getAlexaAccessToken(Long userId) {
        Optional<AlexaToken> tokenOpt = tokenRepository.findByUserId(userId);
        
        if (!tokenOpt.isPresent()) {
            log.warn("用户没有 Alexa Token: userId={}", userId);
            return null;
        }
        
        AlexaToken token = tokenOpt.get();
        
        // 检查是否过期
        if (token.isExpired()) {
            log.info("Alexa Token 已过期，开始刷新: userId={}", userId);
            refreshToken(token);
        }
        
        return token.getAccessToken();
    }
    
    /**
     * 刷新 Token
     */
    private void refreshToken(AlexaToken token) {
        try {
            // 构建请求参数
            MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
            params.add("grant_type", "refresh_token");
            params.add("refresh_token", token.getRefreshToken());
            params.add("client_id", alexaClientId);
            params.add("client_secret", alexaClientSecret);
            
            // 发送请求
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
            
            HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(params, headers);
            
            ResponseEntity<Map> response = restTemplate.postForEntity(TOKEN_URL, request, Map.class);
            
            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                Map<String, Object> body = response.getBody();
                
                String accessToken = (String) body.get("access_token");
                String refreshToken = (String) body.get("refresh_token");
                Integer expiresIn = (Integer) body.get("expires_in");
                
                // 更新 Token
                token.setAccessToken(accessToken);
                if (refreshToken != null) {
                    token.setRefreshToken(refreshToken);
                }
                token.setExpiresAt(LocalDateTime.now().plusSeconds(expiresIn != null ? expiresIn : 3600));
                
                tokenRepository.save(token);
                
                log.info("✓ Alexa Token 刷新成功: userId={}", token.getUserId());
            } else {
                log.error("Alexa Token 刷新失败: status={}", response.getStatusCode());
            }
            
        } catch (Exception e) {
            log.error("Alexa Token 刷新异常: userId={}", token.getUserId(), e);
        }
    }
}
