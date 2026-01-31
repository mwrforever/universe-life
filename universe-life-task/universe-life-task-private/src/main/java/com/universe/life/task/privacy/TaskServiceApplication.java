package com.universe.life.task.privacy;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * 任务服务启动类
 * 
 * @author Universe Life Team
 * @since 2025-01-18
 */
@SpringBootApplication(scanBasePackages = {
    "com.universe.life.task.privacy",
    "com.universe.life.common"
})
@EnableDiscoveryClient
@EnableFeignClients(basePackages = "com.universe.life.task.privacy.interfaces.facade")
@MapperScan("com.universe.life.task.privacy.infrastructure.persistence.mapper")
public class TaskServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(TaskServiceApplication.class, args);
    }
}
