package com.voice.platform.dto.alexa;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Alexa 设备发现端点
 * 用于设备发现响应，描述一个智能家居设备的完整信息
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DiscoveredEndpoint {
    
    /**
     * 端点 ID，即设备的唯一标识符
     * 例如：device-001
     */
    private String endpointId;
    
    /**
     * 制造商名称
     * 例如：Smart Home Demo
     */
    private String manufacturerName;
    
    /**
     * 友好名称，用户可见的设备名称
     * 例如：Living Room Vacuum
     */
    private String friendlyName;
    
    /**
     * 设备描述
     * 例如：Smart Robot Vacuum Cleaner
     */
    private String description;
    
    /**
     * 显示类别列表
     * 例如：["VACUUM_CLEANER"]
     * 其他可选值：LIGHT, SWITCH, THERMOSTAT 等
     */
    private List<String> displayCategories;
    
    /**
     * 能力列表，描述设备支持的功能
     */
    private List<Capability> capabilities;
    
    /**
     * 能力对象，描述设备的一个功能
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Capability {
        /**
         * 能力类型，固定为 "AlexaInterface"
         */
        private String type;
        
        /**
         * 接口名称，标识具体的功能接口
         * 例如：Alexa, Alexa.PowerController, Alexa.ModeController
         * 使用 interfaceName 避免与 Java 关键字 interface 冲突
         */
        @Builder.Default
        private String interfaceName = "Alexa";
        
        /**
         * 接口版本，通常为 "3"
         */
        private String version;
        
        /**
         * 实例名称，用于区分同一接口的多个实例
         * 例如：VacuumMode（清扫模式）
         */
        private String instance;
        
        /**
         * 属性配置，描述支持的属性
         */
        private Properties properties;
        
        /**
         * 能力资源，包含友好名称等本地化信息
         */
        private CapabilityResources capabilityResources;
        
        /**
         * 配置信息，例如模式列表
         */
        private Configuration configuration;
    }
    
    /**
     * 属性配置对象
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Properties {
        /**
         * 支持的属性列表
         */
        private List<SupportedProperty> supported;
        
        /**
         * 是否主动上报状态变化
         */
        private Boolean proactivelyReported;
        
        /**
         * 是否可查询状态
         */
        private Boolean retrievable;
    }
    
    /**
     * 支持的属性对象
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SupportedProperty {
        /**
         * 属性名称
         * 例如：powerState, mode
         */
        private String name;
    }
    
    /**
     * 能力资源对象
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CapabilityResources {
        /**
         * 友好名称列表，支持多语言
         */
        private List<FriendlyName> friendlyNames;
    }
    
    /**
     * 友好名称对象
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FriendlyName {
        /**
         * 类型，固定为 "text"
         * 序列化时会变成 @type
         */
        @Builder.Default
        private String type = "text";
        
        /**
         * 值对象，包含文本和语言
         */
        private Value value;
    }
    
    /**
     * 值对象
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Value {
        /**
         * 文本内容
         * 例如：Cleaning Mode, Auto
         */
        private String text;
        
        /**
         * 语言代码
         * 例如：en-US, zh-CN
         */
        private String locale;
    }
    
    /**
     * 配置对象
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Configuration {
        /**
         * 模式是否有序
         * false 表示模式之间没有顺序关系
         */
        private Boolean ordered;
        
        /**
         * 支持的模式列表
         */
        private List<SupportedMode> supportedModes;
    }
    
    /**
     * 支持的模式对象
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SupportedMode {
        /**
         * 模式值
         * 例如：Auto, Spot, Edge
         */
        private String value;
        
        /**
         * 模式资源，包含友好名称
         */
        private ModeResources modeResources;
    }
    
    /**
     * 模式资源对象
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ModeResources {
        /**
         * 友好名称列表，支持多语言
         */
        private List<FriendlyName> friendlyNames;
    }
    
    /**
     * 创建扫地机器人端点
     */
    public static DiscoveredEndpoint createVacuumEndpoint(String endpointId, String friendlyName) {
        List<Capability> capabilities = new ArrayList<>();
        
        // 1. Alexa 接口（必需）
        capabilities.add(Capability.builder()
            .type("AlexaInterface")
            .interfaceName("Alexa")
            .version("3")
            .build());
        
        // 2. PowerController 接口
        capabilities.add(Capability.builder()
            .type("AlexaInterface")
            .interfaceName("Alexa.PowerController")
            .version("3")
            .properties(Properties.builder()
                .supported(List.of(SupportedProperty.builder().name("powerState").build()))
                .proactivelyReported(true)
                .retrievable(true)
                .build())
            .build());
        
        // 3. ModeController 接口（清扫模式）
        List<SupportedMode> modes = new ArrayList<>();
        modes.add(SupportedMode.builder()
            .value("Auto")
            .modeResources(ModeResources.builder()
                .friendlyNames(List.of(FriendlyName.builder()
                    .type("text")
                    .value(Value.builder()
                        .text("Auto")
                        .locale("en-US")
                        .build())
                    .build()))
                .build())
            .build());
        modes.add(SupportedMode.builder()
            .value("Spot")
            .modeResources(ModeResources.builder()
                .friendlyNames(List.of(FriendlyName.builder()
                    .type("text")
                    .value(Value.builder()
                        .text("Spot")
                        .locale("en-US")
                        .build())
                    .build()))
                .build())
            .build());
        modes.add(SupportedMode.builder()
            .value("Edge")
            .modeResources(ModeResources.builder()
                .friendlyNames(List.of(FriendlyName.builder()
                    .type("text")
                    .value(Value.builder()
                        .text("Edge")
                        .locale("en-US")
                        .build())
                    .build()))
                .build())
            .build());
        
        capabilities.add(Capability.builder()
            .type("AlexaInterface")
            .interfaceName("Alexa.ModeController")
            .version("3")
            .instance("VacuumMode")
            .properties(Properties.builder()
                .supported(List.of(SupportedProperty.builder().name("mode").build()))
                .proactivelyReported(true)
                .retrievable(true)
                .build())
            .capabilityResources(CapabilityResources.builder()
                .friendlyNames(List.of(FriendlyName.builder()
                    .type("text")
                    .value(Value.builder()
                        .text("Cleaning Mode")
                        .locale("en-US")
                        .build())
                    .build()))
                .build())
            .configuration(Configuration.builder()
                .ordered(false)
                .supportedModes(modes)
                .build())
            .build());
        
        // 4. EndpointHealth 接口
        capabilities.add(Capability.builder()
            .type("AlexaInterface")
            .interfaceName("Alexa.EndpointHealth")
            .version("3")
            .properties(Properties.builder()
                .supported(List.of(SupportedProperty.builder().name("connectivity").build()))
                .proactivelyReported(true)
                .retrievable(true)
                .build())
            .build());
        
        return DiscoveredEndpoint.builder()
            .endpointId(endpointId)
            .manufacturerName("Smart Home Demo")
            .friendlyName(friendlyName)
            .description("Smart Robot Vacuum Cleaner")
            .displayCategories(List.of("VACUUM_CLEANER"))
            .capabilities(capabilities)
            .build();
    }
}
