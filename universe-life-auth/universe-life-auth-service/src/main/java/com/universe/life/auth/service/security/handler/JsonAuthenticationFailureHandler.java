package com.universe.life.auth.service.security.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.universe.life.auth.common.domain.Result;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * JSON 格式的认证失败处理器
 * 当登录失败时，返回 JSON 格式的错误响应，而不是重定向到错误页面
 *
 * @author universe-life
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JsonAuthenticationFailureHandler implements AuthenticationFailureHandler {

    private final ObjectMapper objectMapper;

    @Override
    public void onAuthenticationFailure(HttpServletRequest request,
                                        HttpServletResponse response,
                                        AuthenticationException exception) throws IOException, ServletException {
        log.error("登录失败: {} - {}", request.getRequestURI(), exception.getMessage());

        // 设置响应状态码为 401 未授权
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);

        // 设置响应内容类型为 JSON
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());

        // 设置缓存控制
        response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
        response.setHeader("Pragma", "no-cache");
        response.setDateHeader("Expires", 0);

        // 构建错误响应
        Result<Void> result = Result.error(
                HttpServletResponse.SC_UNAUTHORIZED,
                exception.getMessage()
        );

        // 将错误响应对象序列化为 JSON 并写入响应体
        String jsonResponse = objectMapper.writeValueAsString(result);
        response.getWriter().write(jsonResponse);
        response.getWriter().flush();
    }
}
