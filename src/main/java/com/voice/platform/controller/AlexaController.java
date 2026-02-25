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

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * Alexa Smart Home 控制器
 * 实现 Alexa Smart Home Skill API v3
 * 
 * 支持的接口:
 * - Alexa.Discovery: 设备发现
 * - Alexa.PowerController: 电源控制
 * - Alexa.ModeController: 模式控制
 * - Alexa.ReportState: 状态报告
 * - Alexa.Authorization: 授权管理
 * 
 * @author Voice Platform Team
 * @version 1.0
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
     * 
     * @param request Alexa 请求对象
     * @return ResponseEntity 包含 Alexa 响应
     */
    @PostMapping
    public ResponseEntity<?> handleRequest(@RequestBody AlexaRequest request) {
        
        String namespace = request.getDirective().getHeader().getNamespace();
        String name = request.getDirective().getHeader().getName();
        String messageId = request.getDirective().getHeader().getMessageId();
        
        log.info("=== 收到 Alexa 请求 ===");
        log.info("Namespace: {}", namespace);
        log.info("Name: {}", name);
        log.info("MessageId: {}", messageId);
        
        try {
            // 1. 处理设备发现请求 (不需要 endpoint)
            if ("Alexa.Discovery".equals(namespace) && "Discover".equals(name)) {
                log.info("处理设备发现请求");
                return handleDiscovery(request);
            }
            
            // 2. 处理授权请求 (AcceptGrant)
            if ("Alexa.Authorization".equals(namespace) && "AcceptGrant".equals(name)) {
                log.info("处理授权接受请求");
                return handleAcceptGrant(request);
            }
            
            // 3. 验证 Token（其他请求需要验证）
            String token = extractToken(request);
            if (token == null || !oauthService.validateAccessToken(token)) {
                log.warn("Token 验证失败: token={}", token != null ? token.substring(0, Math.min(10, token.length())) + "..." : "null");
                return ResponseEntity.ok(AlexaResponse.createErrorResponse(
                    messageId,
                    request.getDirective().getHeader().getCorrelationToken(),
                    request.getDirective().getEndpoint() != null ? 
                        request.getDirective().getEndpoint().getEndpointId() : null,
                    "INVALID_AUTHORIZATION_CREDENTIAL",
                    "访问令牌无效或已过期"
                ));
            }
            
            // 4. 处理电源控制请求
            if ("Alexa.PowerController".equals(namespace)) {
                log.info("处理电源控制请求: {}", name);
                return handlePowerControl(request);
            }
            
            // 5. 处理模式控制请求
            if ("Alexa.ModeController".equals(namespace)) {
                log.info("处理模式控制请求: {}", name);
                return handleModeControl(request);
            }
            
            // 6. 处理状态报告请求
            if ("Alexa".equals(namespace) && "ReportState".equals(name)) {
                log.info("处理状态报告请求");
                return handleReportState(request);
            }
            
            // 7. 不支持的请求
            log.warn("不支持的请求: namespace={}, name={}", namespace, name);
            return ResponseEntity.ok(AlexaResponse.createErrorResponse(
                messageId,
                request.getDirective().getHeader().getCorrelationToken(),
                request.getDirective().getEndpoint() != null ? 
                    request.getDirective().getEndpoint().getEndpointId() : null,
                "INVALID_DIRECTIVE",
                "不支持的操作: " + namespace + "." + name
            ));
            
        } catch (Exception e) {
            log.error("处理 Alexa 请求失败: namespace={}, name={}", namespace, name, e);
            return ResponseEntity.ok(AlexaResponse.createErrorResponse(
                messageId,
                request.getDirective().getHeader().getCorrelationToken(),
                request.getDirective().getEndpoint() != null ? 
                    request.getDirective().getEndpoint().getEndpointId() : null,
                "INTERNAL_ERROR",
                "服务器内部错误: " + e.getMessage()
            ));
        }
    }
    
    /**
     * 处理设备发现请求
     * Alexa.Discovery.Discover
     * 
     * @param request 发现请求
     * @return 设备列表响应
     */
    private ResponseEntity<?> handleDiscovery(AlexaRequest request) {
        String messageId = request.getDirective().getHeader().getMessageId();
        
        try {
            // 验证 Token (从 payload.scope 中获取)
            String token = null;
            if (request.getDirective().getPayload() != null && 
                request.getDirective().getPayload().get("scope") != null) {
                @SuppressWarnings("unchecked")
                Map<String, Object> scope = (Map<String, Object>) request.getDirective().getPayload().get("scope");
                token = (String) scope.get("token");
            }
            
            if (token == null || !oauthService.validateAccessToken(token)) {
                log.warn("Discovery Token 验证失败: token={}", token != null ? "exists" : "null");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                    AlexaResponse.createErrorResponse(messageId, null, null,
                        "INVALID_AUTHORIZATION_CREDENTIAL", "访问令牌无效或已过期")
                );
            }
            
            // 获取用户 ID（简化处理，实际应从 token 解析）
            // TODO: 从 token 中解析真实的 userId
            Long userId = 1L;
            
            // 查询用户设备
            List<Device> devices = deviceService.findDevicesByUserId(userId);
            log.info("查询到用户设备: userId={}, totalDevices={}", userId, devices.size());
            
            // 构建设备列表
            List<DiscoveredEndpoint> endpoints = new ArrayList<>();
            for (Device device : devices) {
                if ("robot_cleaner".equals(device.getDeviceType())) {
                    endpoints.add(DiscoveredEndpoint.createVacuumEndpoint(
                        device.getDeviceId(),
                        device.getDeviceName()
                    ));
                    log.debug("添加设备: deviceId={}, deviceName={}", device.getDeviceId(), device.getDeviceName());
                }
            }
            
            log.info("设备发现完成: userId={}, discoveredDevices={}", userId, endpoints.size());
            
            AlexaResponse response = AlexaResponse.createDiscoveryResponse(messageId, endpoints);
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("设备发现失败: messageId={}", messageId, e);
            return ResponseEntity.ok(AlexaResponse.createErrorResponse(
                messageId, null, null,
                "INTERNAL_ERROR", "设备发现失败: " + e.getMessage()
            ));
        }
    }
    
    /**
     * 处理电源控制请求
     * 支持 TurnOn 和 TurnOff 指令
     * 
     * @param request 电源控制请求
     * @return 控制响应
     */
    private ResponseEntity<?> handlePowerControl(AlexaRequest request) {
        String name = request.getDirective().getHeader().getName();
        String messageId = request.getDirective().getHeader().getMessageId();
        String correlationToken = request.getDirective().getHeader().getCorrelationToken();
        String endpointId = request.getDirective().getEndpoint().getEndpointId();
        String token = extractToken(request);
        
        log.info("=== 电源控制请求 ===");
        log.info("操作: {}", name);
        log.info("设备ID: {}", endpointId);
        
        try {
            // 1. 查询设备
            Optional<Device> deviceOpt = deviceService.findDeviceByDeviceId(endpointId);
            if (!deviceOpt.isPresent()) {
                log.warn("设备不存在: endpointId={}", endpointId);
                return ResponseEntity.ok(AlexaResponse.createErrorResponse(
                    messageId, correlationToken, endpointId,
                    "NO_SUCH_ENDPOINT", "设备不存在"
                ));
            }
            
            Device device = deviceOpt.get();
            log.info("找到设备: deviceName={}, currentPowerState={}, status={}", 
                    device.getDeviceName(), device.getPowerState(), device.getStatus());
            
            // 2. 检查设备是否在线
            if (!"online".equals(device.getStatus())) {
                log.warn("设备离线: endpointId={}, status={}", endpointId, device.getStatus());
                return ResponseEntity.ok(AlexaResponse.createErrorResponse(
                    messageId, correlationToken, endpointId,
                    "ENDPOINT_UNREACHABLE", "设备离线或无法访问"
                ));
            }
            
            // 3. 执行操作
            String powerState;
            if ("TurnOn".equals(name)) {
                deviceService.turnOn(endpointId);
                powerState = "ON";
                log.info("✓ 设备开机成功: endpointId={}, deviceName={}", endpointId, device.getDeviceName());
            } else if ("TurnOff".equals(name)) {
                deviceService.turnOff(endpointId);
                powerState = "OFF";
                log.info("✓ 设备关机成功: endpointId={}, deviceName={}", endpointId, device.getDeviceName());
            } else {
                log.warn("不支持的电源操作: name={}", name);
                return ResponseEntity.ok(AlexaResponse.createErrorResponse(
                    messageId, correlationToken, endpointId,
                    "INVALID_DIRECTIVE", "不支持的电源操作: " + name
                ));
            }
            
            // 4. 构建响应属性
            List<AlexaResponse.Property> properties = new ArrayList<>();
            
            // 电源状态属性
            properties.add(AlexaResponse.Property.builder()
                .namespace("Alexa.PowerController")
                .name("powerState")
                .value(powerState)
                .timeOfSample(Instant.now().toString())
                .uncertaintyInMilliseconds(500)
                .build());
            
            // 连接状态属性
            Map<String, Object> connectivity = new HashMap<>();
            connectivity.put("value", "OK");
            properties.add(AlexaResponse.Property.builder()
                .namespace("Alexa.EndpointHealth")
                .name("connectivity")
                .value(connectivity)
                .timeOfSample(Instant.now().toString())
                .uncertaintyInMilliseconds(500)
                .build());
            
            // 5. 构建响应
            AlexaResponse response = AlexaResponse.createControlResponse(
                UUID.randomUUID().toString(),
                correlationToken,
                endpointId,
                token,
                properties
            );
            
            log.info("电源控制响应已生成: powerState={}", powerState);
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("电源控制失败: endpointId={}, operation={}", endpointId, name, e);
            return ResponseEntity.ok(AlexaResponse.createErrorResponse(
                messageId, correlationToken, endpointId,
                "INTERNAL_ERROR", "设备控制失败: " + e.getMessage()
            ));
        }
    }
    
    /**
     * 处理模式控制请求
     * 支持 SetMode 和 AdjustMode 指令
     * 
     * @param request 模式控制请求
     * @return 控制响应
     */
    private ResponseEntity<?> handleModeControl(AlexaRequest request) {
        String name = request.getDirective().getHeader().getName();
        String messageId = request.getDirective().getHeader().getMessageId();
        String correlationToken = request.getDirective().getHeader().getCorrelationToken();
        String endpointId = request.getDirective().getEndpoint().getEndpointId();
        String token = extractToken(request);
        
        log.info("=== 模式控制请求 ===");
        log.info("操作: {}", name);
        log.info("设备ID: {}", endpointId);
        
        try {
            // 1. 查询设备
            Optional<Device> deviceOpt = deviceService.findDeviceByDeviceId(endpointId);
            if (!deviceOpt.isPresent()) {
                log.warn("设备不存在: endpointId={}", endpointId);
                return ResponseEntity.ok(AlexaResponse.createErrorResponse(
                    messageId, correlationToken, endpointId,
                    "NO_SUCH_ENDPOINT", "设备不存在"
                ));
            }
            
            Device device = deviceOpt.get();
            log.info("找到设备: deviceName={}, currentMode={}, status={}", 
                    device.getDeviceName(), device.getWorkMode(), device.getStatus());
            
            // 2. 检查设备是否在线
            if (!"online".equals(device.getStatus())) {
                log.warn("设备离线: endpointId={}, status={}", endpointId, device.getStatus());
                return ResponseEntity.ok(AlexaResponse.createErrorResponse(
                    messageId, correlationToken, endpointId,
                    "ENDPOINT_UNREACHABLE", "设备离线或无法访问"
                ));
            }
            
            // 3. 获取模式值
            String mode = null;
            if ("SetMode".equals(name)) {
                mode = (String) request.getDirective().getPayload().get("mode");
                log.info("请求设置模式: mode={}", mode);
            } else if ("AdjustMode".equals(name)) {
                // AdjustMode 用于循环切换模式
                Integer modeDelta = (Integer) request.getDirective().getPayload().get("modeDelta");
                log.info("请求调整模式: modeDelta={}", modeDelta);
                // 简化处理：循环切换 Auto -> Spot -> Edge -> Auto
                String currentMode = device.getWorkMode();
                mode = getNextMode(currentMode, modeDelta != null ? modeDelta : 1);
            }
            
            if (mode == null || mode.isEmpty()) {
                log.warn("模式值无效: mode={}", mode);
                return ResponseEntity.ok(AlexaResponse.createErrorResponse(
                    messageId, correlationToken, endpointId,
                    "INVALID_VALUE", "模式值无效或缺失"
                ));
            }
            
            // 4. 验证模式值
            String normalizedMode = mode.toLowerCase();
            if (!isValidMode(normalizedMode)) {
                log.warn("不支持的模式: mode={}", mode);
                return ResponseEntity.ok(AlexaResponse.createErrorResponse(
                    messageId, correlationToken, endpointId,
                    "INVALID_VALUE", "不支持的模式: " + mode + "。支持的模式: Auto, Spot, Edge"
                ));
            }
            
            // 5. 设置模式
            deviceService.setMode(endpointId, normalizedMode);
            log.info("✓ 模式设置成功: endpointId={}, deviceName={}, newMode={}", 
                    endpointId, device.getDeviceName(), normalizedMode);
            
            // 6. 构建响应属性
            List<AlexaResponse.Property> properties = new ArrayList<>();
            
            // 模式属性
            properties.add(AlexaResponse.Property.builder()
                .namespace("Alexa.ModeController")
                .name("mode")
                .value(capitalizeFirst(normalizedMode))
                .timeOfSample(Instant.now().toString())
                .uncertaintyInMilliseconds(500)
                .build());
            
            // 连接状态属性
            Map<String, Object> connectivity = new HashMap<>();
            connectivity.put("value", "OK");
            properties.add(AlexaResponse.Property.builder()
                .namespace("Alexa.EndpointHealth")
                .name("connectivity")
                .value(connectivity)
                .timeOfSample(Instant.now().toString())
                .uncertaintyInMilliseconds(500)
                .build());
            
            // 7. 构建响应
            AlexaResponse response = AlexaResponse.createControlResponse(
                UUID.randomUUID().toString(),
                correlationToken,
                endpointId,
                token,
                properties
            );
            
            log.info("模式控制响应已生成: mode={}", capitalizeFirst(normalizedMode));
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("模式控制失败: endpointId={}, operation={}", endpointId, name, e);
            return ResponseEntity.ok(AlexaResponse.createErrorResponse(
                messageId, correlationToken, endpointId,
                "INTERNAL_ERROR", "模式设置失败: " + e.getMessage()
            ));
        }
    }
    
    /**
     * 验证模式是否有效
     * 
     * @param mode 模式值（小写）
     * @return 是否有效
     */
    private boolean isValidMode(String mode) {
        return "auto".equals(mode) || "spot".equals(mode) || "edge".equals(mode);
    }
    
    /**
     * 获取下一个模式（用于 AdjustMode）
     * 
     * @param currentMode 当前模式
     * @param delta 调整量
     * @return 下一个模式
     */
    private String getNextMode(String currentMode, int delta) {
        String[] modes = {"auto", "spot", "edge"};
        int currentIndex = 0;
        
        // 查找当前模式的索引
        for (int i = 0; i < modes.length; i++) {
            if (modes[i].equalsIgnoreCase(currentMode)) {
                currentIndex = i;
                break;
            }
        }
        
        // 计算下一个模式的索引（循环）
        int nextIndex = (currentIndex + delta) % modes.length;
        if (nextIndex < 0) {
            nextIndex += modes.length;
        }
        
        return modes[nextIndex];
    }
    
    /**
     * 从请求中提取 Token
     * 
     * @param request Alexa 请求
     * @return 访问令牌，如果不存在返回 null
     */
    private String extractToken(AlexaRequest request) {
        if (request.getDirective().getEndpoint() != null &&
            request.getDirective().getEndpoint().getScope() != null) {
            return request.getDirective().getEndpoint().getScope().getToken();
        }
        return null;
    }
    
    /**
     * 处理授权接受请求 (AcceptGrant)
     * 当用户在 Alexa App 中启用技能时调用
     * 
     * @param request 授权请求
     * @return 授权响应
     */
    private ResponseEntity<?> handleAcceptGrant(AlexaRequest request) {
        String messageId = request.getDirective().getHeader().getMessageId();
        
        try {
            log.info("处理 AcceptGrant 请求: messageId={}", messageId);
            
            // 从 payload 中获取授权码
            Map<String, Object> payload = request.getDirective().getPayload();
            @SuppressWarnings("unchecked")
            Map<String, Object> grant = (Map<String, Object>) payload.get("grant");
            
            if (grant != null) {
                String code = (String) grant.get("code");
                String type = (String) grant.get("type");
                log.info("收到授权: type={}, code={}", type, code != null ? code.substring(0, Math.min(10, code.length())) + "..." : "null");
                
                // TODO: 存储授权信息，用于后续的 proactive state reporting
                // 这里可以将授权码交换为访问令牌并存储
            }
            
            // 返回成功响应
            Map<String, Object> responsePayload = new HashMap<>();
            
            AlexaResponse response = AlexaResponse.builder()
                .event(AlexaResponse.Event.builder()
                    .header(AlexaResponse.Header.builder()
                        .namespace("Alexa.Authorization")
                        .name("AcceptGrant.Response")
                        .payloadVersion("3")
                        .messageId(UUID.randomUUID().toString())
                        .build())
                    .payload(responsePayload)
                    .build())
                .build();
            
            log.info("AcceptGrant 处理成功");
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("AcceptGrant 处理失败", e);
            return ResponseEntity.ok(AlexaResponse.createErrorResponse(
                messageId, null, null,
                "ACCEPT_GRANT_FAILED", "授权接受失败: " + e.getMessage()
            ));
        }
    }
    
    /**
     * 处理状态报告请求 (ReportState)
     * 当用户询问设备状态时调用
     * 
     * @param request 状态报告请求
     * @return 状态报告响应
     */
    private ResponseEntity<?> handleReportState(AlexaRequest request) {
        String messageId = request.getDirective().getHeader().getMessageId();
        String correlationToken = request.getDirective().getHeader().getCorrelationToken();
        String endpointId = request.getDirective().getEndpoint().getEndpointId();
        String token = extractToken(request);
        
        log.info("状态报告请求: endpointId={}", endpointId);
        
        try {
            // 查询设备
            Optional<Device> deviceOpt = deviceService.findDeviceByDeviceId(endpointId);
            if (!deviceOpt.isPresent()) {
                log.warn("设备不存在: endpointId={}", endpointId);
                return ResponseEntity.ok(AlexaResponse.createErrorResponse(
                    messageId, correlationToken, endpointId,
                    "NO_SUCH_ENDPOINT", "设备不存在"
                ));
            }
            
            Device device = deviceOpt.get();
            
            // 构建设备状态属性列表
            List<AlexaResponse.Property> properties = new ArrayList<>();
            
            // 1. 电源状态
            String powerState = "on".equalsIgnoreCase(device.getPowerState()) ? "ON" : "OFF";
            properties.add(AlexaResponse.Property.builder()
                .namespace("Alexa.PowerController")
                .name("powerState")
                .value(powerState)
                .timeOfSample(Instant.now().toString())
                .uncertaintyInMilliseconds(500)
                .build());
            
            // 2. 工作模式
            if (device.getWorkMode() != null) {
                String mode = capitalizeFirst(device.getWorkMode());
                properties.add(AlexaResponse.Property.builder()
                    .namespace("Alexa.ModeController")
                    .name("mode")
                    .value(mode)
                    .timeOfSample(Instant.now().toString())
                    .uncertaintyInMilliseconds(500)
                    .build());
            }
            
            // 3. 连接状态
            Map<String, Object> connectivity = new HashMap<>();
            connectivity.put("value", "online".equals(device.getStatus()) ? "OK" : "UNREACHABLE");
            properties.add(AlexaResponse.Property.builder()
                .namespace("Alexa.EndpointHealth")
                .name("connectivity")
                .value(connectivity)
                .timeOfSample(Instant.now().toString())
                .uncertaintyInMilliseconds(500)
                .build());
            
            log.info("状态报告完成: endpointId={}, powerState={}, mode={}, status={}", 
                    endpointId, powerState, device.getWorkMode(), device.getStatus());
            
            // 构建 StateReport 响应
            AlexaResponse response = AlexaResponse.builder()
                .event(AlexaResponse.Event.builder()
                    .header(AlexaResponse.Header.builder()
                        .namespace("Alexa")
                        .name("StateReport")
                        .payloadVersion("3")
                        .messageId(UUID.randomUUID().toString())
                        .correlationToken(correlationToken)
                        .build())
                    .endpoint(AlexaResponse.Endpoint.builder()
                        .scope(AlexaResponse.Scope.builder()
                            .type("BearerToken")
                            .token(token)
                            .build())
                        .endpointId(endpointId)
                        .build())
                    .payload(new HashMap<>())
                    .build())
                .context(AlexaResponse.Context.builder()
                    .properties(properties)
                    .build())
                .build();
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("状态报告失败: endpointId={}", endpointId, e);
            return ResponseEntity.ok(AlexaResponse.createErrorResponse(
                messageId, correlationToken, endpointId,
                "INTERNAL_ERROR", "状态查询失败: " + e.getMessage()
            ));
        }
    }
    
    /**
     * 首字母大写
     * 
     * @param str 输入字符串
     * @return 首字母大写的字符串
     */
    private String capitalizeFirst(String str) {
        if (str == null || str.isEmpty()) {
            return str;
        }
        return str.substring(0, 1).toUpperCase() + str.substring(1).toLowerCase();
    }
}
