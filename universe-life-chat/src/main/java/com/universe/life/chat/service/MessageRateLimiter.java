package com.universe.life.chat.service;

import com.universe.life.message.enums.MessageType;

/**
 * 消息限流器接口
 * 使用令牌桶算法实现分布式限流
 *
 * @author Kiro
 * @since 2026/01/09
 */
public interface MessageRateLimiter {

    /**
     * 尝试获取发送许可
     *
     * @param userId      用户ID
     * @param messageType 消息类型
     * @return 是否允许发送
     */
    boolean tryAcquire(Long userId, MessageType messageType);

    /**
     * 获取用户剩余令牌数
     *
     * @param userId 用户ID
     * @return 剩余令牌数
     */
    int getRemainingTokens(Long userId);

    /**
     * 获取下次可发送的等待时间（秒）
     *
     * @param userId 用户ID
     * @return 等待秒数，0表示可以立即发送
     */
    int getWaitSeconds(Long userId);
}
