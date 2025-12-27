package com.universe.life.user.privacy.domain.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.constraints.NotNull;

/**
 * 用户角色请求对象
 *
 * @author Claude
 * @since 2025-12-02
 */
@Data
@Schema(description = "用户角色请求对象")
public class UserRoleRequest {

    @NotNull(message = "角色ID不能为空")
    @Schema(description = "角色ID", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long roleId;

    @Schema(description = "角色名称", example = "普通用户")
    private String roleName;
}