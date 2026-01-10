package com.universe.life.message.domain.po;

import com.baomidou.mybatisplus.annotation.*;
import com.universe.life.message.enums.BooleanFlag;
import com.universe.life.message.enums.GroupStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 群组表 PO
 *
 * @author Kiro
 * @since 2026/01/09
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("chat_group")
public class ChatGroup implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 群组名称
     */
    private String name;

    /**
     * 群组头像URL
     */
    private String avatar;

    /**
     * 群组描述
     */
    private String description;

    /**
     * 群主用户ID
     */
    private Long ownerId;

    /**
     * 群聊邀请码（唯一）
     */
    private String groupCode;

    /**
     * 是否公开群: 0-否, 1-是
     */
    private BooleanFlag isPublic;

    /**
     * 允许成员邀请: 0-仅群主可邀请, 1-所有成员可邀请
     */
    private BooleanFlag allowMemberInvite;

    /**
     * 最大成员数
     */
    private Integer maxMembers;

    /**
     * 当前成员数
     */
    private Integer memberCount;

    /**
     * 状态: 1-正常, 0-已解散
     */
    private GroupStatus status;

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
