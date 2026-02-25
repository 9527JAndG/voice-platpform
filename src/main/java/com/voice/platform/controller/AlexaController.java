package com.voice.platform.controller;

import com.voice.platform.dto.alexa.AlexaRequest;
import com.voice.platform.dto.alexa.AlexaResponse;
import com.voice.platform.dto.alexa.DiscoveredEndpoint;
import com.voice.platform.model.Device;
import com.voice.platform.service.DeviceService;
import com.voice.platform.service.OAuthService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Alexa Smart Home 控制器
 * 处理 Alexa 的设备发现和控制请求
 */
@Slf4j
@RestController
@RequestMapping("/alexa")
public class AlexaController {
    
    @Autowired
    private OAuthService oauthService;
    
    @Autowired
    private DeviceService deviceService;
    
    /**
     * Alexa Smart Home 主入口
     * 处理所有 Alexa 请求
     */
    @PostMapping
    public ResponseEntity<?> handleRequest(@RequestBody AlexaRequest request) {
        
        String namespace = request.getDirective().getHeader().getNamespace();
        String name = request.getDirective().getHeader().getName();
        String messageId = request.getDirective().getHeader().getMessageId();
        
        log.info("收到 Alexa 请求: namespace={}, name={}, messageId={}", namespace, name, messageId);
        
        try {
            // 处理设备发现请求
            if ("Alexa.Discovery".equals(namespace) && "Discover".equals(name)) {
                return handleDiscovery(request);
            }
            
            // 验证 Token（非发现请求需要验证）
            String token = extractToken(request);
            if (token == null || !oauthService.validateAccessToken(token)) {
                log.warn("Token 验证失败: token={}", token);
                return ResponseEntity.ok(AlexaResponse.createErrorResponse(
                    messageId,
                    request.getDirective().getHeader().getCorrelationToken(),
                    request.getDirective().getEndpoint().getEndpointId(),
                    "INVALID_AUTHORIZATION_CREDENTIAL",
                    "访问令牌无效或已过期"
                ));
            }
            
            // 处理电源控制请求
            if ("Alexa.PowerController".equals(namespace)) {
                return handlePowerControl(request);
            }
            
            // 处理模式控制请求
            if ("Alexa.ModeController".equals(namespace)) {
                return handleModeControl(request);
            }
            
            // 不支持的请求
            log.warn("不支持的请求: namespace={}, name={}", namespace, name);
            return ResponseEntity.ok(AlexaResponse.createErrorResponse(
                messageId,
                request.getDirective().getHeader().getCorrelationToken(),
                request.getDirective().getEndpoint().getEndpointId(),
                "INVALID_DIRECTIVE",
                "不支持的操作"
            ));
            
        } catch (Exception e) {
            log.error("处理 Alexa 请求失败", e);
            return ResponseEntity.ok(AlexaResponse.createErrorResponse(
                messageId,
                request.getDirective().getHeader().getCorrelationToken(),
                request.getDirective().getEndpoint() != null ? 
                    request.getDirective().getEndpoint().getEndpointId() : null,
                "INTERNAL_ERROR",
                "服务器内部错误"
            ));
        }
    }
    
    /**
     * 处理设备发现请求
     */
    private ResponseEntity<?> handleDiscovery(AlexaRequest request) {
        String messageId = request.getDirective().getHeader().getMessageId();
        
        // 验证 Token
        String token = request.getDirective().getPayload().get("scope") != null ?
            (String) ((java.util.Map<?, ?>) request.getDirective().getPayload().get("scope")).get("token") : null;
        
        if (token == null || !oauthService.validateAccessToken(token)) {
            log.warn("Discovery Token 验证失败");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                AlexaResponse.createErrorResponse(messageId, null, null,
                    "INVALID_AUTHORIZATION_CREDENTIAL", "访问令牌无效")
            );
        }
        
        // 获取用户 ID（简化处理，实际应从 token 获取）
        Long userId = 1L;
        
        // 查询用户设备
        List<Device> devices = deviceService.findDevicesByUserId(userId);
        
        // 构建设备列表
        List<DiscoveredEndpoint> endpoints = new ArrayList<>();
        for (Device device : devices) {
            if ("robot_cleaner".equals(device.getDeviceType())) {
                endpoints.add(DiscoveredEndpoint.createVacuumEndpoint(
                    device.getDeviceId(),
                    device.getDeviceName()
                ));
            }
        }
        
        log.info("设备发现完成: userId={}, deviceCount={}", userId, endpoints.size());
        
