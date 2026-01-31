package com.universe.life.user.privacy.controller;

import com.universe.life.auth.common.domain.Result;
import com.universe.life.user.privacy.domain.dto.request.BatchUserDepartmentRequest;
import com.universe.life.user.privacy.domain.dto.request.UserDepartmentRequest;
import com.universe.life.user.privacy.domain.vo.SysDepartmentSimpleVO;
import com.universe.life.user.privacy.domain.vo.SysUserSimpleVO;
import com.universe.life.user.privacy.service.ISysUserDepartmentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 用户部门关联管理控制器
 *
 * @author 毛伟然
 * @since 2026-01-17
 */
@Slf4j
@Tag(name = "用户部门管理", description = "用户部门关联管理相关接口")
@RequiredArgsConstructor
@RestController
@RequestMapping("/admin/sys-user-department")
@Validated
public class AdminSysUserDepartmentController {

    private final ISysUserDepartmentService userDepartmentService;

    @PostMapping
    @Operation(summary = "添加用户到部门", description = "将用户添加到指定部门")
    @PreAuthorize("@pm.match('sys:admin:user-department:add')")
    public Result<Void> addUserToDepartment(@Valid @RequestBody UserDepartmentRequest request) {
        log.info("添加用户到部门，用户ID：{}，部门ID：{}", request.getUserId(), request.getDepartmentId());
        userDepartmentService.addUserToDepartment(request);
        return Result.success();
    }

    @DeleteMapping
    @Operation(summary = "从部门移除用户", description = "从指定部门移除用户")
    @PreAuthorize("@pm.match('sys:admin:user-department:delete')")
    public Result<Void> removeUserFromDepartment(@Valid @RequestBody UserDepartmentRequest request) {
        log.info("从部门移除用户，用户ID：{}，部门ID：{}", request.getUserId(), request.getDepartmentId());
        userDepartmentService.removeUserFromDepartment(request.getUserId(), request.getDepartmentId());
        return Result.success();
    }

    @PostMapping("/batch")
    @Operation(summary = "批量添加用户到部门", description = "批量将多个用户添加到指定部门")
    @PreAuthorize("@pm.match('sys:admin:user-department:batch:add')")
    public Result<Void> batchAddUsersToDepartment(@Valid @RequestBody BatchUserDepartmentRequest request) {
        log.info("批量添加用户到部门，部门ID：{}，用户ID列表：{}", request.getDepartmentId(), request.getUserIds());
        userDepartmentService.batchAddUsersToDepartment(request);
        return Result.success();
    }

    @DeleteMapping("/batch")
    @Operation(summary = "批量从部门移除用户", description = "批量从指定部门移除多个用户")
    @PreAuthorize("@pm.match('sys:admin:user-department:batch:delete')")
    public Result<Void> batchRemoveUsersFromDepartment(@Valid @RequestBody BatchUserDepartmentRequest request) {
        log.info("批量从部门移除用户，部门ID：{}，用户ID列表：{}", request.getDepartmentId(), request.getUserIds());
        userDepartmentService.batchRemoveUsersFromDepartment(request);
        return Result.success();
    }

    @GetMapping("/departments/{departmentId}/users")
    @Operation(summary = "获取部门的所有用户", description = "查询指定部门的所有员工")
    @PreAuthorize("@pm.match('sys:admin:user-department:departments:users:read')")
    public Result<List<SysUserSimpleVO>> getUsersByDepartment(
            @Parameter(description = "部门ID", required = true) @PathVariable Long departmentId) {
        log.info("获取部门用户列表，部门ID：{}", departmentId);
        return Result.success(userDepartmentService.getUsersByDepartment(departmentId));
    }

    @GetMapping("/users/{userId}/departments")
    @Operation(summary = "获取用户的所有部门", description = "查询指定用户所属的所有部门")
    @PreAuthorize("@pm.match('sys:admin:user-department:users:departments:read')")
    public Result<List<SysDepartmentSimpleVO>> getDepartmentsByUser(
            @Parameter(description = "用户ID", required = true) @PathVariable Long userId) {
        log.info("获取用户部门列表，用户ID：{}", userId);
        return Result.success(userDepartmentService.getDepartmentsByUser(userId));
    }
}
