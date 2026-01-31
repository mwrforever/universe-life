package com.universe.life.auth.resource.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.universe.life.user.common.api.client.UserClient;
import com.universe.life.auth.common.properties.AuthPathProperties;
import com.universe.life.auth.common.util.AntRequestMatchUtil;
import com.universe.life.auth.resource.filter.LoginFilter;
import com.universe.life.auth.resource.handler.JwtAccessDeniedHandler;
import com.universe.life.auth.resource.handler.JwtAuthenticationExceptionHandler;
import com.universe.life.auth.resource.service.AdminAuthInfoService;
import com.universe.life.auth.resource.service.UserAuthInfoService;
import com.universe.life.auth.resource.util.CommonSecurityConfigUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

/**
 * @author 毛伟然
 * @since 2025/11/1 17:24
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@EnableConfigurationProperties(AuthPathProperties.class)
@RequiredArgsConstructor
@Slf4j
public class SecurityConfiguration {

    private final StringRedisTemplate stringRedisTemplate;
    private final ObjectMapper objectMapper;
    private final AuthPathProperties authPathProperties;
    private final AntRequestMatchUtil antRequestMatchUtil;

    /**
     * 默认安全过滤器链 - 优先级为3
     * 处理非OAuth2授权服务器的其他HTTP请求
     */
    @Bean
    @Order(3)
    public SecurityFilterChain defaultSecurityFilterChain(
            HttpSecurity http,
            AccessDeniedHandler jwtAccessDeniedHandler,
            AuthenticationEntryPoint jwtAuthenticationExceptionHandler,
            UserDetailsService adminAuthInfoService
    ) throws Exception {
        log.info("配置默认安全过滤器链");
        return http
                // 排除登录、注册和静态资源路径，避免与authenficationSecurityFilterChain冲突
                .securityMatcher("/**")
                // 配置请求授权规则
                .authorizeHttpRequests(authorize -> {
                    if (authPathProperties.getEnable()) {
                        // 应用配置文件中的排除路径
                        authorize.requestMatchers(authPathProperties.getExcludePath().stream().map(AntPathRequestMatcher::new).toArray(AntPathRequestMatcher[]::new)).permitAll();

                        authorize.anyRequest().authenticated();
                    } else {
                        authorize.anyRequest().permitAll();
                    }
                })

                // 完全禁用CSRF保护 - 适用于RESTful API服务
                .csrf(AbstractHttpConfigurer::disable)

                // 配置会话管理 - 使用无状态会话（STATELESS）
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                // 配置异常处理 - 设置认证失败和权限不足的处理逻辑
                .exceptionHandling(exceptions -> exceptions.authenticationEntryPoint(jwtAuthenticationExceptionHandler).accessDeniedHandler(jwtAccessDeniedHandler)).addFilterBefore(new LoginFilter(stringRedisTemplate, authPathProperties, antRequestMatchUtil,adminAuthInfoService  ), UsernamePasswordAuthenticationFilter.class)
                // 配置HTTP安全头 - 开发阶段简化配置
                .headers(CommonSecurityConfigUtil::getPermissionsPolicyConfig)
                .build();  // 构建并返回安全过滤器链对象
    }


    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationEntryPoint authenticationEntryPoint() {
        return new JwtAuthenticationExceptionHandler(objectMapper);
    }

    @Bean
    public AccessDeniedHandler accessDeniedHandler() {
        return new JwtAccessDeniedHandler(objectMapper);
    }


    @Bean
    public UserDetailsService userAuthInfoService(UserClient userClient, StringRedisTemplate stringRedisTemplate) {
        return new UserAuthInfoService(userClient, stringRedisTemplate);
    }

    @Bean
    public UserDetailsService adminAuthInfoService(UserClient userClient, StringRedisTemplate stringRedisTemplate) {
        return new AdminAuthInfoService(userClient, stringRedisTemplate);
    }



}
