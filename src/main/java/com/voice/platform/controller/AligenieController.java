package com.voice.platform.controller;

import com.voice.platform.dto.AligenieRequest;
import com.voice.platform.dto.AligenieResponse;
import com.voice.platform.service.DeviceService;
import com.voice.platform.service.OAuthService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * 天猫精灵 控制器
 * 处理 天猫精灵 的设备发现和控制请求
 */
@Slf4j
@RestController
@RequestMapping("/aligenie")
public class AligenieController {
    
    @Autowired
    private OAuthService oauthService;
    
    @Autowired
    private DeviceService deviceService;
    
    /**
     * 设备发现接口
     * POST /smarthome/discovery
     */
    @PostMapping("/discovery")
    public ResponseEntity<?> discovery(
            @RequestHeader("Authorization") String authorization,
            @RequestBody AligenieRequest request) {
        
        log.info("设备发现请求: namespace={}, name={}", 
                request.getHeader().getNamespace(), 
                request.getHeader().getName());
        
        // 验证 Token
        String accessToken = extractToken(authorization);
        if (!oauthService.validateAccessToken(accessToken)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(AligenieResponse.error(
                            request.getHeader().getMessageId(),
                            "INVALID_TOKEN",
                            "访问令牌无效或已过期"));
        }
        
        // 获取用户ID（这里简化处理，实际应该从 token 关联的用户信息获取）
        Long userId = 1L;
        
        AligenieResponse response = deviceService.discovery(userId, request.getHeader().getMessageId());
        return ResponseEntity.ok(response);
    }
    
    /**
     * 设备控制接口
     * POST /smarthome/control
     */
    @PostMapping("/control")
    public ResponseEntity<?> control(
            @RequestHeader("Authorization") String authorization,
            @RequestBody AligenieRequest request) {
        
        log.info("设备控制请求: namespace={}, name={}", 
                request.getHeader().getNamespace(), 
                request.getHeader().getName());
        
        // 验证 Token
        String accessToken = extractToken(authorization);
        if (!oauthService.validateAccessToken(accessToken)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(AligenieResponse.error(
                            request.getHeader().getMessageId(),
                            "INVALID_TOKEN",
                            "访问令牌无效或已过期"));
        }
        
        String deviceId = (String) request.getPayload().get("deviceId");
        String action = request.getHeader().getName();
        String messageId = request.getHeader().getMessageId();
        
        AligenieResponse response;
        
        switch (action) {
            case "TurnOn":
                response = deviceService.turnOn(deviceId, messageId);
                break;
            case "TurnOff":
                response = deviceService.turnOff(deviceId, messageId);
                break;
            case "Pause":
                response = deviceService.pause(deviceId, messageId);
                break;
            case "Continue":
                response = deviceService.continueWork(deviceId, messageId);
                break;
            case "SetMode":
                String mode = (String) request.getPayload().get("mode");
                response = deviceService.setMode(deviceId, mode, messageId);
                break;
            default:
                response = AligenieResponse.error(messageId, "UNSUPPORTED_ACTION", "不支持的操作");
        }
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * 设备查询接口
     * POST /smarthome/query
     */
    @PostMapping("/query")
    public ResponseEntity<?> query(
            @RequestHeader("Authorization") String authorization,
            @RequestBody AligenieRequest request) {
        
        log.info("设备查询请求: namespace={}, name={}", 
                request.getHeader().getNamespace(), 
                request.getHeader().getName());
        
        // 验证 Token
        String accessToken = extractToken(authorization);
        if (!oauthService.validateAccessToken(accessToken)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(AligenieResponse.error(
                            request.getHeader().getMessageId(),
                            "INVALID_TOKEN",
                            "访问令牌无效或已过期"));
        }
        
        String deviceId = (String) request.getPayload().get("deviceId");
        String messageId = request.getHeader().getMessageId();
        
        AligenieResponse response = deviceService.query(deviceId, messageId);
        return ResponseEntity.ok(response);
    }
    
    /**
     * 从 Authorization header 中提取 token
     */
    private String extractToken(String authorization) {
        if (authorization != null && authorization.startsWith("Bearer ")) {
            return authorization.substring(7);
        }
        return null;
    }
}
