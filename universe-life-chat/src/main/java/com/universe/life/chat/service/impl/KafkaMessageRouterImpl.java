package com.universe.life.chat.service.impl;

import cn.hutool.json.JSONUtil;
import com.universe.life.chat.domain.dto.ChatMessage;
import com.universe.life.chat.service.KafkaMessageRouter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.concurrent.CompletableFuture;

/**
 * Kafka 消息路由器实现
 * 支持消息重试和死信队列
 *
 * @author Kiro
 * @since 2026/01/09
 */
@Slf4j
@Service
public class KafkaMessageRouterImpl implements KafkaMessageRouter {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final RetryTemplate retryTemplate;

    @Value("${chat.kafka.topic.message:chat-message}")
    private String messageTopic;

    @Value("${chat.kafka.topic.broadcast:chat-broadcast}")
    private String broadcastTopic;

    @Value("${chat.kafka.topic.persistence:chat-persistence}")
    private String persistenceTopic;

    @Value("${chat.kafka.topic.dead-letter:chat-dead-letter}")
    private String deadLetterTopic;

    public KafkaMessageRouterImpl(KafkaTemplate<String, String> kafkaTemplate, RetryTemplate retryTemplate) {
        this.kafkaTemplate = kafkaTemplate;
        this.retryTemplate = retryTemplate;
    }

    @Override
    public void routeMessage(ChatMessage message, Set<String> targetServers) {
        for (String targetServer : targetServers) {
            ChatMessage routedMessage = copyMessage(message);
            routedMessage.setTargetServer(targetServer);
            String routedJson = JSONUtil.toJsonStr(routedMessage);

            sendWithRetry(messageTopic, targetServer, routedJson, message.getMessageId(), "route");
        }
    }

    @Override
    public void broadcastMessage(ChatMessage message) {
        String messageJson = JSONUtil.toJsonStr(message);
        sendWithRetry(broadcastTopic, message.getMessageId(), messageJson, message.getMessageId(), "broadcast");
    }

    @Override
    public void sendToPersistence(ChatMessage message) {
        String messageJson = JSONUtil.toJsonStr(message);
        String partitionKey = String.valueOf(message.getSenderId());
        sendWithRetry(persistenceTopic, partitionKey, messageJson, message.getMessageId(), "persistence");
    }

    /**
     * 带重试机制的消息发送
     */
    private void sendWithRetry(String topic, String key, String value, String messageId, String operation) {
        try {
            retryTemplate.execute(context -> {
                int attempt = context.getRetryCount() + 1;
                if (attempt > 1) {
                    log.warn("Retrying {} message: messageId={}, attempt={}", operation, messageId, attempt);
                }

                CompletableFuture<SendResult<String, String>> future = kafkaTemplate.send(topic, key, value);
                future.whenComplete((result, ex) -> {
                    if (ex != null) {
                        log.error("Failed to {} message: messageId={}, error={}", operation, messageId, ex.getMessage());
                        throw new RuntimeException(ex);
                    } else {
                        log.debug("Message {} success: messageId={}, partition={}, offset={}",
                                operation, messageId,
                                result.getRecordMetadata().partition(),
                                result.getRecordMetadata().offset());
                    }
                });
                // 同步等待结果以支持重试
                future.get();
                return null;
            }, context -> {
                // 重试耗尽后发送到死信队列
                log.error("All retries exhausted for {} message: messageId={}, sending to dead letter queue",
                        operation, messageId);
                sendToDeadLetter(topic, key, value, messageId, operation);
                return null;
            });
        } catch (Exception e) {
            log.error("Unexpected error during {} message: messageId={}", operation, messageId, e);
            sendToDeadLetter(topic, key, value, messageId, operation);
        }
    }

    /**
     * 发送到死信队列
     */
    private void sendToDeadLetter(String originalTopic, String key, String value, String messageId, String operation) {
        try {
            String deadLetterKey = originalTopic + ":" + key;
            kafkaTemplate.send(deadLetterTopic, deadLetterKey, value);
            log.info("Message sent to dead letter queue: messageId={}, originalTopic={}, operation={}",
                    messageId, originalTopic, operation);
        } catch (Exception e) {
            log.error("Failed to send message to dead letter queue: messageId={}", messageId, e);
        }
    }

    private ChatMessage copyMessage(ChatMessage source) {
        ChatMessage copy = new ChatMessage();
        copy.setMessageId(source.getMessageId());
        copy.setSenderId(source.getSenderId());
        copy.setSenderName(source.getSenderName());
        copy.setSenderAvatar(source.getSenderAvatar());
        copy.setReceiverId(source.getReceiverId());
        copy.setGroupId(source.getGroupId());
        copy.setRoomId(source.getRoomId());
        copy.setMessageType(source.getMessageType());
        copy.setContentType(source.getContentType());
        copy.setContent(source.getContent());
        copy.setExtra(source.getExtra());
        copy.setSequence(source.getSequence());
        copy.setStatus(source.getStatus());
        copy.setCreatedAt(source.getCreatedAt());
        copy.setClientMessageId(source.getClientMessageId());
        return copy;
    }
}
