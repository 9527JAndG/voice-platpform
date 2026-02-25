package com.voice.platform.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.HashMap;
import java.util.Map;

/**
 * 天猫精灵响应对象
 * 用于返回给天猫精灵平台的响应数据
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AligenieResponse {
    
    /**
     * 响应头，包含命名空间、操作名称等元数据
     */
    private Header header;
    
    /**
     * 响应负载，包含具体的返回数据
     */
    private Map<String, Object> payload;
    
    /**
     * 响应头信息
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Header {
        /**
         * 命名空间，标识响应的功能域
         */
        private String namespace;
        
        /**
         * 操作名称，标识具体的响应类型
         */
        private String name;
        
        /**
         * 消息ID，与请求的 messageId 对应
         */
        private String messageId;
        
        /**
         * 负载版本号，默认为 "1"
         */
        private String payloadVersion = "1";
    }
    
    public static AligenieResponse success(String namespace, String name, String messageId) {
        Header header = new Header(namespace, name, messageId, "1");
        Map<String, Object> payload = new HashMap<>();
        return new AligenieResponse(header, payload);
    }
    
    public static AligenieResponse error(String messageId, String errorCode, String message) {
        Header header = new Header("AliGenie.Iot.Device.Error", "ErrorResponse", messageId, "1");
        Map<String, Object> payload = new HashMap<>();
        payload.put("errorCode", errorCode);
        payload.put("message", message);
        return new AligenieResponse(header, payload);
    }
}
