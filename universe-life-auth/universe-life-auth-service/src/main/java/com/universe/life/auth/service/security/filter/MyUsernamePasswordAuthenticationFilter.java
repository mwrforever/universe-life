package com.universe.life.auth.service.security.filter;

import cn.hutool.core.util.StrUtil;
import com.universe.life.auth.common.constants.JwtConstants;
import com.universe.life.auth.service.security.token.UsernamePasswordAuthenticationToken;
import com.universe.life.auth.common.exception.AuthException;
import com.universe.life.auth.common.message.ExceptionMessage;
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
 * 支持表单提交和邮箱登录
 *
 * @author 毛伟然
 * @since 2025/11/21
 */
@Slf4j
public class MyUsernamePasswordAuthenticationFilter extends AbstractAuthenticationProcessingFilter {

    private static final String USERNAME = "username";
    private static final String PASSWORD = "password";

    public MyUsernamePasswordAuthenticationFilter(AuthenticationManager authenticationManager) {
        super(new AntPathRequestMatcher("/login/password", "POST"));
        setAuthenticationManager(authenticationManager);
    }

    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response)
            throws AuthenticationException, IOException, ServletException {

        String username = obtainUsername(request);
        String password = obtainPassword(request);

        // 校验数据是否存在
        if (StrUtil.isBlank(username) || StrUtil.isBlank(password)) {
            throw new AuthException.AuthenticationException(ExceptionMessage.AUTH_FAILED);
        }

        // 去除用户名首尾空格
        username = username.trim();

        // 如果用户名包含@符号，认为是邮箱登录，转换为小写
        if (username.contains("@")) {
            username = username.toLowerCase();
        }

        // 封装认证Token
        UsernamePasswordAuthenticationToken authenticationToken =
                new UsernamePasswordAuthenticationToken(username, password, JwtConstants.USER_LOGIN);

        // 设置详细信息
        authenticationToken.setDetails(this.authenticationDetailsSource.buildDetails(request));

        return this.getAuthenticationManager().authenticate(authenticationToken);
    }

    /**
     * 从请求中获取用户名
     */
    protected String obtainUsername(HttpServletRequest request) {
        return request.getParameter(USERNAME);
    }

    /**
     * 从请求中获取密码
     */
    protected String obtainPassword(HttpServletRequest request) {
        return request.getParameter(PASSWORD);
    }


}