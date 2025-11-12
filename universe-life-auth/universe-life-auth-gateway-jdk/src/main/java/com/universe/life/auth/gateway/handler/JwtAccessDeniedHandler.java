package com.universe.life.auth.gateway.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.universe.life.common.domain.Result;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.server.authorization.ServerAccessDeniedHandler;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.security.Principal;

/**
 * JWT访问拒绝处理器 - WebFlux网关版本
 * 当已认证用户但权限不足访问受保护资源时，返回统一的错误响应
 * <p>
 * 区别于JwtAuthenticationExceptionHandler：
 * - 403 Forbidden: 用户已认证但权限不足
 * - 401 Unauthorized: 用户未认证或认证失败
 * 注意：这是网关环境版本，使用WebFlux的响应式编程模式
 *
 * @author 毛伟然
 * @since 2025/11/11 16:24
 */
@Slf4j
@RequiredArgsConstructor
public class JwtAccessDeniedHandler implements ServerAccessDeniedHandler {

    private final ObjectMapper objectMapper;

    @Override
    public Mono<Void> handle(ServerWebExchange exchange, AccessDeniedException denied) {
        ServerHttpResponse response = exchange.getResponse();

        // 创建一个匿名的Principal对象作为默认值
        Principal anonymousPrincipal = () -> "未知用户";

        // 使用响应式方式获取用户信息进行日志记录
        return exchange.getPrincipal()
                .defaultIfEmpty(anonymousPrincipal)
                .doOnNext(principal -> {
                    // 记录权限不足的访问尝试（便于安全审计）
                    log.warn("网关访问被拒绝 - IP: {}, URI: {}, 用户: {}, 错误: {}",
                            exchange.getRequest().getRemoteAddress() != null ?
                                    exchange.getRequest().getRemoteAddress().getAddress().getHostAddress() : "未知IP",
                            exchange.getRequest().getURI().getPath(),
                            principal.getName(),
                            denied.getMessage());
                })
                .flatMap(principal -> {
                    // 设置响应状态码为403禁止访问
                    response.setStatusCode(HttpStatus.FORBIDDEN);

                    // 设置响应头
                    response.getHeaders().set(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
                    response.getHeaders().set(HttpHeaders.CACHE_CONTROL, "no-cache, no-store, must-revalidate");
                    response.getHeaders().set(HttpHeaders.PRAGMA, "no-cache");
                    response.getHeaders().set(HttpHeaders.EXPIRES, "0");

                    try {
                        // 构建错误响应
                        Result<Void> result = Result.error(
                                HttpStatus.FORBIDDEN.value(),
                                "权限不足，无法访问该资源: " + denied.getMessage()
                        );

                        // 将错误响应对象序列化为JSON
                        String jsonResponse = objectMapper.writeValueAsString(result);
                        DataBuffer buffer = response.bufferFactory().wrap(jsonResponse.getBytes(StandardCharsets.UTF_8));

                        return response.writeWith(Mono.just(buffer));

                    } catch (Exception e) {
                        log.error("序列化错误响应失败", e);
                        // 如果序列化失败，返回简单的错误信息
                        String errorMessage = "{\"code\":403,\"message\":\"权限不足\",\"timestamp\":" +
                                System.currentTimeMillis() + "}";
                        DataBuffer buffer = response.bufferFactory().wrap(errorMessage.getBytes(StandardCharsets.UTF_8));
                        return response.writeWith(Mono.just(buffer));
                    }
                });
    }
}
