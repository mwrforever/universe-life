package com.universe.life.auth.service;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;


@EnableScheduling
@SpringBootApplication
@MapperScan(basePackages = "com.universe.life.auth.service.mapper")
public class UniverseLifeAuthServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(UniverseLifeAuthServiceApplication.class, args);
    }

}
