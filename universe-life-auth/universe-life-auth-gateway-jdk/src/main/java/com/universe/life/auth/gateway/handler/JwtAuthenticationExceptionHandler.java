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
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.server.ServerAuthenticationEntryPoint;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;

/**
 * JWT认证异常处理器 - WebFlux网关版本
 * 当用户未认证或认证失败时，返回统一的错误响应
 * <p>
 * 区别于JwtAccessDeniedHandler：
 * - 401 Unauthorized: 用户未认证或认证失败
 * - 403 Forbidden: 用户已认证但权限不足
 * 处理场景包括：
 * - JWT令牌缺失
 * - JWT令牌格式错误
 * - JWT令牌已过期
 * - JWT令牌签名无效
 * - 其他认证相关异常
 * 注意：这是网关环境版本，使用WebFlux的响应式编程模式
 * @author 毛伟然
 * @since 2025/11/11 16:22
 */
@Slf4j
@RequiredArgsConstructor
public class JwtAuthenticationExceptionHandler implements ServerAuthenticationEntryPoint {

    private final ObjectMapper objectMapper;

    @Override
    public Mono<Void> commence(ServerWebExchange exchange, AuthenticationException ex) {
        ServerHttpResponse response = exchange.getResponse();

        // 记录认证失败信息（便于安全监控和调试）
        log.warn("网关认证失败 - IP: {}, URI: {}, 错误: {}",
                exchange.getRequest().getRemoteAddress() != null ?
                        exchange.getRequest().getRemoteAddress().getAddress().getHostAddress() : "未知IP",
                exchange.getRequest().getURI().getPath(),
                ex.getMessage());

        // 设置响应状态码为401未授权
        response.setStatusCode(HttpStatus.UNAUTHORIZED);

        // 设置响应头
        response.getHeaders().set(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
        response.getHeaders().set(HttpHeaders.CACHE_CONTROL, "no-cache, no-store, must-revalidate");
        response.getHeaders().set(HttpHeaders.PRAGMA, "no-cache");
        response.getHeaders().set(HttpHeaders.EXPIRES, "0");

        try {
            // 构建错误响应
            Result<Void> result = Result.error(
                    HttpStatus.UNAUTHORIZED.value(),
                    "认证失败，请重新登录: " + ex.getMessage()
            );

            // 将错误响应对象序列化为JSON
            String jsonResponse = objectMapper.writeValueAsString(result);
            DataBuffer buffer = response.bufferFactory().wrap(jsonResponse.getBytes(StandardCharsets.UTF_8));

            return response.writeWith(Mono.just(buffer));

        } catch (Exception e) {
            log.error("序列化认证错误响应失败", e);
            // 如果序列化失败，返回简单的错误信息
            String errorMessage = "{\"code\":401,\"message\":\"认证失败\",\"timestamp\":" +
                    System.currentTimeMillis() + "}";
            DataBuffer buffer = response.bufferFactory().wrap(errorMessage.getBytes(StandardCharsets.UTF_8));
            return response.writeWith(Mono.just(buffer));
        }
    }
}
