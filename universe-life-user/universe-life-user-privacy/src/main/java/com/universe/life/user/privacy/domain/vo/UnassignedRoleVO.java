package com.universe.life.user.privacy.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 未分配角色VO
 *
 * @author 毛伟然
 * @since 2026-01-17
 */
@Data
@Schema(description = "未分配角色VO")
public class UnassignedRoleVO {

    @Schema(description = "角色ID")
    private Long id;

    @Schema(description = "角色编码")
    private String roleCode;

    @Schema(description = "角色名称")
    private String roleName;
}
