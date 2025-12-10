package com.universe.life.task;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@EnableDiscoveryClient
@MapperScan("com.universe.life.task.mapper")
@SpringBootApplication
public class UniverseLifeTaskApplication {

    public static void main(String[] args) {
        SpringApplication.run(UniverseLifeTaskApplication.class, args);
    }

}
