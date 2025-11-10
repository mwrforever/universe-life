package com.universe.life.user.privacy;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;


@MapperScan("com.universe.life.user.privacy.mapper")
@SpringBootApplication
public class UniverseLifeUserPrivacyApplication {

    public static void main(String[] args) {
        SpringApplication.run(UniverseLifeUserPrivacyApplication.class, args);
    }

}
