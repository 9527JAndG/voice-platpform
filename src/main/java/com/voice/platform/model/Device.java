package com.voice.platform.model;

import java.time.LocalDateTime;
import javax.persistence.*;
import lombok.Data;

/**
 * 智能设备实体
 * 用于存储和管理智能家居设备信息
 */
@Data
@Entity
@Table(name = "devices")
public class Device {
    
    /**
     * 主键ID
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    /**
     * 设备ID
     * 设备的唯一标识符，例如：robot_001
     */
    @Column(name = "device_id", nullable = false, unique = true)
    private String deviceId;
    
    /**
     * 设备名称
     * 用户可见的设备名称，例如：客厅扫地机器人
     */
    @Column(name = "device_name", nullable = false)
    private String deviceName;
    
    /**
     * 设备类型
     * 例如：vacuum（扫地机器人）、light（灯）、switch（开关）
     */
    @Column(name = "device_type", nullable = false)
    private String deviceType;
    
    /**
     * 用户ID
     * 设备所属的用户
     */
    @Column(name = "user_id", nullable = false)
    private Long userId;
    
    /**
     * 设备状态
     * 例如：online（在线）、offline（离线）
     */
    @Column(name = "status")
    private String status;
    
    /**
     * 电源状态
     * 例如：ON（开启）、OFF（关闭）
     */
    @Column(name = "power_state")
    private String powerState;
    
    /**
     * 工作模式
     * 例如：auto（自动）、spot（定点）、edge（沿边）
     */
    @Column(name = "work_mode")
    private String workMode;
    
    /**
     * 电池电量
     * 百分比值，范围 0-100
     */
    @Column(name = "battery_level")
    private Integer batteryLevel;
    
    /**
     * 创建时间
     */
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    /**
     * 更新时间
     */
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
