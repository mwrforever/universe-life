package com.universe.life.user.privacy.controller.admin;

import com.universe.life.common.result.PageResult;
import com.universe.life.common.result.Result;
import com.universe.life.user.privacy.domain.dao.query.AdminUserListQuery;
import com.universe.life.user.privacy.domain.dao.query.UserStatusQuery;
import com.universe.life.user.privacy.domain.dto.request.*;
import com.universe.life.user.privacy.domain.vo.*;
import com.universe.life.user.privacy.service.admin.IAdminUserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 用户管理控制器（管理员接口）
 * 处理路径：/admin/{id}, /admin/list, /admin/{id}, /admin/{id}, /admin/status, /admin/resetPassword/{id}, /admin/status
 *
 * @author 毛伟然
 * @since 2025/12/02
 */
@Slf4j
@Tag(name = "用户管理", description = "管理员用户基础管理相关接口")
@RequiredArgsConstructor
@RestController
@RequestMapping("/admin")
@Validated
public class AdminUserController {

    private final IAdminUserService adminUserService;

    @GetMapping("/{id}")
    @Operation(summary = "根据ID查询用户", description = "管理员根据用户ID获取用户详细信息")
    public Result<AdminUserDetailVO> getUserById(
            @Parameter(description = "用户ID", required = true)
            @PathVariable Long id) {
        log.info("管理员查询用户，用户ID：{}", id);
        return Result.success(adminUserService.getUserById(id));
    }

    @PostMapping
    @Operation(summary = "创建用户", description = "管理员创建新用户")
    public Result<UserCreateVO> createUser(@Valid @RequestBody UserCreateRequest request) {
        log.info("管理员创建用户，用户名：{}", request.getUsername());
        return Result.success(adminUserService.createUser(request));
    }

    @GetMapping("/list")
    @Operation(summary = "分页查询用户列表", description = "管理员根据条件分页查询用户列表")
    public Result<PageResult<AdminUserListVO>> pageUsers(@Valid AdminUserListQuery query) {
        log.info("管理员分页查询用户列表，查询条件：{}", query);
        return Result.success(adminUserService.pageUsers(query));
    }

    @PutMapping("/{id}")
    @Operation(summary = "更新用户信息", description = "管理员更新用户基本信息和角色信息")
    public Result<AdminUserUpdateVO> updateUser(
            @Parameter(description = "用户ID", required = true)
            @PathVariable Long id,
            @Valid @RequestBody UserUpdateRequest request) {
        log.info("管理员更新用户信息，用户ID：{}", id);
        request.setId(id);
        return Result.success(adminUserService.updateUser(request));
    }

    @PostMapping("/{id}")
    @Operation(summary = "删除用户", description = "管理员软删除指定用户")
    public Result<Void> deleteUser(
            @Parameter(description = "用户ID", required = true)
            @PathVariable Long id,
            @RequestBody @Validated PasswordUserRequest request
    ) {
        log.info("管理员删除用户，用户ID：{}", id);
        adminUserService.deleteUser(id, request);
        return Result.success();
    }

    @GetMapping("/status")
    @Operation(summary = "获取用户状态", description = "管理员根据用户名获取用户状态")
    public Result<UserStatusVO> getUserStatus(@Valid UserStatusQuery query) {
        log.info("管理员获取用户状态，用户名：{}", query.getUsername());
        return Result.success(adminUserService.getUserStatus(query.getUsername()));
    }

    @PutMapping("/resetPassword/{id}")
    @Operation(summary = "重置用户密码", description = "管理员重置用户密码")
    public Result<Void> resetPassword(
            @Parameter(description = "用户ID", required = true)
            @PathVariable Long id,
            @RequestBody @Validated PasswordUserRequest request
    ) {
        log.info("管理员重置用户密码，用户ID：{}", id);
        adminUserService.resetPassword(id, request);
        return Result.success();
    }

    @PutMapping("/status")
    @Operation(summary = "更新用户状态", description = "管理员更新用户状态")
    public Result<Void> updateUserStatus(
            @Valid @RequestBody UserStatusUpdateRequest request) {
        log.info("管理员更新用户状态，用户ID：{}，状态：{}", request.getId(), request.getStatus());
        adminUserService.updateUserStatus(request);
        return Result.success();
    }

    @PostMapping("/batch/delete")
    @Operation(summary = "批量删除用户", description = "管理员批量软删除用户")
    public Result<Void> batchDeleteUsers(
            @RequestParam("ids") List<Long> ids,
            @RequestBody @Validated PasswordUserRequest request
            ) {
        log.info("管理员批量删除用户，用户ID列表：{}", ids);
        adminUserService.batchDeleteUsers(ids, request);
        return Result.success();
    }

    @PutMapping("/batch/status")
    @Operation(summary = "批量更新用户状态", description = "管理员批量更新用户状态")
    public Result<Void> batchUpdateUserStatus(
            @Valid @RequestBody UserBatchStatusUpdateRequest request) {
        log.info("管理员批量更新用户状态，用户ID列表：{}，状态：{}", request.getUserIds(), request.getStatus());
        adminUserService.batchUpdateUserStatus(request);
        return Result.success();
    }
}