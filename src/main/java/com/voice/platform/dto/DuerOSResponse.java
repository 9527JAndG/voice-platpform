package com.voice.platform.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.*;

/**
 * 小度音箱（DuerOS）响应对象
 * 用于返回给小度音箱平台的响应数据
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DuerOSResponse {
    
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
         * 负载版本号，默认为 "1.0"
         */
        private String payloadVersion = "1.0";
    }
    
    /**
     * 创建成功响应
     */
    public static DuerOSResponse success(String namespace, String name, String messageId) {
        Header header = new Header(namespace, name, messageId, "1.0");
        Map<String, Object> payload = new HashMap<>();
        return new DuerOSResponse(header, payload);
    }
    
    /**
     * 创建错误响应
     */
    public static DuerOSResponse error(String messageId, String errorCode, String message) {
        Header header = new Header(
            "DuerOS.ConnectedHome.Control",
            "ErrorResponse",
            messageId,
            "1.0"
        );
        
        Map<String, Object> payload = new HashMap<>();
        payload.put("errorCode", errorCode);
        payload.put("message", message);
        
        return new DuerOSResponse(header, payload);
    }
    
    /**
     * 创建设备发现响应
     */
    public static DuerOSResponse discoveryResponse(String messageId, List<Map<String, Object>> appliances) {
        DuerOSResponse response = success(
            "DuerOS.ConnectedHome.Discovery",
            "DiscoverAppliancesResponse",
            messageId
        );
        response.getPayload().put("discoveredAppliances", appliances);
        return response;
    }
    
    /**
     * 创建控制确认响应
     */
    public static DuerOSResponse controlConfirmation(String action, String messageId) {
        return success(
            "DuerOS.ConnectedHome.Control",
            action + "Confirmation",
            messageId
        );
    }
}
