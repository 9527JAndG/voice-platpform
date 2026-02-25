package com.voice.platform.controller;

import com.voice.platform.config.JwtConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.*;

/**
 * OIDC 发现端点控制器
 * 提供 OAuth2/OIDC 服务器元数据
 */
@Slf4j
@RestController
public class OIDCDiscoveryController {
    
    @Autowired
    private JwtConfig jwtConfig;
    
    /**
     * OIDC 发现端点
     * GET /.well-known/openid-configuration
     */
    @GetMapping("/.well-known/openid-configuration")
    public ResponseEntity<?> openidConfiguration() {
        log.info("OIDC 发现端点被访问");
        
        String issuer = jwtConfig.getIssuer();
        
        Map<String, Object> configuration = new LinkedHashMap<>();
        
        // 基本信息
        configuration.put("issuer", issuer);
        
        // 端点
        configuration.put("authorization_endpoint", issuer + "/oauth2/authorize");
        configuration.put("token_endpoint", issuer + "/oauth2/token");
        configuration.put("introspection_endpoint", issuer + "/oauth2/introspect");
        configuration.put("revocation_endpoint", issuer + "/oauth2/revoke");
        configuration.put("jwks_uri", issuer + "/.well-known/jwks.json");
        
        // 支持的响应类型
        configuration.put("response_types_supported", Arrays.asList("code"));
        
        // 支持的授权类型
        configuration.put("grant_types_supported", Arrays.asList(
                "authorization_code",
                "refresh_token",
                "client_credentials"
        ));
        
        // 支持的客户端认证方法
        configuration.put("token_endpoint_auth_methods_supported", Arrays.asList(
                "client_secret_basic",
                "client_secret_post"
        ));
        
        // 支持的 PKCE 方法
        configuration.put("code_challenge_methods_supported", Arrays.asList("plain", "S256"));
        
        // 支持的权限范围
        configuration.put("scopes_supported", Arrays.asList(
                "device:control",
                "device:read",
                "openid",
                "profile",
                "email"
        ));
        
        // 支持的声明
        configuration.put("claims_supported", Arrays.asList(
                "sub",
                "iss",
                "aud",
                "exp",
                "iat",
                "jti",
                "scope",
                "client_id"
        ));
        
        // Token 端点支持的签名算法
        configuration.put("token_endpoint_auth_signing_alg_values_supported", Arrays.asList("HS256"));
        
        // 其他元数据
        configuration.put("service_documentation", issuer + "/docs");
        configuration.put("ui_locales_supported", Arrays.asList("zh-CN", "en-US"));
        
        return ResponseEntity.ok(configuration);
    }
    
    /**
     * OAuth2 授权服务器元数据（RFC 8414）
     * GET /.well-known/oauth-authorization-server
     */
    @GetMapping("/.well-known/oauth-authorization-server")
    public ResponseEntity<?> oauthAuthorizationServer() {
        log.info("OAuth2 授权服务器元数据端点被访问");
        
        // 返回与 OIDC 发现端点相同的信息
        return openidConfiguration();
    }
    
    /**
     * JWKS 端点（JSON Web Key Set）
     * GET /.well-known/jwks.json
     * 
     * 注意：这里返回空的 JWKS，因为我们使用对称密钥（HS256）
     * 如果使用非对称密钥（RS256），需要返回公钥
     */
    @GetMapping("/.well-known/jwks.json")
    public ResponseEntity<?> jwks() {
        log.info("JWKS 端点被访问");
        
        Map<String, Object> jwks = new HashMap<>();
        jwks.put("keys", new ArrayList<>());
        
        // 如果使用 RS256，需要添加公钥信息
        // 示例：
        // {
        //   "kty": "RSA",
        //   "use": "sig",
        //   "kid": "key-id",
        //   "n": "modulus",
        //   "e": "exponent"
        // }
        
        return ResponseEntity.ok(jwks);
    }
}
