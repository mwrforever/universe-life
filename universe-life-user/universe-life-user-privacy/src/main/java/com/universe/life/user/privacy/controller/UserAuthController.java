package com.universe.life.user.privacy.controller;

import com.universe.life.common.result.Result;
import com.universe.life.model.domain.dto.UserInfoDTO;
import com.universe.life.user.privacy.domain.dto.request.DeleteUserAuthRequest;
import com.universe.life.user.privacy.domain.dto.request.UserAuthCreateRequest;
import com.universe.life.user.privacy.domain.dto.request.UserAuthUpdateRequest;
import com.universe.life.user.privacy.domain.vo.UserAuthCreateVO;
import com.universe.life.user.privacy.domain.vo.UserAuthListVO;
import com.universe.life.user.privacy.service.IUserAuthService;
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
 * 用户认证管理控制器
 * 处理路径：/auth/add, /auth/list/{userId}, /auth/{userId}, /auth/{authId}
 *
 * @author 毛伟然
 * @since 2025-11-13
 */
@Slf4j
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Tag(name = "用户认证管理", description = "用户认证管理相关接口")
@Validated
public class UserAuthController {

    private final IUserAuthService userAuthService;

    /**
     * 获取用户信息
     * 服务间调用用户服务获取用户信息
     * @param username 用户名
     * @return 用户信息
     */
    @GetMapping("/getUserInfo")
    @Operation(description = "获取用户信息")
    public Result<UserInfoDTO> getUserInfo(String username) {
        return Result.success(userAuthService.getUserInfo(username));
    }

    // 新增认证管理接口
    @PostMapping("/add")
    @Operation(summary = "添加用户认证方式", description = "为用户添加新的认证方式")
    public Result<UserAuthCreateVO> addUserAuth(@Valid @RequestBody UserAuthCreateRequest request) {
        log.info("添加用户认证方式，用户ID：{}，认证类型：{}", request.getUserId(), request.getIdentificationType());
        return Result.success(userAuthService.createUserAuth(request));
    }

    @GetMapping("/list/{userId}")
    @Operation(summary = "查询用户认证方式列表", description = "查询指定用户的所有认证方式")
    public Result<List<UserAuthListVO>> getUserAuthList(
            @Parameter(description = "用户ID", required = true)
            @PathVariable Long userId) {
        log.info("查询用户认证方式列表，用户ID：{}", userId);
        return Result.success(userAuthService.getUserAuthListByUserId(userId));
    }

    @PutMapping("/resetPassword")
    @Operation(summary = "更新用户认证信息", description = "更新用户的认证信息，如修改密码等")
    public Result<Void> updateUserAuth(
            @Parameter(description = "用户ID", required = true)
            @Valid @RequestBody UserAuthUpdateRequest request) {
        log.info("更新用户认证信息，认证类型：{}", request.getIdentificationType());
        userAuthService.updateUserAuth(request);
        return Result.success();
    }

    @PostMapping("/delete/{id}")
    @Operation(summary = "删除用户认证方式", description = "删除指定用户的认证方式")
    public Result<Void> deleteUserAuth(
            @Parameter(description = "认证ID", required = true)
            @PathVariable Long id,
            @RequestBody DeleteUserAuthRequest request
            ) {
        log.info("删除用户认证方式，认证ID：{}", id);
        userAuthService.deleteUserAuth(id, request);
        return Result.success();
    }
}
