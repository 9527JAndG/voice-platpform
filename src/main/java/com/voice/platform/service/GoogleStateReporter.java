package com.voice.platform.service;

import com.voice.platform.model.Device;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Google State Reporter
 * 实现 Google HomeGraph Report State API
 * 主动推送设备状态变化到 Google
 * 
 * @author Voice Platform Team
 * @version 1.0
 */
@Slf4j
@Service
public class GoogleStateReporter {
    
    @Autowired
    private GoogleServiceAccountService serviceAccountService;
    
    @Autowired
    private RestTemplate restTemplate;
    
    private static final String REPORT_STATE_URL = 
        "https://homegraph.googleapis.com/v1/devices:reportStateAndNotification";
    
    /**
     * 报告设备状态变化
     * 
     * @param device 设备对象
     * @param oldPowerState 旧的电源状态
     * @param newPowerState 新的电源状态
     * @param oldMode 旧的工作模式
     * @param newMode 新的工作模式
     * @return 是否成功
     */
    public boolean reportStateChange(Device device, String oldPowerState, String newPowerState,
                                     String oldMode, String newMode) {
        log.info("=== 开始 Google Report State ===");
        log.info("DeviceId: {}, DeviceName: {}", device.getDeviceId(), device.getDeviceName());
        
        try {
            // 获取 Service Account Access Token
            String token = serviceAccountService.getAccessToken();
            
            if (token == null || token.isEmpty()) {
                log.error("无法获取 Google Access Token");
                return false;
            }
            
            // 检查是否有状态变化
            boolean hasChange = false;
            if (oldPowerState != null && !oldPowerState.equalsIgnoreCase(newPowerState)) {
                hasChange = true;
                log.info("检测到电源状态变化: {} -> {}", oldPowerState, newPowerState);
            }
            if (oldMode != null && newMode != null && !oldMode.equalsIgnoreCase(newMode)) {
                hasChange = true;
                log.info("检测到模式变化: {} -> {}", oldMode, newMode);
            }
            
            if (!hasChange) {
                log.debug("没有状态变化，跳过报告");
                return true;
            }
            
            // 构建请求体
            Map<String, Object> requestBody = buildReportStateRequest(device);
            
            // 设置请求头
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Authorization", "Bearer " + token);
            
            HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);
            
            // 发送请求
            ResponseEntity<String> response = restTemplate.postForEntity(
                REPORT_STATE_URL,
                request,
                String.class
            );
            
            if (response.getStatusCode().is2xxSuccessful()) {
                log.info("✓ Google Report State 成功: deviceId={}, deviceName={}", 
                        device.getDeviceId(), device.getDeviceName());
                return true;
            } else {
                log.warn("Google Report State 失败: status={}, body={}", 
                        response.getStatusCode(), response.getBody());
                return false;
            }
            
        } catch (Exception e) {
            log.error("Google Report State 异常: deviceId={}, deviceName={}", 
                    device.getDeviceId(), device.getDeviceName(), e);
            return false;
        }
    }
    
    /**
     * 构建 Report State 请求体
     */
    private Map<String, Object> buildReportStateRequest(Device device) {
        Map<String, Object> request = new HashMap<>();
        
        // Request ID
        request.put("requestId", System.currentTimeMillis() + "_" + device.getDeviceId());
        
        // Agent User ID
        String agentUserId = "user_" + device.getUserId();
        request.put("agentUserId", agentUserId);
        
        // Payload
        Map<String, Object> payload = new HashMap<>();
        
        // Devices
        Map<String, Object> devices = new HashMap<>();
        Map<String, Object> deviceState = buildDeviceState(device);
        devices.put(device.getDeviceId(), deviceState);
        
        payload.put("devices", devices);
        request.put("payload", payload);
        
        return request;
    }
    
    /**
     * 构建设备状态
     */
    private Map<String, Object> buildDeviceState(Device device) {
        Map<String, Object> state = new HashMap<>();
        
        // Online 状态
        state.put("online", "online".equalsIgnoreCase(device.getStatus()));
        
        // OnOff trait
        boolean isOn = "on".equalsIgnoreCase(device.getPowerState());
        state.put("on", isOn);
        
        // StartStop trait
        state.put("isRunning", isOn);
        state.put("isPaused", false);
        
        // Dock trait
        state.put("isDocked", false);
        
        // Modes trait
        Map<String, String> modeSettings = new HashMap<>();
        modeSettings.put("clean_mode", device.getWorkMode() != null ? device.getWorkMode() : "auto");
        state.put("currentModeSettings", modeSettings);
        
        // EnergyStorage trait
        Integer batteryLevel = device.getBatteryLevel() != null ? device.getBatteryLevel() : 100;
        
        List<Map<String, Object>> capacityRemaining = new ArrayList<>();
        Map<String, Object> capacity = new HashMap<>();
        capacity.put("rawValue", batteryLevel);
        capacity.put("unit", "PERCENTAGE");
        capacityRemaining.add(capacity);
        state.put("capacityRemaining", capacityRemaining);
        
        String capacityDesc = batteryLevel > 60 ? "FULL" :
                             batteryLevel > 30 ? "MEDIUM" : "LOW";
        state.put("descriptiveCapacityRemaining", capacityDesc);
        
        return state;
    }
    
    /**
     * 批量报告设备状态
     * 
     * @param devices 设备列表
     * @param userId 用户ID
     * @return 是否成功
     */
    public boolean reportStateBatch(List<Device> devices, Long userId) {
        log.info("=== 批量 Google Report State ===");
        log.info("UserId: {}, DeviceCount: {}", userId, devices.size());
        
        try {
            // 获取 Service Account Access Token
            String token = serviceAccountService.getAccessToken();
            
            if (token == null || token.isEmpty()) {
                log.error("无法获取 Google Access Token");
                return false;
            }
            
            // 构建请求体
            Map<String, Object> request = new HashMap<>();
            request.put("requestId", System.currentTimeMillis() + "_batch");
            request.put("agentUserId", "user_" + userId);
            
            // Payload
            Map<String, Object> payload = new HashMap<>();
            Map<String, Object> devicesMap = new HashMap<>();
            
            for (Device device : devices) {
                Map<String, Object> deviceState = buildDeviceState(device);
                devicesMap.put(device.getDeviceId(), deviceState);
            }
            
            payload.put("devices", devicesMap);
            request.put("payload", payload);
            
            // 设置请求头
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Authorization", "Bearer " + token);
            
            HttpEntity<Map<String, Object>> httpRequest = new HttpEntity<>(request, headers);
            
            // 发送请求
            ResponseEntity<String> response = restTemplate.postForEntity(
                REPORT_STATE_URL,
                httpRequest,
                String.class
            );
            
            if (response.getStatusCode().is2xxSuccessful()) {
                log.info("✓ 批量 Google Report State 成功: userId={}, deviceCount={}", 
                        userId, devices.size());
                return true;
            } else {
                log.warn("批量 Google Report State 失败: status={}, body={}", 
                        response.getStatusCode(), response.getBody());
                return false;
            }
            
        } catch (Exception e) {
            log.error("批量 Google Report State 异常: userId={}", userId, e);
            return false;
        }
    }
}
