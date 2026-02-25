package com.voice.platform.service;

import com.voice.platform.dto.AligenieResponse;
import com.voice.platform.model.Device;
import com.voice.platform.repository.DeviceRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Slf4j
@Service
public class DeviceService {
    
    @Autowired
    private DeviceRepository deviceRepository;
    
    /**
     * 设备发现 - 返回用户的所有设备
     */
    public AligenieResponse discovery(Long userId, String messageId) {
        List<Device> devices = deviceRepository.findByUserId(userId);
        
        AligenieResponse response = AligenieResponse.success(
            "AliGenie.Iot.Device.Discovery",
            "DiscoveryResponse",
            messageId
        );
        
        List<Map<String, Object>> deviceList = new ArrayList<>();
        for (Device device : devices) {
            Map<String, Object> deviceInfo = new HashMap<>();
            deviceInfo.put("deviceId", device.getDeviceId());
            deviceInfo.put("deviceName", device.getDeviceName());
            deviceInfo.put("deviceType", device.getDeviceType());
            deviceInfo.put("zone", "客厅");
            deviceInfo.put("brand", "自定义品牌");
            deviceInfo.put("model", "V1.0");
            deviceInfo.put("icon", "https://example.com/icon.png");
            
            // 设备属性
            Map<String, Object> properties = new HashMap<>();
            properties.put("powerstate", device.getPowerState());
            properties.put("mode", device.getWorkMode());
            properties.put("battery", device.getBatteryLevel());
            deviceInfo.put("properties", properties);
            
            // 设备支持的操作
            List<String> actions = Arrays.asList("TurnOn", "TurnOff", "Pause", "Continue", "SetMode");
            deviceInfo.put("actions", actions);
            
            deviceList.add(deviceInfo);
        }
        
        response.getPayload().put("devices", deviceList);
        log.info("设备发现: userId={}, deviceCount={}", userId, deviceList.size());
        
        return response;
    }
    
    /**
     * 设备控制 - 开机
     */
    @Transactional
    public AligenieResponse turnOn(String deviceId, String messageId) {
        Optional<Device> deviceOpt = deviceRepository.findByDeviceId(deviceId);
        
        if (!deviceOpt.isPresent()) {
            return AligenieResponse.error(messageId, "DEVICE_NOT_FOUND", "设备不存在");
        }
        
        Device device = deviceOpt.get();
        device.setPowerState("on");
        deviceRepository.save(device);
        
        log.info("设备开机: deviceId={}, deviceName={}", deviceId, device.getDeviceName());
        
        return AligenieResponse.success(
            "AliGenie.Iot.Device.Control",
            "TurnOnResponse",
            messageId
        );
    }
    
    /**
     * 设备控制 - 关机
     */
    @Transactional
    public AligenieResponse turnOff(String deviceId, String messageId) {
        Optional<Device> deviceOpt = deviceRepository.findByDeviceId(deviceId);
        
        if (!deviceOpt.isPresent()) {
            return AligenieResponse.error(messageId, "DEVICE_NOT_FOUND", "设备不存在");
        }
        
        Device device = deviceOpt.get();
        device.setPowerState("off");
        deviceRepository.save(device);
        
        log.info("设备关机: deviceId={}, deviceName={}", deviceId, device.getDeviceName());
        
        return AligenieResponse.success(
            "AliGenie.Iot.Device.Control",
            "TurnOffResponse",
            messageId
        );
    }
    
    /**
     * 设备控制 - 暂停
     */
    @Transactional
    public AligenieResponse pause(String deviceId, String messageId) {
        Optional<Device> deviceOpt = deviceRepository.findByDeviceId(deviceId);
        
        if (!deviceOpt.isPresent()) {
            return AligenieResponse.error(messageId, "DEVICE_NOT_FOUND", "设备不存在");
        }
        
        log.info("设备暂停: deviceId={}", deviceId);
        
        return AligenieResponse.success(
            "AliGenie.Iot.Device.Control",
            "PauseResponse",
            messageId
        );
    }
    
