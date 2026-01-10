package com.universe.life.message.service;

/**
 * 消息幂等处理接口
 *
 * @author Kiro
 * @since 2026/01/09
 */
public interface IdempotentHandler {

    /**
     * 尝试处理消息（幂等检查）
     * 如果消息未处理过，标记为已处理并返回 true
     * 如果消息已处理过，返回 false
     *
     * @param messageId 消息ID
     * @return 是否可以处理（true=未处理过，false=已处理过）
     */
    boolean tryProcess(String messageId);

    /**
     * 检查消息是否已处理
     *
     * @param messageId 消息ID
     * @return 是否已处理
     */
    boolean isProcessed(String messageId);

    /**
     * 标记消息为已处理
     *
     * @param messageId 消息ID
     */
    void markProcessed(String messageId);

    /**
     * 移除已处理标记（用于重试场景）
     *
     * @param messageId 消息ID
     */
    void removeProcessed(String messageId);
}
