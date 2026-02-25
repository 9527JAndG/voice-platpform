package com.voice.platform.controller;

import com.voice.platform.model.OAuthClient;
import com.voice.platform.service.OAuth2AuthorizationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

/**
 * OAuth2 授权控制器
 * 处理授权请求、登录和用户同意
 */
@Slf4j
@Controller
@RequestMapping("/oauth2")
public class OAuth2AuthorizationController {
    
    @Autowired
    private OAuth2AuthorizationService authorizationService;
    
    @Autowired
    private com.voice.platform.service.UserService userService;
    
    /**
     * OAuth2 授权端点
     * GET /oauth2/authorize
     */
    @GetMapping("/authorize")
    public String authorize(
            @RequestParam("client_id") String clientId,
            @RequestParam("redirect_uri") String redirectUri,
            @RequestParam(value = "response_type", defaultValue = "code") String responseType,
            @RequestParam(value = "scope", required = false) String scope,
            @RequestParam(value = "state", required = false) String state,
            @RequestParam(value = "code_challenge", required = false) String codeChallenge,
            @RequestParam(value = "code_challenge_method", defaultValue = "plain") String codeChallengeMethod,
            HttpSession session,
            Model model) {
        
        log.info("收到授权请求: clientId={}, redirectUri={}, scope={}, state={}", 
                clientId, redirectUri, scope, state);
        
        // 验证 response_type
        if (!"code".equals(responseType)) {
            model.addAttribute("error", "unsupported_response_type");
            model.addAttribute("error_description", "只支持 authorization_code 模式");
            return "error";
        }
        
        // 验证客户端
        Optional<OAuthClient> clientOpt = authorizationService.getClient(clientId);
        if (!clientOpt.isPresent()) {
            model.addAttribute("error", "invalid_client");
            model.addAttribute("error_description", "客户端不存在");
            return "error";
        }
        
        OAuthClient client = clientOpt.get();
        
        // 验证重定向 URI
        if (!client.getRedirectUri().equals(redirectUri)) {
            model.addAttribute("error", "invalid_request");
            model.addAttribute("error_description", "重定向 URI 不匹配");
            return "error";
        }
        
        // 验证权限范围
        if (scope != null && !client.supportsScope(scope)) {
            model.addAttribute("error", "invalid_scope");
            model.addAttribute("error_description", "不支持的权限范围");
            return "error";
        }
        
        // 保存授权请求到 session
        session.setAttribute("oauth2_client_id", clientId);
        session.setAttribute("oauth2_redirect_uri", redirectUri);
        session.setAttribute("oauth2_scope", scope != null ? scope : client.getScopes());
        session.setAttribute("oauth2_state", state);
        session.setAttribute("oauth2_code_challenge", codeChallenge);
        session.setAttribute("oauth2_code_challenge_method", codeChallengeMethod);
        
        // 检查用户是否已登录
        Long userId = (Long) session.getAttribute("user_id");
        if (userId == null) {
            // 未登录，跳转到登录页面
            model.addAttribute("clientId", clientId);
            return "login";
        }
        
        // 已登录，检查是否需要用户同意
        if (client.getAutoApprove() != null && client.getAutoApprove()) {
            // 自动批准，直接生成授权码
            return generateAuthorizationCode(session, userId);
        }
        
        // 需要用户同意，跳转到同意页面
        model.addAttribute("clientId", clientId);
        model.addAttribute("scope", scope != null ? scope : client.getScopes());
        return "consent";
    }
    
    /**
     * 显示登录页面
     * GET /oauth2/login
     */
    @GetMapping("/login")
    public String showLoginPage(
            @RequestParam(value = "error", required = false) String error,
            Model model) {
        
        if (error != null) {
            model.addAttribute("error", "用户名或密码错误");
        }
        
        return "login";
    }
    
