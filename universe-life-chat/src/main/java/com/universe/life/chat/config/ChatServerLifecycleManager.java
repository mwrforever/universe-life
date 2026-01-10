package com.universe.life.chat.config;

import com.universe.life.common.server.api.service.ChatServerLoadBalancer;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * 聊天服务器生命周期管理器
 * 负责服务器启动时注册和关闭时注销
 *
 * @author Kiro
 * @since 2026/01/09
 */
@Slf4j
@Component
public class ChatServerLifecycleManager {

    private final ChatServerLoadBalancer loadBalancer;

    @Value("${chat.server.address}")
    private String serverAddress;

    public ChatServerLifecycleManager(ChatServerLoadBalancer loadBalancer) {
        this.loadBalancer = loadBalancer;
    }

    /**
     * 服务器启动时自动注册
     */
    @PostConstruct
    public void onStartup() {
        log.info("Registering chat server: {}", serverAddress);
        loadBalancer.registerServer(serverAddress);
        log.info("Chat server registered successfully: {}", serverAddress);
    }

    /**
     * 服务器关闭时自动注销
     */
    @PreDestroy
    public void onShutdown() {
        log.info("Unregistering chat server: {}", serverAddress);
        loadBalancer.unregisterServer(serverAddress);
        log.info("Chat server unregistered successfully: {}", serverAddress);
    }
}
