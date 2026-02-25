package com.voice.platform.dto.alexa;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Alexa 响应对象
 * 用于构建符合 Alexa Smart Home API v3 规范的响应
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AlexaResponse {
    
    /**
     * 事件对象，包含响应的主要内容
     */
    private Event event;
    
    /**
     * 上下文对象，包含设备状态属性（可选）
     */
    private Context context;
    
    /**
     * 事件对象
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Event {
        /**
         * 消息头，包含命名空间、名称等元数据
         */
        private Header header;
        
        /**
         * 端点信息，包含设备 ID 和授权范围
         */
        private Endpoint endpoint;
        
        /**
         * 负载数据，包含具体的响应内容
         * 例如：设备列表、错误信息等
         */
        private Map<String, Object> payload;
    }
    
    /**
     * 消息头对象
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Header {
        /**
         * 命名空间，标识消息类型
         * 例如：Alexa.Discovery, Alexa.PowerController, Alexa
         */
        private String namespace;
        
        /**
         * 消息名称
         * 例如：Discover.Response, Response, ErrorResponse
         */
        private String name;
        
        /**
         * 协议版本，固定为 "3"
         */
        private String payloadVersion;
        
        /**
         * 消息 ID，用于追踪和调试
         * 格式：UUID
         */
        private String messageId;
        
        /**
         * 关联令牌，用于关联请求和响应
         * 从请求中获取并原样返回
         */
        private String correlationToken;
    }
    
    /**
     * 端点对象
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Endpoint {
        /**
         * 授权范围，包含访问令牌
         */
        private Scope scope;
        
        /**
         * 端点 ID，即设备 ID
         */
        private String endpointId;
    }
    
    /**
     * 授权范围对象
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Scope {
        /**
         * 令牌类型，固定为 "BearerToken"
         */
        private String type;
        
        /**
         * 访问令牌
         */
        private String token;
    }
    
    /**
     * 上下文对象，包含设备状态
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Context {
        /**
         * 属性列表，包含设备的各种状态属性
         */
        private List<Property> properties;
    }
    
    /**
     * 属性对象，表示设备的一个状态属性
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Property {
        /**
         * 命名空间，标识属性所属的接口
         * 例如：Alexa.PowerController, Alexa.ModeController
         */
        private String namespace;
        
        /**
         * 属性名称
         * 例如：powerState, mode
         */
        private String name;
        
        /**
         * 属性值
         * 例如：ON, OFF, Auto, Spot
         */
        private Object value;
        
        /**
         * 采样时间，ISO 8601 格式
         * 例如：2024-01-01T12:00:00Z
         */
        private String timeOfSample;
        
        /**
         * 不确定性（毫秒）
         * 表示属性值的时效性，通常为 500
         */
        private Integer uncertaintyInMilliseconds;
    }
    
    /**
     * 创建设备发现响应
     */
    public static AlexaResponse createDiscoveryResponse(String messageId, List<DiscoveredEndpoint> endpoints) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("endpoints", endpoints);
        
        return AlexaResponse.builder()
            .event(Event.builder()
                .header(Header.builder()
                    .namespace("Alexa.Discovery")
                    .name("Discover.Response")
                    .payloadVersion("3")
                    .messageId(messageId)
                    .build())
                .payload(payload)
                .build())
            .build();
    }
    
    /**
     * 创建控制响应
     */
    public static AlexaResponse createControlResponse(String messageId, String correlationToken, 
                                                      String endpointId, String token,
                                                      List<Property> properties) {
        return AlexaResponse.builder()
            .event(Event.builder()
                .header(Header.builder()
                    .namespace("Alexa")
                    .name("Response")
                    .payloadVersion("3")
                    .messageId(messageId)
                    .correlationToken(correlationToken)
                    .build())
                .endpoint(Endpoint.builder()
                    .scope(Scope.builder()
                        .type("BearerToken")
                        .token(token)
                        .build())
                    .endpointId(endpointId)
                    .build())
                .payload(new HashMap<>())
                .build())
            .context(Context.builder()
                .properties(properties)
                .build())
            .build();
    }
    
    /**
     * 创建错误响应
     */
    public static AlexaResponse createErrorResponse(String messageId, String correlationToken,
                                                    String endpointId, String errorType, String errorMessage) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("type", errorType);
        payload.put("message", errorMessage);
        
        return AlexaResponse.builder()
            .event(Event.builder()
                .header(Header.builder()
                    .namespace("Alexa")
                    .name("ErrorResponse")
                    .payloadVersion("3")
                    .messageId(messageId)
                    .correlationToken(correlationToken)
                    .build())
                .endpoint(Endpoint.builder()
                    .endpointId(endpointId)
                    .build())
                .payload(payload)
                .build())
            .build();
    }
    
    /**
     * 创建电源状态属性
     */
    public static Property createPowerStateProperty(String powerState) {
        return Property.builder()
            .namespace("Alexa.PowerController")
            .name("powerState")
            .value(powerState)
            .timeOfSample(Instant.now().toString())
            .uncertaintyInMilliseconds(500)
            .build();
    }
    
    /**
     * 创建模式属性
     */
    public static Property createModeProperty(String mode) {
        return Property.builder()
            .namespace("Alexa.ModeController")
            .name("mode")
            .value(mode)
            .timeOfSample(Instant.now().toString())
            .uncertaintyInMilliseconds(500)
            .build();
    }
}