    /**
     * 用户登录
     * POST /oauth2/login
     */
    @PostMapping("/login")
    public String login(
            @RequestParam("username") String username,
            @RequestParam("password") String password,
            HttpSession session,
            Model model) {
        
        log.info("收到登录请求: username={}, password长度={}", username, password != null ? password.length() : 0);
        
        // 验证用户凭证
        if (!userService.validateCredentials(username, password)) {
            // 登录失败
            log.warn("登录失败: username={}", username);
            model.addAttribute("error", "用户名或密码错误");
            return "login";
        }
        
        // 登录成功，从数据库获取用户信息
        com.voice.platform.model.User user = userService.getUserByUsername(username).orElse(null);
        if (user == null) {
            log.error("用户验证成功但无法获取用户信息: username={}", username);
            model.addAttribute("error", "系统错误，请稍后重试");
            return "login";
        }
        
        // 保存用户信息到 session
        Long userId = user.getId();
        session.setAttribute("user_id", userId);
        session.setAttribute("username", username);
        
        log.info("用户登录成功: username={}, userId={}", username, userId);
        
        // 检查是否有待处理的授权请求
        String clientId = (String) session.getAttribute("oauth2_client_id");
        if (clientId == null) {
            // 没有授权请求，跳转到首页或显示成功消息
            model.addAttribute("message", "登录成功");
            return "login";
        }
        
        // 检查是否需要用户同意
        Optional<OAuthClient> clientOpt = authorizationService.getClient(clientId);
        
        if (clientOpt.isPresent() && clientOpt.get().getAutoApprove() != null && clientOpt.get().getAutoApprove()) {
            // 自动批准
            return generateAuthorizationCode(session, userId);
        }
        
        // 跳转到同意页面
        String scope = (String) session.getAttribute("oauth2_scope");
        model.addAttribute("clientId", clientId);
        model.addAttribute("scope", scope);
        return "consent";
    }
    
    /**
     * 用户同意授权
     * POST /oauth2/consent
     */
    @PostMapping("/consent")
    public String consent(
            @RequestParam("approve") String approve,
            HttpSession session,
            Model model) {
        
        Long userId = (Long) session.getAttribute("user_id");
        if (userId == null) {
            model.addAttribute("error", "unauthorized");
            model.addAttribute("error_description", "用户未登录");
            return "error";
        }
        
        if ("true".equals(approve)) {
            // 用户同意授权
            return generateAuthorizationCode(session, userId);
        } else {
            // 用户拒绝授权
            return redirectWithError(session, "access_denied", "用户拒绝授权");
        }
    }
    
    /**
     * 生成授权码并重定向
     */
    private String generateAuthorizationCode(HttpSession session, Long userId) {
        String clientId = (String) session.getAttribute("oauth2_client_id");
        String redirectUri = (String) session.getAttribute("oauth2_redirect_uri");
        String scope = (String) session.getAttribute("oauth2_scope");
        String state = (String) session.getAttribute("oauth2_state");
        String codeChallenge = (String) session.getAttribute("oauth2_code_challenge");
        String codeChallengeMethod = (String) session.getAttribute("oauth2_code_challenge_method");
        
        // 生成授权码
        String code = authorizationService.generateAuthorizationCode(
                clientId, userId, redirectUri, scope, state, codeChallenge, codeChallengeMethod);
        
        // 清除 session 中的授权请求信息
        session.removeAttribute("oauth2_client_id");
        session.removeAttribute("oauth2_redirect_uri");
        session.removeAttribute("oauth2_scope");
        session.removeAttribute("oauth2_state");
        session.removeAttribute("oauth2_code_challenge");
        session.removeAttribute("oauth2_code_challenge_method");
        
        // 构建重定向 URL
        StringBuilder redirectUrl = new StringBuilder(redirectUri);
        redirectUrl.append(redirectUri.contains("?") ? "&" : "?");
        redirectUrl.append("code=").append(code);
        
        if (state != null) {
            redirectUrl.append("&state=").append(URLEncoder.encode(state, StandardCharsets.UTF_8));
        }
        
        log.info("重定向到: {}", redirectUrl);
        
        return "redirect:" + redirectUrl.toString();
    }
    
    /**
     * 重定向并返回错误
     */
    private String redirectWithError(HttpSession session, String error, String errorDescription) {
        String redirectUri = (String) session.getAttribute("oauth2_redirect_uri");
        String state = (String) session.getAttribute("oauth2_state");
        
        // 清除 session
        session.removeAttribute("oauth2_client_id");
        session.removeAttribute("oauth2_redirect_uri");
        session.removeAttribute("oauth2_scope");
        session.removeAttribute("oauth2_state");
        session.removeAttribute("oauth2_code_challenge");
        session.removeAttribute("oauth2_code_challenge_method");
        
        // 构建重定向 URL
        StringBuilder redirectUrl = new StringBuilder(redirectUri);
        redirectUrl.append(redirectUri.contains("?") ? "&" : "?");
        redirectUrl.append("error=").append(error);
        redirectUrl.append("&error_description=").append(URLEncoder.encode(errorDescription, StandardCharsets.UTF_8));
        
        if (state != null) {
            redirectUrl.append("&state=").append(URLEncoder.encode(state, StandardCharsets.UTF_8));
        }
        
        return "redirect:" + redirectUrl.toString();
    }
    
    /**
     * 用户登出
     * GET /oauth2/logout
     */
    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/oauth2/authorize";
    }
}
