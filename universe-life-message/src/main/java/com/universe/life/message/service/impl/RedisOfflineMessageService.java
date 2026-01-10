package com.universe.life.message.service.impl;

import cn.hutool.json.JSONUtil;
import com.universe.life.message.domain.dto.ChatMessageDTO;
import com.universe.life.message.service.OfflineMessageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 基于 Redis 的离线消息服务实现
 * 使用 Redis List 存储离线消息
 *
 * @author Kiro
 * @since 2026/01/09
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RedisOfflineMessageService implements OfflineMessageService {

    private final StringRedisTemplate stringRedisTemplate;

    private static final String OFFLINE_MESSAGE_KEY_PREFIX = "chat:offline:";

    /**
     * 离线消息过期时间（天）
     */
    @Value("${chat.offline.expire-days:7}")
    private int expireDays;

    /**
     * 离线消息最大数量
     */
    @Value("${chat.offline.max-count:1000}")
    private int maxCount;

    @Override
    public void storeOfflineMessage(Long userId, ChatMessageDTO message) {
        String key = getKey(userId);

        // 序列化消息
        String messageJson = JSONUtil.toJsonStr(message);

        // 添加到列表尾部
        stringRedisTemplate.opsForList().rightPush(key, messageJson);

        // 设置过期时间
        stringRedisTemplate.expire(key, expireDays, TimeUnit.DAYS);

        // 限制列表长度，保留最新的消息
        Long size = stringRedisTemplate.opsForList().size(key);
        if (size != null && size > maxCount) {
            // 移除最旧的消息
            stringRedisTemplate.opsForList().trim(key, size - maxCount, -1);
            log.warn("用户 {} 离线消息超过上限 {}，已移除旧消息", userId, maxCount);
        }

        log.debug("存储离线消息: userId={}, messageId={}", userId, message.getMessageId());
    }

    @Override
    public List<ChatMessageDTO> fetchAndClearOfflineMessages(Long userId) {
        List<ChatMessageDTO> messages = getOfflineMessages(userId);
        if (!messages.isEmpty()) {
            clearOfflineMessages(userId);
        }
        return messages;
    }

    @Override
    public List<ChatMessageDTO> getOfflineMessages(Long userId) {
        String key = getKey(userId);

        List<String> messageJsonList = stringRedisTemplate.opsForList().range(key, 0, -1);
        if (messageJsonList == null || messageJsonList.isEmpty()) {
            return Collections.emptyList();
        }

        return messageJsonList.stream()
                .map(json -> JSONUtil.toBean(json, ChatMessageDTO.class))
                .collect(Collectors.toList());
    }

    @Override
    public int getOfflineMessageCount(Long userId) {
        String key = getKey(userId);
        Long size = stringRedisTemplate.opsForList().size(key);
        return size != null ? size.intValue() : 0;
    }

    @Override
    public void clearOfflineMessages(Long userId) {
        String key = getKey(userId);
        stringRedisTemplate.delete(key);
        log.debug("清除离线消息: userId={}", userId);
    }

    private String getKey(Long userId) {
        return OFFLINE_MESSAGE_KEY_PREFIX + userId;
    }
}
