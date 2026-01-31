package com.universe.life.trade.privacy.infrastructure.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Knife4j API文档配置
 *
 * @author universe
 */
@Configuration
public class Knife4jConfig {

    @Bean
    public OpenAPI tradeServiceOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("交易服务API文档")
                        .description("交易服务提供订单管理、申诉处理等功能")
                        .version("v1.0.0")
                        .contact(new Contact()
                                .name("Universe Life Team")
                                .email("support@universe-life.com")));
    }
}
