package com.voice.platform.service;

import com.voice.platform.dto.alexa.AlexaResponse;
import com.voice.platform.model.Device;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Alexa State Reporter
 * 负责向 Alexa Event Gateway 主动推送设备状态变化
 * 实现 Proactive State Reporting
 */
@Slf4j
@Service
public class AlexaStateReporter {
    
    @Autowired
    private AlexaTokenService alexaTokenService;
    
    @Autowired
    private RestTemplate restTemplate;
    
    private static final String EVENT_GATEWAY_URL = "https://api.amazonalexa.com/v3/events";
    
    /**
     * 报告设备状态变化
     * 发送 ChangeReport 到 Alexa Event Gateway
     */
    public void reportStateChange(Device device, String oldPowerState, String newPowerState,
                                   String oldMode, String newMode) {
        try {
            // 获取 Alexa Access Token
            String token = alexaTokenService.getAlexaAccessToken(device.getUserId());
            
            if (token == null) {
                log.warn("用户没有 Alexa Token，跳过状态报告: userId={}, deviceId={}", 
                        device.getUserId(), device.getDeviceId());
                return;
            }
            
            // 构建变化的属性列表
            List<AlexaResponse.Property> changedProperties = new ArrayList<>();
            
            // 检查电源状态变化
            if (oldPowerState != null && !oldPowerState.equalsIgnoreCase(newPowerState)) {
                changedProperties.add(AlexaResponse.Property.builder()
                    .namespace("Alexa.PowerController")
                    .name("powerState")
                    .value(newPowerState.toUpperCase())
                    .timeOfSample(Instant.now().toString())
                    .uncertaintyInMilliseconds(500)
                    .build());
                
                log.info("检测到电源状态变化: {} -> {}", oldPowerState, newPowerState);
            }
            
            // 检查模式变化
            if (oldMode != null && newMode != null && !oldMode.equalsIgnoreCase(newMode)) {
                changedProperties.add(AlexaResponse.Property.builder()
                    .namespace("Alexa.ModeController")
                    .name("mode")
                    .value(capitalizeFirst(newMode))
                    .timeOfSample(Instant.now().toString())
                    .uncertaintyInMilliseconds(500)
                    .build());
                
                log.info("检测到模式变化: {} -> {}", oldMode, newMode);
            }
            
            // 如果没有变化，不发送报告
            if (changedProperties.isEmpty()) {
                log.debug("没有状态变化，跳过报告: deviceId={}", device.getDeviceId());
                return;
            }
            
            // 构建 ChangeReport
            Map<String, Object> changeReport = buildChangeReport(
                device.getDeviceId(),
                token,
                changedProperties
            );
            
            // 发送到 Alexa Event Gateway
            sendToEventGateway(changeReport, token);
            
            log.info("✓ 状态报告发送成功: deviceId={}, deviceName={}, changes={}", 
                    device.getDeviceId(), device.getDeviceName(), changedProperties.size());
            
        } catch (Exception e) {
            log.error("状态报告失败: deviceId={}, deviceName={}", 
                    device.getDeviceId(), device.getDeviceName(), e);
        }
    }
    
    /**
     * 构建 ChangeReport 消息
     */
    private Map<String, Object> buildChangeReport(String endpointId, String token,
                                                   List<AlexaResponse.Property> changedProperties) {
        Map<String, Object> report = new HashMap<>();
        
        // Event
        Map<String, Object> event = new HashMap<>();
        
        // Header
        Map<String, Object> header = new HashMap<>();
        header.put("namespace", "Alexa");
        header.put("name", "ChangeReport");
        header.put("payloadVersion", "3");
        header.put("messageId", UUID.randomUUID().toString());
        event.put("header", header);
        
        // Endpoint
        Map<String, Object> endpoint = new HashMap<>();
        Map<String, Object> scope = new HashMap<>();
        scope.put("type", "BearerToken");
        scope.put("token", token);
        endpoint.put("scope", scope);
        endpoint.put("endpointId", endpointId);
        event.put("endpoint", endpoint);
        
        // Payload
        Map<String, Object> payload = new HashMap<>();
        Map<String, Object> change = new HashMap<>();
        
        // Cause
        Map<String, Object> cause = new HashMap<>();
        cause.put("type", "PHYSICAL_INTERACTION");
        change.put("cause", cause);
        
        // Properties
        change.put("properties", changedProperties);
        payload.put("change", change);
        event.put("payload", payload);
        
        report.put("event", event);
        
        // Context (当前所有状态)
        Map<String, Object> context = new HashMap<>();
        context.put("properties", changedProperties);
        report.put("context", context);
        
        return report;
    }
    
    /**
     * 发送到 Alexa Event Gateway
     */
    private void sendToEventGateway(Map<String, Object> changeReport, String token) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Authorization", "Bearer " + token);
            
            HttpEntity<Map<String, Object>> request = new HttpEntity<>(changeReport, headers);
            
            ResponseEntity<String> response = restTemplate.postForEntity(
                EVENT_GATEWAY_URL,
                request,
                String.class
            );
            
            if (response.getStatusCode().is2xxSuccessful()) {
                log.debug("Event Gateway 响应成功: status={}", response.getStatusCode());
            } else {
                log.warn("Event Gateway 响应异常: status={}, body={}", 
                        response.getStatusCode(), response.getBody());
            }
            
        } catch (Exception e) {
            log.error("发送到 Event Gateway 失败", e);
            throw e;
        }
    }
    
    /**
     * 首字母大写
     */
    private String capitalizeFirst(String str) {
        if (str == null || str.isEmpty()) {
            return str;
        }
        return str.substring(0, 1).toUpperCase() + str.substring(1).toLowerCase();
    }
}
