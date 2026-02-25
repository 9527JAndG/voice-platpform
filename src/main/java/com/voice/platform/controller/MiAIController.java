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
 * 小爱同学（MiAI）智能家居控制器
 * 实现小米小爱同学 IoT 设备控制 API
 * 
 * 支持的功能:
 * - 设备发现 (Discovery)
 * - 设备控制 (Control)
 * - 设备查询 (Query)
 * - 健康检查 (Health)
 * 
 * 支持的操作:
 * - turn-on: 开机
 * - turn-off: 关机
 * - pause: 暂停
 * - continue: 继续
 * - set-mode: 设置模式
 * - query: 查询状态
 * 
 * 特点：小爱使用扁平化的 JSON 结构，接口格式最简洁
 * 
 * @author Voice Platform Team
 * @version 1.0
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
     * 
     * @param authorization Authorization header (Bearer token)
     * @return 设备列表响应
     */
    @PostMapping("/discovery")
    public ResponseEntity<?> discovery(
            @RequestHeader("Authorization") String authorization) {
        
        log.info("=== 收到小爱同学设备发现请求 ===");
        
        try {
            // 验证 Token
            String accessToken = extractToken(authorization);
            if (accessToken == null || !oauthService.validateAccessToken(accessToken)) {
                log.warn("Token 验证失败: token={}", accessToken != null ? "exists" : "null");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(MiAIResponse.unauthorized());
            }
            
            // 获取用户ID（简化处理，实际应从 token 获取）
            // TODO: 从 token 中解析真实的 userId
            Long userId = 1L;
            
            log.info("开始设备发现: userId={}", userId);
            
            // 查询用户设备
            List<Device> devices = deviceRepository.findByUserId(userId);
            
            // 构建设备列表
            List<Map<String, Object>> deviceList = buildDeviceList(devices);
            
            log.info("✓ 设备发现完成: userId={}, deviceCount={}", userId, deviceList.size());
            
            return ResponseEntity.ok(MiAIResponse.discoveryResponse(deviceList));
            
        } catch (Exception e) {
            log.error("设备发现失败", e);
            return ResponseEntity.ok(MiAIResponse.error(500, 
                    "服务器内部错误: " + e.getMessage()));
        }
    }
    
    /**
     * 构建设备列表
     * 
     * @param devices 设备实体列表
     * @return 设备信息列表
     */
    private List<Map<String, Object>> buildDeviceList(List<Device> devices) {
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
        
        return deviceList;
    }
    
    /**
     * 设备控制接口
     * POST /miai/control
     * 
     * @param authorization Authorization header (Bearer token)
     * @param request 小爱控制请求
     * @return 控制响应
     */
    @PostMapping("/control")
    public ResponseEntity<?> control(
            @RequestHeader("Authorization") String authorization,
            @RequestBody MiAIRequest request) {
        
        String intent = request.getIntent();
        String deviceId = request.getDeviceId();
        String requestId = request.getRequestId();
        
        log.info("=== 收到小爱同学设备控制请求 ===");
        log.info("RequestId: {}", requestId);
        log.info("Intent: {}", intent);
        log.info("DeviceId: {}", deviceId);
        
        try {
            // 验证 Token
            String accessToken = extractToken(authorization);
            if (accessToken == null || !oauthService.validateAccessToken(accessToken)) {
                log.warn("Token 验证失败: token={}", accessToken != null ? "exists" : "null");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(MiAIResponse.unauthorized());
            }
            
            // 验证请求参数
            if (intent == null || intent.isEmpty()) {
                log.warn("Intent 参数缺失");
                return ResponseEntity.ok(MiAIResponse.badRequest("缺少 intent 参数"));
            }
            
            if (deviceId == null || deviceId.isEmpty()) {
                log.warn("DeviceId 参数缺失");
                return ResponseEntity.ok(MiAIResponse.badRequest("缺少 deviceId 参数"));
            }
            
            // 查询设备
            Optional<Device> deviceOpt = deviceRepository.findByDeviceId(deviceId);
            if (!deviceOpt.isPresent()) {
                log.warn("设备不存在: deviceId={}", deviceId);
                return ResponseEntity.ok(MiAIResponse.deviceNotFound());
            }
            
            Device device = deviceOpt.get();
            
            // 执行控制操作
            MiAIResponse response = executeControl(intent, device, request);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("设备控制失败: intent={}, deviceId={}, requestId={}", 
                    intent, deviceId, requestId, e);
            return ResponseEntity.ok(MiAIResponse.error(500, 
                    "服务器内部错误: " + e.getMessage()));
        }
    }
    
    /**
     * 执行具体的控制操作
     * 
     * @param intent 操作意图
     * @param device 设备对象
     * @param request 请求对象
     * @return 控制响应
     */
    private MiAIResponse executeControl(String intent, Device device, MiAIRequest request) {
        MiAIResponse response;
        String deviceId = device.getDeviceId();
        
        switch (intent) {
            case "turn-on":
                device.setPowerState("on");
                deviceRepository.save(device);
                response = MiAIResponse.success();
                log.info("✓ 设备开机成功: deviceId={}, deviceName={}", 
                        deviceId, device.getDeviceName());
                break;
                
            case "turn-off":
                device.setPowerState("off");
                deviceRepository.save(device);
                response = MiAIResponse.success();
                log.info("✓ 设备关机成功: deviceId={}, deviceName={}", 
                        deviceId, device.getDeviceName());
                break;
                
            case "pause":
                response = MiAIResponse.success();
                log.info("✓ 设备暂停成功: deviceId={}", deviceId);
                break;
                
            case "continue":
                response = MiAIResponse.success();
                log.info("✓ 设备继续成功: deviceId={}", deviceId);
                break;
                
            case "set-mode":
                response = handleSetMode(device, request);
                break;
                
            default:
                log.warn("不支持的操作: intent={}", intent);
                response = MiAIResponse.unsupportedIntent(intent);
        }
        
        return response;
    }
    
    /**
     * 处理设置模式操作
     * 
     * @param device 设备对象
     * @param request 请求对象
     * @return 控制响应
     */
    private MiAIResponse handleSetMode(Device device, MiAIRequest request) {
        if (request.getParams() == null || !request.getParams().containsKey("mode")) {
            log.warn("模式参数缺失");
            return MiAIResponse.badRequest("缺少 mode 参数");
        }
        
        String mode = request.getParams().get("mode").toString();
        
        if (!isValidMode(mode)) {
            log.warn("无效的模式: mode={}", mode);
            return MiAIResponse.badRequest("不支持的模式: " + mode + 
                    "。支持的模式: auto, spot, edge");
        }
        
        device.setWorkMode(mode);
        deviceRepository.save(device);
        log.info("✓ 模式设置成功: deviceId={}, mode={}", device.getDeviceId(), mode);
        
        return MiAIResponse.success();
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
     * POST /miai/query
     * 
     * @param authorization Authorization header (Bearer token)
     * @param request 小爱查询请求
     * @return 设备状态响应
     */
    @PostMapping("/query")
    public ResponseEntity<?> query(
            @RequestHeader("Authorization") String authorization,
            @RequestBody MiAIRequest request) {
        
        String deviceId = request.getDeviceId();
        String requestId = request.getRequestId();
        
        log.info("=== 收到小爱同学设备查询请求 ===");
        log.info("RequestId: {}", requestId);
        log.info("DeviceId: {}", deviceId);
        
        try {
            // 验证 Token
            String accessToken = extractToken(authorization);
            if (accessToken == null || !oauthService.validateAccessToken(accessToken)) {
                log.warn("Token 验证失败: token={}", accessToken != null ? "exists" : "null");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(MiAIResponse.unauthorized());
            }
            
            // 验证请求参数
            if (deviceId == null || deviceId.isEmpty()) {
                log.warn("DeviceId 参数缺失");
                return ResponseEntity.ok(MiAIResponse.badRequest("缺少 deviceId 参数"));
            }
            
            // 查询设备
            Optional<Device> deviceOpt = deviceRepository.findByDeviceId(deviceId);
            if (!deviceOpt.isPresent()) {
                log.warn("设备不存在: deviceId={}", deviceId);
                return ResponseEntity.ok(MiAIResponse.deviceNotFound());
            }
            
            Device device = deviceOpt.get();
            
            // 构建设备状态
            Map<String, Object> status = buildDeviceStatus(device);
            
            log.info("✓ 设备查询成功: deviceId={}, powerState={}, batteryLevel={}", 
                    deviceId, device.getPowerState(), device.getBatteryLevel());
            
            return ResponseEntity.ok(MiAIResponse.statusResponse(status));
            
        } catch (Exception e) {
            log.error("设备查询失败: deviceId={}, requestId={}", deviceId, requestId, e);
            return ResponseEntity.ok(MiAIResponse.error(500, 
                    "服务器内部错误: " + e.getMessage()));
        }
    }
    
    /**
     * 构建设备状态信息
     * 
     * @param device 设备对象
     * @return 设备状态信息
     */
    private Map<String, Object> buildDeviceStatus(Device device) {
        Map<String, Object> status = new HashMap<>();
        status.put("device_id", device.getDeviceId());
        status.put("device_name", device.getDeviceName());
        status.put("power_state", device.getPowerState());
        status.put("work_mode", device.getWorkMode());
        status.put("battery_level", device.getBatteryLevel());
        status.put("status", device.getStatus());
        status.put("online", "online".equals(device.getStatus()));
        return status;
    }
    
    /**
     * 健康检查接口
     * GET /miai/health
     * 
     * @return 健康状态响应
     */
    @GetMapping("/health")
    public ResponseEntity<?> health() {
        log.info("=== 收到小爱同学健康检查请求 ===");
        
        try {
            Map<String, Object> health = new HashMap<>();
            health.put("status", "ok");
            health.put("platform", "xiaomi-miai");
            health.put("timestamp", System.currentTimeMillis());
            
            log.info("✓ 健康检查完成: status=ok");
            
            return ResponseEntity.ok(MiAIResponse.success(health));
            
        } catch (Exception e) {
            log.error("健康检查失败", e);
            return ResponseEntity.ok(MiAIResponse.error(500, 
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
