package com.voice.platform.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

/**
 * Google Sync Service
 * 实现 Google HomeGraph Request Sync API
 * 当设备列表变化时主动触发 Google 同步
 * 
 * @author Voice Platform Team
 * @version 1.0
 */
@Slf4j
@Service
public class GoogleSyncService {
    
    @Autowired
    private GoogleServiceAccountService serviceAccountService;
    
    @Autowired
    private RestTemplate restTemplate;
    
    private static final String HOMEGRAPH_API_URL = 
        "https://homegraph.googleapis.com/v1/devices:requestSync";
    
    /**
     * 请求 Google 同步设备列表
     * 当用户添加、删除或修改设备时调用
     * 
     * @param userId 用户ID
     * @return 是否成功
     */
    public boolean requestSync(Long userId) {
        log.info("=== 开始 Google Request Sync ===");
        log.info("UserId: {}", userId);
        
        try {
            // 获取 Service Account Access Token
            String token = serviceAccountService.getAccessToken();
            
            if (token == null || token.isEmpty()) {
                log.error("无法获取 Google Access Token");
                return false;
            }
            
            // 构建请求体
            String agentUserId = "user_" + userId;
            Map<String, String> requestBody = new HashMap<>();
            requestBody.put("agentUserId", agentUserId);
            
            // 设置请求头
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Authorization", "Bearer " + token);
            
            HttpEntity<Map<String, String>> request = new HttpEntity<>(requestBody, headers);
            
            // 发送请求
            ResponseEntity<String> response = restTemplate.postForEntity(
                HOMEGRAPH_API_URL,
                request,
                String.class
            );
            
            if (response.getStatusCode().is2xxSuccessful()) {
                log.info("✓ Google Request Sync 成功: userId={}, agentUserId={}", 
                        userId, agentUserId);
                return true;
            } else {
                log.warn("Google Request Sync 失败: status={}, body={}", 
                        response.getStatusCode(), response.getBody());
                return false;
            }
            
        } catch (Exception e) {
            log.error("Google Request Sync 异常: userId={}", userId, e);
            return false;
        }
    }
    
    /**
     * 批量请求同步
     * 
     * @param userIds 用户ID列表
     */
    public void requestSyncBatch(Long... userIds) {
        log.info("批量 Request Sync: userCount={}", userIds.length);
        
        for (Long userId : userIds) {
            try {
                requestSync(userId);
            } catch (Exception e) {
                log.error("批量 Request Sync 失败: userId={}", userId, e);
            }
        }
    }
}
