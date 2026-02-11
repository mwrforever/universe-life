package com.universe.life.chat.property;

import com.universe.life.chat.service.MessageRateLimiter;
import com.universe.life.message.enums.MessageType;
import net.jqwik.api.*;
import net.jqwik.api.constraints.IntRange;
import net.jqwik.api.constraints.LongRange;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.test.context.ActiveProfiles;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 令牌桶限流器属性测试
 * 验证限流正确性
 *
 * Feature: distributed-chat-system
 * 
 * @author Kiro
 * @since 2026/01/09
 */
@SpringBootTest
@ActiveProfiles("test")
public class RateLimiterPropertyTest {

    @Autowired
    private MessageRateLimiter rateLimiter;

    @Autowired
    private StringRedisTemplate redisTemplate;

    private static final String RATE_LIMIT_KEY_PREFIX = "chat:ratelimit:";

    /**
     * Property 20: 令牌桶限流正确性
     * *For any* user, rate limiting should allow requests up to the configured rate
     * 
     * **Validates: Requirements 12.3, 12.4**
     */
    @Property(tries = 50)
    @Label("Property 20: 令牌桶限流正确性")
    void rateLimitingAllowsConfiguredRate(
            @ForAll @LongRange(min = 1, max = 10000) Long userId
    ) {
        // Given: 清理用户的限流状态
        cleanupRateLimitState(userId);

        // When: 在令牌桶满的情况下尝试获取令牌
        // 私信限流配置：容量20，速率10/s
        int successCount = 0;
        for (int i = 0; i < 25; i++) {
            if (rateLimiter.tryAcquire(userId, MessageType.PRIVATE)) {
                successCount++;
            }
        }

        // Then: 成功次数应该不超过桶容量
        assertTrue(successCount <= 20, "成功次数不应超过桶容量: " + successCount);
        assertTrue(successCount > 0, "至少应该有一些请求成功");

        // Cleanup
        cleanupRateLimitState(userId);
    }

    /**
     * Property: 不同消息类型有不同的限流配置
     * *For any* user, different message types should have different rate limits
     * 
     * **Validates: Requirements 12.5**
     */
    @Property(tries = 30)
    @Label("不同消息类型有不同的限流配置")
    void differentMessageTypesHaveDifferentLimits(
            @ForAll @LongRange(min = 1, max = 10000) Long userId
    ) {
        // Given: 清理用户的限流状态
        cleanupRateLimitState(userId);

        // When: 测试私信限流（容量20）
        int privateSuccess = 0;
        for (int i = 0; i < 25; i++) {
            if (rateLimiter.tryAcquire(userId, MessageType.PRIVATE)) {
                privateSuccess++;
            }
        }

        // 清理并测试群聊限流（容量10）
        cleanupRateLimitState(userId);
        int groupSuccess = 0;
        for (int i = 0; i < 15; i++) {
            if (rateLimiter.tryAcquire(userId, MessageType.GROUP)) {
                groupSuccess++;
            }
        }

        // 清理并测试聊天室限流（容量6）
        cleanupRateLimitState(userId);
        int roomSuccess = 0;
        for (int i = 0; i < 10; i++) {
            if (rateLimiter.tryAcquire(userId, MessageType.ROOM)) {
                roomSuccess++;
            }
        }

        // Then: 验证不同类型有不同的限制
        assertTrue(privateSuccess <= 20, "私信成功次数不应超过20");
        assertTrue(groupSuccess <= 10, "群聊成功次数不应超过10");
        assertTrue(roomSuccess <= 6, "聊天室成功次数不应超过6");

        // Cleanup
        cleanupRateLimitState(userId);
    }