        AlexaResponse response = AlexaResponse.createDiscoveryResponse(messageId, endpoints);
        return ResponseEntity.ok(response);
    }
    
    /**
     * 处理电源控制请求
     */
    private ResponseEntity<?> handlePowerControl(AlexaRequest request) {
        String name = request.getDirective().getHeader().getName();
        String messageId = request.getDirective().getHeader().getMessageId();
        String correlationToken = request.getDirective().getHeader().getCorrelationToken();
        String endpointId = request.getDirective().getEndpoint().getEndpointId();
        String token = extractToken(request);
        
        log.info("电源控制: name={}, endpointId={}", name, endpointId);
        
        // 查询设备
        Optional<Device> deviceOpt = deviceService.findDeviceByDeviceId(endpointId);
        if (!deviceOpt.isPresent()) {
            return ResponseEntity.ok(AlexaResponse.createErrorResponse(
                messageId, correlationToken, endpointId,
                "NO_SUCH_ENDPOINT", "设备不存在"
            ));
        }
        
        Device device = deviceOpt.get();
        
        // 检查设备是否在线
        if (!"online".equals(device.getStatus())) {
            return ResponseEntity.ok(AlexaResponse.createErrorResponse(
                messageId, correlationToken, endpointId,
                "ENDPOINT_UNREACHABLE", "设备离线"
            ));
        }
        
        // 执行操作
        String powerState;
        if ("TurnOn".equals(name)) {
            deviceService.turnOn(endpointId);
            powerState = "ON";
            log.info("设备开机成功: endpointId={}", endpointId);
        } else if ("TurnOff".equals(name)) {
            deviceService.turnOff(endpointId);
            powerState = "OFF";
            log.info("设备关机成功: endpointId={}", endpointId);
        } else {
            return ResponseEntity.ok(AlexaResponse.createErrorResponse(
                messageId, correlationToken, endpointId,
                "INVALID_DIRECTIVE", "不支持的操作"
            ));
        }
        
        // 构建响应
        List<AlexaResponse.Property> properties = new ArrayList<>();
        properties.add(AlexaResponse.createPowerStateProperty(powerState));
        
        AlexaResponse response = AlexaResponse.createControlResponse(
            UUID.randomUUID().toString(),
            correlationToken,
            endpointId,
            token,
            properties
        );
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * 处理模式控制请求
     */
    private ResponseEntity<?> handleModeControl(AlexaRequest request) {
        String name = request.getDirective().getHeader().getName();
        String messageId = request.getDirective().getHeader().getMessageId();
        String correlationToken = request.getDirective().getHeader().getCorrelationToken();
        String endpointId = request.getDirective().getEndpoint().getEndpointId();
        String token = extractToken(request);
        
        log.info("模式控制: name={}, endpointId={}", name, endpointId);
        
        // 查询设备
        Optional<Device> deviceOpt = deviceService.findDeviceByDeviceId(endpointId);
        if (!deviceOpt.isPresent()) {
            return ResponseEntity.ok(AlexaResponse.createErrorResponse(
                messageId, correlationToken, endpointId,
                "NO_SUCH_ENDPOINT", "设备不存在"
            ));
        }
        
        Device device = deviceOpt.get();
        
        // 检查设备是否在线
        if (!"online".equals(device.getStatus())) {
            return ResponseEntity.ok(AlexaResponse.createErrorResponse(
                messageId, correlationToken, endpointId,
                "ENDPOINT_UNREACHABLE", "设备离线"
            ));
        }
        
        // 获取模式值
        String mode = null;
        if ("SetMode".equals(name)) {
            mode = (String) request.getDirective().getPayload().get("mode");
        }
        
        if (mode == null) {
            return ResponseEntity.ok(AlexaResponse.createErrorResponse(
                messageId, correlationToken, endpointId,
                "INVALID_VALUE", "模式值无效"
            ));
        }
        
        // 设置模式
        deviceService.setMode(endpointId, mode.toLowerCase());
        log.info("设置模式成功: endpointId={}, mode={}", endpointId, mode);
        
        // 构建响应
        List<AlexaResponse.Property> properties = new ArrayList<>();
        properties.add(AlexaResponse.createModeProperty(mode));
        
        AlexaResponse response = AlexaResponse.createControlResponse(
            UUID.randomUUID().toString(),
            correlationToken,
            endpointId,
            token,
            properties
        );
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * 从请求中提取 Token
     */
    private String extractToken(AlexaRequest request) {
        if (request.getDirective().getEndpoint() != null &&
            request.getDirective().getEndpoint().getScope() != null) {
            return request.getDirective().getEndpoint().getScope().getToken();
        }
        return null;
    }
}
