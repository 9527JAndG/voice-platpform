package com.voice.platform.dto.alexa;

import lombok.Data;
import java.util.Map;

/**
 * Alexa 请求对象
 * 支持 Discovery（设备发现）和 Control（设备控制）请求
 * 符合 Alexa Smart Home Skill API v3 规范
 */
@Data
public class AlexaRequest {
    
    /**
     * 指令对象，包含请求的所有信息
     */
    private Directive directive;
    
    /**
     * 指令对象
     */
    @Data
    public static class Directive {
        /**
         * 请求头，包含命名空间、操作名称等元数据
         */
        private Header header;
        
        /**
         * 端点信息，包含设备ID和访问令牌
         */
        private Endpoint endpoint;
        
        /**
         * 负载数据，包含具体的请求参数
         */
        private Map<String, Object> payload;
    }
    
    /**
     * 请求头信息
     */
    @Data
    public static class Header {
        /**
         * 命名空间，标识请求的功能域
         * 例如：Alexa.Discovery（设备发现）
         *      Alexa.PowerController（电源控制）
         *      Alexa.ModeController（模式控制）
         */
        private String namespace;
        
        /**
         * 操作名称，标识具体的操作类型
         * 例如：Discover（发现设备）
         *      TurnOn（开机）、TurnOff（关机）
         *      SetMode（设置模式）
         */
        private String name;
        
        /**
         * 负载版本号，固定为 "3"
         */
        private String payloadVersion;
        
        /**
         * 消息ID，用于追踪和关联请求响应
         */
        private String messageId;
        
        /**
         * 关联令牌，用于关联请求和响应
         * 可选字段
         */
        private String correlationToken;
    }
    
    /**
     * 端点信息
     */
    @Data
    public static class Endpoint {
        /**
         * 作用域，包含访问令牌
         */
        private Scope scope;
        
        /**
         * 端点ID（设备ID）
         */
        private String endpointId;
        
        /**
         * Cookie，用于存储设备相关的额外信息
         * 可选字段
         */
        private Map<String, Object> cookie;
    }
    
    /**
     * 作用域对象，包含访问令牌
     */
    @Data
    public static class Scope {
        /**
         * 令牌类型
         * 固定为 "BearerToken"
         */
        private String type;
        
        /**
         * 访问令牌，用于验证用户身份
         */
        private String token;
    }
}
