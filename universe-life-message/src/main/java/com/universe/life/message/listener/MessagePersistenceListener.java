package com.universe.life.message.listener;

import com.universe.life.message.config.KafkaConfig;
import com.universe.life.message.domain.dto.ChatMessageDTO;
import com.universe.life.message.service.MessagePersistenceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

/**
 * 消息持久化 Kafka 监听器
 * 监听 chat-persistence topic，将消息持久化到 MongoDB
 *
 * @author Kiro
 * @since 2026/01/09
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class MessagePersistenceListener {

    private final MessagePersistenceService messagePersistenceService;

    @KafkaListener(topics = KafkaConfig.TOPIC_CHAT_PERSISTENCE, groupId = "${spring.kafka.consumer.group-id:message-service-group}")
    public void handleMessagePersistence(ChatMessageDTO message) {
        try {
            log.debug("收到消息持久化请求: messageId={}, senderId={}, type={}",
                    message.getMessageId(), message.getSenderId(), message.getMessageType());

            messagePersistenceService.saveMessage(message);

            log.debug("消息持久化成功: messageId={}", message.getMessageId());
        } catch (Exception e) {
            log.error("消息持久化失败: messageId={}, error={}", message.getMessageId(), e.getMessage(), e);
            // TODO: 发送到死信队列或重试队列
        }
    }
}
