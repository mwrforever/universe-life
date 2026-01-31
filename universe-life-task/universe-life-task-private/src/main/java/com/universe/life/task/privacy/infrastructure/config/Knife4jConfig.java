package com.universe.life.task.privacy.infrastructure.config;

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
    public OpenAPI taskServiceOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("任务服务API文档")
                        .description("任务服务提供任务发布、审核、分类管理等功能")
                        .version("v1.0.0")
                        .contact(new Contact()
                                .name("Universe Life Team")
                                .email("support@universe-life.com")));
    }
}
