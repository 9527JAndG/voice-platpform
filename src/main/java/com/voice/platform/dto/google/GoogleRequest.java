package com.voice.platform.dto.google;

import lombok.Data;
import java.util.List;
import java.util.Map;

/**
 * Google Assistant 请求对象
 * 支持 SYNC, QUERY, EXECUTE, DISCONNECT 四种 Intent
 * 符合 Google Smart Home API 规范
 */
@Data
public class GoogleRequest {
    
    /**
     * 请求ID，用于追踪和关联请求响应
     * 由 Google 平台生成的唯一标识符
     */
    private String requestId;
    
    /**
     * 输入列表，包含一个或多个意图
     * 通常只包含一个 Input 对象
     */
    private List<Input> inputs;
    
    /**
     * 输入对象，包含意图和负载
     */
    @Data
    public static class Input {
        /**
         * 意图类型
         * 支持的意图：
         * - action.devices.SYNC: 设备发现/同步
         * - action.devices.QUERY: 查询设备状态
         * - action.devices.EXECUTE: 执行设备控制
         * - action.devices.DISCONNECT: 账号解绑
         */
        private String intent;
        
        /**
         * 负载数据，包含具体的请求参数
         * 不同的 intent 有不同的 payload 结构
         */
        private Map<String, Object> payload;
    }
}
