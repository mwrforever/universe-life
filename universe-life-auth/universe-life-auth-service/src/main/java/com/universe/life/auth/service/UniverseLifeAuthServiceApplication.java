package com.universe.life.auth.service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class UniverseLifeAuthServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(UniverseLifeAuthServiceApplication.class, args);
    }

}
