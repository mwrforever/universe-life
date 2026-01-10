package com.universe.life.message.domain.po;

import com.baomidou.mybatisplus.annotation.*;
import com.universe.life.message.enums.FriendStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 好友关系表 PO
 *
 * @author Kiro
 * @since 2026/01/09
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("user_friend")
public class UserFriend implements Serializable {

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
     * 好友ID
     */
    private Long friendId;

    /**
     * 好友备注名
     */
    private String remark;

    /**
     * 状态: 1-正常, 0-已删除
     */
    private FriendStatus status;

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
