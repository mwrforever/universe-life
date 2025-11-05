package com.universe.life.auth.resource.filter;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.universe.life.common.constants.JwtConstants;
import com.universe.life.common.constants.RedisConstants;
import com.universe.life.common.domain.dto.UserAuthInfo;
import com.universe.life.common.exception.AuthException;
import com.universe.life.common.message.ExceptionMessage;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * @author 毛伟然
 * @since 2025/11/3 16:02
 */
@RequiredArgsConstructor
public class LoginFilter extends OncePerRequestFilter {

    private final StringRedisTemplate stringRedisTemplate;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        // 1. 从请求头中获取用户信息
        String userId = request.getHeader(JwtConstants.USER_INFO);
        if (StrUtil.isBlank(userId)) {
            throw new AuthException.AuthorizationException(ExceptionMessage.LOGIN_REQUIRED);
        }
        // 2. 设置用户以及认证完毕
        String userAuthInfoStr = stringRedisTemplate.opsForValue().get(RedisConstants.USER_AUTH_UID_KEY + userId);
        if (StrUtil.isBlank(userAuthInfoStr)) {
            // TODO 从数据库中获取用户信息
        }
        UserAuthInfo userAuthInfo = JSONUtil.toBean(userAuthInfoStr, UserAuthInfo.class);
        // 创建认证对象并存入spring security context 中
        Authentication authentication = new UsernamePasswordAuthenticationToken(userAuthInfo, null, userAuthInfo.getPermissions());
        SecurityContextHolder.getContext().setAuthentication(authentication);
        filterChain.doFilter(request, response);
    }
}