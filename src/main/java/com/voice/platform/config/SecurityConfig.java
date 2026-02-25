package com.voice.platform.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Spring Security 配置
 * 
 * @author Voice Platform
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    /**
     * 配置 HTTP 安全
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            // 禁用 CSRF（API 服务通常禁用）
            .csrf(csrf -> csrf.disable())
            
            // 配置授权规则
            .authorizeHttpRequests(authorize -> authorize
                // OAuth2 端点允许匿名访问
                .requestMatchers("/oauth2/**").permitAll()
                .requestMatchers("/.well-known/**").permitAll()
                
                // 旧的 OAuth 端点（向后兼容）
                .requestMatchers("/authorize/**").permitAll()
                .requestMatchers("/token").permitAll()
                
                // 平台控制器端点需要认证（由各平台自己的 Token 验证）
                .requestMatchers("/aligenie/**").permitAll()
                .requestMatchers("/dueros/**").permitAll()
                .requestMatchers("/miai/**").permitAll()
                .requestMatchers("/alexa/**").permitAll()
                .requestMatchers("/google/**").permitAll()
                .requestMatchers("/smarthome/**").permitAll()
                
                // 静态资源
                .requestMatchers("/static/**").permitAll()
                
                // 其他请求需要认证
                .anyRequest().authenticated()
            )
            
            // 禁用表单登录（我们自己处理登录）
            .formLogin(form -> form.disable())
            
            // 禁用 HTTP Basic 认证
            .httpBasic(basic -> basic.disable())
            
            // 配置会话管理
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
            );
        
        return http.build();
    }

    /**
     * 密码编码器
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
