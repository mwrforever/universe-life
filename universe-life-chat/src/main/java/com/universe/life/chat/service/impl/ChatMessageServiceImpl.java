package com.universe.life.chat.service.impl;

import cn.hutool.core.lang.Snowflake;
import cn.hutool.core.util.IdUtil;
import com.universe.life.auth.common.exception.BusinessException;
import com.universe.life.chat.domain.dto.ChatMessage;
import com.universe.life.chat.domain.dto.request.GroupMessageRequest;
import com.universe.life.chat.domain.dto.request.PrivateMessageRequest;
import com.universe.life.chat.domain.dto.request.PublicMessageRequest;
import com.universe.life.chat.domain.dto.request.RoomMessageRequest;
import com.universe.life.chat.service.ChatMessageService;
import com.universe.life.chat.service.KafkaMessageRouter;
import com.universe.life.chat.service.MessagePermissionChecker;
import com.universe.life.chat.service.MessageRateLimiter;
import com.universe.life.chat.service.SensitiveWordFilter;
import com.universe.life.common.server.api.domain.dto.SessionInfo;
import com.universe.life.common.server.api.service.SessionMappingManager;
import com.universe.life.message.enums.MessageStatus;
import com.universe.life.message.enums.MessageType;
import com.universe.life.message.exception.ChatExceptionMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

/**
 * 聊天消息服务实现
 *
 * @author Kiro
 * @since 2026/01/09
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ChatMessageServiceImpl implements ChatMessageService {

    private final MessageRateLimiter rateLimiter;
    private final MessagePermissionChecker permissionChecker;
    private final KafkaMessageRouter messageRouter;
    private final SessionMappingManager sessionMappingManager;
    private final SensitiveWordFilter sensitiveWordFilter;

    private final Snowflake snowflake = IdUtil.getSnowflake(1, 1);

    @Override
    public void sendPrivateMessage(Long senderId, String senderName, String senderAvatar, PrivateMessageRequest request) {
        checkRateLimit(senderId, MessageType.PRIVATE);
        checkSensitiveContent(request.getContent());
        permissionChecker.checkPrivateMessagePermission(senderId, request.getReceiverId());

        ChatMessage message = buildMessage(senderId, senderName, senderAvatar, MessageType.PRIVATE);
        message.setReceiverId(request.getReceiverId());
        message.setContentType(request.getContentType());
        message.setContent(request.getContent());
        message.setExtra(request.getExtra());
        message.setClientMessageId(request.getClientMessageId());

        routePrivateMessage(message);
        messageRouter.sendToPersistence(message);

        log.info("Private message sent: messageId={}, senderId={}, receiverId={}",
                message.getMessageId(), senderId, request.getReceiverId());
    }

    @Override
    public void sendGroupMessage(Long senderId, String senderName, String senderAvatar, GroupMessageRequest request) {
        checkRateLimit(senderId, MessageType.GROUP);
        checkSensitiveContent(request.getContent());
        permissionChecker.checkGroupMessagePermission(senderId, request.getGroupId());

        ChatMessage message = buildMessage(senderId, senderName, senderAvatar, MessageType.GROUP);
        message.setGroupId(request.getGroupId());
        message.setContentType(request.getContentType());
        message.setContent(request.getContent());
        message.setExtra(request.getExtra());
        message.setClientMessageId(request.getClientMessageId());

        messageRouter.broadcastMessage(message);
        messageRouter.sendToPersistence(message);

        log.info("Group message sent: messageId={}, senderId={}, groupId={}",
                message.getMessageId(), senderId, request.getGroupId());
    }

    @Override
    public void sendRoomMessage(Long senderId, String senderName, String senderAvatar, RoomMessageRequest request) {
        checkRateLimit(senderId, MessageType.ROOM);
        checkSensitiveContent(request.getContent());
        permissionChecker.checkRoomMessagePermission(senderId, request.getRoomId());

        ChatMessage message = buildMessage(senderId, senderName, senderAvatar, MessageType.ROOM);
        message.setRoomId(request.getRoomId());
        message.setContentType(request.getContentType());
        message.setContent(request.getContent());
        message.setExtra(request.getExtra());
        message.setClientMessageId(request.getClientMessageId());

        messageRouter.broadcastMessage(message);
        messageRouter.sendToPersistence(message);

        log.info("Room message sent: messageId={}, senderId={}, roomId={}",
                message.getMessageId(), senderId, request.getRoomId());
    }

    @Override
    public void sendPublicMessage(Long senderId, String senderName, String senderAvatar, PublicMessageRequest request) {
        checkRateLimit(senderId, MessageType.PUBLIC);
        checkSensitiveContent(request.getContent());
        permissionChecker.checkPublicMessagePermission(senderId, request.getReceiverId());

        ChatMessage message = buildMessage(senderId, senderName, senderAvatar, MessageType.PUBLIC);
        message.setReceiverId(request.getReceiverId());
        message.setContentType(request.getContentType());
        message.setContent(request.getContent());
        message.setExtra(request.getExtra());
        message.setClientMessageId(request.getClientMessageId());

        routePrivateMessage(message);
        messageRouter.sendToPersistence(message);

        log.info("Public message sent: messageId={}, senderId={}, receiverId={}",
                message.getMessageId(), senderId, request.getReceiverId());
    }

    private void checkRateLimit(Long senderId, MessageType messageType) {
        if (!rateLimiter.tryAcquire(senderId, messageType)) {
            int waitSeconds = rateLimiter.getWaitSeconds(senderId);
            throw new BusinessException.OperationNotAllowedException(
                    ChatExceptionMessage.ChatFormatter.rateLimitWait(waitSeconds)
            );
        }
    }

    private void checkSensitiveContent(String content) {
        if (sensitiveWordFilter.containsSensitiveWord(content)) {
            throw new BusinessException.ParamException(ChatExceptionMessage.SENSITIVE_CONTENT_DETECTED);
        }
    }

    private ChatMessage buildMessage(Long senderId, String senderName, String senderAvatar, MessageType messageType) {
        ChatMessage message = new ChatMessage();
        message.setMessageId(String.valueOf(snowflake.nextId()));
        message.setSenderId(senderId);
        message.setSenderName(senderName);
        message.setSenderAvatar(senderAvatar);
        message.setMessageType(messageType);
        message.setStatus(MessageStatus.SENT);
        message.setCreatedAt(LocalDateTime.now());
        return message;
    }

    private void routePrivateMessage(ChatMessage message) {
        Set<String> targetServers = new HashSet<>();

        SessionInfo receiverSession = sessionMappingManager.getSessionByUserId(message.getReceiverId());
        if (receiverSession != null) {
            targetServers.add(receiverSession.getServerAddress());
        }

        SessionInfo senderSession = sessionMappingManager.getSessionByUserId(message.getSenderId());
        if (senderSession != null) {
            targetServers.add(senderSession.getServerAddress());
        }

        if (!targetServers.isEmpty()) {
            messageRouter.routeMessage(message, targetServers);
        }
    }
}
