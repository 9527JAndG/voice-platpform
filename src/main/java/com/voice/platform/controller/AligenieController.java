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
 * 天猫精灵（AliGenie）智能家居控制器
 * 实现天猫精灵 IoT 设备控制 API
 * 
 * 支持的功能:
 * - 设备发现 (Discovery)
 * - 设备控制 (Control)
 * - 设备查询 (Query)
 * 
 * 支持的操作:
 * - TurnOn: 开机
 * - TurnOff: 关机
 * - Pause: 暂停
 * - Continue: 继续
 * - SetMode: 设置模式
 * 
 * @author Voice Platform Team
 * @version 1.0
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
     * POST /aligenie/discovery
     * 
     * @param authorization Authorization header (Bearer token)
     * @param request 天猫精灵发现请求
     * @return 设备列表响应
     */
    @PostMapping("/discovery")
    public ResponseEntity<?> discovery(
            @RequestHeader("Authorization") String authorization,
            @RequestBody AligenieRequest request) {
        
        String messageId = request.getHeader().getMessageId();
        String namespace = request.getHeader().getNamespace();
        String name = request.getHeader().getName();
        
        log.info("=== 收到天猫精灵设备发现请求 ===");
        log.info("MessageId: {}", messageId);
        log.info("Namespace: {}", namespace);
        log.info("Name: {}", name);
        
        try {
            // 验证 Token
            String accessToken = extractToken(authorization);
            if (accessToken == null || !oauthService.validateAccessToken(accessToken)) {
                log.warn("Token 验证失败: token={}", accessToken != null ? "exists" : "null");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(AligenieResponse.error(
                                messageId,
                                "INVALID_TOKEN",
                                "访问令牌无效或已过期"));
            }
            
            // 获取用户ID（简化处理，实际应从 token 获取）
            // TODO: 从 token 中解析真实的 userId
            Long userId = 1L;
            
            log.info("开始设备发现: userId={}", userId);
            
            AligenieResponse response = deviceService.discovery(userId, messageId);
            
            log.info("✓ 设备发现完成: userId={}, messageId={}", userId, messageId);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("设备发现失败: messageId={}", messageId, e);
            return ResponseEntity.ok(AligenieResponse.error(
                    messageId,
                    "INTERNAL_ERROR",
                    "服务器内部错误: " + e.getMessage()));
        }
    }
    
    /**
     * 设备控制接口
     * POST /aligenie/control
     * 
     * @param authorization Authorization header (Bearer token)
     * @param request 天猫精灵控制请求
     * @return 控制响应
     */
    @PostMapping("/control")
    public ResponseEntity<?> control(
            @RequestHeader("Authorization") String authorization,
            @RequestBody AligenieRequest request) {
        
        String messageId = request.getHeader().getMessageId();
        String namespace = request.getHeader().getNamespace();
        String action = request.getHeader().getName();
        
        log.info("=== 收到天猫精灵设备控制请求 ===");
        log.info("MessageId: {}", messageId);
        log.info("Namespace: {}", namespace);
        log.info("Action: {}", action);
        
        try {
            // 验证 Token
            String accessToken = extractToken(authorization);
            if (accessToken == null || !oauthService.validateAccessToken(accessToken)) {
                log.warn("Token 验证失败: token={}", accessToken != null ? "exists" : "null");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(AligenieResponse.error(
                                messageId,
                                "INVALID_TOKEN",
                                "访问令牌无效或已过期"));
            }
            
            // 获取设备ID
            String deviceId = (String) request.getPayload().get("deviceId");
            
            if (deviceId == null || deviceId.isEmpty()) {
                log.warn("设备ID缺失");
                return ResponseEntity.ok(AligenieResponse.error(
                        messageId,
                        "MISSING_PARAMETER",
                        "缺少设备ID参数"));
            }
            
            log.info("设备ID: {}", deviceId);
            
            // 执行控制操作
            AligenieResponse response = executeControl(deviceId, action, request, messageId);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("设备控制失败: action={}, messageId={}", action, messageId, e);
            return ResponseEntity.ok(AligenieResponse.error(
                    messageId,
                    "INTERNAL_ERROR",
                    "服务器内部错误: " + e.getMessage()));
        }
    }
    
    /**
     * 执行具体的控制操作
     * 
     * @param deviceId 设备ID
     * @param action 操作类型
     * @param request 请求对象
     * @param messageId 消息ID
     * @return 控制响应
     */
    private AligenieResponse executeControl(String deviceId, String action, 
                                           AligenieRequest request, String messageId) {
        AligenieResponse response;
        
        switch (action) {
            case "TurnOn":
                response = deviceService.turnOn(deviceId, messageId);
                log.info("✓ 设备开机成功: deviceId={}", deviceId);
                break;
                
            case "TurnOff":
                response = deviceService.turnOff(deviceId, messageId);
                log.info("✓ 设备关机成功: deviceId={}", deviceId);
                break;
                
            case "Pause":
                response = deviceService.pause(deviceId, messageId);
                log.info("✓ 设备暂停成功: deviceId={}", deviceId);
                break;
                
            case "Continue":
                response = deviceService.continueWork(deviceId, messageId);
                log.info("✓ 设备继续成功: deviceId={}", deviceId);
                break;
                
            case "SetMode":
                String mode = (String) request.getPayload().get("mode");
                if (mode == null || mode.isEmpty()) {
                    log.warn("模式参数缺失");
                    response = AligenieResponse.error(messageId, "MISSING_PARAMETER", "缺少模式参数");
                } else if (!isValidMode(mode)) {
                    log.warn("无效的模式: mode={}", mode);
                    response = AligenieResponse.error(messageId, "INVALID_MODE", 
                            "不支持的模式: " + mode + "。支持的模式: auto, spot, edge");
                } else {
                    response = deviceService.setMode(deviceId, mode, messageId);
                    log.info("✓ 模式设置成功: deviceId={}, mode={}", deviceId, mode);
                }
                break;
                
            default:
                log.warn("不支持的操作: action={}", action);
                response = AligenieResponse.error(messageId, "UNSUPPORTED_ACTION", 
                        "不支持的操作: " + action);
        }
        
        return response;
    }
    
    /**
     * 验证模式是否有效
     * 
     * @param mode 模式值
     * @return 是否有效
     */
    private boolean isValidMode(String mode) {
        return "auto".equals(mode) || "spot".equals(mode) || "edge".equals(mode);
    }
    
    /**
     * 设备查询接口
     * POST /aligenie/query
     * 
     * @param authorization Authorization header (Bearer token)
     * @param request 天猫精灵查询请求
     * @return 设备状态响应
     */
    @PostMapping("/query")
    public ResponseEntity<?> query(
            @RequestHeader("Authorization") String authorization,
            @RequestBody AligenieRequest request) {
        
        String messageId = request.getHeader().getMessageId();
        String namespace = request.getHeader().getNamespace();
        String name = request.getHeader().getName();
        
        log.info("=== 收到天猫精灵设备查询请求 ===");
        log.info("MessageId: {}", messageId);
        log.info("Namespace: {}", namespace);
        log.info("Name: {}", name);
        
        try {
            // 验证 Token
            String accessToken = extractToken(authorization);
            if (accessToken == null || !oauthService.validateAccessToken(accessToken)) {
                log.warn("Token 验证失败: token={}", accessToken != null ? "exists" : "null");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(AligenieResponse.error(
                                messageId,
                                "INVALID_TOKEN",
                                "访问令牌无效或已过期"));
            }
            
            // 获取设备ID
            String deviceId = (String) request.getPayload().get("deviceId");
            
            if (deviceId == null || deviceId.isEmpty()) {
                log.warn("设备ID缺失");
                return ResponseEntity.ok(AligenieResponse.error(
                        messageId,
                        "MISSING_PARAMETER",
                        "缺少设备ID参数"));
            }
            
            log.info("查询设备: deviceId={}", deviceId);
            
            AligenieResponse response = deviceService.query(deviceId, messageId);
            
            log.info("✓ 设备查询成功: deviceId={}", deviceId);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("设备查询失败: messageId={}", messageId, e);
            return ResponseEntity.ok(AligenieResponse.error(
                    messageId,
                    "INTERNAL_ERROR",
                    "服务器内部错误: " + e.getMessage()));
        }
    }
    
    /**
     * 从 Authorization header 中提取 Token
     * 
     * @param authorization Authorization header 值
     * @return 访问令牌，如果不存在返回 null
     */
    private String extractToken(String authorization) {
        if (authorization != null && authorization.startsWith("Bearer ")) {
            return authorization.substring(7);
        }
        return null;
    }
}
