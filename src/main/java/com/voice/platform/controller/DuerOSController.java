package com.voice.platform.controller;

import com.voice.platform.dto.DuerOSRequest;
import com.voice.platform.dto.DuerOSResponse;
import com.voice.platform.model.Device;
import com.voice.platform.repository.DeviceRepository;
import com.voice.platform.service.OAuthService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

/**
 * 小度音箱（DuerOS）智能家居控制器
 * 实现 DuerOS ConnectedHome API
 * 
 * 支持的功能:
 * - 设备发现 (Discovery)
 * - 设备控制 (Control)
 * - 设备查询 (Query)
 * 
 * @author Voice Platform Team
 * @version 1.0
 */
@Slf4j
@RestController
@RequestMapping("/dueros")
public class DuerOSController {
    
    @Autowired
    private OAuthService oauthService;
    
    @Autowired
    private DeviceRepository deviceRepository;
    
    /**
     * 设备发现接口
     * POST /dueros/discovery
     * 
     * @param authorization Authorization header (Bearer token)
     * @param request DuerOS 发现请求
     * @return 设备列表响应
     */
    @PostMapping("/discovery")
    public ResponseEntity<?> discovery(
            @RequestHeader("Authorization") String authorization,
            @RequestBody DuerOSRequest request) {
        
        String messageId = request.getHeader().getMessageId();
        
        log.info("=== 收到小度设备发现请求 ===");
        log.info("MessageId: {}", messageId);
        log.info("Namespace: {}", request.getHeader().getNamespace());
        log.info("Name: {}", request.getHeader().getName());
        
        try {
            // 验证 Token
            String accessToken = extractToken(authorization);
            if (accessToken == null || !oauthService.validateAccessToken(accessToken)) {
                log.warn("Token 验证失败: token={}", accessToken != null ? "exists" : "null");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(DuerOSResponse.error(
                                messageId,
                                "INVALID_TOKEN",
                                "访问令牌无效或已过期"));
            }
            
            // 获取用户ID（简化处理，实际应从 token 获取）
            // TODO: 从 token 中解析真实的 userId
            Long userId = 1L;
            
            // 查询用户设备
            List<Device> devices = deviceRepository.findByUserId(userId);
            log.info("查询到用户设备: userId={}, totalDevices={}", userId, devices.size());
            
            // 构建设备列表
            List<Map<String, Object>> discoveredAppliances = new ArrayList<>();
            
            for (Device device : devices) {
                if ("robot_cleaner".equals(device.getDeviceType())) {
                    Map<String, Object> appliance = buildApplianceInfo(device);
                    discoveredAppliances.add(appliance);
                    log.debug("添加设备: applianceId={}, friendlyName={}", 
                            device.getDeviceId(), device.getDeviceName());
                }
            }
            
            log.info("小度设备发现完成: userId={}, discoveredDevices={}", userId, discoveredAppliances.size());
            
            DuerOSResponse response = DuerOSResponse.discoveryResponse(
                messageId,
                discoveredAppliances
            );
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("设备发现失败: messageId={}", messageId, e);
            return ResponseEntity.ok(DuerOSResponse.error(
                messageId,
                "INTERNAL_ERROR",
                "服务器内部错误: " + e.getMessage()
            ));
        }
    }
    
    /**
     * 构建设备信息
     * 
     * @param device 设备实体
     * @return 设备信息 Map
     */
    private Map<String, Object> buildApplianceInfo(Device device) {
        Map<String, Object> appliance = new HashMap<>();
        
        // 基本信息
        appliance.put("applianceId", device.getDeviceId());
        appliance.put("manufacturerName", "Smart Home Demo");
        appliance.put("modelName", "智能扫地机器人");
        appliance.put("version", "1.0");
        appliance.put("friendlyName", device.getDeviceName());
        appliance.put("friendlyDescription", "智能扫地机器人，支持语音控制");
        appliance.put("isReachable", "online".equals(device.getStatus()));
        
        // 设备类型
        List<String> applianceTypes = new ArrayList<>();
        applianceTypes.add("ROBOT_CLEANER");
        appliance.put("applianceTypes", applianceTypes);
        
        // 支持的操作
        List<String> actions = Arrays.asList(
            "turnOn", "turnOff", "pause", "continue", "setMode", "getState"
        );
        appliance.put("actions", actions);
        
        // 设备属性
        Map<String, Object> additionalApplianceDetails = new HashMap<>();
        additionalApplianceDetails.put("powerState", device.getPowerState());
        additionalApplianceDetails.put("workMode", device.getWorkMode());
        additionalApplianceDetails.put("batteryLevel", device.getBatteryLevel());
        additionalApplianceDetails.put("status", device.getStatus());
        appliance.put("additionalApplianceDetails", additionalApplianceDetails);
        
        return appliance;
    }
    
