package com.universe.life.chat.interceptor;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.universe.life.auth.common.constants.RedisConstants;
import com.universe.life.auth.common.domain.dto.UserAuthInfo;
import com.universe.life.auth.common.exception.AuthException;
import com.universe.life.auth.common.message.ExceptionMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Component;

import java.security.Principal;
import java.util.Map;

/**
 * WebSocket 认证拦截器
 * 在 STOMP CONNECT 时验证用户身份
 * 通过 user-info header 获取用户ID，从 Redis 加载用户信息
 *
 * @author Kiro
 * @since 2026/01/09
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class WebSocketAuthInterceptor implements ChannelInterceptor {

    private static final String USER_INFO_HEADER = "user-info";

    private final StringRedisTemplate stringRedisTemplate;

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

        if (accessor != null && StompCommand.CONNECT.equals(accessor.getCommand())) {
            // 获取 user-info header (由 Gateway 传递)
            String userId = accessor.getFirstNativeHeader(USER_INFO_HEADER);

            if (StrUtil.isBlank(userId)) {
                log.warn("WebSocket connection rejected: missing user-info header");
                throw new AuthException.AuthorizationException(ExceptionMessage.LOGIN_REQUIRED);
            }

            try {
                // 从 Redis 加载用户信息
                UserAuthInfo userAuthInfo = loadUserAuthInfo(userId);

                if (userAuthInfo == null) {
                    log.warn("WebSocket connection rejected: user auth info not found, userId={}", userId);
                    throw new AuthException.AuthenticationException(ExceptionMessage.AUTH_FAILED);
                }

                // 设置用户 Principal
                accessor.setUser(new ChatUserPrincipal(userAuthInfo));

                log.info("WebSocket connection authenticated: userId={}, username={}",
                        userAuthInfo.getId(), userAuthInfo.getUsername());
            } catch (AuthenticationException | AccessDeniedException e) {
                throw e;
            } catch (Exception e) {
                log.error("WebSocket authentication failed", e);
                throw new AuthException.AuthenticationException(ExceptionMessage.AUTH_FAILED);
            }
        }

        return message;
    }

    /**
     * 从 Redis 加载用户认证信息
     */
    private UserAuthInfo loadUserAuthInfo(String userId) {
        String key = RedisConstants.USER_AUTH_UID_KEY + userId;
        Map<Object, Object> entries = stringRedisTemplate.opsForHash().entries(key);

        if (CollUtil.isEmpty(entries)) {
            return null;
        }

        Object info = entries.get(RedisConstants.AUTH_USER_DATA);
        Object type = entries.get(RedisConstants.AUTH_USER_TYPE);

        if (ObjectUtil.isNull(info) || ObjectUtil.isNull(type)) {
            return null;
        }

        String typeStr = String.valueOf(type);
        String dataStr = String.valueOf(info);

        // 只支持普通用户连接 WebSocket
        if ("user".equals(typeStr)) {
            return JSONUtil.toBean(dataStr, UserAuthInfo.class);
        }

        return null;
    }

    /**
     * 聊天用户 Principal
     */
    public static class ChatUserPrincipal implements Principal {
        private final UserAuthInfo userAuthInfo;

        public ChatUserPrincipal(UserAuthInfo userAuthInfo) {
            this.userAuthInfo = userAuthInfo;
        }

        @Override
        public String getName() {
            return String.valueOf(userAuthInfo.getId());
        }

        public Long getUserId() {
            return userAuthInfo.getId();
        }

        public String getUsername() {
            return userAuthInfo.getUsername();
        }

        public String getAvatar() {
            return userAuthInfo.getAvatar();
        }

        public UserAuthInfo getUserAuthInfo() {
            return userAuthInfo;
        }
    }
}
