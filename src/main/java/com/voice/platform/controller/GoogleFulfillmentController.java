package com.voice.platform.controller;

import com.voice.platform.dto.google.GoogleDevice;
import com.voice.platform.dto.google.GoogleRequest;
import com.voice.platform.dto.google.GoogleResponse;
import com.voice.platform.model.Device;
import com.voice.platform.service.DeviceService;
import com.voice.platform.service.OAuthService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

/**
 * Google Assistant Fulfillment 控制器
 * 处理 SYNC, QUERY, EXECUTE, DISCONNECT 四种 Intent
 */
@Slf4j
@RestController
@RequestMapping("/google/fulfillment")
public class GoogleFulfillmentController {
    
    @Autowired
    private OAuthService oauthService;
    
    @Autowired
    private DeviceService deviceService;
    
    /**
     * Google Smart Home 主入口
     */
    @PostMapping
    public ResponseEntity<?> handleIntent(
        @RequestHeader("Authorization") String authorization,
        @RequestBody GoogleRequest request
    ) {
        String requestId = request.getRequestId();
        String intent = request.getInputs().get(0).getIntent();
        
        log.info("收到 Google 请求: requestId={}, intent={}", requestId, intent);
        
        try {
            // 提取 Token
            String token = extractToken(authorization);
            
            // 处理不同的 Intent
            switch (intent) {
                case "action.devices.SYNC":
                    return handleSync(request, token);
                case "action.devices.QUERY":
                    return handleQuery(request, token);
                case "action.devices.EXECUTE":
                    return handleExecute(request, token);
                case "action.devices.DISCONNECT":
                    return handleDisconnect(request, token);
                default:
                    log.warn("不支持的 Intent: {}", intent);
                    return ResponseEntity.ok(GoogleResponse.createErrorResponse(
                        requestId, "protocolError", "不支持的 Intent"
                    ));
            }
        } catch (Exception e) {
            log.error("处理 Google 请求失败", e);
            return ResponseEntity.ok(GoogleResponse.createErrorResponse(
                requestId, "transientError", "服务器内部错误"
            ));
        }
    }

    
    /**
     * 处理 SYNC Intent - 设备发现
     */
    private ResponseEntity<?> handleSync(GoogleRequest request, String token) {
        String requestId = request.getRequestId();
        
        // 验证 Token
        if (!oauthService.validateAccessToken(token)) {
            log.warn("SYNC Token 验证失败");
            return ResponseEntity.ok(GoogleResponse.createErrorResponse(
                requestId, "authFailure", "访问令牌无效"
            ));
        }
        
        // 获取用户 ID（简化处理，实际应从 token 获取）
        Long userId = 1L;
        String agentUserId = "user_" + userId;
        
        // 查询用户设备
        List<Device> devices = deviceService.findDevicesByUserId(userId);
        
        // 构建设备列表
        List<GoogleDevice> googleDevices = new ArrayList<>();
        for (Device device : devices) {
            if ("robot_cleaner".equals(device.getDeviceType())) {
                googleDevices.add(GoogleDevice.createVacuumDevice(
                    device.getDeviceId(),
                    device.getDeviceName(),
                    "Living Room"
                ));
            }
        }
        
        log.info("设备发现完成: userId={}, deviceCount={}", userId, googleDevices.size());
        
        return ResponseEntity.ok(GoogleResponse.createSyncResponse(
            requestId, agentUserId, googleDevices
        ));
    }
    
