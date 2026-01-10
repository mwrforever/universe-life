package com.universe.life.user.privacy.controller;

import com.universe.life.auth.common.domain.Result;
import com.universe.life.user.privacy.domain.dto.request.ResourceIdsRequest;
import com.universe.life.user.privacy.domain.dto.request.ResourceRoleAssignRequest;
import com.universe.life.user.privacy.domain.dto.request.ResourceRoleBatchAssignRequest;
import com.universe.life.user.privacy.domain.vo.BatchResultVO;
import com.universe.life.user.privacy.domain.vo.ResourceRoleVO;
import com.universe.life.user.privacy.domain.vo.ResourceTreeVO;
import com.universe.life.user.privacy.domain.vo.RoleOptionVO;
import com.universe.life.user.privacy.service.IAdminResourceRoleService;
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
 * 资源角色关联管理控制器
 *
 * @author 毛伟然
 * @since 2025-12-08
 */
@Slf4j
@Tag(name = "资源角色管理", description = "资源角色关联管理相关接口")
@RequiredArgsConstructor
@RestController
@RequestMapping("/admin/resource-role")
@Validated
public class AdminResourceRoleController {

    private final IAdminResourceRoleService resourceRoleService;

    @PostMapping
    @Operation(summary = "为角色分配资源权限", description = "为角色分配资源权限（会替换原有权限）")
    @PreAuthorize("@pm.match('sys:admin:resource-role:add')")
    public Result<Void> assignResources(@Valid @RequestBody ResourceRoleAssignRequest request) {
        log.info("为角色分配资源权限，角色ID：{}，资源ID列表：{}", request.getRoleId(), request.getResourceIds());
        resourceRoleService.assignResources(request);
        return Result.success();
    }

    @GetMapping("/roles/{roleId}/resources")
    @Operation(summary = "获取角色的资源权限列表", description = "获取角色的所有资源权限")
    @PreAuthorize("@pm.match('sys:admin:resource-role:roles:resources:read')")
    public Result<List<ResourceRoleVO>> getRoleResources(
            @Parameter(description = "角色ID", required = true) @PathVariable Long roleId) {
        log.info("获取角色资源权限列表，角色ID：{}", roleId);
        return Result.success(resourceRoleService.getRoleResources(roleId));
    }

    @DeleteMapping("/roles/{roleId}/resources/{resourceId}")
    @Operation(summary = "移除角色的资源权限", description = "移除角色的指定资源权限")
    @PreAuthorize("@pm.match('sys:admin:resource-role:roles:resources:delete')")
    public Result<Void> removeRoleResource(
            @Parameter(description = "角色ID", required = true) @PathVariable Long roleId,
            @Parameter(description = "资源ID", required = true) @PathVariable Long resourceId) {
        log.info("移除角色资源权限，角色ID：{}，资源ID：{}", roleId, resourceId);
        resourceRoleService.removeRoleResource(roleId, resourceId);
        return Result.success();
    }

    @DeleteMapping("/roles/{roleId}/resources")
    @Operation(summary = "批量移除角色的资源权限", description = "批量移除角色的资源权限")
    @PreAuthorize("@pm.match('sys:admin:resource-role:roles:resources:delete')")
    public Result<Void> batchRemoveRoleResources(
            @Parameter(description = "角色ID", required = true) @PathVariable Long roleId,
            @Valid @RequestBody ResourceIdsRequest request) {
        log.info("批量移除角色资源权限，角色ID：{}，资源ID列表：{}", roleId, request.getResourceIds());
        resourceRoleService.batchRemoveRoleResources(roleId, request);
        return Result.success();
    }

    @PutMapping("/roles/{roleId}/resources")
    @Operation(summary = "更新角色的资源权限（全量替换）", description = "更新角色的资源权限，全量替换")
    @PreAuthorize("@pm.match('sys:admin:resource-role:roles:resources:update')")
    public Result<Void> updateRoleResources(
            @Parameter(description = "角色ID", required = true) @PathVariable Long roleId,
            @Valid @RequestBody ResourceIdsRequest request) {
        log.info("更新角色资源权限，角色ID：{}，资源ID列表：{}", roleId, request.getResourceIds());
        resourceRoleService.updateRoleResources(roleId, request.getResourceIds());
        return Result.success();
    }

    @GetMapping("/resources/{resourceId}/roles")
    @Operation(summary = "获取拥有某资源权限的角色列表", description = "获取拥有指定资源权限的所有角色")
    @PreAuthorize("@pm.match('sys:admin:resource-role:resources:roles:read')")
    public Result<List<RoleOptionVO>> getResourceRoles(
            @Parameter(description = "资源ID", required = true) @PathVariable Long resourceId) {
        log.info("获取拥有资源权限的角色列表，资源ID：{}", resourceId);
        return Result.success(resourceRoleService.getResourceRoles(resourceId));
    }

    @GetMapping("/roles/{roleId}/resources/{resourceId}/check")
    @Operation(summary = "检查角色是否拥有某资源权限", description = "检查角色是否拥有指定资源权限")
    @PreAuthorize("@pm.match('sys:admin:resource-role:check:read')")
    public Result<Map<String, Boolean>> checkRoleResource(
            @Parameter(description = "角色ID", required = true) @PathVariable Long roleId,
            @Parameter(description = "资源ID", required = true) @PathVariable Long resourceId) {
        log.info("检查角色资源权限，角色ID：{}，资源ID：{}", roleId, resourceId);
        Boolean hasPermission = resourceRoleService.checkRoleResource(roleId, resourceId);
        return Result.success(Map.of("hasPermission", hasPermission));
    }

    @GetMapping("/roles/{roleId}/resources/tree")
    @Operation(summary = "获取角色的资源权限树", description = "获取角色的资源权限树（带checked状态）")
    @PreAuthorize("@pm.match('sys:admin:resource-role:roles:resources:tree:read')")
    public Result<List<ResourceTreeVO>> getRoleResourceTree(
            @Parameter(description = "角色ID", required = true) @PathVariable Long roleId) {
        log.info("获取角色资源权限树，角色ID：{}", roleId);
        return Result.success(resourceRoleService.getRoleResourceTree(roleId));
    }

    @PostMapping("/batch")
    @Operation(summary = "批量为角色分配资源权限", description = "批量为多个角色分配同一资源权限")
    @PreAuthorize("@pm.match('sys:admin:resource-role:batch:add')")
    public Result<BatchResultVO> batchAssignResource(@Valid @RequestBody ResourceRoleBatchAssignRequest request) {
        log.info("批量为角色分配资源权限，角色ID列表：{}，资源ID：{}", request.getRoleIds(), request.getResourceId());
        return Result.success(resourceRoleService.batchAssignResource(request));
    }

    @GetMapping("/users/{userId}/resources/{resourceCode}/check")
    @Operation(summary = "检查用户是否拥有某资源权限", description = "检查用户是否拥有指定资源权限")
    @PreAuthorize("@pm.match('sys:admin:resource-role:users:resources:check:read')")
    public Result<Map<String, Boolean>> checkUserResourcePermission(
            @Parameter(description = "用户ID", required = true) @PathVariable Long userId,
            @Parameter(description = "资源编码", required = true) @PathVariable String resourceCode) {
        log.info("检查用户资源权限，用户ID：{}，资源编码：{}", userId, resourceCode);
        Boolean hasPermission = resourceRoleService.checkUserResourcePermission(userId, resourceCode);
        return Result.success(Map.of("hasPermission", hasPermission));
    }
}
