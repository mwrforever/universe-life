package com.universe.life.auth.resource.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.universe.life.auth.resource.filter.LoginFilter;
import com.universe.life.auth.resource.properties.AuthPathProperties;
import com.universe.life.auth.resource.service.JwtAccessDeniedHandler;
import com.universe.life.auth.resource.service.JwtAuthenticationExceptionHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.filter.OncePerRequestFilter;

import static org.springframework.security.config.Customizer.withDefaults;

/**
 * @author 毛伟然
 * @since 2025/11/1 17:24
 */
@Configuration
@EnableWebSecurity
@EnableConfigurationProperties(AuthPathProperties.class)
@RequiredArgsConstructor
public class SecurityConfiguration {

    @Bean
    public SecurityFilterChain filterChain(
            HttpSecurity http,
            AuthPathProperties authPathProperties,
            JwtAuthenticationExceptionHandler jwtAuthenticationExceptionHandler,
            JwtAccessDeniedHandler jwtAccessDeniedHandler,
            LoginFilter loginFilter
    ) throws Exception {
        // 设置请求权限
        http.authorizeHttpRequests(auth -> {
            auth.requestMatchers(HttpMethod.OPTIONS, "/**").permitAll();
            if (authPathProperties.getEnable()) {
                auth.requestMatchers(authPathProperties.getExcludePath().toArray(new String[0])).permitAll();
                auth.requestMatchers("/**").authenticated();
            } else {
                auth.anyRequest().permitAll();
            }
        });
        // 关闭csrf校验
        http.csrf(AbstractHttpConfigurer::disable);
        // 使用无状态会话
        http.sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS));
        // 配置跨域检验
        http.cors(withDefaults());
        // 配置统一的认证失败处理
        http.exceptionHandling(exception -> {
            exception.authenticationEntryPoint(jwtAuthenticationExceptionHandler);
            exception.accessDeniedHandler(jwtAccessDeniedHandler);
        });
        // 创建一个jwt认证过滤器
        http.addFilterBefore(loginFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }


    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationEntryPoint authenticationEntryPoint(ObjectMapper objectMapper) {
        return new JwtAuthenticationExceptionHandler(objectMapper);
    }

    @Bean
    public AccessDeniedHandler accessDeniedHandler(ObjectMapper objectMapper) {
        return new JwtAccessDeniedHandler(objectMapper);
    }

    @Bean
    public OncePerRequestFilter loginFilter(StringRedisTemplate stringRedisTemplate) {
        return new LoginFilter(stringRedisTemplate);
    }


}