    /**
     * 设备控制接口
     * POST /dueros/control
     * 
     * @param authorization Authorization header (Bearer token)
     * @param request DuerOS 控制请求
     * @return 控制响应
     */
    @PostMapping("/control")
    public ResponseEntity<?> control(
            @RequestHeader("Authorization") String authorization,
            @RequestBody DuerOSRequest request) {
        
        String action = request.getHeader().getName();
        String messageId = request.getHeader().getMessageId();
        
        log.info("=== 收到小度设备控制请求 ===");
        log.info("Action: {}", action);
        log.info("MessageId: {}", messageId);
        
        try {
            // 1. 验证 Token
            String accessToken = extractToken(authorization);
            if (accessToken == null || !oauthService.validateAccessToken(accessToken)) {
                log.warn("Token 验证失败: token={}", accessToken != null ? "exists" : "null");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(DuerOSResponse.error(messageId, "INVALID_TOKEN", "访问令牌无效或已过期"));
            }
            
            // 2. 获取设备ID
            String deviceId = request.getPayload().getAppliance().getApplianceId();
            log.info("设备ID: {}", deviceId);
            
            // 3. 查询设备
            Optional<Device> deviceOpt = deviceRepository.findByDeviceId(deviceId);
            if (!deviceOpt.isPresent()) {
                log.warn("设备不存在: deviceId={}", deviceId);
                return ResponseEntity.ok(
                    DuerOSResponse.error(messageId, "DEVICE_NOT_FOUND", "设备不存在")
                );
            }
            
            Device device = deviceOpt.get();
            log.info("找到设备: deviceName={}, currentPowerState={}, status={}", 
                    device.getDeviceName(), device.getPowerState(), device.getStatus());
            
            // 4. 检查设备是否在线
            if (!"online".equals(device.getStatus())) {
                log.warn("设备离线: deviceId={}, status={}", deviceId, device.getStatus());
                return ResponseEntity.ok(
                    DuerOSResponse.error(messageId, "DEVICE_OFFLINE", "设备离线或无法访问")
                );
            }
            
            // 5. 执行控制操作
            DuerOSResponse response = executeControl(action, messageId, device, request);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("设备控制失败: action={}, messageId={}", action, messageId, e);
            return ResponseEntity.ok(DuerOSResponse.error(
                messageId,
                "INTERNAL_ERROR",
                "服务器内部错误: " + e.getMessage()
            ));
        }
    }
    
    /**
     * 执行具体的控制操作
     * 
     * @param action 操作类型
     * @param messageId 消息ID
     * @param device 设备对象
     * @param request 请求对象
     * @return 控制响应
     */
    private DuerOSResponse executeControl(String action, String messageId, Device device, DuerOSRequest request) {
        String deviceId = device.getDeviceId();
        DuerOSResponse response;
        
        switch (action) {
            case "TurnOnRequest":
                device.setPowerState("on");
                deviceRepository.save(device);
                response = DuerOSResponse.controlConfirmation("TurnOn", messageId);
                log.info("✓ 设备开机成功: deviceId={}, deviceName={}", deviceId, device.getDeviceName());
                break;
                
            case "TurnOffRequest":
                device.setPowerState("off");
                deviceRepository.save(device);
                response = DuerOSResponse.controlConfirmation("TurnOff", messageId);
                log.info("✓ 设备关机成功: deviceId={}, deviceName={}", deviceId, device.getDeviceName());
                break;
                
            case "PauseRequest":
                // 暂停操作（扫地机器人暂停清扫）
                response = DuerOSResponse.controlConfirmation("Pause", messageId);
                log.info("✓ 设备暂停成功: deviceId={}, deviceName={}", deviceId, device.getDeviceName());
                break;
                
            case "ContinueRequest":
                // 继续操作（扫地机器人继续清扫）
                response = DuerOSResponse.controlConfirmation("Continue", messageId);
                log.info("✓ 设备继续成功: deviceId={}, deviceName={}", deviceId, device.getDeviceName());
                break;
                
            case "SetModeRequest":
                // 从 additionalInfo 中获取模式
                Map<String, Object> additionalInfo = request.getPayload().getAdditionalInfo();
                if (additionalInfo != null && additionalInfo.containsKey("mode")) {
                    String mode = additionalInfo.get("mode").toString().toLowerCase();
                    
                    // 验证模式值
                    if (!isValidMode(mode)) {
                        log.warn("不支持的模式: mode={}", mode);
                        return DuerOSResponse.error(messageId, "INVALID_MODE", 
                            "不支持的模式: " + mode + "。支持的模式: auto, spot, edge");
                    }
                    
                    device.setWorkMode(mode);
                    deviceRepository.save(device);
                    log.info("✓ 模式设置成功: deviceId={}, deviceName={}, newMode={}", 
                            deviceId, device.getDeviceName(), mode);
                } else {
                    log.warn("SetModeRequest 缺少 mode 参数");
                    return DuerOSResponse.error(messageId, "MISSING_PARAMETER", "缺少 mode 参数");
                }
                response = DuerOSResponse.controlConfirmation("SetMode", messageId);
                break;
                
            case "GetStateRequest":
                // 查询设备状态
                response = buildStateResponse(messageId, device);
                log.info("✓ 状态查询成功: deviceId={}, powerState={}, workMode={}", 
                        deviceId, device.getPowerState(), device.getWorkMode());
                break;
                
            default:
                response = DuerOSResponse.error(messageId, "UNSUPPORTED_ACTION", "不支持的操作: " + action);
                log.warn("不支持的操作: action={}", action);
        }
        
        return response;
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
     * 构建状态查询响应
     * 
     * @param messageId 消息ID
     * @param device 设备对象
     * @return 状态响应
     */
    private DuerOSResponse buildStateResponse(String messageId, Device device) {
        DuerOSResponse response = DuerOSResponse.success(
            "DuerOS.ConnectedHome.Query",
            "GetStateResponse",
            messageId
        );
        
        Map<String, Object> deviceState = new HashMap<>();
        deviceState.put("powerState", device.getPowerState());
        deviceState.put("workMode", device.getWorkMode());
        deviceState.put("batteryLevel", device.getBatteryLevel());
        deviceState.put("status", device.getStatus());
        
        response.getPayload().put("deviceState", deviceState);
        
        return response;
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
