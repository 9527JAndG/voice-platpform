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
 * 实现 Google Smart Home API
 * 
 * 支持的功能:
 * - SYNC: 设备发现和同步
 * - QUERY: 设备状态查询
 * - EXECUTE: 设备控制执行
 * - DISCONNECT: 账号解绑
 * 
 * 支持的设备类型:
 * - VACUUM: 扫地机器人
 * 
 * 支持的 Traits:
 * - StartStop: 启动/停止
 * - OnOff: 开关
 * - Dock: 回充
 * - Modes: 模式控制
 * - Locator: 设备定位
 * - EnergyStorage: 电量查询
 * 
 * @author Voice Platform Team
 * @version 1.0
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
     * 处理所有 Google Assistant 请求
     * 
     * @param authorization Authorization header (Bearer token)
     * @param request Google 请求对象
     * @return ResponseEntity 包含 Google 响应
     */
    @PostMapping
    public ResponseEntity<?> handleIntent(
        @RequestHeader("Authorization") String authorization,
        @RequestBody GoogleRequest request
    ) {
        String requestId = request.getRequestId();
        String intent = request.getInputs().get(0).getIntent();
        
        log.info("=== 收到 Google Assistant 请求 ===");
        log.info("RequestId: {}", requestId);
        log.info("Intent: {}", intent);
        
        try {
            // 提取 Token
            String token = extractToken(authorization);
            
            // 处理不同的 Intent
            switch (intent) {
                case "action.devices.SYNC":
                    log.info("处理 SYNC Intent - 设备发现");
                    return handleSync(request, token);
                    
                case "action.devices.QUERY":
                    log.info("处理 QUERY Intent - 状态查询");
                    return handleQuery(request, token);
                    
                case "action.devices.EXECUTE":
                    log.info("处理 EXECUTE Intent - 设备控制");
                    return handleExecute(request, token);
                    
                case "action.devices.DISCONNECT":
                    log.info("处理 DISCONNECT Intent - 账号解绑");
                    return handleDisconnect(request, token);
                    
                default:
                    log.warn("不支持的 Intent: {}", intent);
                    return ResponseEntity.ok(GoogleResponse.createErrorResponse(
                        requestId, "protocolError", "不支持的 Intent: " + intent
                    ));
            }
        } catch (Exception e) {
            log.error("处理 Google 请求失败: requestId={}, intent={}", requestId, intent, e);
            return ResponseEntity.ok(GoogleResponse.createErrorResponse(
                requestId, "transientError", "服务器内部错误: " + e.getMessage()
            ));
        }
    }

    /**
     * 处理 SYNC Intent - 设备发现
     * 返回用户的所有设备及其能力
     * 
     * @param request SYNC 请求
     * @param token 访问令牌
     * @return 设备列表响应
     */
    private ResponseEntity<?> handleSync(GoogleRequest request, String token) {
        String requestId = request.getRequestId();
        
        log.info("=== 开始设备发现 ===");
        
        try {
            // 验证 Token
            if (token == null || !oauthService.validateAccessToken(token)) {
                log.warn("SYNC Token 验证失败: token={}", token != null ? "exists" : "null");
                return ResponseEntity.ok(GoogleResponse.createErrorResponse(
                    requestId, "authFailure", "访问令牌无效或已过期"
                ));
            }
            
            // 获取用户 ID（简化处理，实际应从 token 获取）
            // TODO: 从 token 中解析真实的 userId
            Long userId = 1L;
            String agentUserId = "user_" + userId;
            
            // 查询用户设备
            List<Device> devices = deviceService.findDevicesByUserId(userId);
            log.info("查询到用户设备: userId={}, totalDevices={}", userId, devices.size());
            
            // 构建设备列表
            List<GoogleDevice> googleDevices = new ArrayList<>();
            for (Device device : devices) {
                if ("robot_cleaner".equals(device.getDeviceType())) {
                    googleDevices.add(GoogleDevice.createVacuumDevice(
                        device.getDeviceId(),
                        device.getDeviceName(),
                        "Living Room"
                    ));
                    log.debug("添加设备: deviceId={}, deviceName={}", 
                            device.getDeviceId(), device.getDeviceName());
                }
            }
            
            log.info("✓ 设备发现完成: userId={}, discoveredDevices={}", userId, googleDevices.size());
            
            return ResponseEntity.ok(GoogleResponse.createSyncResponse(
                requestId, agentUserId, googleDevices
            ));
            
        } catch (Exception e) {
            log.error("设备发现失败: requestId={}", requestId, e);
            return ResponseEntity.ok(GoogleResponse.createErrorResponse(
                requestId, "transientError", "设备发现失败: " + e.getMessage()
            ));
        }
    }
    
    /**
     * 处理 QUERY Intent - 状态查询
     * 查询指定设备的当前状态
     * 
     * @param request QUERY 请求
     * @param token 访问令牌
     * @return 设备状态响应
     */
    private ResponseEntity<?> handleQuery(GoogleRequest request, String token) {
        String requestId = request.getRequestId();
        
        log.info("=== 开始状态查询 ===");
        
        try {
            // 验证 Token
            if (token == null || !oauthService.validateAccessToken(token)) {
                log.warn("QUERY Token 验证失败: token={}", token != null ? "exists" : "null");
                return ResponseEntity.ok(GoogleResponse.createErrorResponse(
                    requestId, "authFailure", "访问令牌无效或已过期"
                ));
            }
            
            // 获取设备列表
            Map<String, Object> payload = request.getInputs().get(0).getPayload();
            @SuppressWarnings("unchecked")
            List<Map<String, String>> deviceList = (List<Map<String, String>>) payload.get("devices");
            
            log.info("查询设备数量: {}", deviceList.size());
            
            Map<String, GoogleResponse.DeviceState> deviceStates = new HashMap<>();
            
            for (Map<String, String> deviceMap : deviceList) {
                String deviceId = deviceMap.get("id");
                log.debug("查询设备状态: deviceId={}", deviceId);
                
                Optional<Device> deviceOpt = deviceService.findDeviceByDeviceId(deviceId);
                
                if (deviceOpt.isPresent()) {
                    Device device = deviceOpt.get();
                    
                    // 构建设备状态
                    GoogleResponse.DeviceState state = buildDeviceState(device);
                    deviceStates.put(deviceId, state);
                    
                    log.debug("✓ 设备状态查询成功: deviceId={}, online={}, powerState={}", 
                            deviceId, device.getStatus(), device.getPowerState());
                } else {
                    // 设备不存在
                    GoogleResponse.DeviceState state = GoogleResponse.DeviceState.builder()
                        .online(false)
                        .status("ERROR")
                        .errorCode("deviceNotFound")
                        .build();
                    deviceStates.put(deviceId, state);
                    log.warn("设备不存在: deviceId={}", deviceId);
                }
            }
            
            log.info("✓ 状态查询完成: deviceCount={}", deviceStates.size());
            
            return ResponseEntity.ok(GoogleResponse.createQueryResponse(requestId, deviceStates));
            
        } catch (Exception e) {
            log.error("状态查询失败: requestId={}", requestId, e);
            return ResponseEntity.ok(GoogleResponse.createErrorResponse(
                requestId, "transientError", "状态查询失败: " + e.getMessage()
            ));
        }
    }
    
    /**
     * 构建设备状态对象
     * 
     * @param device 设备实体
     * @return 设备状态
     */
    private GoogleResponse.DeviceState buildDeviceState(Device device) {
        // 构建模式设置
        Map<String, String> modeSettings = new HashMap<>();
        modeSettings.put("clean_mode", device.getWorkMode());
        
        // 构建电量信息
        List<GoogleResponse.CapacityRemaining> capacity = new ArrayList<>();
        capacity.add(GoogleResponse.CapacityRemaining.builder()
            .rawValue(device.getBatteryLevel())
            .unit("PERCENTAGE")
            .build());
        
        // 电量描述
        String capacityDesc = device.getBatteryLevel() > 60 ? "FULL" :
                             device.getBatteryLevel() > 30 ? "MEDIUM" : "LOW";
        
        return GoogleResponse.DeviceState.builder()
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
    }

        /**
     * 处理 EXECUTE Intent - 设备控制
     * 执行设备控制命令
     * 
     * @param request EXECUTE 请求
     * @param token 访问令牌
     * @return 命令执行结果响应
     */
    private ResponseEntity<?> handleExecute(GoogleRequest request, String token) {
        String requestId = request.getRequestId();
        
        log.info("=== 开始执行设备控制 ===");
        
        try {
            // 验证 Token
            if (token == null || !oauthService.validateAccessToken(token)) {
                log.warn("EXECUTE Token 验证失败: token={}", token != null ? "exists" : "null");
                return ResponseEntity.ok(GoogleResponse.createErrorResponse(
                    requestId, "authFailure", "访问令牌无效或已过期"
                ));
            }
            
            // 获取命令列表
            Map<String, Object> payload = request.getInputs().get(0).getPayload();
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> commandList = (List<Map<String, Object>>) payload.get("commands");
            
            log.info("命令数量: {}", commandList.size());
            
            List<GoogleResponse.Command> responses = new ArrayList<>();
            
            for (Map<String, Object> commandMap : commandList) {
                @SuppressWarnings("unchecked")
                List<Map<String, String>> devices = (List<Map<String, String>>) commandMap.get("devices");
                @SuppressWarnings("unchecked")
                List<Map<String, Object>> executions = (List<Map<String, Object>>) commandMap.get("execution");
                
                for (Map<String, String> deviceMap : devices) {
                    String deviceId = deviceMap.get("id");
                    log.info("处理设备: deviceId={}", deviceId);
                    
                    // 执行设备命令
                    GoogleResponse.Command response = executeDeviceCommands(deviceId, executions);
                    responses.add(response);
                }
            }
            
            log.info("✓ 命令执行完成: commandCount={}", responses.size());
            
            return ResponseEntity.ok(GoogleResponse.createExecuteResponse(requestId, responses));
            
        } catch (Exception e) {
            log.error("命令执行失败: requestId={}", requestId, e);
            return ResponseEntity.ok(GoogleResponse.createErrorResponse(
                requestId, "transientError", "命令执行失败: " + e.getMessage()
            ));
        }
    }
    
    /**
     * 执行设备命令
     * 
     * @param deviceId 设备ID
     * @param executions 命令列表
     * @return 命令执行结果
     */
    private GoogleResponse.Command executeDeviceCommands(String deviceId, List<Map<String, Object>> executions) {
        // 检查设备是否存在
        Optional<Device> deviceOpt = deviceService.findDeviceByDeviceId(deviceId);
        if (!deviceOpt.isPresent()) {
            log.warn("设备不存在: deviceId={}", deviceId);
            return GoogleResponse.Command.builder()
                .ids(List.of(deviceId))
                .status("ERROR")
                .errorCode("deviceNotFound")
                .build();
        }
        
        Device device = deviceOpt.get();
        
        // 检查设备是否在线
        if (!"online".equals(device.getStatus())) {
            log.warn("设备离线: deviceId={}, status={}", deviceId, device.getStatus());
            return GoogleResponse.Command.builder()
                .ids(List.of(deviceId))
                .status("ERROR")
                .errorCode("deviceOffline")
                .build();
        }
        
        // 执行命令
        Map<String, Object> states = new HashMap<>();
        boolean success = true;
        
        for (Map<String, Object> execution : executions) {
            String command = (String) execution.get("command");
            @SuppressWarnings("unchecked")
            Map<String, Object> params = (Map<String, Object>) execution.get("params");
            
            log.info("执行命令: deviceId={}, command={}", deviceId, command);
            
            try {
                boolean commandSuccess = executeCommand(deviceId, device, command, params, states);
                if (!commandSuccess) {
                    success = false;
                }
            } catch (Exception e) {
                log.error("命令执行失败: deviceId={}, command={}", deviceId, command, e);
                success = false;
            }
        }
        
        if (success) {
            states.put("online", true);
            log.info("✓ 设备命令执行成功: deviceId={}", deviceId);
            return GoogleResponse.Command.builder()
                .ids(List.of(deviceId))
                .status("SUCCESS")
                .states(states)
                .build();
        } else {
            log.warn("设备命令执行失败: deviceId={}", deviceId);
            return GoogleResponse.Command.builder()
                .ids(List.of(deviceId))
                .status("ERROR")
                .errorCode("hardError")
                .build();
        }
    }
    
    /**
     * 执行单个命令
     * 
     * @param deviceId 设备ID
     * @param device 设备对象
     * @param command 命令名称
     * @param params 命令参数
     * @param states 状态映射（输出）
     * @return 是否成功
     */
    private boolean executeCommand(String deviceId, Device device, String command, 
                                   Map<String, Object> params, Map<String, Object> states) {
        switch (command) {
            case "action.devices.commands.OnOff":
                return handleOnOffCommand(deviceId, params, states);
                
            case "action.devices.commands.StartStop":
                return handleStartStopCommand(deviceId, params, states);
                
            case "action.devices.commands.PauseUnpause":
                return handlePauseUnpauseCommand(deviceId, params, states);
                
            case "action.devices.commands.Dock":
                return handleDockCommand(deviceId, states);
                
            case "action.devices.commands.SetModes":
                return handleSetModesCommand(deviceId, params, states);
                
            case "action.devices.commands.Locate":
                return handleLocateCommand(deviceId, params);
                
            default:
                log.warn("不支持的命令: command={}", command);
                return false;
        }
    }
    
    /**
     * 处理 OnOff 命令
     */
    private boolean handleOnOffCommand(String deviceId, Map<String, Object> params, Map<String, Object> states) {
        Boolean on = (Boolean) params.get("on");
        if (on) {
            deviceService.turnOn(deviceId);
            states.put("on", true);
            states.put("isRunning", true);
            log.info("✓ OnOff 命令执行成功: deviceId={}, on=true", deviceId);
        } else {
            deviceService.turnOff(deviceId);
            states.put("on", false);
            states.put("isRunning", false);
            log.info("✓ OnOff 命令执行成功: deviceId={}, on=false", deviceId);
        }
        return true;
    }
    
    /**
     * 处理 StartStop 命令
     */
    private boolean handleStartStopCommand(String deviceId, Map<String, Object> params, Map<String, Object> states) {
        Boolean start = (Boolean) params.get("start");
        if (start) {
            deviceService.turnOn(deviceId);
            states.put("isRunning", true);
            states.put("isPaused", false);
            log.info("✓ StartStop 命令执行成功: deviceId={}, start=true", deviceId);
        } else {
            deviceService.turnOff(deviceId);
            states.put("isRunning", false);
            log.info("✓ StartStop 命令执行成功: deviceId={}, start=false", deviceId);
        }
        return true;
    }
    
    /**
     * 处理 PauseUnpause 命令
     */
    private boolean handlePauseUnpauseCommand(String deviceId, Map<String, Object> params, Map<String, Object> states) {
        Boolean pause = (Boolean) params.get("pause");
        states.put("isPaused", pause);
        log.info("✓ PauseUnpause 命令执行成功: deviceId={}, pause={}", deviceId, pause);
        return true;
    }
    
    /**
     * 处理 Dock 命令
     */
    private boolean handleDockCommand(String deviceId, Map<String, Object> states) {
        states.put("isDocked", true);
        states.put("isRunning", false);
        log.info("✓ Dock 命令执行成功: deviceId={}", deviceId);
        return true;
    }
    
    /**
     * 处理 SetModes 命令
     */
    private boolean handleSetModesCommand(String deviceId, Map<String, Object> params, Map<String, Object> states) {
        @SuppressWarnings("unchecked")
        Map<String, String> updateModeSettings = (Map<String, String>) params.get("updateModeSettings");
        
        if (updateModeSettings == null || !updateModeSettings.containsKey("clean_mode")) {
            log.warn("SetModes 命令缺少 clean_mode 参数");
            return false;
        }
        
        String mode = updateModeSettings.get("clean_mode");
        
        // 验证模式值
        if (!isValidMode(mode)) {
            log.warn("无效的清扫模式: mode={}", mode);
            return false;
        }
        
        deviceService.setMode(deviceId, mode);
        Map<String, String> modeSettings = new HashMap<>();
        modeSettings.put("clean_mode", mode);
        states.put("currentModeSettings", modeSettings);
        log.info("✓ SetModes 命令执行成功: deviceId={}, mode={}", deviceId, mode);
        return true;
    }
    
    /**
     * 处理 Locate 命令
     */
    private boolean handleLocateCommand(String deviceId, Map<String, Object> params) {
        // 定位设备（播放声音）
        Boolean silent = params != null ? (Boolean) params.get("silent") : false;
        log.info("✓ Locate 命令执行成功: deviceId={}, silent={}", deviceId, silent);
        return true;
    }
    
    /**
     * 验证清扫模式是否有效
     * 
     * @param mode 模式值
     * @return 是否有效
     */
    private boolean isValidMode(String mode) {
        return "auto".equals(mode) || "spot".equals(mode) || "edge".equals(mode);
    }
    
    /**
     * 处理 DISCONNECT Intent - 账号解绑
     * 当用户取消账号关联时调用
     * 
     * @param request DISCONNECT 请求
     * @param token 访问令牌
     * @return 解绑响应
     */
    private ResponseEntity<?> handleDisconnect(GoogleRequest request, String token) {
        String requestId = request.getRequestId();
        
        log.info("=== 处理账号解绑 ===");
        log.info("RequestId: {}", requestId);
        
        try {
            // 这里可以添加清理逻辑，如删除 token、清理缓存等
            // TODO: 实现 token 清理逻辑
            
            log.info("✓ 账号解绑成功: requestId={}", requestId);
            
            return ResponseEntity.ok(GoogleResponse.createDisconnectResponse(requestId));
            
        } catch (Exception e) {
            log.error("账号解绑失败: requestId={}", requestId, e);
            return ResponseEntity.ok(GoogleResponse.createErrorResponse(
                requestId, "transientError", "账号解绑失败: " + e.getMessage()
            ));
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
