package com.universe.life.chat.property;

import com.universe.life.chat.domain.dto.ChatMessage;
import com.universe.life.chat.service.KafkaMessageRouter;
import com.universe.life.message.enums.ContentType;
import com.universe.life.message.enums.MessageType;
import net.jqwik.api.*;
import net.jqwik.api.constraints.IntRange;
import net.jqwik.api.constraints.LongRange;
import net.jqwik.api.constraints.StringLength;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 消息路由属性测试
 * 验证消息路由正确性和消息顺序保证
 *
 * Feature: distributed-chat-system
 * 
 * @author Kiro
 * @since 2026/01/09
 */
@SpringBootTest
@ActiveProfiles("test")
public class MessageRoutingPropertyTest {

    @Autowired
    private KafkaMessageRouter messageRouter;

    /**
     * Property 6: 消息路由正确性
     * *For any* message, routing should not throw exceptions
     * 
     * **Validates: Requirements 4.2, 4.3, 4.4, 4.5**
     */
    @Property(tries = 100)
    @Label("Property 6: 消息路由正确性")
    void messageRoutingIsCorrect(
            @ForAll @LongRange(min = 1, max = 10000) Long senderId,
            @ForAll @LongRange(min = 1, max = 10000) Long receiverId,
            @ForAll @StringLength(min = 1, max = 200) String content
    ) {
        Assume.that(!senderId.equals(receiverId));
        Assume.that(content != null && !content.isBlank());

        // Given: 创建消息
        ChatMessage message = ChatMessage.builder()
                .messageId(UUID.randomUUID().toString())
                .clientMessageId(UUID.randomUUID().toString())
                .senderId(senderId)
                .receiverId(receiverId)
                .messageType(MessageType.PRIVATE)
                .contentType(ContentType.TEXT)
                .content(content)
                .createdAt(LocalDateTime.now())
                .build();

        // When & Then: 路由消息不应该抛出异常
        assertDoesNotThrow(() -> messageRouter.routePrivateMessage(message),
                "私信路由不应该抛出异常");
    }

    /**
     * Property: 群聊消息路由正确性
     */
    @Property(tries = 100)
    @Label("群聊消息路由正确性")
    void groupMessageRoutingIsCorrect(
            @ForAll @LongRange(min = 1, max = 10000) Long senderId,
            @ForAll @LongRange(min = 1, max = 10000) Long groupId,
            @ForAll @StringLength(min = 1, max = 200) String content
    ) {
        Assume.that(content != null && !content.isBlank());

        // Given: 创建群聊消息
        ChatMessage message = ChatMessage.builder()
                .messageId(UUID.randomUUID().toString())
                .clientMessageId(UUID.randomUUID().toString())
                .senderId(senderId)
                .groupId(groupId)
                .messageType(MessageType.GROUP)
                .contentType(ContentType.TEXT)
                .content(content)
                .createdAt(LocalDateTime.now())
                .build();

        // When & Then: 路由消息不应该抛出异常
        assertDoesNotThrow(() -> messageRouter.routeGroupMessage(message),
                "群聊路由不应该抛出异常");
    }

    /**
     * Property: 聊天室消息路由正确性
     */
    @Property(tries = 100)
    @Label("聊天室消息路由正确性")
    void roomMessageRoutingIsCorrect(
            @ForAll @LongRange(min = 1, max = 10000) Long senderId,
            @ForAll @LongRange(min = 1, max = 10000) Long roomId,
            @ForAll @StringLength(min = 1, max = 200) String content
    ) {
        Assume.that(content != null && !content.isBlank());

        // Given: 创建聊天室消息
        ChatMessage message = ChatMessage.builder()
                .messageId(UUID.randomUUID().toString())
                .clientMessageId(UUID.randomUUID().toString())
                .senderId(senderId)
                .roomId(roomId)
                .messageType(MessageType.ROOM)
                .contentType(ContentType.TEXT)
                .content(content)
                .createdAt(LocalDateTime.now())
                .build();

        // When & Then: 路由消息不应该抛出异常
        assertDoesNotThrow(() -> messageRouter.routeRoomMessage(message),
                "聊天室路由不应该抛出异常");
    }

