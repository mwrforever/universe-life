package com.universe.life.user.privacy.controller;

import com.universe.life.auth.common.domain.Result;
import com.universe.life.user.privacy.domain.dto.request.RoleIdsRequest;
import com.universe.life.user.privacy.domain.dto.request.UserRoleAssignRequest;
import com.universe.life.user.privacy.domain.dto.request.UserRoleBatchAssignRequest;
import com.universe.life.user.privacy.domain.vo.BatchResultVO;
import com.universe.life.user.privacy.domain.vo.UserPermissionVO;
import com.universe.life.user.privacy.domain.vo.UserRoleDetailVO;
import com.universe.life.user.privacy.service.IAdminUserRoleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 用户角色关联管理控制器
 *
 * @author 毛伟然
 * @since 2025-12-08
 */
@Slf4j
@Tag(name = "用户角色管理", description = "用户角色关联管理相关接口")
@RequiredArgsConstructor
@RestController
@RequestMapping("/admin/user-role")
@Validated
public class AdminUserRoleController {

    private final IAdminUserRoleService userRoleService;

    @PostMapping
    @Operation(summary = "为用户分配角色", description = "为用户分配角色（会替换原有角色）")
    @PreAuthorize("@pm.match('sys:admin:user-role:add')")
    public Result<Void> assignRoles(@Valid @RequestBody UserRoleAssignRequest request) {
        log.info("为用户分配角色，用户ID：{}，角色ID列表：{}", request.getUserId(), request.getRoleIds());
        userRoleService.assignRoles(request);
        return Result.success();
    }

    @GetMapping("/users/{userId}/roles")
    @Operation(summary = "获取用户的角色列表", description = "获取用户的所有角色")
    @PreAuthorize("@pm.match('sys:admin:user-role:users:roles:read')")
    public Result<List<UserRoleDetailVO>> getUserRoles(
            @Parameter(description = "用户ID", required = true) @PathVariable Long userId) {
        log.info("获取用户角色列表，用户ID：{}", userId);
        return Result.success(userRoleService.getUserRoles(userId));
    }

    @DeleteMapping("/users/{userId}/roles/{roleId}")
    @Operation(summary = "移除用户的角色", description = "移除用户的指定角色")
    @PreAuthorize("@pm.match('sys:admin:user-role:users:roles:delete')")
    public Result<Void> removeUserRole(
            @Parameter(description = "用户ID", required = true) @PathVariable Long userId,
            @Parameter(description = "角色ID", required = true) @PathVariable Long roleId) {
        log.info("移除用户角色，用户ID：{}，角色ID：{}", userId, roleId);
        userRoleService.removeUserRole(userId, roleId);
        return Result.success();
    }

    @DeleteMapping("/users/{userId}/roles")
    @Operation(summary = "批量移除用户的角色", description = "批量移除用户的角色")
    @PreAuthorize("@pm.match('sys:admin:user-role:users:roles:delete')")
    public Result<Void> batchRemoveUserRoles(
            @Parameter(description = "用户ID", required = true) @PathVariable Long userId,
            @Valid @RequestBody RoleIdsRequest request) {
        log.info("批量移除用户角色，用户ID：{}，角色ID列表：{}", userId, request.getRoleIds());
        userRoleService.batchRemoveUserRoles(userId, request);
        return Result.success();
    }

    @PutMapping("/users/{userId}/roles")
    @Operation(summary = "更新用户的角色（全量替换）", description = "更新用户的角色，全量替换")
    @PreAuthorize("@pm.match('sys:admin:user-role:users:roles:update')")
    public Result<Void> updateUserRoles(
            @Parameter(description = "用户ID", required = true) @PathVariable Long userId,
            @Valid @RequestBody RoleIdsRequest request) {
        log.info("更新用户角色，用户ID：{}，角色ID列表：{}", userId, request.getRoleIds());
        userRoleService.updateUserRoles(userId, request.getRoleIds());
        return Result.success();
    }

    @PostMapping("/batch")
    @Operation(summary = "批量为用户分配角色", description = "批量为多个用户分配同一角色")
    @PreAuthorize("@pm.match('sys:admin:user-role:batch:add')")
    public Result<BatchResultVO> batchAssignRole(@Valid @RequestBody UserRoleBatchAssignRequest request) {
        log.info("批量为用户分配角色，用户ID列表：{}，角色ID：{}", request.getUserIds(), request.getRoleId());
        return Result.success(userRoleService.batchAssignRole(request));
    }

    @GetMapping("/users/{userId}/roles/{roleId}/check")
    @Operation(summary = "检查用户是否拥有某角色", description = "检查用户是否拥有指定角色")
    @PreAuthorize("@pm.match('sys:admin:user-role:check:read')")
    public Result<Map<String, Boolean>> checkUserRole(
            @Parameter(description = "用户ID", required = true) @PathVariable Long userId,
            @Parameter(description = "角色ID", required = true) @PathVariable Long roleId) {
        log.info("检查用户角色，用户ID：{}，角色ID：{}", userId, roleId);
        Boolean hasRole = userRoleService.checkUserRole(userId, roleId);
        return Result.success(Map.of("hasRole", hasRole));
    }

    @GetMapping("/users/{userId}/permissions")
    @Operation(summary = "获取用户的所有权限", description = "获取用户的所有角色和资源权限")
    @PreAuthorize("@pm.match('sys:admin:user-role:users:permissions:read')")
    public Result<UserPermissionVO> getUserPermissions(
            @Parameter(description = "用户ID", required = true) @PathVariable Long userId) {
        log.info("获取用户权限，用户ID：{}", userId);
        return Result.success(userRoleService.getUserPermissions(userId));
    }
}
