package com.universe.life.message.repository;

import com.universe.life.message.domain.document.ChatMessageDocument;
import com.universe.life.message.enums.MessageType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 聊天消息 MongoDB Repository
 *
 * @author Kiro
 * @since 2026/01/09
 */
@Repository
public interface ChatMessageRepository extends MongoRepository<ChatMessageDocument, String> {

    /**
     * 根据客户端消息ID查询（幂等检查）
     */
    Optional<ChatMessageDocument> findByClientMessageId(String clientMessageId);

    /**
     * 查询私聊历史消息
     */
    @Query("{ $or: [ { 'senderId': ?0, 'receiverId': ?1 }, { 'senderId': ?1, 'receiverId': ?0 } ], 'messageType': ?2 }")
    Page<ChatMessageDocument> findPrivateMessages(Long userId1, Long userId2, MessageType messageType, Pageable pageable);

    /**
     * 查询群聊历史消息
     */
    Page<ChatMessageDocument> findByGroupIdAndMessageTypeOrderByCreatedAtDesc(Long groupId, MessageType messageType, Pageable pageable);

    /**
     * 查询聊天室历史消息
     */
    Page<ChatMessageDocument> findByRoomIdAndMessageTypeOrderByCreatedAtDesc(Long roomId, MessageType messageType, Pageable pageable);

    /**
     * 查询公共会话历史消息
     */
    @Query("{ $or: [ { 'senderId': ?0, 'receiverId': ?1 }, { 'senderId': ?1, 'receiverId': ?0 } ], 'messageType': ?2 }")
    Page<ChatMessageDocument> findPublicMessages(Long userId1, Long userId2, MessageType messageType, Pageable pageable);

    /**
     * 查询用户发送的消息（用于消息同步）
     */
    List<ChatMessageDocument> findBySenderIdAndSequenceGreaterThanOrderBySequenceAsc(Long senderId, Long sequence);

    /**
     * 查询指定时间之后的私聊消息
     */
    @Query("{ $or: [ { 'senderId': ?0, 'receiverId': ?1 }, { 'senderId': ?1, 'receiverId': ?0 } ], 'messageType': ?2, 'createdAt': { $gt: ?3 } }")
    List<ChatMessageDocument> findPrivateMessagesAfter(Long userId1, Long userId2, MessageType messageType, LocalDateTime after);

    /**
     * 查询指定时间之后的群聊消息
     */
    List<ChatMessageDocument> findByGroupIdAndMessageTypeAndCreatedAtAfterOrderByCreatedAtAsc(Long groupId, MessageType messageType, LocalDateTime after);

    /**
     * 统计用户未读消息数
     */
    long countByReceiverIdAndStatusAndCreatedAtAfter(Long receiverId, com.universe.life.message.enums.MessageStatus status, LocalDateTime after);
}
