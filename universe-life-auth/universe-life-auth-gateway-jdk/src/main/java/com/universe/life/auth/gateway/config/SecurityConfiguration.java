package com.universe.life.auth.gateway.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.universe.life.auth.gateway.handler.JwtAccessDeniedHandler;
import com.universe.life.auth.gateway.handler.JwtAuthenticationExceptionHandler;
import com.universe.life.auth.gateway.manager.JwtDecoderManager;
import com.universe.life.common.properties.AuthPathProperties;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoder;
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoders;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.ReactiveJwtAuthenticationConverterAdapter;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.ServerAuthenticationEntryPoint;
import org.springframework.security.web.server.authorization.ServerAccessDeniedHandler;
import reactor.core.publisher.Mono;

import static org.springframework.security.config.Customizer.withDefaults;

/**
 * WebFlux网关安全配置 - 使用Spring Security WebFlux
 *
 * @author 毛伟然
 * @since 2025/11/3 15:40
 */
@Configuration
@EnableWebFluxSecurity
public class SecurityConfiguration {

    private final ObjectMapper objectMapper;


    private String issuerUri;

    public SecurityConfiguration(
            @Value("${spring.security.oauth2.resourceserver.jwt.issuer-uri}") String issuerUri,
            ObjectMapper objectMapper
    ) {
        this.objectMapper = objectMapper;
        this.issuerUri = issuerUri;
    }


    @Bean
    public ReactiveJwtDecoder jwtDecoder() {
        ReactiveJwtDecoder reactiveJwtDecoder = ReactiveJwtDecoders.fromOidcIssuerLocation(issuerUri);
        JwtDecoderManager jwtDecoderManager = jwtDecoderManager();
        jwtDecoderManager.setDecoder(reactiveJwtDecoder);
        return jwt -> jwtDecoderManager.getDecoder().decode(jwt);
    }

    @Bean
    public JwtDecoderManager jwtDecoderManager() {
        return new JwtDecoderManager(issuerUri);
    }


    @Bean
    public SecurityWebFilterChain securityWebFilterChain(
            ServerHttpSecurity http,
            AuthPathProperties authPathProperties
    ) {
        // 设置请求权限
        http.authorizeExchange(exchanges -> {
            exchanges.pathMatchers(HttpMethod.OPTIONS, "/**").permitAll();
            if (authPathProperties.getEnable()) {
                exchanges.pathMatchers(authPathProperties.getExcludePath().toArray(new String[0])).permitAll();
                exchanges.anyExchange().authenticated();
            } else {
                exchanges.anyExchange().permitAll();
            }
        });

        // 关闭csrf校验
        http.csrf(ServerHttpSecurity.CsrfSpec::disable);

        // 配置跨域检验
        http.cors(withDefaults());

        // 配置统一的认证失败处理
        http.exceptionHandling(exceptions -> exceptions
                .authenticationEntryPoint(authenticationEntryPoint())
                .accessDeniedHandler(accessDeniedHandler())
        );

        // 配置资源服务器认证
        http.oauth2ResourceServer(oauth2 -> oauth2.jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthenticationConverter())));
        // 创建一个jwt认证过滤器
        return http.build();
    }

    /**
     * 权限映射器：把 JWT 里的 scope / authorities 字段转成 Spring Security 认识的权限对象
     * 默认 JwtAuthenticationConverter 已经支持：
     * scope -> SCOPE_*
     * authorities -> 原样复制
     * 如果有自定义字段可在这里扩展
     */
    private Converter<Jwt, Mono<AbstractAuthenticationToken>> jwtAuthenticationConverter() {
        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
        return new ReactiveJwtAuthenticationConverterAdapter(converter);
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public ServerAuthenticationEntryPoint authenticationEntryPoint() {
        return new JwtAuthenticationExceptionHandler(objectMapper);
    }

    @Bean
    public ServerAccessDeniedHandler accessDeniedHandler() {
        return new JwtAccessDeniedHandler(objectMapper);
    }

}
