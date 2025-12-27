package com.universe.life.user.privacy.domain.po;

import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.universe.life.user.privacy.enums.CommonStatus;
import com.universe.life.user.privacy.enums.DataScope;
import com.universe.life.user.privacy.enums.RoleType;
import java.time.LocalDateTime;
import java.io.Serializable;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * <p>
 * 系统角色表
 * </p>
 *
 * @author 毛伟然
 * @since 2025-12-01
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("role")
public class Role implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 角色ID
     */
    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 角色编码（唯一标识）
     */
    private String roleCode;

    /**
     * 角色名称
     */
    private String roleName;

    /**
     * 角色类型：0 系统角色 1 业务角色 2 自定义角色
     */
    private RoleType roleType;

    /**
     * 数据权限范围：0 全部 1 本部门 2 本部门及下级 3 仅自己
     */
    private DataScope dataScope;

    /**
     * 排序序号
     */
    private Integer sortOrder;

    /**
     * 状态：0 禁用 1 启用
     */
    private CommonStatus status;

    /**
     * 角色描述
     */
    private String description;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    private LocalDateTime updatedAt;

    /**
     * 软删除标记（0 未删除 1 已删除）
     */
    private Boolean deleted;


}
