package com.universe.life.message.domain.po;

import com.baomidou.mybatisplus.annotation.*;
import com.universe.life.message.enums.BooleanFlag;
import com.universe.life.message.enums.ConversationType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 会话列表表 PO
 *
 * @author Kiro
 * @since 2026/01/09
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("conversation")
public class Conversation implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 目标ID（用户ID/群组ID/聊天室ID）
     */
    private Long targetId;

    /**
     * 会话类型: 1-私聊, 2-群聊, 3-公共聊天室, 4-公共单人会话
     */
    private ConversationType conversationType;

    /**
     * 最后一条消息ID
     */
    private String lastMessageId;

    /**
     * 最后一条消息内容摘要
     */
    private String lastMessageContent;

    /**
     * 最后一条消息时间
     */
    private LocalDateTime lastMessageTime;

    /**
     * 未读消息数
     */
    private Integer unreadCount;

    /**
     * 是否置顶: 0-否, 1-是
     */
    private BooleanFlag isTop;

    /**
     * 是否免打扰: 0-否, 1-是
     */
    private BooleanFlag isMuted;

    /**
     * 是否删除: 0-否, 1-是
     */
    private BooleanFlag isDeleted;

    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