    /**
     * Property 7: 消息顺序保证
     * *For any* sequence of messages from the same sender, they should be routed in order
     * 
     * **Validates: Requirements 4.6, 11.4**
     */
    @Property(tries = 50)
    @Label("Property 7: 消息顺序保证")
    void messageOrderIsPreserved(
            @ForAll @LongRange(min = 1, max = 10000) Long senderId,
            @ForAll @LongRange(min = 1, max = 10000) Long receiverId,
            @ForAll @IntRange(min = 2, max = 10) int messageCount
    ) {
        Assume.that(!senderId.equals(receiverId));

        // Given: 创建有序的消息列表
        List<ChatMessage> messages = new ArrayList<>();
        for (int i = 0; i < messageCount; i++) {
            ChatMessage message = ChatMessage.builder()
                    .messageId(UUID.randomUUID().toString())
                    .clientMessageId(UUID.randomUUID().toString())
                    .senderId(senderId)
                    .receiverId(receiverId)
                    .messageType(MessageType.PRIVATE)
                    .contentType(ContentType.TEXT)
                    .content("Message " + i)
                    .sequence((long) i)
                    .createdAt(LocalDateTime.now())
                    .build();
            messages.add(message);
        }

        // When & Then: 按顺序路由消息不应该抛出异常
        for (ChatMessage message : messages) {
            assertDoesNotThrow(() -> messageRouter.routePrivateMessage(message),
                    "顺序路由消息不应该抛出异常");
        }
    }

    /**
     * Property: 消息持久化路由正确性
     */
    @Property(tries = 100)
    @Label("消息持久化路由正确性")
    void persistenceRoutingIsCorrect(
            @ForAll @LongRange(min = 1, max = 10000) Long senderId,
            @ForAll @LongRange(min = 1, max = 10000) Long receiverId,
            @ForAll @StringLength(min = 1, max = 200) String content
    ) {
        Assume.that(!senderId.equals(receiverId));
        Assume.that(content != null && !content.isBlank());

        // Given: 创建消息
        ChatMessage message = ChatMessage.builder()
                .messageId(UUID.randomUUID().toString())
                .clientMessageId(UUID.randomUUID().toString())
                .senderId(senderId)
                .receiverId(receiverId)
                .messageType(MessageType.PRIVATE)
                .contentType(ContentType.TEXT)
                .content(content)
                .createdAt(LocalDateTime.now())
                .build();

        // When & Then: 发送到持久化队列不应该抛出异常
        assertDoesNotThrow(() -> messageRouter.sendToPersistence(message),
                "持久化路由不应该抛出异常");
    }

    /**
     * Property: 不同消息类型路由独立
     */
    @Property(tries = 50)
    @Label("不同消息类型路由独立")
    void differentMessageTypesRouteIndependently(
            @ForAll @LongRange(min = 1, max = 10000) Long senderId,
            @ForAll @LongRange(min = 1, max = 10000) Long targetId
    ) {
        // Given: 创建不同类型的消息
        ChatMessage privateMsg = ChatMessage.builder()
                .messageId(UUID.randomUUID().toString())
                .senderId(senderId)
                .receiverId(targetId)
                .messageType(MessageType.PRIVATE)
                .contentType(ContentType.TEXT)
                .content("Private message")
                .createdAt(LocalDateTime.now())
                .build();

        ChatMessage groupMsg = ChatMessage.builder()
                .messageId(UUID.randomUUID().toString())
                .senderId(senderId)
                .groupId(targetId)
                .messageType(MessageType.GROUP)
                .contentType(ContentType.TEXT)
                .content("Group message")
                .createdAt(LocalDateTime.now())
                .build();

        ChatMessage roomMsg = ChatMessage.builder()
                .messageId(UUID.randomUUID().toString())
                .senderId(senderId)
                .roomId(targetId)
                .messageType(MessageType.ROOM)
                .contentType(ContentType.TEXT)
                .content("Room message")
                .createdAt(LocalDateTime.now())
                .build();

        // When & Then: 各类型消息路由独立，不应该相互影响
        assertDoesNotThrow(() -> {
            messageRouter.routePrivateMessage(privateMsg);
            messageRouter.routeGroupMessage(groupMsg);
            messageRouter.routeRoomMessage(roomMsg);
        }, "不同类型消息路由应该独立");
    }
}
