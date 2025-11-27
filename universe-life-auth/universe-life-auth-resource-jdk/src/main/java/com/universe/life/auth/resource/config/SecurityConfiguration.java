package com.universe.life.auth.resource.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.universe.life.api.client.UserClient;
import com.universe.life.auth.resource.filter.LoginFilter;
import com.universe.life.auth.resource.handler.JwtAccessDeniedHandler;
import com.universe.life.auth.resource.handler.JwtAuthenticationExceptionHandler;
import com.universe.life.auth.resource.service.UserAuthInfoService;
import com.universe.life.common.properties.AuthPathProperties;
import com.universe.life.common.util.AntRequestMatchUtil;
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
     * 默认安全过滤器链 - 优先级为2
     * 处理非OAuth2授权服务器的其他HTTP请求
     */
    @Bean
    @Order(3)
    public SecurityFilterChain defaultSecurityFilterChain(
            HttpSecurity http,
            AccessDeniedHandler jwtAccessDeniedHandler,
            AuthenticationEntryPoint jwtAuthenticationExceptionHandler) throws Exception {
        log.info("配置默认安全过滤器链");
        return http
                // 排除登录、注册和静态资源路径，避免与authenficationSecurityFilterChain冲突
                .securityMatcher("/**")
                // 配置请求授权规则
                .authorizeHttpRequests(authorize -> {
                    if (authPathProperties.getEnable()) {
                        // 应用配置文件中的排除路径
                        authorize.requestMatchers(
                                authPathProperties.getExcludePath().stream()
                                        .map(AntPathRequestMatcher::new)
                                        .toArray(AntPathRequestMatcher[]::new)
                        ).permitAll();

                        authorize.anyRequest().authenticated();
                    } else {
                        authorize.anyRequest().permitAll();
                    }
                })

                // 完全禁用CSRF保护 - 适用于RESTful API服务
                .csrf(AbstractHttpConfigurer::disable)

                // 配置会话管理 - 使用无状态会话（STATELESS）
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                // 配置异常处理 - 设置认证失败和权限不足的处理逻辑
                .exceptionHandling(exceptions -> exceptions
                        .authenticationEntryPoint(jwtAuthenticationExceptionHandler)
                        .accessDeniedHandler(jwtAccessDeniedHandler))
                .addFilterBefore(new LoginFilter(stringRedisTemplate, authPathProperties, antRequestMatchUtil), UsernamePasswordAuthenticationFilter.class)
                // 配置HTTP安全头 - 开发阶段简化配置
                .headers((headers) -> {
                })
                .build();  // 构建并返回默认安全过滤器链对象


        // ================================================================================
        // 生产环境默认安全过滤器链配置 - 注释状态，需要时启用
        // ================================================================================
        /*
        // 生产环境完整安全配置
        return http
                // 匹配所有请求（除了已被授权服务器过滤器链处理的OAuth2端点）
                .securityMatcher("/**")

                // 配置请求授权规则 - 生产环境更严格的访问控制
                .authorizeHttpRequests(authorize -> authorize
                        // 健康检查端点 - 允许公开访问，用于服务监控
                        .requestMatchers("/actuator/health", "/actuator/info").permitAll()
                        // 错误页面和网站图标 - 允许公开访问
                        .requestMatchers("/error", "/favicon.ico").permitAll()
                        // 静态资源文件 - 生产环境需要认证访问
                        .requestMatchers("/static/**", "/css/**", "/js/**", "/images/**").authenticated()
                        // API文档 - 生产环境禁用或需要管理员权限
                        .requestMatchers("/v3/api-docs/**", "/swagger-ui/**", "/doc.html").hasRole("ADMIN")
                        // 管理端点 - 需要管理员权限访问
                        .requestMatchers("/actuator/**", "/admin/**").hasRole("ADMIN")
                        // 其他所有请求 - 需要认证
                        .anyRequest().authenticated())

                // 生产环境CSRF保护 - 对关键操作启用CSRF
                .csrf(csrf -> csrf
                        .ignoringRequestMatchers("/actuator/health", "/actuator/info"))

                // 配置会话管理 - 生产环境启用会话保护
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
                        .maximumSessions(10)
                        .maxSessionsPreventsLogin(true))

                // 配置异常处理 - 设置认证失败和权限不足的处理逻辑
                .exceptionHandling(exceptions -> exceptions
                        .authenticationEntryPoint(jwtAuthenticationExceptionHandler)
                        .accessDeniedHandler(jwtAccessDeniedHandler))

                // 完整的HTTP安全头配置 - 生产环境启用
                .headers(headers -> headers
                        // 内容安全策略 - 防止XSS攻击
                        .contentSecurityPolicy(csp -> csp
                                .policyDirectives("default-src 'self'; script-src 'self'; style-src 'self'; img-src 'self' data: https:; font-src 'self'; connect-src 'self'"))

                        // 跨域嵌入保护 - 防止点击劫持
                        .frameOptions(frame -> frame.sameOrigin())

                        // 传输安全 - 强制HTTPS
                        .hsts(hsts -> hsts
                                .maxAgeInSeconds(31536000)  // 1年
                                .includeSubDomains(true))

                        // 内容类型选项 - 防止MIME类型嗅探
                        .contentTypeOptions(contentType -> {})

                        // XSS保护 - 启用浏览器XSS过滤器
                        .xssProtection(xss -> xss.headerValue(HeaderWriterFilter.XXSSProtectionMode.ENABLED_MODE_BLOCK))

                        // 引用策略 - 防止敏感信息泄露
                        .referrerPolicy(referrer -> referrer.policy(ReferrerPolicy.STRICT_ORIGIN_WHEN_CROSS_ORIGIN))
                )
                .build();  // 构建并返回生产环境安全过滤器链对象
        */
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
    public UserDetailsService userDetailsService(UserClient userClient) {
        return new UserAuthInfoService(userClient, stringRedisTemplate);
    }


}
