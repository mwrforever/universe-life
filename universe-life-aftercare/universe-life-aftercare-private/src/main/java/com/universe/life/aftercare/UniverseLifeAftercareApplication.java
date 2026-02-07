package com.universe.life.aftercare;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication(scanBasePackages = {
        "com.universe.life.aftercare",
        "com.universe.life.common",
        "com.universe.life.auth.common"
})
@EnableDiscoveryClient
@MapperScan("com.universe.life.aftercare.infrastructure.persistence.mapper")
public class UniverseLifeAftercareApplication {

    public static void main(String[] args) {
        SpringApplication.run(UniverseLifeAftercareApplication.class, args);
    }

}
