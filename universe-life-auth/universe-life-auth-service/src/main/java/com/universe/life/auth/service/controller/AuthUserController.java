package com.universe.life.auth.service.controller;

import com.universe.life.auth.service.domain.dto.request.RegisterFormRequest;
import com.universe.life.auth.service.service.IAuthUserService;
import com.universe.life.auth.common.domain.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * 用户权限管理控制器
 * 增强了安全防护机制，包括速率限制和暴力破解防护
 *
 * @author 毛伟然
 * @since 2025/11/13 14:48
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@Tag(name = "用户权限管理", description = "提供用户注册、登录等功能，包含安全防护机制")
@Validated
public class AuthUserController {

    private final IAuthUserService authUserService;

    @PostMapping("/register")
    @Operation(
            summary = "用户注册",
            description = "新用户注册，包含验证码校验和速率限制保护"
    )
    public Result<Void> register(@Validated @RequestBody RegisterFormRequest request) {
        log.info("收到用户注册请求，用户标识: {}", request.getIdentification());

        authUserService.register(request);
        log.info("用户注册成功，用户标识: {}", request.getIdentification());
        return Result.success();
    }

}
