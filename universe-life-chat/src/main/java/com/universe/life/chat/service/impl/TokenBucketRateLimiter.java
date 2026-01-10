package com.universe.life.chat.service.impl;

import com.universe.life.chat.service.MessageRateLimiter;
import com.universe.life.message.enums.MessageType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

/**
 * 令牌桶限流器实现
 * 使用 Redis + Lua 脚本实现分布式限流
 *
 * @author Kiro
 * @since 2026/01/09
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TokenBucketRateLimiter implements MessageRateLimiter {

    private final StringRedisTemplate redisTemplate;

    private static final String RATE_LIMIT_KEY_PREFIX = "chat:ratelimit:";

    /**
     * 私信限流配置：每秒10条
     */
    @Value("${chat.ratelimit.private.rate:10}")
    private int privateRate;

    @Value("${chat.ratelimit.private.capacity:20}")
    private int privateCapacity;

    /**
     * 群聊限流配置：每秒5条
     */
    @Value("${chat.ratelimit.group.rate:5}")
    private int groupRate;

    @Value("${chat.ratelimit.group.capacity:10}")
    private int groupCapacity;

    /**
     * 公共聊天室限流配置：每秒3条
     */
    @Value("${chat.ratelimit.room.rate:3}")
    private int roomRate;

    @Value("${chat.ratelimit.room.capacity:6}")
    private int roomCapacity;

    /**
     * 令牌桶 Lua 脚本
     * KEYS[1]: 限流 key
     * ARGV[1]: 令牌补充速率（每秒）
     * ARGV[2]: 桶容量
     * ARGV[3]: 当前时间戳（毫秒）
     * ARGV[4]: 请求的令牌数
     * 返回: [是否允许(1/0), 剩余令牌数, 下次可用等待毫秒]
     */
    private static final String TOKEN_BUCKET_SCRIPT = """
            local key = KEYS[1]
            local rate = tonumber(ARGV[1])
            local capacity = tonumber(ARGV[2])
            local now = tonumber(ARGV[3])
            local requested = tonumber(ARGV[4])
            
            local data = redis.call('HMGET', key, 'tokens', 'lastRefill')
            local tokens = tonumber(data[1])
            local lastRefill = tonumber(data[2])
            
            if tokens == nil then
                tokens = capacity
                lastRefill = now
            end
            
            -- 计算需要补充的令牌数
            local elapsed = now - lastRefill
            local refill = math.floor(elapsed * rate / 1000)
            tokens = math.min(capacity, tokens + refill)
            
            if refill > 0 then
                lastRefill = now
            end
            
            local allowed = 0
            local waitMs = 0
            
            if tokens >= requested then
                tokens = tokens - requested
                allowed = 1
            else
                -- 计算需要等待的时间
                local needed = requested - tokens
                waitMs = math.ceil(needed * 1000 / rate)
            end
            
            -- 更新 Redis
            redis.call('HMSET', key, 'tokens', tokens, 'lastRefill', lastRefill)
            redis.call('EXPIRE', key, 3600)
            
            return {allowed, tokens, waitMs}
            """;

    private final DefaultRedisScript<List> tokenBucketScript = new DefaultRedisScript<>(TOKEN_BUCKET_SCRIPT, List.class);

    @Override
    public boolean tryAcquire(Long userId, MessageType messageType) {
        String key = buildKey(userId, messageType);
        int rate = getRate(messageType);
        int capacity = getCapacity(messageType);

        List<Long> result = executeScript(key, rate, capacity, 1);
        boolean allowed = result.get(0) == 1L;

        if (!allowed) {
            log.debug("Rate limit exceeded: userId={}, messageType={}, remainingTokens={}",
                    userId, messageType, result.get(1));
        }

        return allowed;
    }

    @Override
    public int getRemainingTokens(Long userId) {
        // 使用私信类型作为默认查询
        String key = buildKey(userId, MessageType.PRIVATE);
        int rate = privateRate;
        int capacity = privateCapacity;

        List<Long> result = executeScript(key, rate, capacity, 0);
        return result.get(1).intValue();
    }

    @Override
    public int getWaitSeconds(Long userId) {
        String key = buildKey(userId, MessageType.PRIVATE);
        int rate = privateRate;
        int capacity = privateCapacity;

        List<Long> result = executeScript(key, rate, capacity, 1);
        if (result.get(0) == 1L) {
            return 0;
        }
        return (int) Math.ceil(result.get(2) / 1000.0);
    }

    @SuppressWarnings("unchecked")
    private List<Long> executeScript(String key, int rate, int capacity, int requested) {
        long now = System.currentTimeMillis();
        return redisTemplate.execute(
                tokenBucketScript,
                Collections.singletonList(key),
                String.valueOf(rate),
                String.valueOf(capacity),
                String.valueOf(now),
                String.valueOf(requested)
        );
    }

    private String buildKey(Long userId, MessageType messageType) {
        return RATE_LIMIT_KEY_PREFIX + userId + ":" + messageType.getCode();
    }

    private int getRate(MessageType messageType) {
        return switch (messageType) {
            case PRIVATE, PUBLIC -> privateRate;
            case GROUP -> groupRate;
            case ROOM -> roomRate;
        };
    }

    private int getCapacity(MessageType messageType) {
        return switch (messageType) {
            case PRIVATE, PUBLIC -> privateCapacity;
            case GROUP -> groupCapacity;
            case ROOM -> roomCapacity;
        };
    }
}
