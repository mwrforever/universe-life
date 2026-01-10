package com.universe.life.message.domain.po;

import com.baomidou.mybatisplus.annotation.*;
import com.universe.life.message.enums.BooleanFlag;
import com.universe.life.message.enums.GroupMemberRole;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 群组成员表 PO
 *
 * @author Kiro
 * @since 2026/01/09
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("group_member")
public class GroupMember implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 群组ID
     */
    private Long groupId;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 群内昵称
     */
    private String nickname;

    /**
     * 角色: 0-普通成员, 1-管理员, 2-群主
     */
    private GroupMemberRole role;

    /**
     * 是否被禁言: 0-否, 1-是
     */
    private BooleanFlag muted;

    /**
     * 禁言截止时间（NULL表示永久禁言）
     */
    private LocalDateTime mutedUntil;

    /**
     * 邀请人ID
     */
    private Long inviterId;

    /**
     * 加入时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime joinedAt;

    /**
     * 更新时间
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
