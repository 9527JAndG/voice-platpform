package com.voice.platform.controller;

import com.voice.platform.dto.TokenResponse;
import com.voice.platform.service.OAuthService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Controller
public class OAuthController {
    
    @Autowired
    private OAuthService oauthService;
    
    /**
     * OAuth 授权页面
     * GET /authorize?client_id=xxx&redirect_uri=xxx&state=xxx&response_type=code
     */
    @GetMapping("/authorize")
    public String authorize(
            @RequestParam("client_id") String clientId,
            @RequestParam("redirect_uri") String redirectUri,
            @RequestParam("state") String state,
            @RequestParam(value = "response_type", defaultValue = "code") String responseType) {
        
        log.info("收到授权请求: clientId={}, redirectUri={}, state={}", clientId, redirectUri, state);
        
        // 这里简化处理，实际应该展示授权页面让用户确认
        // 返回授权页面，页面会提交到 /authorize/confirm
        return "authorize";
    }
    
    /**
     * 用户确认授权
     * POST /authorize/confirm
     */
    @PostMapping("/authorize/confirm")
    public void confirmAuthorize(
            @RequestParam("client_id") String clientId,
            @RequestParam("redirect_uri") String redirectUri,
            @RequestParam("state") String state,
            @RequestParam("authorize") String authorize,
            HttpServletResponse response) throws IOException {
        
        log.info("用户确认授权: clientId={}, authorize={}", clientId, authorize);
        
        if ("yes".equals(authorize)) {
            // 生成授权码
            String code = oauthService.generateAuthorizationCode(clientId, redirectUri);
            
            // 重定向到回调地址
            String decodedUri = URLDecoder.decode(redirectUri, "UTF-8");
            String redirectUrl = decodedUri + "&code=" + code + "&state=" + state;
            
            log.info("重定向到: {}", redirectUrl);
            response.sendRedirect(redirectUrl);
        } else {
            response.sendRedirect(redirectUri + "?error=access_denied&state=" + state);
        }
    }
    
    /**
     * 获取访问令牌
     * POST /token
     */
    @PostMapping("/token")
    @ResponseBody
    public ResponseEntity<?> token(
            @RequestParam("grant_type") String grantType,
            @RequestParam("client_id") String clientId,
            @RequestParam("client_secret") String clientSecret,
            @RequestParam(value = "code", required = false) String code,
            @RequestParam(value = "refresh_token", required = false) String refreshToken) {
        
        log.info("Token请求: grantType={}, clientId={}", grantType, clientId);
        
        // 验证客户端
        if (!oauthService.validateClient(clientId, clientSecret)) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "invalid_client");
            error.put("error_description", "客户端认证失败");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
        }
        
        TokenResponse tokenResponse = null;
        
        if ("authorization_code".equals(grantType)) {
            // 授权码模式
            tokenResponse = oauthService.exchangeToken(code, clientId);
        } else if ("refresh_token".equals(grantType)) {
            // 刷新令牌模式
            tokenResponse = oauthService.refreshToken(refreshToken, clientId);
        } else {
            Map<String, String> error = new HashMap<>();
            error.put("error", "unsupported_grant_type");
            error.put("error_description", "不支持的授权类型");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }
        
        if (tokenResponse == null) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "invalid_grant");
            error.put("error_description", "授权码或刷新令牌无效");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }
        
        return ResponseEntity.ok(tokenResponse);
    }
}
