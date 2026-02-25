package com.voice.platform.dto;

import lombok.Data;
import java.util.Map;

/**
 * 小度音箱（DuerOS）请求对象
 * 用于接收来自小度音箱平台的控制指令
 */
@Data
public class DuerOSRequest {
    
    /**
     * 请求头，包含命名空间、操作名称等元数据
     */
    private Header header;
    
    /**
     * 请求负载，包含访问令牌、设备信息和操作参数
     */
    private Payload payload;
    
    /**
     * 请求头信息
     */
    @Data
    public static class Header {
        /**
         * 命名空间，标识请求的功能域
         * 例如：DuerOS.ConnectedHome.Discovery（设备发现）
         *      DuerOS.ConnectedHome.Control（设备控制）
         */
        private String namespace;
        
        /**
         * 操作名称，标识具体的操作类型
         * 例如：DiscoverAppliancesRequest（发现设备）
         *      TurnOnRequest（开机）、TurnOffRequest（关机）
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
    
    /**
     * 请求负载信息
     */
    @Data
    public static class Payload {
        /**
         * 访问令牌，用于验证用户身份
         */
        private String accessToken;
        
        /**
         * 设备信息
         */
        private Appliance appliance;
        
        /**
         * 附加信息，包含操作相关的额外参数
         */
        private Map<String, Object> additionalInfo;
    }
    
    /**
     * 设备信息
     */
    @Data
    public static class Appliance {
        /**
         * 设备ID，唯一标识一个设备
         */
        private String applianceId;
        
        /**
         * 设备附加详情
         */
        private Map<String, Object> additionalApplianceDetails;
    }
}
