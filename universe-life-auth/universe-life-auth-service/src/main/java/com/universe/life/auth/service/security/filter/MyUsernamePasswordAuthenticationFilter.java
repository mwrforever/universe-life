package com.universe.life.auth.service.security.filter;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.universe.life.auth.service.security.token.UsernamePasswordAuthenticationToken;
import com.universe.life.common.exception.AuthException;
import com.universe.life.common.message.ExceptionMessage;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AbstractAuthenticationProcessingFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

import java.io.IOException;

/**
 * 用户名密码认证过滤器
 * 支持JSON格式的用户名密码登录
 *
 * @author 毛伟然
 * @since 2025/11/21
 */
@Slf4j
public class MyUsernamePasswordAuthenticationFilter extends AbstractAuthenticationProcessingFilter {

    private static final String method = "POST";
    private static final String USERNAME = "username";
    private static final String PASSWORD = "password";

    public MyUsernamePasswordAuthenticationFilter(AuthenticationManager authenticationManager) {
        super(new AntPathRequestMatcher("/login/password", "POST"));
        setAuthenticationManager(authenticationManager);
    }

    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response) throws AuthenticationException, IOException, ServletException {
        // 验证请求方式
        if (!request.getMethod().equals(method)) {
            throw new AuthException.AuthenticationException(String.format("%s: %S", ExceptionMessage.REQUEST_METHOD_NOT_ALLOWED, request.getMethod()));
        }
        // 从请求体中获取JSON数据
        String requestBody = request.getReader().lines().reduce("", String::concat);
        if (StrUtil.isBlank(requestBody)) {
            throw new AuthException.AuthenticationException(ExceptionMessage.AUTH_FAILED);
        }

        // 解析JSON
        JSONObject jsonObject = JSONUtil.parseObj(requestBody);
        String username = jsonObject.getStr(USERNAME);
        String password = jsonObject.getStr(PASSWORD);

        // 校验数据是否存在
        if (StrUtil.isBlank(username) || StrUtil.isBlank(password)) {
            throw new AuthException.AuthenticationException(ExceptionMessage.AUTH_FAILED);
        }

        log.debug("用户名密码认证 - 用户名: {}", username);

        // 封装返回accessToken
        UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken = new UsernamePasswordAuthenticationToken(username, password);
        return this.getAuthenticationManager().authenticate(usernamePasswordAuthenticationToken);
    }
}