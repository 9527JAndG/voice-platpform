package com.voice.platform.controller;

import com.voice.platform.dto.MiAIRequest;
import com.voice.platform.dto.MiAIResponse;
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
 * 小爱同学智能家居控制器
 * 小爱的接口格式最简洁，使用扁平的 JSON 结构
 */
@Slf4j
@RestController
@RequestMapping("/miai")
public class MiAIController {
    
    @Autowired
    private OAuthService oauthService;
    
    @Autowired
    private DeviceRepository deviceRepository;
    
    /**
     * 设备发现接口
     * POST /miai/discovery
     */
    @PostMapping("/discovery")
    public ResponseEntity<?> discovery(
            @RequestHeader("Authorization") String authorization) {
        
        log.info("小爱设备发现请求");
        
        // 验证 Token
        String accessToken = extractToken(authorization);
        if (!oauthService.validateAccessToken(accessToken)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(MiAIResponse.unauthorized());
        }
        
        // 获取用户ID（简化处理，实际应从 token 获取）
        Long userId = 1L;
        
        // 查询用户设备
        List<Device> devices = deviceRepository.findByUserId(userId);
        
        // 构建设备列表
        List<Map<String, Object>> deviceList = new ArrayList<>();
        
        for (Device device : devices) {
            Map<String, Object> deviceInfo = new HashMap<>();
            deviceInfo.put("device_id", device.getDeviceId());
            deviceInfo.put("device_name", device.getDeviceName());
            
            // 小爱的设备类型命名：使用小写+连字符
            if ("robot_cleaner".equals(device.getDeviceType())) {
                deviceInfo.put("device_type", "vacuum-cleaner");
            } else {
                deviceInfo.put("device_type", device.getDeviceType());
            }
            
            deviceInfo.put("online", "online".equals(device.getStatus()));
            deviceInfo.put("room", "客厅");
            
            // 设备属性
            Map<String, Object> properties = new HashMap<>();
            properties.put("power_state", device.getPowerState());
            properties.put("work_mode", device.getWorkMode());
            properties.put("battery_level", device.getBatteryLevel());
            deviceInfo.put("properties", properties);
            
            // 支持的操作（小爱使用小写+连字符）
            List<String> actions = Arrays.asList(
                "turn-on", "turn-off", "pause", "continue", "set-mode", "query"
            );
            deviceInfo.put("actions", actions);
            
            deviceList.add(deviceInfo);
        }
        
        log.info("小爱设备发现完成: userId={}, deviceCount={}", userId, deviceList.size());
        
        return ResponseEntity.ok(MiAIResponse.discoveryResponse(deviceList));
    }
    
    /**
     * 设备控制接口
     * POST /miai/control
     */
    @PostMapping("/control")
    public ResponseEntity<?> control(
            @RequestHeader("Authorization") String authorization,
            @RequestBody MiAIRequest request) {
        
        String intent = request.getIntent();
        String deviceId = request.getDeviceId();
        String requestId = request.getRequestId();
        
        log.info("小爱设备控制请求: intent={}, deviceId={}, requestId={}", 
                intent, deviceId, requestId);
        
        // 验证 Token
        String accessToken = extractToken(authorization);
        if (!oauthService.validateAccessToken(accessToken)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(MiAIResponse.unauthorized());
        }
        
        // 验证请求参数
        if (intent == null || deviceId == null) {
            return ResponseEntity.ok(MiAIResponse.badRequest("缺少必要参数"));
        }
        
        // 查询设备
        Optional<Device> deviceOpt = deviceRepository.findByDeviceId(deviceId);
        if (!deviceOpt.isPresent()) {
            return ResponseEntity.ok(MiAIResponse.deviceNotFound());
        }
        
        Device device = deviceOpt.get();
        
        // 执行控制操作
        MiAIResponse response;
        
        switch (intent) {
            case "turn-on":
                device.setPowerState("on");
                deviceRepository.save(device);
                response = MiAIResponse.success();
                log.info("小爱设备开机: deviceId={}, deviceName={}", deviceId, device.getDeviceName());
                break;
                
            case "turn-off":
                device.setPowerState("off");
                deviceRepository.save(device);
                response = MiAIResponse.success();
                log.info("小爱设备关机: deviceId={}, deviceName={}", deviceId, device.getDeviceName());
                break;
                
            case "pause":
                response = MiAIResponse.success();
                log.info("小爱设备暂停: deviceId={}", deviceId);
                break;
                
            case "continue":
                response = MiAIResponse.success();
                log.info("小爱设备继续: deviceId={}", deviceId);
                break;
                
            case "set-mode":
                // 从 params 中获取模式
                if (request.getParams() != null && request.getParams().containsKey("mode")) {
                    String mode = request.getParams().get("mode").toString();
                    device.setWorkMode(mode);
                    deviceRepository.save(device);
                    log.info("小爱设置模式: deviceId={}, mode={}", deviceId, mode);
                    response = MiAIResponse.success();
                } else {
                    response = MiAIResponse.badRequest("缺少 mode 参数");
                }
                break;
                
            default:
                response = MiAIResponse.unsupportedIntent(intent);
                log.warn("小爱不支持的操作: intent={}", intent);
        }
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * 设备查询接口
     * POST /miai/query
     */
    @PostMapping("/query")
    public ResponseEntity<?> query(
            @RequestHeader("Authorization") String authorization,
            @RequestBody MiAIRequest request) {
        
        String deviceId = request.getDeviceId();
        String requestId = request.getRequestId();
        
        log.info("小爱设备查询请求: deviceId={}, requestId={}", deviceId, requestId);
        
        // 验证 Token
        String accessToken = extractToken(authorization);
        if (!oauthService.validateAccessToken(accessToken)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(MiAIResponse.unauthorized());
        }
        
        // 验证请求参数
        if (deviceId == null) {
            return ResponseEntity.ok(MiAIResponse.badRequest("缺少 deviceId 参数"));
        }
        
        // 查询设备
        Optional<Device> deviceOpt = deviceRepository.findByDeviceId(deviceId);
        if (!deviceOpt.isPresent()) {
            return ResponseEntity.ok(MiAIResponse.deviceNotFound());
        }
        
        Device device = deviceOpt.get();
        
        // 构建设备状态
        Map<String, Object> status = new HashMap<>();
        status.put("device_id", device.getDeviceId());
        status.put("device_name", device.getDeviceName());
        status.put("power_state", device.getPowerState());
        status.put("work_mode", device.getWorkMode());
        status.put("battery_level", device.getBatteryLevel());
        status.put("status", device.getStatus());
        status.put("online", "online".equals(device.getStatus()));
        
        log.info("小爱设备查询完成: deviceId={}, powerState={}, batteryLevel={}", 
                deviceId, device.getPowerState(), device.getBatteryLevel());
        
        return ResponseEntity.ok(MiAIResponse.statusResponse(status));
    }
    
    /**
     * 健康检查接口
     * GET /miai/health
     */
    @GetMapping("/health")
    public ResponseEntity<?> health() {
        Map<String, Object> health = new HashMap<>();
        health.put("status", "ok");
        health.put("platform", "xiaomi-miai");
        health.put("timestamp", System.currentTimeMillis());
        return ResponseEntity.ok(MiAIResponse.success(health));
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
