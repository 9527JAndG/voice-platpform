package com.voice.platform.service;

import com.voice.platform.dto.TokenResponse;
import com.voice.platform.model.AccessToken;
import com.voice.platform.model.OAuth2Authorization;
import com.voice.platform.model.OAuthClient;
import com.voice.platform.model.RefreshToken;
import com.voice.platform.repository.AccessTokenRepository;
import com.voice.platform.repository.RefreshTokenRepository;
import com.voice.platform.util.JwtUtil;
import com.voice.platform.util.TokenGenerator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

/**
 * OAuth2 Token 服务
 */
@Slf4j
@Service
public class OAuth2TokenService {
    
    @Autowired
    private OAuth2AuthorizationService authorizationService;
    
    @Autowired
    private AccessTokenRepository accessTokenRepository;
    
    @Autowired
    private RefreshTokenRepository refreshTokenRepository;
    
    @Autowired
    private JwtUtil jwtUtil;
    
    /**
     * 授权码换取 Token
     */
    @Transactional
    public TokenResponse exchangeAuthorizationCode(String code, String clientId, String clientSecret,
                                                   String redirectUri, String codeVerifier) {
        // 验证客户端
        if (!authorizationService.validateClient(clientId, clientSecret)) {
            log.warn("客户端验证失败: clientId={}", clientId);
            return null;
        }
        
        // 验证授权码
        OAuth2Authorization authorization = authorizationService.validateAuthorizationCode(
                code, clientId, redirectUri, codeVerifier);
        
        if (authorization == null) {
            log.warn("授权码验证失败: code={}", code);
            return null;
        }
        
        // 获取客户端信息
        Optional<OAuthClient> clientOpt = authorizationService.getClient(clientId);
        if (!clientOpt.isPresent()) {
            return null;
        }
        
        OAuthClient client = clientOpt.get();
        String userId = String.valueOf(authorization.getUserId());
        String scope = authorization.getScope() != null ? authorization.getScope() : client.getScopes();
        
        // 生成 Token
        return generateTokens(userId, clientId, scope, client);
    }
    
    /**
     * 刷新 Token
     */
    @Transactional
    public TokenResponse refreshAccessToken(String refreshTokenValue, String clientId, String clientSecret) {
        // 验证客户端
        if (!authorizationService.validateClient(clientId, clientSecret)) {
            log.warn("客户端验证失败: clientId={}", clientId);
            return null;
        }
        
        // 查找刷新令牌
        Optional<RefreshToken> refreshTokenOpt = refreshTokenRepository.findByToken(refreshTokenValue);
        
        if (!refreshTokenOpt.isPresent()) {
            log.warn("刷新令牌不存在: token={}", refreshTokenValue);
            return null;
        }
        
        RefreshToken refreshToken = refreshTokenOpt.get();
        
        // 检查是否过期
        if (refreshToken.getExpiresAt().isBefore(LocalDateTime.now())) {
            log.warn("刷新令牌已过期: token={}", refreshTokenValue);
            refreshTokenRepository.delete(refreshToken);
            return null;
        }
        
        // 检查客户端 ID
        if (!refreshToken.getClientId().equals(clientId)) {
            log.warn("客户端 ID 不匹配: expected={}, actual={}", refreshToken.getClientId(), clientId);
            return null;
        }
        
        // 获取客户端信息
        Optional<OAuthClient> clientOpt = authorizationService.getClient(clientId);
        if (!clientOpt.isPresent()) {
            return null;
        }
        
        OAuthClient client = clientOpt.get();
        String userId = String.valueOf(refreshToken.getUserId());
        String scope = refreshToken.getScope();
        
        // 删除旧的访问令牌
        accessTokenRepository.deleteByUserId(refreshToken.getUserId());
        
        // 生成新的 Token
        return generateTokens(userId, clientId, scope, client);
    }
    
    /**
     * 客户端凭证模式
     */
    @Transactional
    public TokenResponse clientCredentials(String clientId, String clientSecret, String scope) {
        // 验证客户端
        if (!authorizationService.validateClient(clientId, clientSecret)) {
            log.warn("客户端验证失败: clientId={}", clientId);
            return null;
        }
        
        // 获取客户端信息
        Optional<OAuthClient> clientOpt = authorizationService.getClient(clientId);
        if (!clientOpt.isPresent()) {
            return null;
        }
        
        OAuthClient client = clientOpt.get();
        
        // 检查是否支持客户端凭证模式
        if (!client.supportsGrantType("client_credentials")) {
            log.warn("客户端不支持 client_credentials 模式: clientId={}", clientId);
            return null;
        }
        
        // 验证权限范围
        if (scope != null && !client.supportsScope(scope)) {
            log.warn("不支持的权限范围: scope={}", scope);
            return null;
        }
        
        String finalScope = scope != null ? scope : client.getScopes();
        
        // 客户端凭证模式不需要用户 ID，使用客户端 ID 作为主体
        return generateClientToken(clientId, finalScope, client);
    }
    
