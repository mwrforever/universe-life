package com.universe.life.auth.service.security.handler;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * JSON 格式的认证失败处理器
 * 当登录失败时，重定向到登录页面并显示错误信息
 *
 * @author universe-life
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JsonAuthenticationFailureHandler implements AuthenticationFailureHandler {


    @Override
    public void onAuthenticationFailure(HttpServletRequest request,
                                        HttpServletResponse response,
                                        AuthenticationException exception) throws IOException, ServletException {
        log.error("===== JsonAuthenticationFailureHandler被调用 =====");
        log.error("请求URI: {}", request.getRequestURI());
        log.error("错误信息: {}", exception.getMessage());
        log.error("异常类型: {}", exception.getClass().getName());

        // 获取错误信息
        String errorMessage = exception.getMessage();

        // 如果错误信息为空，使用默认提示
        if (errorMessage == null || errorMessage.trim().isEmpty()) {
            errorMessage = "用户名或密码错误，请重试";
        }

        // URL编码错误信息
        String encodedErrorMessage = java.net.URLEncoder.encode(errorMessage, "UTF-8");

        // 重定向到登录页面，并通过URL参数传递错误信息
        String redirectUrl = "/login?error=" + encodedErrorMessage;
        log.error("重定向到: {}", redirectUrl);
        response.sendRedirect(redirectUrl);
    }
}
