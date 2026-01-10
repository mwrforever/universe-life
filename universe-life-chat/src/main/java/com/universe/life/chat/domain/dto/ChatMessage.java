package com.universe.life.chat.domain.dto;

import com.universe.life.message.enums.ContentType;
import com.universe.life.message.enums.MessageStatus;
import com.universe.life.message.enums.MessageType;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 聊天消息 DTO
 * 用于 Kafka 消息传输和内部处理
 *
 * @author Kiro
 * @since 2026/01/09
 */
@Data
public class ChatMessage implements Serializable {

    /**
     * 消息ID（雪花算法生成）
     */
    private String messageId;

    /**
     * 发送者ID
     */
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
    private Long receiverId;

    /**
     * 群组ID（群聊）
     */
    private Long groupId;

    /**
     * 聊天室ID（公共聊天室）
     */
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
     * 消息内容
     */
    private String content;

    /**
     * 扩展信息（JSON格式）
     */
    private String extra;

    /**
     * 消息序列号
     */
    private Long sequence;

    /**
     * 消息状态
     */
    private MessageStatus status;

    /**
     * 目标服务器地址（用于消息路由）
     */
    private String targetServer;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;

    /**
     * 客户端消息ID（用于幂等处理）
     */
    private String clientMessageId;
}
