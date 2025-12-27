package com.universe.life.user.privacy.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * 用户权限VO
 *
 * @author 毛伟然
 * @since 2025-12-08
 */
@Data
@Schema(description = "用户权限VO")
public class UserPermissionVO {

    @Schema(description = "用户ID")
    private Long userId;

    @Schema(description = "角色列表")
    private List<RoleOptionVO> roles;

    @Schema(description = "权限列表")
    private List<ResourceSimpleVO> permissions;
}
