package com.universe.life.gateway.filter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.cors.reactive.CorsUtils;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * CORS 跨域过滤器
 *
 * <p>处理跨域预检请求（OPTIONS）和实际的跨域请求。
 * 与 YAML 中的 globalcors 配置配合使用，确保跨域请求正常工作。
 *
 * @author Claude
 * @since 2025/12/25
 */
@Slf4j
@Component
public class CorsGlobalFilter implements GlobalFilter, Ordered {

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String path = request.getURI().getPath();

        // 处理预检请求（OPTIONS）
        if (CorsUtils.isPreFlightRequest(request)) {
            log.info("CORS预检请求: 方法={}, 路径={}, Origin={}",
                    request.getMethod(), path, request.getHeaders().getOrigin());

            ServerHttpResponse response = exchange.getResponse();
            HttpHeaders headers = response.getHeaders();

            // 添加CORS响应头
            headers.add("Access-Control-Allow-Origin", request.getHeaders().getOrigin());
            headers.add("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS, PATCH");
            headers.add("Access-Control-Allow-Headers", "*");
            headers.add("Access-Control-Max-Age", "3600");
            headers.add("Access-Control-Allow-Credentials", "true");
            headers.add("Access-Control-Expose-Headers", "Content-Type, Set-Cookie, Authorization");

            // 返回204 No Content
            response.setStatusCode(HttpStatus.NO_CONTENT);
            return response.setComplete();
        }
 // 处理实际请求

        if (request.getHeaders().getOrigin() != null) {
            log.debug("CORS实际请求: 方法={}, 路径={}, Origin={}",
                    request.getMethod(), path, request.getHeaders().getOrigin());

            // 让后续的过滤器链处理CORS
            return chain.filter(exchange).then(Mono.fromRunnable(() -> {
                ServerHttpResponse response = exchange.getResponse();
                HttpHeaders responseHeaders = response.getHeaders();

                // 确保响应包含CORS头
                if (!responseHeaders.containsKey("Access-Control-Allow-Origin")) {
                    responseHeaders.add("Access-Control-Allow-Origin", request.getHeaders().getOrigin());
                }
                if (!responseHeaders.containsKey("Access-Control-Allow-Credentials")) {
                    responseHeaders.add("Access-Control-Allow-Credentials", "true");
                }
                if (!responseHeaders.containsKey("Access-Control-Expose-Headers")) {
                    responseHeaders.add("Access-Control-Expose-Headers", "Content-Type, Set-Cookie, Authorization");
                }
            }));
        }

        return chain.filter(exchange);
    }

    @Override
    public int getOrder() {
        // 最高优先级，确保在所有其他过滤器之前执行
        return Ordered.HIGHEST_PRECEDENCE;
    }
}
