package com.universe.life.pay;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication(scanBasePackages = {
        "com.universe.life.pay",
        "com.universe.life.common",
        "com.universe.life.auth.common"
})
@EnableDiscoveryClient
@EnableFeignClients(basePackages = {
        "com.universe.life.pay.client"
})
@MapperScan("com.universe.life.pay.infrastructure.persistence.mapper")
public class UniverseLifePayApplication {

    public static void main(String[] args) {
        SpringApplication.run(UniverseLifePayApplication.class, args);
    }

}
