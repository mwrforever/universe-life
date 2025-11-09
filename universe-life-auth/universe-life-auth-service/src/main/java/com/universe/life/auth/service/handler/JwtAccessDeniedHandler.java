package com.universe.life.auth.service.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.universe.life.common.domain.Result;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * JWT访问拒绝处理器
 * 当已认证用户但权限不足访问受保护资源时，返回统一的错误响应
 * <p>
 * 区别于JwtAuthenticationExceptionHandler：
 * - 403 Forbidden: 用户已认证但权限不足
 * - 401 Unauthorized: 用户未认证或认证失败
 *
 * @author 毛伟然
 * @since 2025/11/3 14:35
 */
@Slf4j
@RequiredArgsConstructor
public class JwtAccessDeniedHandler implements AccessDeniedHandler {

    private final ObjectMapper objectMapper;

    @Override
    public void handle(HttpServletRequest request,
                       HttpServletResponse response,
                       AccessDeniedException accessDeniedException) throws IOException, ServletException {

        // 记录权限不足的访问尝试（便于安全审计）
        log.warn("用户访问被拒绝 - IP: {}, URI: {}, 用户: {}, 错误: {}",
                request.getRemoteAddr(),
                request.getRequestURI(),
                request.getUserPrincipal() != null ? request.getUserPrincipal().getName() : "未知用户",
                accessDeniedException.getMessage());

        // 设置响应状态码为403禁止访问
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);

        // 设置响应内容类型为JSON
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());

        // 设置缓存控制
        response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
        response.setHeader("Pragma", "no-cache");
        response.setDateHeader("Expires", 0);

        // 构建错误响应
        Result<Void> result = Result.error(
                HttpServletResponse.SC_FORBIDDEN,
                "权限不足，无法访问该资源: " + accessDeniedException.getMessage()
        );

        // 将错误响应对象序列化为JSON并写入响应体
        String jsonResponse = objectMapper.writeValueAsString(result);
        response.getWriter().write(jsonResponse);
        response.getWriter().flush();
    }
}