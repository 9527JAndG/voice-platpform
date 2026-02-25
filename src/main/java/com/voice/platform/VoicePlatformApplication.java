package com.voice.platform;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 多平台智能音箱对接应用启动类
 * 支持：天猫精灵、小度音箱、小爱同学
 */
@SpringBootApplication
public class VoicePlatformApplication {

    public static void main(String[] args) {
        SpringApplication.run(VoicePlatformApplication.class, args);
        System.out.println("========================================");
        System.out.println("多平台智能音箱服务启动成功！");
        System.out.println("支持平台：天猫精灵、小爱同学、小度、AWS Alexa、Google Assistant");
        System.out.println("========================================");
    }
}
