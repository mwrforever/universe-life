package com.universe.life.message.domain.document;

import com.universe.life.message.enums.ContentType;
import com.universe.life.message.enums.MessageStatus;
import com.universe.life.message.enums.MessageType;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

/**
 * 聊天消息 MongoDB 文档
 *
 * @author Kiro
 * @since 2026/01/09
 */
@Data
@Document(collection = "chat_message")
@CompoundIndexes({
        @CompoundIndex(name = "idx_private_chat", def = "{'senderId': 1, 'receiverId': 1, 'createdAt': -1}"),
        @CompoundIndex(name = "idx_group_chat", def = "{'groupId': 1, 'createdAt': -1}"),
        @CompoundIndex(name = "idx_room_chat", def = "{'roomId': 1, 'createdAt': -1}"),
        @CompoundIndex(name = "idx_sync", def = "{'senderId': 1, 'sequence': 1}")
})
public class ChatMessageDocument {

    /**
     * 消息ID（雪花算法生成）
     */
    @Id
    private String id;

    /**
     * 发送者ID
     */
    @Indexed
    private Long senderId;

    /**
     * 发送者用户名
     */
    private String senderName;

    /**
     * 发送者头像
     */
    private String senderAvatar;

    /**
     * 接收者ID（私聊/公共单人会话）
     */
    @Indexed
    private Long receiverId;

    /**
     * 群组ID（群聊）
     */
    @Indexed
    private Long groupId;

    /**
     * 聊天室ID（公共聊天室）
     */
    @Indexed
    private Long roomId;

    /**
     * 消息类型
     */
    private MessageType messageType;

    /**
     * 内容类型
     */
    private ContentType contentType;

    /**
     * 消息内容（加密存储）
     */
    private String content;

    /**
     * 扩展信息（JSON格式）
     */
    private String extra;

    /**
     * 消息序列号
     */
    @Indexed
    private Long sequence;

    /**
     * 消息状态
     */
    private MessageStatus status;

    /**
     * 客户端消息ID（用于幂等处理）
     */
    @Indexed(unique = true, sparse = true)
    private String clientMessageId;

    /**
     * 是否加密
     */
    private Boolean encrypted;

    /**
     * 创建时间
     */
    @Indexed
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    private LocalDateTime updatedAt;
}
