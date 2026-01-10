package com.universe.life.chat.service;

import com.universe.life.chat.domain.dto.request.GroupMessageRequest;
import com.universe.life.chat.domain.dto.request.PrivateMessageRequest;
import com.universe.life.chat.domain.dto.request.PublicMessageRequest;
import com.universe.life.chat.domain.dto.request.RoomMessageRequest;

/**
 * 聊天消息服务接口
 *
 * @author Kiro
 * @since 2026/01/09
 */
public interface ChatMessageService {

    /**
     * 发送私信消息
     */
    void sendPrivateMessage(Long senderId, String senderName, String senderAvatar, PrivateMessageRequest request);

    /**
     * 发送群聊消息
     */
    void sendGroupMessage(Long senderId, String senderName, String senderAvatar, GroupMessageRequest request);

    /**
     * 发送公共聊天室消息
     */
    void sendRoomMessage(Long senderId, String senderName, String senderAvatar, RoomMessageRequest request);

    /**
     * 发送公共单人会话消息
     */
    void sendPublicMessage(Long senderId, String senderName, String senderAvatar, PublicMessageRequest request);
}