    /**
     * 处理 QUERY Intent - 状态查询
     */
    private ResponseEntity<?> handleQuery(GoogleRequest request, String token) {
        String requestId = request.getRequestId();
        
        // 验证 Token
        if (!oauthService.validateAccessToken(token)) {
            log.warn("QUERY Token 验证失败");
            return ResponseEntity.ok(GoogleResponse.createErrorResponse(
                requestId, "authFailure", "访问令牌无效"
            ));
        }
        
        // 获取设备列表
        Map<String, Object> payload = request.getInputs().get(0).getPayload();
        @SuppressWarnings("unchecked")
        List<Map<String, String>> deviceList = (List<Map<String, String>>) payload.get("devices");
        
        Map<String, GoogleResponse.DeviceState> deviceStates = new HashMap<>();
        
        for (Map<String, String> deviceMap : deviceList) {
            String deviceId = deviceMap.get("id");
            Optional<Device> deviceOpt = deviceService.findDeviceByDeviceId(deviceId);
            
            if (deviceOpt.isPresent()) {
                Device device = deviceOpt.get();
                
                // 构建设备状态
                Map<String, String> modeSettings = new HashMap<>();
                modeSettings.put("clean_mode", device.getWorkMode());
                
                List<GoogleResponse.CapacityRemaining> capacity = new ArrayList<>();
                capacity.add(GoogleResponse.CapacityRemaining.builder()
                    .rawValue(device.getBatteryLevel())
                    .unit("PERCENTAGE")
                    .build());
                
                String capacityDesc = device.getBatteryLevel() > 60 ? "FULL" :
                                     device.getBatteryLevel() > 30 ? "MEDIUM" : "LOW";
                
                GoogleResponse.DeviceState state = GoogleResponse.DeviceState.builder()
                    .online("online".equals(device.getStatus()))
                    .status("SUCCESS")
                    .on("on".equals(device.getPowerState()))
                    .isRunning("on".equals(device.getPowerState()))
                    .isPaused(false)
                    .isDocked(false)
                    .currentModeSettings(modeSettings)
                    .descriptiveCapacityRemaining(capacityDesc)
                    .capacityRemaining(capacity)
                    .build();
                
                deviceStates.put(deviceId, state);
            } else {
                // 设备不存在
                GoogleResponse.DeviceState state = GoogleResponse.DeviceState.builder()
                    .online(false)
                    .status("ERROR")
                    .errorCode("deviceNotFound")
                    .build();
                deviceStates.put(deviceId, state);
            }
        }
        
        log.info("状态查询完成: deviceCount={}", deviceStates.size());
        
        return ResponseEntity.ok(GoogleResponse.createQueryResponse(requestId, deviceStates));
    }

    
    /**
     * 处理 EXECUTE Intent - 设备控制
     */
    private ResponseEntity<?> handleExecute(GoogleRequest request, String token) {
        String requestId = request.getRequestId();
        
        // 验证 Token
        if (!oauthService.validateAccessToken(token)) {
            log.warn("EXECUTE Token 验证失败");
            return ResponseEntity.ok(GoogleResponse.createErrorResponse(
                requestId, "authFailure", "访问令牌无效"
            ));
        }
        
        // 获取命令列表
        Map<String, Object> payload = request.getInputs().get(0).getPayload();
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> commandList = (List<Map<String, Object>>) payload.get("commands");
        
        List<GoogleResponse.Command> responses = new ArrayList<>();
        
        for (Map<String, Object> commandMap : commandList) {
            @SuppressWarnings("unchecked")
            List<Map<String, String>> devices = (List<Map<String, String>>) commandMap.get("devices");
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> executions = (List<Map<String, Object>>) commandMap.get("execution");
            
            for (Map<String, String> deviceMap : devices) {
                String deviceId = deviceMap.get("id");
                
                // 检查设备是否存在
                Optional<Device> deviceOpt = deviceService.findDeviceByDeviceId(deviceId);
                if (!deviceOpt.isPresent()) {
                    responses.add(GoogleResponse.Command.builder()
                        .ids(List.of(deviceId))
                        .status("ERROR")
                        .errorCode("deviceNotFound")
                        .build());
                    continue;
                }
                
                Device device = deviceOpt.get();
                
                // 检查设备是否在线
                if (!"online".equals(device.getStatus())) {
                    responses.add(GoogleResponse.Command.builder()
                        .ids(List.of(deviceId))
                        .status("ERROR")
                        .errorCode("deviceOffline")
                        .build());
                    continue;
                }
                
                // 执行命令
                Map<String, Object> states = new HashMap<>();
                boolean success = true;
                
                for (Map<String, Object> execution : executions) {
                    String command = (String) execution.get("command");
                    @SuppressWarnings("unchecked")
                    Map<String, Object> params = (Map<String, Object>) execution.get("params");
                    
                    try {
                        switch (command) {
                            case "action.devices.commands.OnOff":
                                Boolean on = (Boolean) params.get("on");
                                if (on) {
                                    deviceService.turnOn(deviceId);
                                    states.put("on", true);
                                    states.put("isRunning", true);
                                } else {
                                    deviceService.turnOff(deviceId);
                                    states.put("on", false);
                                    states.put("isRunning", false);
                                }
                                log.info("OnOff 命令执行成功: deviceId={}, on={}", deviceId, on);
                                break;
                                
                            case "action.devices.commands.StartStop":
                                Boolean start = (Boolean) params.get("start");
                                if (start) {
                                    deviceService.turnOn(deviceId);
                                    states.put("isRunning", true);
                                    states.put("isPaused", false);
                                } else {
                                    deviceService.turnOff(deviceId);
                                    states.put("isRunning", false);
                                }
                                log.info("StartStop 命令执行成功: deviceId={}, start={}", deviceId, start);
                                break;
                                
                            case "action.devices.commands.PauseUnpause":
                                Boolean pause = (Boolean) params.get("pause");
                                states.put("isPaused", pause);
                                log.info("PauseUnpause 命令执行成功: deviceId={}, pause={}", deviceId, pause);
                                break;
                                
                            case "action.devices.commands.Dock":
                                states.put("isDocked", true);
                                states.put("isRunning", false);
                                log.info("Dock 命令执行成功: deviceId={}", deviceId);
                                break;
                                
                            case "action.devices.commands.SetModes":
                                @SuppressWarnings("unchecked")
                                Map<String, String> updateModeSettings = 
                                    (Map<String, String>) params.get("updateModeSettings");
                                String mode = updateModeSettings.get("clean_mode");
                                deviceService.setMode(deviceId, mode);
                                Map<String, String> modeSettings = new HashMap<>();
                                modeSettings.put("clean_mode", mode);
                                states.put("currentModeSettings", modeSettings);
                                log.info("SetModes 命令执行成功: deviceId={}, mode={}", deviceId, mode);
                                break;
                                
                            case "action.devices.commands.Locate":
                                // 定位设备（播放声音）
                                log.info("Locate 命令执行成功: deviceId={}", deviceId);
                                break;
                                
                            default:
                                log.warn("不支持的命令: {}", command);
                                success = false;
                        }
                    } catch (Exception e) {
                        log.error("命令执行失败: deviceId={}, command={}", deviceId, command, e);
                        success = false;
                    }
                }
                
                if (success) {
                    states.put("online", true);
                    responses.add(GoogleResponse.Command.builder()
                        .ids(List.of(deviceId))
                        .status("SUCCESS")
                        .states(states)
                        .build());
                } else {
                    responses.add(GoogleResponse.Command.builder()
                        .ids(List.of(deviceId))
                        .status("ERROR")
                        .errorCode("hardError")
                        .build());
                }
            }
        }
        
        log.info("命令执行完成: commandCount={}", responses.size());
        
        return ResponseEntity.ok(GoogleResponse.createExecuteResponse(requestId, responses));
    }
    
    /**
     * 处理 DISCONNECT Intent - 账号解绑
     */
    private ResponseEntity<?> handleDisconnect(GoogleRequest request, String token) {
        String requestId = request.getRequestId();
        
        log.info("账号解绑: requestId={}", requestId);
        
        // 这里可以添加清理逻辑，如删除 token 等
        
        return ResponseEntity.ok(GoogleResponse.createDisconnectResponse(requestId));
    }
    
    /**
     * 从 Authorization header 中提取 Token
     */
    private String extractToken(String authorization) {
        if (authorization != null && authorization.startsWith("Bearer ")) {
            return authorization.substring(7);
        }
        return null;
    }
}
