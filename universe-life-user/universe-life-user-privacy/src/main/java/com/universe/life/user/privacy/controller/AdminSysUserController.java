package com.universe.life.user.privacy.controller;

import com.universe.life.auth.common.domain.Result;
import com.universe.life.common.domain.PageResult;
import com.universe.life.model.domain.dto.AdminUserInfoDTO;
import com.universe.life.user.privacy.domain.dao.query.SysUserListQuery;
import com.universe.life.user.privacy.domain.dto.request.SysUserCreateRequest;
import com.universe.life.user.privacy.domain.dto.request.SysUserPasswordResetRequest;
import com.universe.life.user.privacy.domain.dto.request.SysUserStatusUpdateRequest;
import com.universe.life.user.privacy.domain.dto.request.SysUserUpdateRequest;
import com.universe.life.user.privacy.domain.vo.AdminSysUserProfileVO;
import com.universe.life.user.privacy.domain.vo.SysUserDetailVO;
import com.universe.life.user.privacy.domain.vo.SysUserListVO;
import com.universe.life.user.privacy.domain.vo.SysUserOptionVO;
import com.universe.life.user.privacy.service.ISysUserService;
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
 * 平台员工管理控制器
 *
 * @author 毛伟然
 * @since 2025-12-11
 */
@Slf4j
@Tag(name = "平台员工管理", description = "平台员工CRUD相关接口")
@RequiredArgsConstructor
@RestController
@RequestMapping("/admin/sys-user")
@Validated
public class AdminSysUserController {

    private final ISysUserService sysUserService;

    @GetMapping("/login")
    @Operation(summary = "员工登录", description = "员工页面")
    public AdminUserInfoDTO login(String username) {
        log.info("员工：{}正在登录", username);
        return sysUserService.login(username);
    }

    @PostMapping
    @Operation(summary = "创建员工", description = "创建新的平台员工")
    @PreAuthorize("@pm.match('sys:admin:sys-user:add')")
    public Result<SysUserDetailVO> createSysUser(@Valid @RequestBody SysUserCreateRequest request) {
        log.info("创建员工，工号：{}", request.getEmployeeNo());
        return Result.success(sysUserService.createSysUser(request));
    }



    @GetMapping("/{id}")
    @Operation(summary = "获取员工详情", description = "根据ID获取员工详细信息")
    @PreAuthorize("@pm.match('sys:admin:sys-user:read')")
    public Result<SysUserDetailVO> getSysUserById(
            @Parameter(description = "员工ID", required = true) @PathVariable Long id) {
        log.info("查询员工详情，员工ID：{}", id);
        return Result.success(sysUserService.getSysUserById(id));
    }

    @PutMapping("/{id}")
    @Operation(summary = "更新员工", description = "更新员工信息")
    @PreAuthorize("@pm.match('sys:admin:sys-user:update')")
    public Result<SysUserDetailVO> updateSysUser(
            @Parameter(description = "员工ID", required = true) @PathVariable Long id,
            @Valid @RequestBody SysUserUpdateRequest request) {
        log.info("更新员工，员工ID：{}", id);
        return Result.success(sysUserService.updateSysUser(id, request));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除员工", description = "删除员工")
    @PreAuthorize("@pm.match('sys:admin:sys-user:delete')")
    public Result<Void> deleteSysUser(
            @Parameter(description = "员工ID", required = true) @PathVariable Long id) {
        log.info("删除员工，员工ID：{}", id);
        sysUserService.deleteSysUser(id);
        return Result.success();
    }

    @GetMapping("/list")
    @Operation(summary = "分页查询员工列表", description = "分页查询员工列表")
    @PreAuthorize("@pm.match('sys:admin:sys-user:list:read')")
    public Result<PageResult<SysUserListVO>> pageSysUsers(@Valid SysUserListQuery query) {
        log.info("分页查询员工列表，查询条件：{}", query);
        return Result.success(sysUserService.pageSysUsers(query));
    }

    @PatchMapping("/{id}/status")
    @Operation(summary = "修改员工状态", description = "修改员工启用/禁用状态")
    @PreAuthorize("@pm.match('sys:admin:sys-user:status:update')")
    public Result<Void> updateSysUserStatus(
            @Parameter(description = "员工ID", required = true) @PathVariable Long id,
            @Valid @RequestBody SysUserStatusUpdateRequest request) {
        log.info("修改员工状态，员工ID：{}，状态：{}", id, request.getStatus());
        sysUserService.updateSysUserStatus(id, request);
        return Result.success();
    }

    @PutMapping("/{id}/password")
    @Operation(summary = "重置员工密码", description = "重置员工密码")
    @PreAuthorize("@pm.match('sys:admin:sys-user:password:update')")
    public Result<Void> resetPassword(
            @Parameter(description = "员工ID", required = true) @PathVariable Long id,
            @Valid @RequestBody SysUserPasswordResetRequest request) {
        log.info("重置员工密码，员工ID：{}", id);
        sysUserService.resetPassword(id, request);
        return Result.success();
    }

    @GetMapping("/options")
    @Operation(summary = "获取员工选项", description = "获取所有启用的员工选项（用于下拉列表）")
    @PreAuthorize("@pm.match('sys:admin:sys-user:options:read')")
    public Result<List<SysUserOptionVO>> getSysUserOptions() {
        log.info("获取员工选项列表");
        return Result.success(sysUserService.getSysUserOptions());
    }

    @GetMapping("/{sysUserId}/permissions")
    @Operation(summary = "获取员工权限列表", description = "获取员工的所有权限标识（内部调用）")
    public List<String> getSysUserPermissions(
            @Parameter(description = "员工ID", required = true) @PathVariable Long sysUserId) {
        log.info("获取员工权限列表，员工ID：{}", sysUserId);
        return sysUserService.getSysUserPermissions(sysUserId);
    }

    @GetMapping("/profile")
    @Operation(summary = "获取当前登录用户的信息", description = "获取当前登录用户的信息")
    public Result<AdminSysUserProfileVO> profile() {
        log.info("获取当前登录用户信息");
        return Result.success(sysUserService.profile());
    }
}
