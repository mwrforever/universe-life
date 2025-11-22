package com.universe.life.gateway.filter;

import com.universe.life.common.constants.JwtConstants;
import com.universe.life.common.exception.AuthException;
import com.universe.life.common.message.ExceptionMessage;
import com.universe.life.common.properties.AuthPathProperties;
import com.universe.life.common.util.AntRequestMatchUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.Objects;
import java.util.function.Consumer;

/**
 * 全局网关过滤器
 *
 * <p>用于获取基于Spring Security过滤器链处理后的用户数据，
 * 并将用户信息传递给后续微服务。</p>
 *
 * @author 毛伟然
 * @since 2025/11/13 20:50
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class GlobalGatewayFilter implements GlobalFilter, Ordered {

    private final AntRequestMatchUtil antRequestMatchUtil;

    private final AuthPathProperties authPathProperties;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String path = exchange.getRequest().getURI().getRawPath();
        log.info("GlobalGatewayFilter: 开始处理请求：{}", path);

        // 排除不需要处理的路径
        if (!authPathProperties.getEnable() || antRequestMatchUtil.matchAny(path, authPathProperties.getExcludePath())) {
            // 将请求中的前缀去除
            return chain.filter(exchange);
        }

        // 安全地获取SecurityContext，如果没有则继续执行
        return ReactiveSecurityContextHolder.getContext()
                .cast(SecurityContext.class)
                .map(SecurityContext::getAuthentication)
                .flatMap(authentication -> {
                    // 处理用户信息并转发（authentication可能为null）
                    return processRequest(exchange, chain, authentication);
                })
                .onErrorResume(throwable -> {
                    log.error("GlobalGatewayFilter: 处理请求时发生异常 - 路径: {}", path, throwable);
                    // 发生异常时继续执行原始请求
                    return chain.filter(exchange);
                });
    }


    /**
     * 处理请求，添加用户信息到请求头
     *
     * @param exchange       交换对象
     * @param chain          过滤器链
     * @param authentication 认证信息
     * @return Mono<Void>
     */
    private Mono<Void> processRequest(ServerWebExchange exchange, GatewayFilterChain chain, Authentication authentication) {
        ServerHttpRequest request = exchange.getRequest();
        String path = request.getURI().getRawPath();
        try {
            // 构建修改后的请求
            Consumer<HttpHeaders> headersMapper = headers -> {
                if (authentication != null && authentication.isAuthenticated()) {
                    log.info("GlobalGatewayFilter: 检测到已认证用户: {}", authentication.getName());
                    // 根据认证类型处理用户信息
                    if (!(authentication instanceof JwtAuthenticationToken jwtAuthenticationToken)) {
                        throw new AuthException.AuthenticationException(ExceptionMessage.LOGIN_REQUIRED);
                    }
                    // 获取JWT属性，确保不为null
                    var tokenAttributes = jwtAuthenticationToken.getTokenAttributes();
                    if (tokenAttributes == null) {
                        log.warn("GlobalGatewayFilter: JWT Token属性为空");
                        return;
                    }
                    Object userId = tokenAttributes.get("user_id");
                    if (Objects.isNull(userId)) {
                        throw new AuthException.AuthenticationException(ExceptionMessage.AUTH_FAILED);
                    }
                    // 将用户信息添加到请求头，确保参数不为null
                    headers.add(JwtConstants.USER_INFO, String.valueOf(userId));
                }
            };
            ServerHttpRequest.Builder mutate = request.mutate();

            // 创建修改后的请求
            ServerHttpRequest modifiedRequest = mutate
                    .headers(headersMapper)
                    .build();

            // 继续执行过滤器链
            return chain.filter(exchange.mutate().request(modifiedRequest).build())
                    .doOnSuccess(aVoid -> log.info("GlobalGatewayFilter: 请求处理完成 - 路径: {}", path))
                    .doOnError(error -> log.error("GlobalGatewayFilter: 请求处理失败 - 路径: {}, 错误: {}", path, error.getMessage()));
        } catch (Exception e) {
            log.error("GlobalGatewayFilter: 处理请求时发生异常 - 路径: {}", path, e);
            // 发生异常时，继续执行原始请求
            return chain.filter(exchange);
        }
    }

    @Override
    public int getOrder() {
        // 设置较低的优先级，确保在Security过滤器之后执行
        return -90;
    }
}
