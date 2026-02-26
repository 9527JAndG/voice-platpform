package com.voice.platform.repository;

import com.voice.platform.model.AlexaToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Alexa Token Repository
 * 用于管理 Alexa Event Gateway 访问令牌
 */
@Repository
public interface AlexaTokenRepository extends JpaRepository<AlexaToken, Long> {
    
    /**
     * 根据用户ID查找Token
     */
    Optional<AlexaToken> findByUserId(Long userId);
    
    /**
     * 根据 Grantee Token 查找
     */
    Optional<AlexaToken> findByGranteeToken(String granteeToken);
    
    /**
     * 检查用户是否已有Token
     */
    boolean existsByUserId(Long userId);
}