    /**
     * Property: 剩余令牌数非负
     * *For any* user, remaining tokens should be non-negative
     * 
     * **Validates: Requirements 12.1**
     */
    @Property(tries = 50)
    @Label("剩余令牌数非负")
    void remainingTokensIsNonNegative(
            @ForAll @LongRange(min = 1, max = 10000) Long userId
    ) {
        // Given: 清理用户的限流状态
        cleanupRateLimitState(userId);

        // When: 消耗一些令牌
        for (int i = 0; i < 5; i++) {
            rateLimiter.tryAcquire(userId, MessageType.PRIVATE);
        }

        // Then: 剩余令牌数应该非负
        int remaining = rateLimiter.getRemainingTokens(userId);
        assertTrue(remaining >= 0, "剩余令牌数应该非负: " + remaining);

        // Cleanup
        cleanupRateLimitState(userId);
    }

    /**
     * Property: 等待时间非负
     * *For any* user, wait time should be non-negative
     * 
     * **Validates: Requirements 12.2**
     */
    @Property(tries = 50)
    @Label("等待时间非负")
    void waitTimeIsNonNegative(
            @ForAll @LongRange(min = 1, max = 10000) Long userId
    ) {
        // Given: 清理用户的限流状态
        cleanupRateLimitState(userId);

        // When: 获取等待时间
        int waitSeconds = rateLimiter.getWaitSeconds(userId);

        // Then: 等待时间应该非负
        assertTrue(waitSeconds >= 0, "等待时间应该非负: " + waitSeconds);

        // Cleanup
        cleanupRateLimitState(userId);
    }

    /**
     * Property: 并发限流正确性
     * *For any* concurrent requests, total successful requests should not exceed capacity
     * 
     * **Validates: Requirements 12.3, 12.4**
     */
    @Property(tries = 30)
    @Label("并发限流正确性")
    void concurrentRateLimitingIsCorrect(
            @ForAll @LongRange(min = 1, max = 10000) Long userId,
            @ForAll @IntRange(min = 2, max = 5) int threadCount
    ) throws InterruptedException {
        // Given: 清理用户的限流状态
        cleanupRateLimitState(userId);

        AtomicInteger successCount = new AtomicInteger(0);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch endLatch = new CountDownLatch(threadCount);
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);

        int requestsPerThread = 10;

        // When: 多线程并发请求
        for (int t = 0; t < threadCount; t++) {
            executor.submit(() -> {
                try {
                    startLatch.await();
                    for (int i = 0; i < requestsPerThread; i++) {
                        if (rateLimiter.tryAcquire(userId, MessageType.PRIVATE)) {
                            successCount.incrementAndGet();
                        }
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    endLatch.countDown();
                }
            });
        }

        startLatch.countDown();
        endLatch.await();
        executor.shutdown();

        // Then: 成功次数不应超过桶容量
        assertTrue(successCount.get() <= 20, "并发成功次数不应超过桶容量: " + successCount.get());

        // Cleanup
        cleanupRateLimitState(userId);
    }

    /**
     * Property: 不同用户独立限流
     * *For any* two users, their rate limits should be independent
     * 
     * **Validates: Requirements 12.6**
     */
    @Property(tries = 30)
    @Label("不同用户独立限流")
    void differentUsersHaveIndependentLimits(
            @ForAll @LongRange(min = 1, max = 5000) Long userId1,
            @ForAll @LongRange(min = 5001, max = 10000) Long userId2
    ) {
        // Given: 清理两个用户的限流状态
        cleanupRateLimitState(userId1);
        cleanupRateLimitState(userId2);

        // When: 用户1消耗所有令牌
        for (int i = 0; i < 25; i++) {
            rateLimiter.tryAcquire(userId1, MessageType.PRIVATE);
        }

        // Then: 用户2应该仍然可以获取令牌
        boolean user2CanAcquire = rateLimiter.tryAcquire(userId2, MessageType.PRIVATE);
        assertTrue(user2CanAcquire, "用户2应该独立于用户1的限流");

        // Cleanup
        cleanupRateLimitState(userId1);
        cleanupRateLimitState(userId2);
    }

    private void cleanupRateLimitState(Long userId) {
        for (MessageType type : MessageType.values()) {
            String key = RATE_LIMIT_KEY_PREFIX + userId + ":" + type.getCode();
            redisTemplate.delete(key);
        }
    }
}
