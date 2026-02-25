package com.voice.platform.service;

import com.voice.platform.dto.TokenResponse;
import com.voice.platform.model.AccessToken;
import com.voice.platform.model.AuthorizationCode;
import com.voice.platform.model.OAuthClient;
import com.voice.platform.model.RefreshToken;
import com.voice.platform.repository.AccessTokenRepository;
import com.voice.platform.repository.AuthorizationCodeRepository;
import com.voice.platform.repository.OAuthClientRepository;
import com.voice.platform.repository.RefreshTokenRepository;
import com.voice.platform.util.TokenGenerator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Slf4j
@Service
public class OAuthService {
    
    @Autowired
    private OAuthClientRepository clientRepository;
    
    @Autowired
    private AuthorizationCodeRepository codeRepository;
    
    @Autowired
    private AccessTokenRepository accessTokenRepository;
    
    @Autowired
    private RefreshTokenRepository refreshTokenRepository;
    
    @Value("${oauth2.code-expire-seconds}")
    private Long codeExpireSeconds;
    
    @Value("${oauth2.access-token-expire-seconds}")
    private Long accessTokenExpireSeconds;
    
    @Value("${oauth2.refresh-token-expire-seconds}")
    private Long refreshTokenExpireSeconds;
    
    /**
     * 验证客户端
     */
    public boolean validateClient(String clientId, String clientSecret) {
        Optional<OAuthClient> client = clientRepository.findByClientIdAndClientSecret(clientId, clientSecret);
        return client.isPresent();
    }
    
    /**
     * 生成授权码
     */
    @Transactional
    public String generateAuthorizationCode(String clientId, String redirectUri) {
        String code = TokenGenerator.generateCode();
        
        AuthorizationCode authCode = new AuthorizationCode();
        authCode.setAuthorizationCode(code);
        authCode.setClientId(clientId);
        authCode.setRedirectUri(redirectUri);
        authCode.setExpiresAt(LocalDateTime.now().plusSeconds(codeExpireSeconds));
        authCode.setCreatedAt(LocalDateTime.now());
        
        codeRepository.save(authCode);
        log.info("生成授权码: clientId={}, code={}", clientId, code);
        
        return code;
    }
    
    /**
     * 验证授权码并生成 Token
     */
    @Transactional
    public TokenResponse exchangeToken(String code, String clientId) {
        Optional<AuthorizationCode> authCodeOpt = codeRepository.findByAuthorizationCode(code);
        
        if (!authCodeOpt.isPresent()) {
            log.error("授权码不存在: code={}", code);
            return null;
        }
        
        AuthorizationCode authCode = authCodeOpt.get();
        
        // 验证授权码是否过期
        if (authCode.getExpiresAt().isBefore(LocalDateTime.now())) {
            log.error("授权码已过期: code={}", code);
            return null;
        }
        
        // 验证客户端ID
        if (!authCode.getClientId().equals(clientId)) {
            log.error("客户端ID不匹配: code={}, clientId={}", code, clientId);
            return null;
        }
        
        // 生成访问令牌
        String accessToken = TokenGenerator.generateAccessToken();
        AccessToken token = new AccessToken();
        token.setAccessToken(accessToken);
        token.setClientId(clientId);
        token.setExpiresAt(LocalDateTime.now().plusSeconds(accessTokenExpireSeconds));
        token.setCreatedAt(LocalDateTime.now());
        accessTokenRepository.save(token);
        
        // 生成刷新令牌
        String refreshToken = TokenGenerator.generateRefreshToken();
        RefreshToken refresh = new RefreshToken();
        refresh.setRefreshToken(refreshToken);
        refresh.setClientId(clientId);
        refresh.setExpiresAt(LocalDateTime.now().plusSeconds(refreshTokenExpireSeconds));
        refresh.setCreatedAt(LocalDateTime.now());
        refreshTokenRepository.save(refresh);
        
        log.info("生成Token: clientId={}, accessToken={}", clientId, accessToken);
        
        return new TokenResponse(accessToken, accessTokenExpireSeconds, refreshToken);
    }
    
    /**
     * 刷新 Token
     */
    @Transactional
    public TokenResponse refreshToken(String refreshToken, String clientId) {
        Optional<RefreshToken> refreshOpt = refreshTokenRepository.findByToken(refreshToken);
        
        if (!refreshOpt.isPresent()) {
            log.error("刷新令牌不存在: refreshToken={}", refreshToken);
            return null;
        }
        
        RefreshToken refresh = refreshOpt.get();
        
        // 验证刷新令牌是否过期
        if (refresh.getExpiresAt().isBefore(LocalDateTime.now())) {
            log.error("刷新令牌已过期: refreshToken={}", refreshToken);
            return null;
        }
        
        // 验证客户端ID
        if (!refresh.getClientId().equals(clientId)) {
            log.error("客户端ID不匹配: refreshToken={}, clientId={}", refreshToken, clientId);
            return null;
        }
        
        // 生成新的访问令牌
        String newAccessToken = TokenGenerator.generateAccessToken();
        AccessToken token = new AccessToken();
        token.setAccessToken(newAccessToken);
        token.setClientId(clientId);
        token.setExpiresAt(LocalDateTime.now().plusSeconds(accessTokenExpireSeconds));
        token.setCreatedAt(LocalDateTime.now());
        accessTokenRepository.save(token);
        
        log.info("刷新Token: clientId={}, newAccessToken={}", clientId, newAccessToken);
        
        return new TokenResponse(newAccessToken, accessTokenExpireSeconds, refreshToken);
    }
    
    /**
     * 验证访问令牌
     */
    public boolean validateAccessToken(String accessToken) {
        Optional<AccessToken> tokenOpt = accessTokenRepository.findByToken(accessToken);
        
        if (!tokenOpt.isPresent()) {
            return false;
        }
        
        AccessToken token = tokenOpt.get();
        return token.getExpiresAt().isAfter(LocalDateTime.now());
    }
    
    /**
     * 根据访问令牌获取客户端ID
     */
    public String getClientIdByAccessToken(String accessToken) {
        Optional<AccessToken> tokenOpt = accessTokenRepository.findByToken(accessToken);
        return tokenOpt.map(AccessToken::getClientId).orElse(null);
    }
}
