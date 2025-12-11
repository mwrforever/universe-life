package com.universe.life.auth.resource.filter;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.universe.life.auth.common.constants.JwtConstants;
import com.universe.life.auth.common.constants.RedisConstants;
import com.universe.life.auth.common.domain.dto.UserAuthInfo;
import com.universe.life.auth.common.exception.AuthException;
import com.universe.life.auth.common.message.ExceptionMessage;
import com.universe.life.auth.common.properties.AuthPathProperties;
import com.universe.life.auth.common.util.AntRequestMatchUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * @author 毛伟然
 * @since 2025/11/18 12:19
 */
@Slf4j
@RequiredArgsConstructor
public class LoginFilter extends OncePerRequestFilter {

    private final StringRedisTemplate stringRedisTemplate;
    private final AuthPathProperties authPathProperties;
    private final AntRequestMatchUtil antRequestMatchUtil;


    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        log.info("{}：进入过滤器链进行认证", request.getRequestURL());
        // 1. 从请求头中获取用户信息
        String userId = request.getHeader(JwtConstants.USER_INFO);
        if (StrUtil.isBlank(userId)) {
            log.info("{}：用户未登录", request.getRequestURL());
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

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String requestURI = request.getRequestURI();
        if (authPathProperties.getExcludePath() == null) {
            return false;
        }
        return antRequestMatchUtil.matchAny(requestURI, authPathProperties.getExcludePath());
    }
}
