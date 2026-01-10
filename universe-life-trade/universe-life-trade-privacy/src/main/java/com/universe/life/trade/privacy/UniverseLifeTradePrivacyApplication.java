package com.universe.life.trade.privacy;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * 交易服务启动类
 *
 * @author universe-life
 */
@SpringBootApplication(scanBasePackages = {
        "com.universe.life.trade",
        "com.universe.life.common",
        "com.universe.life.auth.common",
        "com.universe.life.task.api"
})
@EnableDiscoveryClient
@EnableFeignClients(basePackages = "com.universe.life.task.api.client")
@MapperScan("com.universe.life.trade.privacy.mapper")
public class UniverseLifeTradePrivacyApplication {

    public static void main(String[] args) {
        SpringApplication.run(UniverseLifeTradePrivacyApplication.class, args);
    }

}
