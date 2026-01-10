package com.universe.life.chat.listener;

import com.universe.life.chat.interceptor.WebSocketAuthInterceptor.ChatUserPrincipal;
import com.universe.life.chat.service.ChatConnectionManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectedEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;
import org.springframework.web.socket.messaging.SessionSubscribeEvent;
import org.springframework.web.socket.messaging.SessionUnsubscribeEvent;

import java.security.Principal;

/**
 * WebSocket 事件监听器
 * 处理连接建立、断开、订阅、取消订阅等事件
 *
 * @author Kiro
 * @since 2026/01/09
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class WebSocketEventListener {

    private final ChatConnectionManager connectionManager;

    /**
     * 处理连接建立事件
     */
    @EventListener
    public void handleSessionConnected(SessionConnectedEvent event) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());
        String sessionId = accessor.getSessionId();
        Principal principal = accessor.getUser();

        if (principal instanceof ChatUserPrincipal chatUser) {
            Long userId = chatUser.getUserId();
            String username = chatUser.getUsername();

            log.info("WebSocket connected: sessionId={}, userId={}, username={}", sessionId, userId, username);

            // 创建会话映射并更新连接数
            connectionManager.onUserConnected(userId, sessionId);
        } else {
            log.warn("WebSocket connected but no valid principal: sessionId={}", sessionId);
        }
    }

    /**
     * 处理连接断开事件
     */
    @EventListener
    public void handleSessionDisconnect(SessionDisconnectEvent event) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());
        String sessionId = accessor.getSessionId();
        Principal principal = accessor.getUser();

        if (principal instanceof ChatUserPrincipal chatUser) {
            Long userId = chatUser.getUserId();
            String username = chatUser.getUsername();

            log.info("WebSocket disconnected: sessionId={}, userId={}, username={}", sessionId, userId, username);

            // 清理会话映射并更新连接数
            connectionManager.onUserDisconnected(userId, sessionId);
        } else {
            log.warn("WebSocket disconnected but no valid principal: sessionId={}", sessionId);
            // 尝试通过 sessionId 清理
            connectionManager.onSessionDisconnected(sessionId);
        }
    }

    /**
     * 处理订阅事件
     */
    @EventListener
    public void handleSessionSubscribe(SessionSubscribeEvent event) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());
        String sessionId = accessor.getSessionId();
        String destination = accessor.getDestination();
        Principal principal = accessor.getUser();

        if (principal instanceof ChatUserPrincipal chatUser) {
            log.debug("User subscribed: userId={}, sessionId={}, destination={}",
                    chatUser.getUserId(), sessionId, destination);

            // 处理聊天室订阅
            if (destination != null && destination.startsWith("/topic/room/")) {
                String roomIdStr = destination.substring("/topic/room/".length());
                try {
                    Long roomId = Long.parseLong(roomIdStr);
                    connectionManager.onRoomSubscribed(chatUser.getUserId(), roomId);
                } catch (NumberFormatException e) {
                    log.warn("Invalid room id in destination: {}", destination);
                }
            }
        }
    }

    /**
     * 处理取消订阅事件
     */
    @EventListener
    public void handleSessionUnsubscribe(SessionUnsubscribeEvent event) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());
        String sessionId = accessor.getSessionId();
        Principal principal = accessor.getUser();

        if (principal instanceof ChatUserPrincipal chatUser) {
            log.debug("User unsubscribed: userId={}, sessionId={}", chatUser.getUserId(), sessionId);
        }
    }
}
