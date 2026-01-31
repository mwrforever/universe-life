package com.universe.life.auth.common.config;

import com.universe.life.auth.common.util.AntRequestMatchUtil;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SecurityConfiguration {


    @Bean
    public AntRequestMatchUtil antRequestMatchUtil() {
        return new AntRequestMatchUtil();
    }

}
