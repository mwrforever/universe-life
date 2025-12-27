package com.universe.life.user.privacy.domain.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.universe.life.user.privacy.enums.CommonStatus;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * <p>
 * 部门表
 * </p>
 *
 * @author 毛伟然
 * @since 2025-12-11
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("sys_department")
public class SysDepartment implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 部门ID
     */
    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 父部门ID（0为顶级部门）
     */
    private Long parentId;

    /**
     * 部门编码（唯一标识）
     */
    private String deptCode;

    /**
     * 部门名称
     */
    private String deptName;

    /**
     * 部门负责人ID（关联sys_user.id）
     */
    private Long leaderId;

    /**
     * 排序序号
     */
    private Integer sortOrder;

    /**
     * 状态：0 禁用 1 启用
     */
    private CommonStatus status;

    /**
     * 部门描述
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
