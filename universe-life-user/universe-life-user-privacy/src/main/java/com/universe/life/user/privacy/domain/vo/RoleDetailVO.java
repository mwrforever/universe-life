package com.universe.life.user.privacy.domain.vo;

import com.universe.life.user.privacy.enums.CommonStatus;
import com.universe.life.user.privacy.enums.DataScope;
import com.universe.life.user.privacy.enums.RoleType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 角色详情VO
 *
 * @author 毛伟然
 * @since 2025-12-08
 */
@Data
@Schema(description = "角色详情VO")
public class RoleDetailVO {

    @Schema(description = "角色ID")
    private Long id;

    @Schema(description = "角色编码")
    private String roleCode;

    @Schema(description = "角色名称")
    private String roleName;

    @Schema(description = "角色类型")
    private RoleType roleType;

    @Schema(description = "数据权限范围")
    private DataScope dataScope;

    @Schema(description = "排序序号")
    private Integer sortOrder;

    @Schema(description = "状态")
    private CommonStatus status;

    @Schema(description = "角色描述")
    private String description;

    @Schema(description = "创建时间")
    private LocalDateTime createdAt;

    @Schema(description = "更新时间")
    private LocalDateTime updatedAt;
}
