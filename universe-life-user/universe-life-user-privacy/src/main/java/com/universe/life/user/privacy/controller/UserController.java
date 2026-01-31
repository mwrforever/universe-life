package com.universe.life.user.privacy.controller;

import com.universe.life.auth.common.domain.Result;
import com.universe.life.auth.resource.util.SecurityUtil;
import com.universe.life.model.domain.dto.UserStatusDTO;
import com.universe.life.user.privacy.domain.dto.request.RegisterFormRequest;
import com.universe.life.user.privacy.domain.dto.request.UserProfileUpdateRequest;
import com.universe.life.user.privacy.domain.vo.UserInfoVO;
import com.universe.life.user.privacy.service.IUserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * <p>
 * 系统用户表 前端控制器
 * </p>
 *
 * @author 毛伟然
 * @since 2025-11-13
 */
@Slf4j
@Tag(name = "用户接口")
@RequiredArgsConstructor
@RestController
@Validated
public class UserController {

    private final IUserService userService;

    @PostMapping("/register")
    @Operation(
            summary = "用户注册",
            description = "新用户注册，包含验证码校验和速率限制保护"
    )
    public Result<Void> register(@Validated @RequestBody RegisterFormRequest request) {
        log.info("收到用户注册请求，用户标识: {}", request.getIdentification());

        userService.register(request);
        log.info("用户注册成功，用户标识: {}", request.getIdentification());
        return Result.success();
    }

    /**
     * 服务间调用
     * 获取用户状态
     */
    @GetMapping("/privacy/status")
    @Operation(description = "获取用户状态")
    public UserStatusDTO getStatusById(String username) {
        return userService.getStatusByUsername(username);
    }

    // ==================== 用户端接口 ====================

    /**
     * 获取当前登录用户信息
     */
    @GetMapping("/user/me")
    @Operation(summary = "获取当前登录用户信息", description = "获取当前登录用户的基本信息")
    public Result<UserInfoVO> getCurrentUser() {
        log.info("获取当前登录用户信息");
        return Result.success(userService.getUserById(SecurityUtil.getUserId()));
    }

    /**
     * 更新当前用户个人信息
     */
    @PutMapping("/user/profile")
    @Operation(summary = "更新个人信息", description = "更新当前登录用户的个人信息")
    public Result<UserInfoVO> updateProfile(@Valid @RequestBody UserProfileUpdateRequest request) {
        log.info("更新个人信息");
        return Result.success(userService.updateUserProfile(SecurityUtil.getUserId(), request));
    }
}
