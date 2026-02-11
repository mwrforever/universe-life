package com.universe.life.chat.property;

import cn.hutool.json.JSONUtil;
import com.universe.life.auth.common.constants.RedisConstants;
import com.universe.life.auth.common.domain.dto.UserAuthInfo;
import com.universe.life.chat.interceptor.WebSocketAuthInterceptor;
import net.jqwik.api.*;
import net.jqwik.api.constraints.LongRange;
import net.jqwik.api.constraints.StringLength;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.messaging.Message;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.test.context.ActiveProfiles;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * WebSocket 认证属性测试
 * 验证 JWT Token 验证正确性
 *
 * Feature: distributed-chat-system
 * 
 * @author Kiro
 * @since 2026/01/09
 */
@SpringBootTest
@ActiveProfiles("test")
public class WebSocketAuthPropertyTest {

    @Autowired
    private WebSocketAuthInterceptor authInterceptor;

    @Autowired
    private StringRedisTemplate redisTemplate;

    /**
     * Property 5: JWT Token 验证正确性
     * *For any* valid user, authentication should succeed
     * 
     * **Validates: Requirements 3.2**
     */
    @Property(tries = 50)
    @Label("Property 5: JWT Token 验证正确性 - 有效用户认证成功")
    void validUserAuthenticationSucceeds(
            @ForAll @LongRange(min = 1, max = 10000) Long userId,
            @ForAll @StringLength(min = 3, max = 20) String username
    ) {
        Assume.that(username != null && !username.isBlank());

        // Given: 在 Redis 中设置用户认证信息
        String key = RedisConstants.USER_AUTH_UID_KEY + userId;
        UserAuthInfo userAuthInfo = new UserAuthInfo();
        userAuthInfo.setId(userId);
        userAuthInfo.setUsername(username);
        userAuthInfo.setAvatar("https://example.com/avatar.jpg");

        Map<String, String> authData = new HashMap<>();
        authData.put(RedisConstants.AUTH_USER_DATA, JSONUtil.toJsonStr(userAuthInfo));
        authData.put(RedisConstants.AUTH_USER_TYPE, "user");
        redisTemplate.opsForHash().putAll(key, authData);

        try {
            // When: 创建 STOMP CONNECT 消息
            StompHeaderAccessor accessor = StompHeaderAccessor.create(StompCommand.CONNECT);
            accessor.setNativeHeader("user-info", String.valueOf(userId));
            Message<?> message = MessageBuilder.createMessage(new byte[0], accessor.getMessageHeaders());

            // Then: 认证应该成功
            Message<?> result = authInterceptor.preSend(message, null);
            assertNotNull(result, "认证成功应该返回消息");

            // 验证 Principal 已设置
            StompHeaderAccessor resultAccessor = StompHeaderAccessor.wrap(result);
            assertNotNull(resultAccessor.getUser(), "应该设置用户 Principal");
            assertEquals(String.valueOf(userId), resultAccessor.getUser().getName(), "用户ID应该正确");
        } finally {
            // Cleanup
            redisTemplate.delete(key);
        }
    }

    /**
     * Property: 无效用户认证失败
     */
    @Property(tries = 50)
    @Label("无效用户认证失败")
    void invalidUserAuthenticationFails(
            @ForAll @LongRange(min = 100000, max = 200000) Long nonExistentUserId
    ) {
        // Given: 确保用户不存在于 Redis
        String key = RedisConstants.USER_AUTH_UID_KEY + nonExistentUserId;
        redisTemplate.delete(key);

        // When: 创建 STOMP CONNECT 消息
        StompHeaderAccessor accessor = StompHeaderAccessor.create(StompCommand.CONNECT);
        accessor.setNativeHeader("user-info", String.valueOf(nonExistentUserId));
        Message<?> message = MessageBuilder.createMessage(new byte[0], accessor.getMessageHeaders());

        // Then: 认证应该失败
        assertThrows(Exception.class, () -> authInterceptor.preSend(message, null),
                "不存在的用户认证应该失败");
    }

