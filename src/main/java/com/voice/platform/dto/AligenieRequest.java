package com.voice.platform.dto;

import lombok.Data;
import java.util.Map;

/**
 * 天猫精灵请求对象
 * 用于接收来自天猫精灵平台的控制指令
 */
@Data
public class AligenieRequest {
    
    /**
     * 请求头，包含命名空间、操作名称等元数据
     */
    private Header header;
    
    /**
     * 请求负载，包含具体的设备信息和操作参数
     */
    private Map<String, Object> payload;
    
    /**
     * 请求头信息
     */
    @Data
    public static class Header {
        /**
         * 命名空间，标识请求的功能域
         * 例如：AliGenie.Iot.Device.Discovery（设备发现）
         *      AliGenie.Iot.Device.Control（设备控制）
         */
        private String namespace;
        
        /**
         * 操作名称，标识具体的操作类型
         * 例如：DiscoveryDevices（发现设备）
         *      TurnOn（开机）、TurnOff（关机）
         */
        private String name;
        
        /**
         * 消息ID，用于追踪和关联请求响应
         */
        private String messageId;
        
        /**
         * 负载版本号，标识协议版本
         */
        private String payloadVersion;
    }
}
