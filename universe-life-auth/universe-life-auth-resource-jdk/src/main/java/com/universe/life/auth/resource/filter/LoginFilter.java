package com.universe.life.auth.resource.filter;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.universe.life.auth.common.constants.JwtConstants;
import com.universe.life.auth.common.constants.RedisConstants;
import com.universe.life.auth.common.domain.dto.AdminAuthInfo;
import com.universe.life.auth.common.domain.dto.UserAuthInfo;
import com.universe.life.auth.common.exception.AuthException;
import com.universe.life.auth.common.message.ExceptionMessage;
import com.universe.life.auth.common.properties.AuthPathProperties;
import com.universe.life.auth.common.util.AntRequestMatchUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Map;

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
    protected void doFilterInternal(HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull FilterChain filterChain) throws ServletException, IOException {
        log.info("LoginFilter.doFilterInternal - 开始处理请求: {} {}", request.getMethod(), request.getRequestURL());
        // 1. 从请求头中获取用户信息
        String userId = request.getHeader(JwtConstants.USER_INFO);
        if (StrUtil.isBlank(userId)) {
            log.info("{}：用户未登录", request.getRequestURL());
            throw new AuthException.AuthorizationException(ExceptionMessage.LOGIN_REQUIRED);
        }
        // 2. 设置用户以及认证完毕
        String key = RedisConstants.USER_AUTH_UID_KEY + userId;
        Map<Object, Object> entries = stringRedisTemplate.opsForHash().entries(key);
        if (CollUtil.isEmpty(entries)) {
            // TODO 从数据库中加载相关数据
        }
        Object info = entries.get(RedisConstants.AUTH_USER_DATA);
        Object type = entries.get(RedisConstants.AUTH_USER_TYPE);
        if (ObjectUtil.isNull(info) || ObjectUtil.isNull(type)) {
            throw new AuthException.AuthenticationException(ExceptionMessage.AUTH_FAILED);
        }
        // 将数据放入上下文
        UserDetails userDetails;
        String typeStr = String.valueOf(type);
        String dataStr = String.valueOf(info);
        if (typeStr.equals("user")) {
            userDetails = JSONUtil.toBean(dataStr, UserAuthInfo.class);
        } else if (typeStr.equals("admin")) {
            userDetails = JSONUtil.toBean(dataStr, AdminAuthInfo.class);
        } else {
            log.warn("用户：{}登录异常", userId);
            throw new AuthException.AuthenticationException(ExceptionMessage.AUTH_FAILED);
        }
        // 创建认证对象并存入spring security context 中
        Authentication authentication = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
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
