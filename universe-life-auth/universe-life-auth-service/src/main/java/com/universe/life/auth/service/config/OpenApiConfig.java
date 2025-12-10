package com.universe.life.auth.service.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * OpenAPI配置类
 *
 * @author Claude
 * @since 2025-11-18
 */
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Universe Life 认证服务 API")
                        .description("Universe Life微服务项目 - 认证授权服务接口文档")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("Universe Life Team")
                                .email("support@universe-life.com")
                                .url("https://www.universe-life.com"))
                        .license(new License()
                                .name("Apache 2.0")
                                .url("https://www.apache.org/licenses/LICENSE-2.0")));
    }
}