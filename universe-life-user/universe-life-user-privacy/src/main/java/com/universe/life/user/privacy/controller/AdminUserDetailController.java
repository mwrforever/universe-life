package com.universe.life.user.privacy.controller;

import com.universe.life.auth.common.domain.Result;
import com.universe.life.user.privacy.domain.dto.request.UserDetailUpdateRequest;
import com.universe.life.user.privacy.domain.vo.UserDetailVO;
import com.universe.life.user.privacy.service.IAdminUserDetailService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 用户详情管理控制器（管理员接口）
 * 处理路径：/admin/detail/{id}
 *
 * @author 毛伟然
 * @since 2025-12-02
 */
@Slf4j
@Tag(name = "用户详情管理", description = "管理员用户详情管理相关接口")
@RequiredArgsConstructor
@RestController
@RequestMapping("/admin")
@Validated
public class AdminUserDetailController {

    private final IAdminUserDetailService adminUserDetailService;

    @GetMapping("/detail/{id}")
    @Operation(summary = "管理后台获取用户详情", description = "管理员获取指定用户的详细信息")
    @PreAuthorize("@pm.match('sys:admin:detail:read')")
    public Result<UserDetailVO> getUserDetail(
            @Parameter(description = "用户ID", required = true)
            @PathVariable Long id) {
        log.info("管理员获取用户详情，用户ID：{}", id);
        return Result.success(adminUserDetailService.getUserDetailByUserId(id));
    }

    @PutMapping("/detail/{id}")
    @Operation(summary = "更新用户详情", description = "管理员更新用户详细信息")
    @PreAuthorize("@pm.match('sys:admin:detail:update')")
    public Result<Void> updateUserDetail(
            @Parameter(description = "用户ID", required = true)
            @PathVariable Long id,
            @Valid @RequestBody UserDetailUpdateRequest request) {
        log.info("管理员更新用户详情，用户ID：{}", id);
        adminUserDetailService.updateUserDetail(id, request);
        return Result.success();
    }
}