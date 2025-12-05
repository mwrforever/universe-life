package com.universe.life.user.privacy.controller;

import com.universe.life.common.result.Result;
import com.universe.life.model.domain.dto.RegisterFormDTO;
import com.universe.life.model.domain.dto.UserStatusDTO;
import com.universe.life.user.privacy.service.IUserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
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
@Tag(name = "用户接口")
@RequiredArgsConstructor
@RestController
@Validated
public class UserController {

    private final IUserService userService;

    /**
     * 添加用户
     * 服务间调用添加用户信息
     */
    @PostMapping("/add")
    @Operation(description = "添加用户")
    public Result<Void> add(@RequestBody RegisterFormDTO registerFormDTO) {
        userService.add(registerFormDTO);
        return Result.success();
    }

    /**
     * 服务间调用
     * 获取用户状态
     */
    @GetMapping("/privacy/status")
    @Operation(description = "获取用户状态")
    public Result<UserStatusDTO> getStatusById(String username) {
        UserStatusDTO userStatusDTO = userService.getStatusByUsername(username);
        return Result.success(userStatusDTO);
    }


}
