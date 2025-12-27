package com.universe.life.user.privacy.controller;

import com.universe.life.auth.common.domain.Result;
import com.universe.life.common.result.PageResult;
import com.universe.life.user.privacy.domain.dao.query.RoleListQuery;
import com.universe.life.user.privacy.domain.dto.request.RoleCreateRequest;
import com.universe.life.user.privacy.domain.dto.request.RoleStatusUpdateRequest;
import com.universe.life.user.privacy.domain.dto.request.RoleUpdateRequest;
import com.universe.life.user.privacy.domain.vo.ResourceSimpleVO;
import com.universe.life.user.privacy.domain.vo.RoleDetailVO;
import com.universe.life.user.privacy.domain.vo.RoleListVO;
import com.universe.life.user.privacy.domain.vo.RoleOptionVO;
import com.universe.life.user.privacy.service.IAdminRoleService;
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

/**
 * 角色管理控制器
 *
 * @author 毛伟然
 * @since 2025-12-08
 */
@Slf4j
@Tag(name = "角色管理", description = "RBAC角色管理相关接口")
@RequiredArgsConstructor
@RestController
@RequestMapping("/admin/role")
@Validated
public class AdminRoleController {

    private final IAdminRoleService roleService;

    @PostMapping
    @Operation(summary = "创建角色", description = "创建新的角色")
    @PreAuthorize("@pm.match('sys:admin:role:add')")
    public Result<RoleDetailVO> createRole(@Valid @RequestBody RoleCreateRequest request) {
        log.info("创建角色，角色编码：{}", request.getRoleCode());
        return Result.success(roleService.createRole(request));
    }

    @GetMapping("/{id}")
    @Operation(summary = "获取角色详情", description = "根据ID获取角色详细信息")
    @PreAuthorize("@pm.match('sys:admin:role:read')")
    public Result<RoleDetailVO> getRoleById(
            @Parameter(description = "角色ID", required = true) @PathVariable Long id) {
        log.info("查询角色详情，角色ID：{}", id);
        return Result.success(roleService.getRoleById(id));
    }

    @PutMapping("/{id}")
    @Operation(summary = "更新角色", description = "更新角色信息")
    @PreAuthorize("@pm.match('sys:admin:role:update')")
    public Result<RoleDetailVO> updateRole(
            @Parameter(description = "角色ID", required = true) @PathVariable Long id,
            @Valid @RequestBody RoleUpdateRequest request) {
        log.info("更新角色，角色ID：{}", id);
        return Result.success(roleService.updateRole(id, request));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除角色", description = "删除角色")
    @PreAuthorize("@pm.match('sys:admin:role:delete')")
    public Result<Void> deleteRole(
            @Parameter(description = "角色ID", required = true) @PathVariable Long id) {
        log.info("删除角色，角色ID：{}", id);
        roleService.deleteRole(id);
        return Result.success();
    }

    @GetMapping("/list")
    @Operation(summary = "分页查询角色列表", description = "分页查询角色列表")
    @PreAuthorize("@pm.match('sys:admin:role:list:read')")
    public Result<PageResult<RoleListVO>> pageRoles(@Valid RoleListQuery query) {
        log.info("分页查询角色列表，查询条件：{}", query);
        return Result.success(roleService.pageRoles(query));
    }

    @PatchMapping("/{id}/status")
    @Operation(summary = "修改角色状态", description = "修改角色启用/禁用状态")
    @PreAuthorize("@pm.match('sys:admin:role:status:update')")
    public Result<Void> updateRoleStatus(
            @Parameter(description = "角色ID", required = true) @PathVariable Long id,
            @Valid @RequestBody RoleStatusUpdateRequest request) {
        log.info("修改角色状态，角色ID：{}，状态：{}", id, request.getStatus());
        roleService.updateRoleStatus(id, request);
        return Result.success();
    }

    @GetMapping("/{id}/resources")
    @Operation(summary = "获取角色的资源权限", description = "获取角色关联的所有资源权限")
    @PreAuthorize("@pm.match('sys:admin:role:resources:read')")
    public Result<List<ResourceSimpleVO>> getRoleResources(
            @Parameter(description = "角色ID", required = true) @PathVariable Long id) {
        log.info("获取角色资源权限，角色ID：{}", id);
        return Result.success(roleService.getRoleResources(id));
    }

    @GetMapping("/options")
    @Operation(summary = "获取角色选项", description = "获取所有启用的角色选项（用于下拉列表）")
    @PreAuthorize("@pm.match('sys:admin:role:options:read')")
    public Result<List<RoleOptionVO>> getRoleOptions() {
        log.info("获取角色选项列表");
        return Result.success(roleService.getRoleOptions());
    }
}
