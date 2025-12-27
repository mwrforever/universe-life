package com.universe.life.user.privacy.controller;

import com.universe.life.auth.common.domain.Result;
import com.universe.life.user.privacy.domain.dto.request.RoleDepartmentAssignRequest;
import com.universe.life.user.privacy.domain.vo.RoleOptionVO;
import com.universe.life.user.privacy.domain.vo.SysDepartmentSimpleVO;
import com.universe.life.user.privacy.service.ISysRoleDepartmentService;
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
 * 部门角色关联管理控制器
 *
 * @author 毛伟然
 * @since 2025-12-11
 */
@Slf4j
@Tag(name = "部门角色关联管理", description = "部门与角色关联相关接口")
@RequiredArgsConstructor
@RestController
@RequestMapping("/admin/role-department")
@Validated
public class AdminRoleDepartmentController {

    private final ISysRoleDepartmentService roleDepartmentService;

    @PostMapping
    @Operation(summary = "批量分配部门角色", description = "为角色批量分配部门")
    @PreAuthorize("@pm.match('sys:admin:role-department:add')")
    public Result<Void> assignRoleDepartments(@Valid @RequestBody RoleDepartmentAssignRequest request) {
        log.info("批量分配部门角色，角色ID：{}，部门ID列表：{}", request.getRoleId(), request.getDepartmentIds());
        roleDepartmentService.assignRoleDepartments(request);
        return Result.success();
    }

    @DeleteMapping
    @Operation(summary = "批量移除部门角色", description = "为角色批量移除部门")
    @PreAuthorize("@pm.match('sys:admin:role-department:delete')")
    public Result<Void> removeRoleDepartments(@Valid @RequestBody RoleDepartmentAssignRequest request) {
        log.info("批量移除部门角色，角色ID：{}，部门ID列表：{}", request.getRoleId(), request.getDepartmentIds());
        roleDepartmentService.removeRoleDepartments(request);
        return Result.success();
    }

    @GetMapping("/role/{roleId}")
    @Operation(summary = "获取角色关联的部门", description = "获取角色关联的所有部门")
    @PreAuthorize("@pm.match('sys:admin:role-department:role:read')")
    public Result<List<SysDepartmentSimpleVO>> getRoleDepartments(
            @Parameter(description = "角色ID", required = true) @PathVariable Long roleId) {
        log.info("获取角色关联的部门，角色ID：{}", roleId);
        return Result.success(roleDepartmentService.getRoleDepartments(roleId));
    }

    @GetMapping("/department/{departmentId}")
    @Operation(summary = "获取部门关联的角色", description = "获取部门关联的所有角色")
    @PreAuthorize("@pm.match('sys:admin:role-department:department:read')")
    public Result<List<RoleOptionVO>> getDepartmentRoles(
            @Parameter(description = "部门ID", required = true) @PathVariable Long departmentId) {
        log.info("获取部门关联的角色，部门ID：{}", departmentId);
        return Result.success(roleDepartmentService.getDepartmentRoles(departmentId));
    }
}
