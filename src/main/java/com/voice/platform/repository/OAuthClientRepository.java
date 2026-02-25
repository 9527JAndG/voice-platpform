package com.voice.platform.repository;

import com.voice.platform.model.OAuthClient;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface OAuthClientRepository extends JpaRepository<OAuthClient, Long> {
    
    Optional<OAuthClient> findByClientId(String clientId);
    
    Optional<OAuthClient> findByClientIdAndClientSecret(String clientId, String clientSecret);
}
