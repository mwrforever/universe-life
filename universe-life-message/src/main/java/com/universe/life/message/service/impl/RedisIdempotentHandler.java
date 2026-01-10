package com.universe.life.message.service.impl;

import com.universe.life.message.service.IdempotentHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

/**
 * 基于 Redis 的消息幂等处理实现
 * 使用 Redis String 记录已处理的消息ID
 *
 * @author Kiro
 * @since 2026/01/09
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RedisIdempotentHandler implements IdempotentHandler {

    private final StringRedisTemplate stringRedisTemplate;

    private static final String PROCESSED_KEY_PREFIX = "chat:msg:processed:";
    private static final String PROCESSED_VALUE = "1";

    /**
     * 幂等记录过期时间（小时）
     */
    @Value("${chat.idempotent.expire-hours:24}")
    private int expireHours;

    @Override
    public boolean tryProcess(String messageId) {
        if (messageId == null || messageId.isEmpty()) {
            return true;
        }

        String key = getKey(messageId);

        // 使用 setIfAbsent 实现原子性的检查和设置
        Boolean success = stringRedisTemplate.opsForValue()
                .setIfAbsent(key, PROCESSED_VALUE, expireHours, TimeUnit.HOURS);

        if (Boolean.TRUE.equals(success)) {
            log.debug("消息首次处理: messageId={}", messageId);
            return true;
        } else {
            log.debug("消息已处理过，跳过: messageId={}", messageId);
            return false;
        }
    }

    @Override
    public boolean isProcessed(String messageId) {
        if (messageId == null || messageId.isEmpty()) {
            return false;
        }

        String key = getKey(messageId);
        return Boolean.TRUE.equals(stringRedisTemplate.hasKey(key));
    }

    @Override
    public void markProcessed(String messageId) {
        if (messageId == null || messageId.isEmpty()) {
            return;
        }

        String key = getKey(messageId);
        stringRedisTemplate.opsForValue().set(key, PROCESSED_VALUE, expireHours, TimeUnit.HOURS);
        log.debug("标记消息已处理: messageId={}", messageId);
    }

    @Override
    public void removeProcessed(String messageId) {
        if (messageId == null || messageId.isEmpty()) {
            return;
        }

        String key = getKey(messageId);
        stringRedisTemplate.delete(key);
        log.debug("移除消息处理标记: messageId={}", messageId);
    }

    private String getKey(String messageId) {
        return PROCESSED_KEY_PREFIX + messageId;
    }
}
