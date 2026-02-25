package com.voice.platform.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.*;

/**
 * 小爱同学响应对象
 * 小爱使用简单的 code + message + data 格式
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MiAIResponse {
    
    /**
     * 状态码
     * 0: 成功
     * 400: 请求参数错误
     * 401: 未授权
     * 404: 设备不存在
     * 500: 服务器错误
     */
    private Integer code;
    
    /**
     * 消息
     */
    private String message;
    
    /**
     * 数据
     */
    private Map<String, Object> data;
    
    /**
     * 创建成功响应
     */
    public static MiAIResponse success() {
        return new MiAIResponse(0, "success", new HashMap<>());
    }
    
    /**
     * 创建成功响应（带数据）
     */
    public static MiAIResponse success(Map<String, Object> data) {
        return new MiAIResponse(0, "success", data);
    }
    
    /**
     * 创建成功响应（带单个数据）
     */
    public static MiAIResponse success(String key, Object value) {
        Map<String, Object> data = new HashMap<>();
        data.put(key, value);
        return new MiAIResponse(0, "success", data);
    }
    
    /**
     * 创建错误响应
     */
    public static MiAIResponse error(Integer code, String message) {
        return new MiAIResponse(code, message, new HashMap<>());
    }
    
    /**
     * 创建设备发现响应
     */
    public static MiAIResponse discoveryResponse(List<Map<String, Object>> devices) {
        Map<String, Object> data = new HashMap<>();
        data.put("devices", devices);
        data.put("count", devices.size());
        return success(data);
    }
    
    /**
     * 创建设备状态响应
     */
    public static MiAIResponse statusResponse(Map<String, Object> status) {
        return success("status", status);
    }
    
    /**
     * 常用错误响应
     */
    public static MiAIResponse unauthorized() {
        return error(401, "访问令牌无效或已过期");
    }
    
    public static MiAIResponse deviceNotFound() {
        return error(404, "设备不存在");
    }
    
    public static MiAIResponse unsupportedIntent(String intent) {
        return error(400, "不支持的操作: " + intent);
    }
    
    public static MiAIResponse badRequest(String message) {
        return error(400, message);
    }
}
