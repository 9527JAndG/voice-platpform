package com.voice.platform.dto.google;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Google Assistant 响应对象
 * 符合 Google Smart Home API 规范
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GoogleResponse {
    
    /**
     * 请求ID，与请求的 requestId 对应
     */
    private String requestId;
    
    /**
     * 响应负载，包含具体的返回数据
     */
    private Payload payload;
    
    /**
     * 响应负载对象
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Payload {
        /**
         * 代理用户ID（SYNC 响应使用）
         * 标识用户在第三方系统中的唯一ID
         */
        private String agentUserId;
        
        /**
         * 设备列表（SYNC 响应使用）
         * 包含用户的所有设备信息
         */
        private List<GoogleDevice> deviceList;
        
        /**
         * 设备状态映射（QUERY 响应使用）
         * Key: 设备ID，Value: 设备状态
         */
        private Map<String, DeviceState> deviceStates;
        
        /**
         * 命令执行结果列表（EXECUTE 响应使用）
         */
        private List<Command> commands;
        
        /**
         * 错误码（错误响应使用）
         */
        private String errorCode;
        
        /**
         * 调试信息（错误响应使用）
         */
        private String debugString;
    }
    
    /**
     * 命令执行结果
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Command {
        /**
         * 设备ID列表
         * 此命令影响的设备
         */
        private List<String> ids;
        
        /**
         * 执行状态
         * SUCCESS: 成功
         * ERROR: 失败
         */
        private String status;
        
        /**
         * 设备状态
         * 命令执行后的设备状态
         */
        private Map<String, Object> states;
        
        /**
         * 错误码（失败时使用）
         */
        private String errorCode;
    }
    
    /**
     * 设备状态对象
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DeviceState {
        /**
         * 设备是否在线
         */
        private Boolean online;
        
        /**
         * 查询状态
         * SUCCESS: 成功
         * ERROR: 失败
         */
        private String status;
        
        /**
         * 设备是否开启
         */
        private Boolean on;
        
        /**
         * 是否正在运行
         */
        private Boolean isRunning;
        
        /**
         * 是否已暂停
         */
        private Boolean isPaused;
        
        /**
         * 是否已回充
         */
        private Boolean isDocked;
        
        /**
         * 当前模式设置
         * 例如：{"clean_mode": "auto"}
         */
        private Map<String, String> currentModeSettings;
        
        /**
         * 电量描述
         * FULL: 满电
         * MEDIUM: 中等
         * LOW: 低电量
         */
        private String descriptiveCapacityRemaining;
        
        /**
         * 电量详情列表
         */
        private List<CapacityRemaining> capacityRemaining;
        
        /**
         * 错误码（失败时使用）
         */
        private String errorCode;
    }
    
    /**
     * 电量信息
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CapacityRemaining {
        /**
         * 原始值（百分比）
         * 例如：85 表示 85%
         */
        private Integer rawValue;
        
        /**
         * 单位
         * 固定为 "PERCENTAGE"
         */
        private String unit;
    }
    
    /**
     * 创建 SYNC 响应
     */
    public static GoogleResponse createSyncResponse(String requestId, String agentUserId, 
                                                    List<GoogleDevice> devices) {
        return GoogleResponse.builder()
            .requestId(requestId)
            .payload(Payload.builder()
                .agentUserId(agentUserId)
                .deviceList(devices)
                .build())
            .build();
    }
    
    /**
     * 创建 QUERY 响应
     */
    public static GoogleResponse createQueryResponse(String requestId, 
                                                     Map<String, DeviceState> devices) {
        return GoogleResponse.builder()
            .requestId(requestId)
            .payload(Payload.builder()
                .deviceStates(devices)
                .build())
            .build();
    }
    
    /**
     * 创建 EXECUTE 响应
     */
    public static GoogleResponse createExecuteResponse(String requestId, List<Command> commands) {
        return GoogleResponse.builder()
            .requestId(requestId)
            .payload(Payload.builder()
                .commands(commands)
                .build())
            .build();
    }
    
    /**
     * 创建 DISCONNECT 响应
     */
    public static GoogleResponse createDisconnectResponse(String requestId) {
        return GoogleResponse.builder()
            .requestId(requestId)
            .build();
    }
    
    /**
     * 创建错误响应
     */
    public static GoogleResponse createErrorResponse(String requestId, String errorCode, 
                                                     String debugString) {
        return GoogleResponse.builder()
            .requestId(requestId)
            .payload(Payload.builder()
                .errorCode(errorCode)
                .debugString(debugString)
                .build())
            .build();
    }
}
