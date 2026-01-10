package com.universe.life.chat.listener;

import cn.hutool.json.JSONUtil;
import com.universe.life.chat.domain.dto.ChatMessage;
import com.universe.life.chat.domain.vo.ChatMessageVO;
import com.universe.life.chat.service.ChatConnectionManager;
import com.universe.life.message.enums.MessageType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

/**
 * Kafka 消息监听器
 * 接收 Kafka 消息并通过 WebSocket 推送给客户端
 *
 * @author Kiro
 * @since 2026/01/09
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class KafkaMessageListener {

    private final SimpMessagingTemplate messagingTemplate;
    private final ChatConnectionManager connectionManager;

    @Value("${chat.server.address:${spring.cloud.nacos.discovery.ip:localhost}:${server.port:8102}}")
    private String serverAddress;

    /**
     * 监听点对点消息
     */
    @KafkaListener(topics = "${chat.kafka.topic.message:chat-message}", groupId = "${spring.application.name}")
    public void handleMessage(String messageJson) {
        try {
            ChatMessage message = JSONUtil.toBean(messageJson, ChatMessage.class);

            // 检查消息是否发送到当前服务器
            if (!serverAddress.equals(message.getTargetServer())) {
                log.debug("Message not for this server: targetServer={}, currentServer={}",
                        message.getTargetServer(), serverAddress);
                return;
            }

            // 转换为 VO 并推送
            ChatMessageVO vo = convertToVO(message);
            deliverMessage(vo);

            log.debug("Message delivered: messageId={}, type={}", message.getMessageId(), message.getMessageType());
        } catch (Exception e) {
            log.error("Failed to handle message: {}", messageJson, e);
        }
    }

    /**
     * 监听广播消息
     */
    @KafkaListener(topics = "${chat.kafka.topic.broadcast:chat-broadcast}", groupId = "${spring.application.name}")
    public void handleBroadcast(String messageJson) {
        try {
            ChatMessage message = JSONUtil.toBean(messageJson, ChatMessage.class);

            // 转换为 VO 并推送
            ChatMessageVO vo = convertToVO(message);
            deliverBroadcastMessage(vo);

            log.debug("Broadcast message delivered: messageId={}, type={}", message.getMessageId(), message.getMessageType());
        } catch (Exception e) {
            log.error("Failed to handle broadcast message: {}", messageJson, e);
        }
    }

    /**
     * 推送点对点消息
     */
    private void deliverMessage(ChatMessageVO message) {
        MessageType type = message.getMessageType();

        if (type.isPrivate() || type.isPublic()) {
            // 私聊或公共单人会话：推送给接收者
            String destination = "/queue/messages";
            messagingTemplate.convertAndSendToUser(
                    String.valueOf(message.getReceiverId()),
                    destination,
                    message
            );
            log.debug("Private message sent to user {}: messageId={}", message.getReceiverId(), message.getMessageId());
        }
    }

    /**
     * 推送广播消息
     */
    private void deliverBroadcastMessage(ChatMessageVO message) {
        MessageType type = message.getMessageType();

        if (type.isGroup()) {
            // 群聊消息：推送到群组 topic
            String destination = "/topic/group/" + message.getGroupId();
            messagingTemplate.convertAndSend(destination, message);
            log.debug("Group message sent to {}: messageId={}", destination, message.getMessageId());
        } else if (type.isRoom()) {
            // 聊天室消息：推送到聊天室 topic
            String destination = "/topic/room/" + message.getRoomId();
            messagingTemplate.convertAndSend(destination, message);
            log.debug("Room message sent to {}: messageId={}", destination, message.getMessageId());
        }
    }

    /**
     * 转换为 VO
     */
    private ChatMessageVO convertToVO(ChatMessage message) {
        ChatMessageVO vo = new ChatMessageVO();
        vo.setMessageId(message.getMessageId());
        vo.setSenderId(message.getSenderId());
        vo.setSenderName(message.getSenderName());
        vo.setSenderAvatar(message.getSenderAvatar());
        vo.setReceiverId(message.getReceiverId());
        vo.setGroupId(message.getGroupId());
        vo.setRoomId(message.getRoomId());
        vo.setMessageType(message.getMessageType());
        vo.setContentType(message.getContentType());
        vo.setContent(message.getContent());
        vo.setExtra(message.getExtra());
        vo.setSequence(message.getSequence());
        vo.setStatus(message.getStatus());
        vo.setCreatedAt(message.getCreatedAt());
        vo.setClientMessageId(message.getClientMessageId());
        return vo;
    }
}
