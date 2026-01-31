package com.universe.life.user.privacy.controller;

import com.universe.life.auth.common.domain.Result;
import com.universe.life.auth.resource.util.SecurityUtil;
import com.universe.life.user.privacy.domain.dto.request.UserDetailUpdateRequest;
import com.universe.life.user.privacy.domain.vo.UserDetailVO;
import com.universe.life.user.privacy.service.IUserDetailService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * <p>
 * 用户详情表 前端控制器
 * </p>
 *
 * @author 毛伟然
 * @since 2025-11-13
 */
@Slf4j
@Tag(name = "用户详情接口", description = "用户详情相关接口")
@RequiredArgsConstructor
@RestController
@RequestMapping("/detail")
@Validated
public class UserDetailController {

    private final IUserDetailService userDetailService;

    /**
     * 获取当前用户详情
     */
    @GetMapping("/me")
    @Operation(summary = "获取当前用户详情", description = "获取当前登录用户的详细信息")
    public Result<UserDetailVO> getCurrentUserDetail() {
        log.info("获取当前用户详情");
        return Result.success(userDetailService.getUserDetailByUserId(SecurityUtil.getUserId()));
    }

    /**
     * 更新当前用户详情
     */
    @PutMapping("/me")
    @Operation(summary = "更新当前用户详情", description = "更新当前登录用户的详细信息")
    public Result<UserDetailVO> updateCurrentUserDetail(@Valid @RequestBody UserDetailUpdateRequest request) {
        log.info("更新当前用户详情");
        return Result.success(userDetailService.updateUserDetail(SecurityUtil.getUserId(), request));
    }
}
