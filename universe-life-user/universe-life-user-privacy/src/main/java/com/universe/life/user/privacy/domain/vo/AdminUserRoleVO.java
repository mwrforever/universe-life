package com.universe.life.user.privacy.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 用户角色响应视图对象（用于创建用户响应）
 * 对应接口中roles字段格式
 *
 * @author Claude
 * @since 2025-12-03
 */
@Data
@Schema(description = "用户角色响应视图对象")
public class AdminUserRoleVO {

    @Schema(description = "角色ID", example = "1")
    private Long roleId;

    @Schema(description = "角色名称", example = "普通用户")
    private String roleName;
}