    /**
     * Property: 缺少 user-info header 认证失败
     */
    @Property(tries = 20)
    @Label("缺少 user-info header 认证失败")
    void missingUserInfoHeaderFails() {
        // When: 创建没有 user-info header 的 STOMP CONNECT 消息
        StompHeaderAccessor accessor = StompHeaderAccessor.create(StompCommand.CONNECT);
        Message<?> message = MessageBuilder.createMessage(new byte[0], accessor.getMessageHeaders());

        // Then: 认证应该失败
        assertThrows(Exception.class, () -> authInterceptor.preSend(message, null),
                "缺少 user-info header 应该认证失败");
    }

    /**
     * Property: 空 user-info header 认证失败
     */
    @Property(tries = 20)
    @Label("空 user-info header 认证失败")
    void emptyUserInfoHeaderFails() {
        // When: 创建空 user-info header 的 STOMP CONNECT 消息
        StompHeaderAccessor accessor = StompHeaderAccessor.create(StompCommand.CONNECT);
        accessor.setNativeHeader("user-info", "");
        Message<?> message = MessageBuilder.createMessage(new byte[0], accessor.getMessageHeaders());

        // Then: 认证应该失败
        assertThrows(Exception.class, () -> authInterceptor.preSend(message, null),
                "空 user-info header 应该认证失败");
    }

    /**
     * Property: 非 CONNECT 命令不需要认证
     */
    @Property(tries = 50)
    @Label("非 CONNECT 命令不需要认证")
    void nonConnectCommandDoesNotRequireAuth(
            @ForAll("nonConnectCommands") StompCommand command
    ) {
        // When: 创建非 CONNECT 命令的消息
        StompHeaderAccessor accessor = StompHeaderAccessor.create(command);
        Message<?> message = MessageBuilder.createMessage(new byte[0], accessor.getMessageHeaders());

        // Then: 应该直接通过，不抛出异常
        Message<?> result = authInterceptor.preSend(message, null);
        assertNotNull(result, "非 CONNECT 命令应该直接通过");
    }

    @Provide
    Arbitrary<StompCommand> nonConnectCommands() {
        return Arbitraries.of(
                StompCommand.SEND,
                StompCommand.SUBSCRIBE,
                StompCommand.UNSUBSCRIBE,
                StompCommand.DISCONNECT
        );
    }

    /**
     * Property: 认证结果一致性
     */
    @Property(tries = 30)
    @Label("认证结果一致性")
    void authenticationResultIsConsistent(
            @ForAll @LongRange(min = 1, max = 10000) Long userId,
            @ForAll @StringLength(min = 3, max = 20) String username
    ) {
        Assume.that(username != null && !username.isBlank());

        // Given: 在 Redis 中设置用户认证信息
        String key = RedisConstants.USER_AUTH_UID_KEY + userId;
        UserAuthInfo userAuthInfo = new UserAuthInfo();
        userAuthInfo.setId(userId);
        userAuthInfo.setUsername(username);

        Map<String, String> authData = new HashMap<>();
        authData.put(RedisConstants.AUTH_USER_DATA, JSONUtil.toJsonStr(userAuthInfo));
        authData.put(RedisConstants.AUTH_USER_TYPE, "user");
        redisTemplate.opsForHash().putAll(key, authData);

        try {
            // When: 多次认证相同用户
            for (int i = 0; i < 3; i++) {
                StompHeaderAccessor accessor = StompHeaderAccessor.create(StompCommand.CONNECT);
                accessor.setNativeHeader("user-info", String.valueOf(userId));
                Message<?> message = MessageBuilder.createMessage(new byte[0], accessor.getMessageHeaders());

                Message<?> result = authInterceptor.preSend(message, null);
                assertNotNull(result, "认证应该成功");

                StompHeaderAccessor resultAccessor = StompHeaderAccessor.wrap(result);
                assertEquals(String.valueOf(userId), resultAccessor.getUser().getName(),
                        "多次认证结果应该一致");
            }
        } finally {
            // Cleanup
            redisTemplate.delete(key);
        }
    }
}
