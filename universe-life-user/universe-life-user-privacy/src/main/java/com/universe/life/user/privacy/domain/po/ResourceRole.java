package com.universe.life.user.privacy.domain.po;

import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import java.time.LocalDateTime;
import java.io.Serializable;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * <p>
 * 资源角色关联表
 * </p>
 *
 * @author 毛伟然
 * @since 2025-12-01
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("resource_role")
public class ResourceRole implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 资源ID
     */
    private Long resourceId;

    /**
     * 角色ID
     */
    private Long roleId;

    /**
     * 授权人ID
     */
    private Long grantedBy;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;


}
