package com.voice.platform.service;

import com.voice.platform.config.JwtConfig;
import com.voice.platform.model.OAuth2Authorization;
import com.voice.platform.model.OAuthClient;
import com.voice.platform.repository.OAuth2AuthorizationRepository;
import com.voice.platform.repository.OAuthClientRepository;
import com.voice.platform.util.TokenGenerator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.Optional;

/**
 * OAuth2 授权服务
 */
@Slf4j
@Service
public class OAuth2AuthorizationService {
    
    @Autowired
    private OAuth2AuthorizationRepository authorizationRepository;
    
    @Autowired
    private OAuthClientRepository clientRepository;
    
    @Autowired
    private JwtConfig jwtConfig;
    
    /**
     * 生成授权码
     */
    @Transactional
    public String generateAuthorizationCode(String clientId, Long userId, String redirectUri, 
                                           String scope, String state, 
                                           String codeChallenge, String codeChallengeMethod) {
        // 生成授权码
        String code = TokenGenerator.generateToken();
        
        // 计算过期时间
        LocalDateTime expiresAt = LocalDateTime.now()
                .plusSeconds(jwtConfig.getAuthorizationCodeValidity());
        
        // 创建授权记录
        OAuth2Authorization authorization = new OAuth2Authorization();
        authorization.setAuthorizationCode(code);
        authorization.setClientId(clientId);
        authorization.setUserId(userId);
        authorization.setRedirectUri(redirectUri);
        authorization.setScope(scope);
        authorization.setState(state);
        authorization.setCodeChallenge(codeChallenge);
        authorization.setCodeChallengeMethod(codeChallengeMethod);
        authorization.setExpiresAt(expiresAt);
        authorization.setUsed(false);
        
        authorizationRepository.save(authorization);
        
        log.info("生成授权码: code={}, clientId={}, userId={}", code, clientId, userId);
        
        return code;
    }
    
    /**
     * 验证授权码
     */
    @Transactional
    public OAuth2Authorization validateAuthorizationCode(String code, String clientId, 
                                                         String redirectUri, String codeVerifier) {
        Optional<OAuth2Authorization> authOpt = authorizationRepository.findByAuthorizationCode(code);
        
        if (!authOpt.isPresent()) {
            log.warn("授权码不存在: code={}", code);
            return null;
        }
        
        OAuth2Authorization authorization = authOpt.get();
        
        // 检查是否已使用
        if (authorization.isUsed()) {
            log.warn("授权码已使用: code={}", code);
            return null;
        }
        
        // 检查是否过期
        if (authorization.isExpired()) {
            log.warn("授权码已过期: code={}", code);
            return null;
        }
        
        // 检查客户端 ID
        if (!authorization.getClientId().equals(clientId)) {
            log.warn("客户端 ID 不匹配: expected={}, actual={}", authorization.getClientId(), clientId);
            return null;
        }
        
        // 检查重定向 URI
        if (!authorization.getRedirectUri().equals(redirectUri)) {
            log.warn("重定向 URI 不匹配: expected={}, actual={}", authorization.getRedirectUri(), redirectUri);
            return null;
        }
        
        // 验证 PKCE（如果有）
        if (authorization.getCodeChallenge() != null) {
            if (codeVerifier == null) {
                log.warn("缺少 code_verifier");
                return null;
            }
            
            if (!verifyPKCE(codeVerifier, authorization.getCodeChallenge(), 
                           authorization.getCodeChallengeMethod())) {
                log.warn("PKCE 验证失败");
                return null;
            }
        }
        
        // 标记为已使用
        authorization.markAsUsed();
        authorizationRepository.save(authorization);
        
        log.info("授权码验证成功: code={}, clientId={}", code, clientId);
        
        return authorization;
    }
    
    /**
     * 验证 PKCE
     */
    private boolean verifyPKCE(String codeVerifier, String codeChallenge, String method) {
        if ("plain".equals(method)) {
            return codeVerifier.equals(codeChallenge);
        } else if ("S256".equals(method)) {
            try {
                MessageDigest digest = MessageDigest.getInstance("SHA-256");
                byte[] hash = digest.digest(codeVerifier.getBytes(StandardCharsets.US_ASCII));
                String computed = Base64.getUrlEncoder().withoutPadding().encodeToString(hash);
                return computed.equals(codeChallenge);
            } catch (NoSuchAlgorithmException e) {
                log.error("SHA-256 算法不可用", e);
                return false;
            }
        }
        return false;
    }
    
    /**
     * 清理过期的授权码
     */
    @Transactional
    public int cleanupExpiredAuthorizations() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime threshold = now.minusDays(1); // 已使用的授权码保留 1 天
        
        int count = authorizationRepository.deleteExpiredAuthorizations(now, threshold);
        
        log.info("清理过期授权码: count={}", count);
        
        return count;
    }
    
    /**
     * 验证客户端
     */
    public boolean validateClient(String clientId, String clientSecret) {
        Optional<OAuthClient> clientOpt = clientRepository.findByClientId(clientId);
        
        if (!clientOpt.isPresent()) {
            return false;
        }
        
        OAuthClient client = clientOpt.get();
        return client.getClientSecret().equals(clientSecret);
    }
    
    /**
     * 获取客户端
     */
    public Optional<OAuthClient> getClient(String clientId) {
        return clientRepository.findByClientId(clientId);
    }
}