    /**
     * 设备控制 - 继续
     */
    @Transactional
    public AligenieResponse continueWork(String deviceId, String messageId) {
        Optional<Device> deviceOpt = deviceRepository.findByDeviceId(deviceId);
        
        if (!deviceOpt.isPresent()) {
            return AligenieResponse.error(messageId, "DEVICE_NOT_FOUND", "设备不存在");
        }
        
        log.info("设备继续: deviceId={}", deviceId);
        
        return AligenieResponse.success(
            "AliGenie.Iot.Device.Control",
            "ContinueResponse",
            messageId
        );
    }
    
    /**
     * 设备控制 - 设置模式
     */
    @Transactional
    public AligenieResponse setMode(String deviceId, String mode, String messageId) {
        Optional<Device> deviceOpt = deviceRepository.findByDeviceId(deviceId);
        
        if (!deviceOpt.isPresent()) {
            return AligenieResponse.error(messageId, "DEVICE_NOT_FOUND", "设备不存在");
        }
        
        Device device = deviceOpt.get();
        device.setWorkMode(mode);
        deviceRepository.save(device);
        
        log.info("设置设备模式: deviceId={}, mode={}", deviceId, mode);
        
        return AligenieResponse.success(
            "AliGenie.Iot.Device.Control",
            "SetModeResponse",
            messageId
        );
    }
    
    /**
     * 查询设备状态
     */
    public AligenieResponse query(String deviceId, String messageId) {
        Optional<Device> deviceOpt = deviceRepository.findByDeviceId(deviceId);
        
        if (!deviceOpt.isPresent()) {
            return AligenieResponse.error(messageId, "DEVICE_NOT_FOUND", "设备不存在");
        }
        
        Device device = deviceOpt.get();
        
        AligenieResponse response = AligenieResponse.success(
            "AliGenie.Iot.Device.Query",
            "QueryResponse",
            messageId
        );
        
        Map<String, Object> properties = new HashMap<>();
        properties.put("powerstate", device.getPowerState());
        properties.put("mode", device.getWorkMode());
        properties.put("battery", device.getBatteryLevel());
        
        response.getPayload().put("properties", properties);
        
        log.info("查询设备状态: deviceId={}", deviceId);
        
        return response;
    }
    
    // ============================================
    // Alexa 专用方法
    // ============================================
    
    /**
     * 查找用户的所有设备（Alexa 使用）
     */
    public List<Device> findDevicesByUserId(Long userId) {
        return deviceRepository.findByUserId(userId);
    }
    
    /**
     * 根据设备 ID 查找设备（Alexa 使用）
     */
    public Optional<Device> findDeviceByDeviceId(String deviceId) {
        return deviceRepository.findByDeviceId(deviceId);
    }
    
    /**
     * 开机（Alexa 使用）
     */
    @Transactional
    public void turnOn(String deviceId) {
        Optional<Device> deviceOpt = deviceRepository.findByDeviceId(deviceId);
        if (deviceOpt.isPresent()) {
            Device device = deviceOpt.get();
            device.setPowerState("on");
            deviceRepository.save(device);
            log.info("Alexa 设备开机: deviceId={}", deviceId);
        }
    }
    
    /**
     * 关机（Alexa 使用）
     */
    @Transactional
    public void turnOff(String deviceId) {
        Optional<Device> deviceOpt = deviceRepository.findByDeviceId(deviceId);
        if (deviceOpt.isPresent()) {
            Device device = deviceOpt.get();
            device.setPowerState("off");
            deviceRepository.save(device);
            log.info("Alexa 设备关机: deviceId={}", deviceId);
        }
    }
    
    /**
     * 设置模式（Alexa 使用）
     */
    @Transactional
    public void setMode(String deviceId, String mode) {
        Optional<Device> deviceOpt = deviceRepository.findByDeviceId(deviceId);
        if (deviceOpt.isPresent()) {
            Device device = deviceOpt.get();
            device.setWorkMode(mode);
            deviceRepository.save(device);
            log.info("Alexa 设置模式: deviceId={}, mode={}", deviceId, mode);
        }
    }
}
