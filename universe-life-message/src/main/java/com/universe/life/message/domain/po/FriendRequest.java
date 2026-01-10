package com.universe.life.message.domain.po;

import com.baomidou.mybatisplus.annotation.*;
import com.universe.life.message.enums.FriendRequestStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 好友申请表 PO
 *
 * @author Kiro
 * @since 2026/01/09
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("friend_request")
public class FriendRequest implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 申请人ID
     */
    private Long fromUserId;

    /**
     * 目标用户ID
     */
    private Long toUserId;

    /**
     * 申请消息/验证信息
     */
    private String message;

    /**
     * 状态: 0-待处理, 1-已接受, 2-已拒绝, 3-已过期
     */
    private FriendRequestStatus status;

    /**
     * 处理时间
     */
    private LocalDateTime handledAt;

    /**
     * 过期时间
     */
    private LocalDateTime expiredAt;

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
