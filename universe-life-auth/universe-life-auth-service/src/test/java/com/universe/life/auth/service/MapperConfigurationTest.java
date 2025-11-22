package com.universe.life.auth.service;

import com.universe.life.auth.service.mapper.Oauth2JwkMapper;
import com.universe.life.auth.service.mapper.TokenBlacklistMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Mapper配置测试
 * 验证MyBatis Mapper Bean是否正确注册到Spring容器
 */
@SpringBootTest
@TestPropertySource(properties = {
    "spring.cloud.nacos.config.enabled=false",
    "spring.datasource.url=jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1",
    "spring.datasource.driver-class-name=org.h2.Driver",
    "spring.jpa.hibernate.ddl-auto=create-drop"
})
class MapperConfigurationTest {

    @Autowired
    private Oauth2JwkMapper oauth2JwkMapper;

    @Autowired
    private TokenBlacklistMapper tokenBlacklistMapper;

    @Test
    void oauth2JwkMapperShouldBeNotNull() {
        assertThat(oauth2JwkMapper).isNotNull();
        System.out.println("✅ Oauth2JwkMapper Bean 注入成功");
    }

    @Test
    void tokenBlacklistMapperShouldBeNotNull() {
        assertThat(tokenBlacklistMapper).isNotNull();
        System.out.println("✅ TokenBlacklistMapper Bean 注入成功");
    }
}