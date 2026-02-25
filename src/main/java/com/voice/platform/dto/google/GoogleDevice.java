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
 * Google Assistant 设备对象
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GoogleDevice {
    
    private String id;
    private String type;
    private List<String> traits;
    private DeviceName name;
    private Boolean willReportState;
    private String roomHint;
    private DeviceInfo deviceInfo;
    private Map<String, Object> attributes;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DeviceName {
        private List<String> defaultNames;
        private String name;
        private List<String> nicknames;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DeviceInfo {
        private String manufacturer;
        private String model;
        private String hwVersion;
        private String swVersion;
    }
    
    /**
     * 创建扫地机器人设备
     */
    public static GoogleDevice createVacuumDevice(String deviceId, String deviceName, String roomHint) {
        // 设备特征
        List<String> traits = new ArrayList<>();
        traits.add("action.devices.traits.StartStop");
        traits.add("action.devices.traits.OnOff");
        traits.add("action.devices.traits.Dock");
        traits.add("action.devices.traits.Modes");
        traits.add("action.devices.traits.Locator");
        traits.add("action.devices.traits.EnergyStorage");
        
        // 设备名称
        List<String> defaultNames = new ArrayList<>();
        defaultNames.add("Smart Vacuum");
        
        List<String> nicknames = new ArrayList<>();
        nicknames.add("vacuum");
        nicknames.add("robot");
        
        DeviceName name = DeviceName.builder()
            .defaultNames(defaultNames)
            .name(deviceName)
            .nicknames(nicknames)
            .build();
        
        // 设备信息
        DeviceInfo deviceInfo = DeviceInfo.builder()
            .manufacturer("Smart Home Demo")
            .model("V1.0")
            .hwVersion("1.0")
            .swVersion("1.0.0")
            .build();
        
        // 设备属性
        Map<String, Object> attributes = new HashMap<>();
        attributes.put("pausable", true);
        
        // 模式配置
        List<Map<String, Object>> availableModes = new ArrayList<>();
        Map<String, Object> cleanMode = new HashMap<>();
        cleanMode.put("name", "clean_mode");
        
        List<Map<String, Object>> nameValues = new ArrayList<>();
        Map<String, Object> nameValue = new HashMap<>();
        nameValue.put("name_synonym", List.of("cleaning mode", "clean mode"));
        nameValue.put("lang", "en");
        nameValues.add(nameValue);
        cleanMode.put("name_values", nameValues);
        
        List<Map<String, Object>> settings = new ArrayList<>();
        
        // Auto 模式
        Map<String, Object> autoSetting = new HashMap<>();
        autoSetting.put("setting_name", "auto");
        List<Map<String, Object>> autoValues = new ArrayList<>();
        Map<String, Object> autoValue = new HashMap<>();
        autoValue.put("setting_synonym", List.of("automatic", "auto"));
        autoValue.put("lang", "en");
        autoValues.add(autoValue);
        autoSetting.put("setting_values", autoValues);
        settings.add(autoSetting);
        
        // Spot 模式
        Map<String, Object> spotSetting = new HashMap<>();
        spotSetting.put("setting_name", "spot");
        List<Map<String, Object>> spotValues = new ArrayList<>();
        Map<String, Object> spotValue = new HashMap<>();
        spotValue.put("setting_synonym", List.of("spot cleaning", "spot"));
        spotValue.put("lang", "en");
        spotValues.add(spotValue);
        spotSetting.put("setting_values", spotValues);
        settings.add(spotSetting);
        
        // Edge 模式
        Map<String, Object> edgeSetting = new HashMap<>();
        edgeSetting.put("setting_name", "edge");
        List<Map<String, Object>> edgeValues = new ArrayList<>();
        Map<String, Object> edgeValue = new HashMap<>();
        edgeValue.put("setting_synonym", List.of("edge cleaning", "edge"));
        edgeValue.put("lang", "en");
        edgeValues.add(edgeValue);
        edgeSetting.put("setting_values", edgeValues);
        settings.add(edgeSetting);
        
        cleanMode.put("settings", settings);
        cleanMode.put("ordered", false);
        availableModes.add(cleanMode);
        
        attributes.put("availableModes", availableModes);
        attributes.put("isRechargeable", true);
        attributes.put("queryOnlyEnergyStorage", true);
        
        return GoogleDevice.builder()
            .id(deviceId)
            .type("action.devices.types.VACUUM")
            .traits(traits)
            .name(name)
            .willReportState(true)
            .roomHint(roomHint)
            .deviceInfo(deviceInfo)
            .attributes(attributes)
            .build();
    }
}
