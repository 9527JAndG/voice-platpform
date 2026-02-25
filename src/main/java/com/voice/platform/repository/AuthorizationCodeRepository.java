package com.voice.platform.repository;

import com.voice.platform.model.AuthorizationCode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface AuthorizationCodeRepository extends JpaRepository<AuthorizationCode, Long> {
    
    Optional<AuthorizationCode> findByAuthorizationCode(String authorizationCode);
}
