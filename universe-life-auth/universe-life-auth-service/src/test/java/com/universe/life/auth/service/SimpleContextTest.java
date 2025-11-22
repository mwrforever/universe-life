package com.universe.life.auth.service;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

/**
 * 简化的上下文测试
 * 验证应用上下文能否正常加载
 */
@SpringBootTest
@TestPropertySource(properties = {
    "spring.cloud.nacos.config.enabled=false",
    "spring.cloud.nacos.discovery.enabled=false",
    "spring.datasource.url=jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1",
    "spring.datasource.driver-class-name=org.h2.Driver",
    "spring.datasource.username=sa",
    "spring.datasource.password=",
    "mybatis-plus.configuration.map-underscore-to-camel-case=true",
    "logging.level.com.universe.life=DEBUG"
})
class SimpleContextTest {

    @Test
    void contextLoads() {
        // 如果上下文能正常加载，这个测试就会通过
        System.out.println("✅ Spring 上下文加载成功");
    }
}