    /**
     * 生成 Token（用户模式）
     */
    private TokenResponse generateTokens(String userId, String clientId, String scope, OAuthClient client) {
        TokenResponse response = new TokenResponse();
        
        if ("jwt".equals(client.getTokenType())) {
            // 生成 JWT Token
            String accessToken = jwtUtil.generateAccessToken(userId, clientId, scope);
            String refreshToken = jwtUtil.generateRefreshToken(userId, clientId, scope);
            
            response.setAccessToken(accessToken);
            response.setRefreshToken(refreshToken);
            response.setTokenType("Bearer");
            response.setExpiresIn(client.getAccessTokenValidity().longValue());
            response.setScope(scope);
            
            // 保存到数据库（用于撤销）
            saveAccessToken(accessToken, Long.parseLong(userId), clientId, scope, client.getAccessTokenValidity());
            saveRefreshToken(refreshToken, Long.parseLong(userId), clientId, scope, client.getRefreshTokenValidity());
            
        } else {
            // 生成 Opaque Token
            String accessToken = TokenGenerator.generateToken();
            String refreshToken = TokenGenerator.generateToken();
            
            response.setAccessToken(accessToken);
            response.setRefreshToken(refreshToken);
            response.setTokenType("Bearer");
            response.setExpiresIn(client.getAccessTokenValidity().longValue());
            response.setScope(scope);
            
            // 保存到数据库
            saveAccessToken(accessToken, Long.parseLong(userId), clientId, scope, client.getAccessTokenValidity());
            saveRefreshToken(refreshToken, Long.parseLong(userId), clientId, scope, client.getRefreshTokenValidity());
        }
        
        log.info("生成 Token 成功: userId={}, clientId={}, tokenType={}", userId, clientId, client.getTokenType());
        
        return response;
    }
    
    /**
     * 生成 Token（客户端模式）
     */
    private TokenResponse generateClientToken(String clientId, String scope, OAuthClient client) {
        TokenResponse response = new TokenResponse();
        
        if ("jwt".equals(client.getTokenType())) {
            // 生成 JWT Token（使用客户端 ID 作为主体）
            String accessToken = jwtUtil.generateAccessToken(clientId, clientId, scope);
            
            response.setAccessToken(accessToken);
            response.setTokenType("Bearer");
            response.setExpiresIn(client.getAccessTokenValidity().longValue());
            response.setScope(scope);
            
            // 客户端凭证模式不生成刷新令牌
            
        } else {
            // 生成 Opaque Token
            String accessToken = TokenGenerator.generateToken();
            
            response.setAccessToken(accessToken);
            response.setTokenType("Bearer");
            response.setExpiresIn(client.getAccessTokenValidity().longValue());
            response.setScope(scope);
            
            // 保存到数据库（使用 userId = 0 表示客户端凭证模式）
            saveAccessToken(accessToken, 0L, clientId, scope, client.getAccessTokenValidity());
        }
        
        log.info("生成客户端 Token 成功: clientId={}, tokenType={}", clientId, client.getTokenType());
        
        return response;
    }
    
    /**
     * 保存访问令牌
     */
    private void saveAccessToken(String token, Long userId, String clientId, String scope, Integer validity) {
        AccessToken accessToken = new AccessToken();
        accessToken.setToken(token);
        accessToken.setUserId(userId);
        accessToken.setClientId(clientId);
        accessToken.setScope(scope);
        accessToken.setExpiresAt(LocalDateTime.now().plusSeconds(validity));
        
        accessTokenRepository.save(accessToken);
    }
    
    /**
     * 保存刷新令牌
     */
    private void saveRefreshToken(String token, Long userId, String clientId, String scope, Integer validity) {
        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setToken(token);
        refreshToken.setUserId(userId);
        refreshToken.setClientId(clientId);
        refreshToken.setScope(scope);
        refreshToken.setExpiresAt(LocalDateTime.now().plusSeconds(validity));
        
        refreshTokenRepository.save(refreshToken);
    }
    
    /**
     * 验证访问令牌
     */
    public boolean validateAccessToken(String token) {
        // 如果是 JWT，直接验证
        if (token.contains(".")) {
            return jwtUtil.validateToken(token);
        }
        
        // 如果是 Opaque Token，查询数据库
        Optional<AccessToken> tokenOpt = accessTokenRepository.findByToken(token);
        if (!tokenOpt.isPresent()) {
            return false;
        }
        
        AccessToken accessToken = tokenOpt.get();
        return accessToken.getExpiresAt().isAfter(LocalDateTime.now());
    }
    
    /**
     * 撤销访问令牌
     */
    @Transactional
    public boolean revokeAccessToken(String token) {
        Optional<AccessToken> tokenOpt = accessTokenRepository.findByToken(token);
        if (tokenOpt.isPresent()) {
            accessTokenRepository.delete(tokenOpt.get());
            log.info("撤销访问令牌: token={}", token);
            return true;
        }
        return false;
    }
    
    /**
     * 撤销刷新令牌
     */
    @Transactional
    public boolean revokeRefreshToken(String token) {
        Optional<RefreshToken> tokenOpt = refreshTokenRepository.findByToken(token);
        if (tokenOpt.isPresent()) {
            refreshTokenRepository.delete(tokenOpt.get());
            log.info("撤销刷新令牌: token={}", token);
            return true;
        }
        return false;
    }
}
