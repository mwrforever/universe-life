package com.universe.life.user.privacy.domain.po;

import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.universe.life.user.privacy.enums.ResourceStatus;
import com.universe.life.user.privacy.enums.ResourceType;
import java.time.LocalDateTime;
import java.io.Serializable;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * <p>
 * 系统资源表
 * </p>
 *
 * @author 毛伟然
 * @since 2025-12-01
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("resource")
public class Resource implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 资源ID
     */
    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 资源编码（唯一标识）
     */
    private String resourceCode;

    /**
     * 资源名称
     */
    private String resourceName;

    /**
     * 资源类型：0 菜单 1 按钮 2 接口 3 数据
     */
    private ResourceType resourceType;

    /**
     * 所属微服务名称（如：user-service, chat-service）
     */
    private String serviceName;

    /**
     * URL路径模式（如：/api/v1/users/**）
     */
    private String urlPattern;

    /**
     * HTTP方法（GET,POST,PUT,DELETE等，逗号分隔）
     */
    private String httpMethod;

    /**
     * 父资源ID（用于构建树形结构）
     */
    private Long parentId;

    /**
     * 排序序号
     */
    private Integer sortOrder;

    /**
     * 状态：0 禁用 1 启用
     */
    private ResourceStatus status;

    /**
     * 资源描述
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
