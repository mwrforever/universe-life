package com.universe.life.chat.config;

import com.universe.life.chat.interceptor.WebSocketAuthInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

/**
 * WebSocket + STOMP 配置类
 *
 * @author Kiro
 * @since 2026/01/09
 */
@Configuration
@EnableWebSocketMessageBroker
@RequiredArgsConstructor
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    private final WebSocketAuthInterceptor webSocketAuthInterceptor;

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        // 配置消息代理前缀
        // /topic - 广播消息（群聊、聊天室）
        // /queue - 点对点消息（私聊）
        registry.enableSimpleBroker("/topic", "/queue");
        
        // 配置应用目的地前缀（客户端发送消息的前缀）
        registry.setApplicationDestinationPrefixes("/app");
        
        // 配置用户目的地前缀（点对点消息）
        registry.setUserDestinationPrefix("/user");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // 注册 STOMP 端点
        registry.addEndpoint("/ws/chat")
                .setAllowedOriginPatterns("*")
                .withSockJS()
                .setHeartbeatTime(25000);  // 心跳间隔 25 秒
    }

    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        // 配置入站通道拦截器（JWT 认证）
        registration.interceptors(webSocketAuthInterceptor);
    }
}
