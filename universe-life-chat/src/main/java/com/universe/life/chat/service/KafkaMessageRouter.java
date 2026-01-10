package com.universe.life.chat.service;

import com.universe.life.chat.domain.dto.ChatMessage;

import java.util.Set;

/**
 * Kafka 消息路由器接口
 * 负责将聊天消息路由到目标服务器
 *
 * @author Kiro
 * @since 2026/01/09
 */
public interface KafkaMessageRouter {

    /**
     * 路由消息到目标服务器
     *
     * @param message       聊天消息
     * @param targetServers 目标服务器列表
     */
    void routeMessage(ChatMessage message, Set<String> targetServers);

    /**
     * 广播消息到所有服务器
     *
     * @param message 聊天消息
     */
    void broadcastMessage(ChatMessage message);

    /**
     * 发送消息到持久化服务
     *
     * @param message 聊天消息
     */
    void sendToPersistence(ChatMessage message);
}
