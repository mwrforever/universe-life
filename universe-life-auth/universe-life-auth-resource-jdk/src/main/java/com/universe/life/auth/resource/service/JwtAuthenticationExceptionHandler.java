package com.universe.life.auth.resource.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.universe.life.common.domain.Result;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * JWT认证异常处理器
 * 当用户未提供有效的JWT令牌访问受保护资源时，返回统一的错误响应
 *
 * @author 毛伟然
 * @since 2025/11/3 14:10
 */
@Component
@RequiredArgsConstructor
public class JwtAuthenticationExceptionHandler implements AuthenticationEntryPoint {

    private final ObjectMapper objectMapper;

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
                         AuthenticationException authException) throws IOException, ServletException {

        // 设置响应状态码为401未授权
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);

        // 设置响应内容类型为JSON
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());

        // 设置缓存控制
        response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
        response.setHeader("Pragma", "no-cache");
        response.setDateHeader("Expires", 0);

        // 构建错误响应
        Result<Void> result = Result.error(
                HttpServletResponse.SC_UNAUTHORIZED,
                "认证失败: " + authException.getMessage()
        );

        // 将错误响应对象序列化为JSON并写入响应体
        String jsonResponse = objectMapper.writeValueAsString(result);
        response.getWriter().write(jsonResponse);
        response.getWriter().flush();
    }
}
