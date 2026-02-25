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
            .csrf().disable()
            
            // 配置授权规则
            .authorizeHttpRequests(authorize -> authorize
                // OAuth2 端点允许匿名访问
                .antMatchers("/oauth2/**").permitAll()
                .antMatchers("/.well-known/**").permitAll()
                
                // 旧的 OAuth 端点（向后兼容）
                .antMatchers("/authorize/**").permitAll()
                .antMatchers("/token").permitAll()
                
                // 平台控制器端点需要认证（由各平台自己的 Token 验证）
                .antMatchers("/aligenie/**").permitAll()
                .antMatchers("/dueros/**").permitAll()
                .antMatchers("/miai/**").permitAll()
                .antMatchers("/alexa/**").permitAll()
                .antMatchers("/google/**").permitAll()
                .antMatchers("/smarthome/**").permitAll()
                
                // 静态资源
                .antMatchers("/static/**").permitAll()
                
                // 其他请求需要认证
                .anyRequest().authenticated()
            )
            
            // 禁用表单登录（我们自己处理登录）
            .formLogin().disable()
            
            // 禁用 HTTP Basic 认证
            .httpBasic().disable()
            
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
