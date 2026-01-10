package com.universe.life.chat.service;

import com.universe.life.common.server.api.domain.dto.SessionInfo;
import com.universe.life.common.server.api.service.ChatServerLoadBalancer;
import com.universe.life.common.server.api.service.SessionMappingManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 聊天连接管理器
 * 管理 WebSocket 连接的生命周期，包括会话映射和连接数统计
 *
 * @author Kiro
 * @since 2026/01/09
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ChatConnectionManager {

    private final SessionMappingManager sessionMappingManager;
    private final ChatServerLoadBalancer loadBalancer;

    @Value("${chat.server.address:${spring.cloud.nacos.discovery.ip:localhost}:${server.port:8102}}")
    private String serverAddress;

    @Value("${chat.server.max-connections:10000}")
    private int maxConnections;

    /**
     * 本地连接数计数器
     */
    private final AtomicInteger localConnectionCount = new AtomicInteger(0);

    /**
     * 用户连接时调用
     */
    public void onUserConnected(Long userId, String sessionId) {
        // 创建会话信息
        SessionInfo sessionInfo = new SessionInfo();
        sessionInfo.setUserId(userId);
        sessionInfo.setSessionId(sessionId);
        sessionInfo.setServerAddress(serverAddress);
        sessionInfo.setConnectedAt(LocalDateTime.now());
        sessionInfo.setLastActiveAt(LocalDateTime.now());

        // 创建会话映射
        sessionMappingManager.createSessionMapping(userId, sessionId, serverAddress, sessionInfo);

        // 更新本地连接数
        int currentCount = localConnectionCount.incrementAndGet();

        // 更新 Redis 中的服务器负载
        loadBalancer.updateServerLoad(serverAddress, currentCount);

        log.info("User connected: userId={}, sessionId={}, serverAddress={}, currentConnections={}",
                userId, sessionId, serverAddress, currentCount);
    }

    /**
     * 用户断开连接时调用
     */
    public void onUserDisconnected(Long userId, String sessionId) {
        // 删除会话映射
        sessionMappingManager.removeSessionMapping(userId, sessionId);

        // 更新本地连接数
        int currentCount = localConnectionCount.decrementAndGet();
        if (currentCount < 0) {
            currentCount = 0;
            localConnectionCount.set(0);
        }

        // 更新 Redis 中的服务器负载
        loadBalancer.updateServerLoad(serverAddress, currentCount);

        log.info("User disconnected: userId={}, sessionId={}, currentConnections={}", userId, sessionId, currentCount);
    }

    /**
     * 会话断开时调用（无法获取用户信息时）
     */
    public void onSessionDisconnected(String sessionId) {
        // 通过 sessionId 获取 userId
        Long userId = sessionMappingManager.getUserIdBySessionId(sessionId);
        if (userId != null) {
            onUserDisconnected(userId, sessionId);
        } else {
            log.warn("Cannot find userId for sessionId: {}", sessionId);
        }
    }

    /**
     * 用户订阅聊天室时调用
     */
    public void onRoomSubscribed(Long userId, Long roomId) {
        log.debug("User subscribed to room: userId={}, roomId={}", userId, roomId);
        // TODO: 更新聊天室在线人数
    }

    /**
     * 用户取消订阅聊天室时调用
     */
    public void onRoomUnsubscribed(Long userId, Long roomId) {
        log.debug("User unsubscribed from room: userId={}, roomId={}", userId, roomId);
        // TODO: 更新聊天室在线人数
    }

    /**
     * 检查是否可以接受新连接
     */
    public boolean canAcceptConnection() {
        return localConnectionCount.get() < maxConnections;
    }

    /**
     * 获取当前连接数
     */
    public int getCurrentConnectionCount() {
        return localConnectionCount.get();
    }

    /**
     * 获取服务器地址
     */
    public String getServerAddress() {
        return serverAddress;
    }

    /**
     * 更新用户最后活跃时间
     */
    public void updateLastActiveTime(Long userId, String sessionId) {
        SessionInfo sessionInfo = sessionMappingManager.getSessionByUserId(userId);
        if (sessionInfo != null) {
            sessionInfo.setLastActiveAt(LocalDateTime.now());
            sessionMappingManager.createSessionMapping(userId, sessionId, serverAddress, sessionInfo);
        }
    }
}
