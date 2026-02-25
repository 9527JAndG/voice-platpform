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
     */
    @PostMapping("/discovery")
    public ResponseEntity<?> discovery(
            @RequestHeader("Authorization") String authorization,
            @RequestBody DuerOSRequest request) {
        
        log.info("小度设备发现请求: messageId={}", request.getHeader().getMessageId());
        
        // 验证 Token
        String accessToken = extractToken(authorization);
        if (!oauthService.validateAccessToken(accessToken)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(DuerOSResponse.error(
                            request.getHeader().getMessageId(),
                            "INVALID_TOKEN",
                            "访问令牌无效或已过期"));
        }
        
        // 获取用户ID（简化处理，实际应从 token 获取）
        Long userId = 1L;
        
        // 查询用户设备
        List<Device> devices = deviceRepository.findByUserId(userId);
        
        // 构建设备列表
        List<Map<String, Object>> discoveredAppliances = new ArrayList<>();
        
        for (Device device : devices) {
            Map<String, Object> appliance = new HashMap<>();
            appliance.put("applianceId", device.getDeviceId());
            appliance.put("manufacturerName", "自定义品牌");
            appliance.put("modelName", "智能扫地机器人");
            appliance.put("version", "1.0");
            appliance.put("friendlyName", device.getDeviceName());
            appliance.put("friendlyDescription", "智能扫地机器人，支持语音控制");
            appliance.put("isReachable", "online".equals(device.getStatus()));
            
            // 设备类型
            List<String> applianceTypes = new ArrayList<>();
            if ("robot_cleaner".equals(device.getDeviceType())) {
                applianceTypes.add("ROBOT_CLEANER");
            }
            appliance.put("applianceTypes", applianceTypes);
            
            // 支持的操作
            List<String> actions = Arrays.asList(
                "turnOn", "turnOff", "pause", "continue", "setMode"
            );
            appliance.put("actions", actions);
            
            // 设备属性
            Map<String, Object> additionalApplianceDetails = new HashMap<>();
            additionalApplianceDetails.put("powerState", device.getPowerState());
            additionalApplianceDetails.put("workMode", device.getWorkMode());
            additionalApplianceDetails.put("batteryLevel", device.getBatteryLevel());
            appliance.put("additionalApplianceDetails", additionalApplianceDetails);
            
            discoveredAppliances.add(appliance);
        }
        
        log.info("小度设备发现完成: userId={}, deviceCount={}", userId, discoveredAppliances.size());
        
        DuerOSResponse response = DuerOSResponse.discoveryResponse(
            request.getHeader().getMessageId(),
            discoveredAppliances
        );
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * 设备控制接口
     * POST /dueros/control
     */
    @PostMapping("/control")
    public ResponseEntity<?> control(
            @RequestHeader("Authorization") String authorization,
            @RequestBody DuerOSRequest request) {
        
        String action = request.getHeader().getName();
        String messageId = request.getHeader().getMessageId();
        
        log.info("小度设备控制请求: action={}, messageId={}", action, messageId);
        
        // 验证 Token
        String accessToken = extractToken(authorization);
        if (!oauthService.validateAccessToken(accessToken)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(DuerOSResponse.error(messageId, "INVALID_TOKEN", "访问令牌无效或已过期"));
        }
        
        // 获取设备ID
        String deviceId = request.getPayload().getAppliance().getApplianceId();
        
        // 查询设备
        Optional<Device> deviceOpt = deviceRepository.findByDeviceId(deviceId);
        if (!deviceOpt.isPresent()) {
            return ResponseEntity.ok(
                DuerOSResponse.error(messageId, "DEVICE_NOT_FOUND", "设备不存在")
            );
        }
        
        Device device = deviceOpt.get();
        
        // 执行控制操作
        DuerOSResponse response;
        
        switch (action) {
            case "TurnOnRequest":
                device.setPowerState("on");
                deviceRepository.save(device);
                response = DuerOSResponse.controlConfirmation("TurnOn", messageId);
                log.info("小度设备开机: deviceId={}, deviceName={}", deviceId, device.getDeviceName());
                break;
                
            case "TurnOffRequest":
                device.setPowerState("off");
                deviceRepository.save(device);
                response = DuerOSResponse.controlConfirmation("TurnOff", messageId);
                log.info("小度设备关机: deviceId={}, deviceName={}", deviceId, device.getDeviceName());
                break;
                
            case "PauseRequest":
                response = DuerOSResponse.controlConfirmation("Pause", messageId);
                log.info("小度设备暂停: deviceId={}", deviceId);
                break;
                
            case "ContinueRequest":
                response = DuerOSResponse.controlConfirmation("Continue", messageId);
                log.info("小度设备继续: deviceId={}", deviceId);
                break;
                
            case "SetModeRequest":
                // 从 additionalInfo 中获取模式
                Map<String, Object> additionalInfo = request.getPayload().getAdditionalInfo();
                if (additionalInfo != null && additionalInfo.containsKey("mode")) {
                    String mode = additionalInfo.get("mode").toString();
                    device.setWorkMode(mode);
                    deviceRepository.save(device);
                    log.info("小度设置模式: deviceId={}, mode={}", deviceId, mode);
                }
                response = DuerOSResponse.controlConfirmation("SetMode", messageId);
                break;
                
            default:
                response = DuerOSResponse.error(messageId, "UNSUPPORTED_ACTION", "不支持的操作: " + action);
                log.warn("小度不支持的操作: action={}", action);
        }
        
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
