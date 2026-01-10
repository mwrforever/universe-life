package com.universe.life.message.domain.po;

import com.baomidou.mybatisplus.annotation.*;
import com.universe.life.message.enums.RoomStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 公共聊天室表 PO
 *
 * @author Kiro
 * @since 2026/01/09
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("public_room")
public class PublicRoom implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 聊天室名称
     */
    private String name;

    /**
     * 聊天室头像URL
     */
    private String avatar;

    /**
     * 聊天室描述
     */
    private String description;

    /**
     * 聊天室分类
     */
    private String category;

    /**
     * 累计加入人数
     */
    private Integer totalMembers;

    /**
     * 最大在线人数
     */
    private Integer maxOnline;

    /**
     * 当前在线人数
     */
    private Integer currentOnline;

    /**
     * 状态: 1-正常, 0-已关闭
     */
    private RoomStatus status;

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
