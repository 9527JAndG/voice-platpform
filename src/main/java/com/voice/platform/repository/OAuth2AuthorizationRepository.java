package com.voice.platform.repository;

import com.voice.platform.model.OAuth2Authorization;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

/**
 * OAuth2 授权记录 Repository
 */
@Repository
public interface OAuth2AuthorizationRepository extends JpaRepository<OAuth2Authorization, Long> {
    
    /**
     * 根据授权码查找
     */
    Optional<OAuth2Authorization> findByAuthorizationCode(String authorizationCode);
    
    /**
     * 根据客户端 ID 和用户 ID 查找未使用的授权码
     */
    Optional<OAuth2Authorization> findByClientIdAndUserIdAndUsedFalse(String clientId, Long userId);
    
    /**
     * 删除过期的授权码
     */
    @Modifying
    @Transactional
    @Query("DELETE FROM OAuth2Authorization a WHERE a.expiresAt < :now OR (a.used = true AND a.usedAt < :threshold)")
    int deleteExpiredAuthorizations(LocalDateTime now, LocalDateTime threshold);
    
    /**
     * 删除用户的所有授权码
     */
    @Modifying
    @Transactional
    void deleteByUserId(Long userId);
    
    /**
     * 删除客户端的所有授权码
     */
    @Modifying
    @Transactional
    void deleteByClientId(String clientId);
}
