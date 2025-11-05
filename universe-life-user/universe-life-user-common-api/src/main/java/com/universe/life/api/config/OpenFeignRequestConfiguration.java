package com.universe.life.api.config;

import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Configuration;

/**
 * @author 毛伟然
 * @since 2025/11/4 13:33
 */
@EnableFeignClients(basePackages = "com.universe.life.api")
@Configuration
public class OpenFeignRequestConfiguration {

}
