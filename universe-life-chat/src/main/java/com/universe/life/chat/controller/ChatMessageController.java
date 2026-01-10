package com.universe.life.chat.controller;

import com.universe.life.chat.domain.dto.ChatMessage;
import com.universe.life.chat.domain.dto.request.GroupMessageRequest;
import com.universe.life.chat.domain.dto.request.PrivateMessageRequest;
import com.universe.life.chat.domain.dto.request.PublicMessageRequest;
import com.universe.life.chat.domain.dto.request.RoomMessageRequest;
import com.universe.life.chat.interceptor.WebSocketAuthInterceptor.ChatUserPrincipal;
import com.universe.life.chat.service.ChatMessageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Controller;

import java.security.Principal;

/**
 * 聊天消息控制器
 * 处理 WebSocket STOMP 消息
 *
 * @author Kiro
 * @since 2026/01/09
 */
@Slf4j
@Controller
@RequiredArgsConstructor
public class ChatMessageController {

    private final ChatMessageService chatMessageService;

    /**
     * 处理私信消息
     * 客户端发送到: /app/chat/private
     */
    @MessageMapping("/chat/private")
    public void handlePrivateMessage(@Payload PrivateMessageRequest request, Principal principal) {
        ChatUserPrincipal chatUser = (ChatUserPrincipal) principal;
        Long senderId = chatUser.getUserId();
        String senderName = chatUser.getUsername();
        String senderAvatar = chatUser.getAvatar();

        log.debug("Received private message: senderId={}, receiverId={}", senderId, request.getReceiverId());

        chatMessageService.sendPrivateMessage(senderId, senderName, senderAvatar, request);
    }

    /**
     * 处理群聊消息
     * 客户端发送到: /app/chat/group
     */
    @MessageMapping("/chat/group")
    public void handleGroupMessage(@Payload GroupMessageRequest request, Principal principal) {
        ChatUserPrincipal chatUser = (ChatUserPrincipal) principal;
        Long senderId = chatUser.getUserId();
        String senderName = chatUser.getUsername();
        String senderAvatar = chatUser.getAvatar();

        log.debug("Received group message: senderId={}, groupId={}", senderId, request.getGroupId());

        chatMessageService.sendGroupMessage(senderId, senderName, senderAvatar, request);
    }

    /**
     * 处理公共聊天室消息
     * 客户端发送到: /app/chat/room
     */
    @MessageMapping("/chat/room")
    public void handleRoomMessage(@Payload RoomMessageRequest request, Principal principal) {
        ChatUserPrincipal chatUser = (ChatUserPrincipal) principal;
        Long senderId = chatUser.getUserId();
        String senderName = chatUser.getUsername();
        String senderAvatar = chatUser.getAvatar();

        log.debug("Received room message: senderId={}, roomId={}", senderId, request.getRoomId());

        chatMessageService.sendRoomMessage(senderId, senderName, senderAvatar, request);
    }

    /**
     * 处理公共单人会话消息
     * 客户端发送到: /app/chat/public
     */
    @MessageMapping("/chat/public")
    public void handlePublicMessage(@Payload PublicMessageRequest request, Principal principal) {
        ChatUserPrincipal chatUser = (ChatUserPrincipal) principal;
        Long senderId = chatUser.getUserId();
        String senderName = chatUser.getUsername();
        String senderAvatar = chatUser.getAvatar();

        log.debug("Received public message: senderId={}, receiverId={}", senderId, request.getReceiverId());

        chatMessageService.sendPublicMessage(senderId, senderName, senderAvatar, request);
    }
}
