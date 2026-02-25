package com.voice.platform.dto;

import lombok.Data;
import java.util.Map;

/**
 * 小爱同学请求对象
 * 小爱的请求格式比天猫精灵和小度更简洁，采用扁平化结构
 */
@Data
public class MiAIRequest {
    
    /**
     * 意图/操作类型
     * 支持的操作：
     * - turn-on: 开机
     * - turn-off: 关机
     * - pause: 暂停
     * - continue: 继续
     * - set-mode: 设置模式
     * - query: 查询状态
     * - discovery: 设备发现
     */
    private String intent;
    
    /**
     * 设备ID，唯一标识一个设备
     * 例如：robot_001
     */
    private String deviceId;
    
    /**
     * 请求ID，用于追踪和关联请求响应
     * 由小爱平台生成的唯一标识符
     */
    private String requestId;
    
    /**
     * 额外参数，包含操作相关的具体参数
     * 例如：
     * - 设置模式时：{"mode": "auto"}
     * - 查询状态时：可能为空
     */
    private Map<String, Object> params;
}
