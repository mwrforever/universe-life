package com.universe.life.user.privacy.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 角色选项VO（用于下拉列表）
 *
 * @author 毛伟然
 * @since 2025-12-08
 */
@Data
@Schema(description = "角色选项VO")
public class RoleOptionVO {

    @Schema(description = "角色ID")
    private Long id;

    @Schema(description = "角色编码")
    private String roleCode;

    @Schema(description = "角色名称")
    private String roleName;
}
