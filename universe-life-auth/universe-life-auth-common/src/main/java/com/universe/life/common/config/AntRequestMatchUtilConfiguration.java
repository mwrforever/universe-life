package com.universe.life.common.config;

import com.universe.life.common.util.AntRequestMatchUtil;
import com.universe.life.common.util.PermissionMatcher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * AntRequestMatchUtil 工具类配置
 *
 * <p>为 AntRequestMatchUtil 提供 Spring 容器管理支持，便于依赖注入使用。
 * 支持通过配置文件自定义路径分隔符等参数。
 * </p>
 *
 * @author 毛伟然
 * @since 2025/11/18 14:45
 */
@Configuration
public class AntRequestMatchUtilConfiguration {

    @Bean
    public AntRequestMatchUtil antRequestMatchUtil() {
        return new AntRequestMatchUtil();
    }

    @Bean("pm")
    public PermissionMatcher permissionMatcher() {
        return new PermissionMatcher();
    }
}