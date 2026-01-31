package com.universe.life.user.privacy.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * 用户角色分配状态VO
 *
 * @author 毛伟然
 * @since 2026-01-17
 */
@Data
@Schema(description = "用户角色分配状态VO")
public class RoleAssignmentStatusVO {

    @Schema(description = "用户ID")
    private Long userId;

    @Schema(description = "已分配的角色列表")
    private List<AssignedRoleVO> assignedRoles;

    @Schema(description = "未分配的角色列表")
    private List<UnassignedRoleVO> unassignedRoles;
}
