package com.voice.platform.service;

import com.voice.platform.model.User;
import com.voice.platform.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.util.Optional;

/**
 * 用户服务
 * 
 * 使用数据库存储用户信息
 * 
 * @author Voice Platform
 */
@Slf4j
@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    @PostConstruct
    public void init() {
        // 检查是否有测试用户，如果没有则创建
        initTestUsersIfNeeded();
    }
    
    /**
     * 初始化测试用户（如果不存在）
     */
    private void initTestUsersIfNeeded() {
        // 检查是否已有用户
        if (userRepository.count() > 0) {
            log.info("数据库中已有 {} 个用户", userRepository.count());
            return;
        }
        
        log.info("数据库中没有用户，创建测试用户...");
        
        // 创建测试用户
        createUser("user1", "password", "user1@example.com");
        createUser("user2", "password", "user2@example.com");
        createUser("testuser", "password", "testuser@example.com");
        
        log.info("测试用户创建完成，用户名: user1/user2/testuser, 密码: password");
    }
    
    /**
     * 创建用户
     */
    private void createUser(String username, String password, String email) {
        User user = new User();
        user.setUsername(username);
        
        // 生成 BCrypt 密码哈希
        String encodedPassword = passwordEncoder.encode(password);
        user.setPassword(encodedPassword);
        
        user.setEmail(email);
        user.setEnabled(true);
        user.setAccountNonExpired(true);
        user.setAccountNonLocked(true);
        user.setCredentialsNonExpired(true);
        
        userRepository.save(user);
        log.info("创建用户: username={}, email={}, 密码哈希长度={}", 
                username, email, encodedPassword.length());
    }
    
    /**
     * 验证用户凭证
     */
    public boolean validateCredentials(String username, String password) {
        log.info("验证用户凭证: username={}", username);
        
        Optional<User> userOpt = userRepository.findByUsername(username);
        if (!userOpt.isPresent()) {
            log.warn("用户不存在: username={}", username);
            return false;
        }
        
        User user = userOpt.get();
        
        log.debug("数据库中的密码哈希: {}", user.getPassword());
        log.debug("输入的明文密码: {}", password);
        
        // 检查账户状态
        if (!user.getEnabled()) {
            log.warn("用户已禁用: username={}", username);
            return false;
        }
        
        if (!user.getAccountNonLocked()) {
            log.warn("用户已锁定: username={}", username);
            return false;
        }
        
        // 验证密码
        boolean valid = passwordEncoder.matches(password, user.getPassword());
        log.info("密码验证结果: username={}, valid={}, 数据库密码长度={}", 
                username, valid, user.getPassword() != null ? user.getPassword().length() : 0);
        
        return valid;
    }
    
    /**
     * 根据用户名获取用户
     */
    public Optional<User> getUserByUsername(String username) {
        return userRepository.findByUsername(username);
    }
    
    /**
     * 根据用户 ID 获取用户
     */
    public Optional<User> getUserById(Long userId) {
        return userRepository.findById(userId);
    }
    
    /**
     * 检查用户名是否存在
     */
    public boolean existsByUsername(String username) {
        return userRepository.existsByUsername(username);
    }
}
